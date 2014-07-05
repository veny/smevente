package veny.smevente.client.rest;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import veny.smevente.client.App;
import veny.smevente.client.mvp.Hideable;
import veny.smevente.client.mvp.SingletonEventBus;
import veny.smevente.shared.ExceptionJsonWrapper;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

/**
 * Client-side resource handler sends and receives data to and from server.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 */
public class RestHandler {

    /** Timeout from last sent request to send a signal to disable the loading bar. */
    private static final int DISABLE_LOADING_BAR_TIMEOUT = 40000;

    /** Set of currently running request. */
    private static Set<RestHandler> runningRequests = new HashSet<RestHandler>();

    /**
     * Counter of exclusive requests.
     * @see #exlusiveIndex
     */
    private static long exclusiveCounter = 1L;

    /**
     * Timer running from last sent request to send a signal to disable the loading bar.
     */
    private static Timer runningRequestsCleaner = new Timer() {
        @Override
        public void run() {
            checkRunninRequests();
        }
    };


    /** Request URI.*/
    private String uri;

    /** The hideable component that has to be checked before. */
    private Hideable hideable;

    /** Registered callback. */
    private RestCallback callback;

    /** Data to be sent. */
    private String encodedDataToSend;

    /** Flag whether the response should be ignored. */
    private boolean ignoreResponse = false;

    /**
     * Flag whether the request is exclusive (value > 0).
     * There can be only one parallel exclusive request.
     * Each exclusive request is identified with a number given by counter. If more parallel
     * exclusive requests occur, response of the one with highest index will be processed.
     */
    private final long exlusiveIndex;

    /**
     * Constructor with a destination URI string.
     * @param uri the target URI
     */
    public RestHandler(final String uri) {
        this(uri, null);
    }

    /**
     * Constructor with a destination URI string and a visible component to bind to.
     * The response will not be sent to the callback if the component is not visible.
     * @param uri the target URI
     * @param hideable the visible component to bind to
     */
    public RestHandler(final String uri, final Hideable hideable) {
        this(uri, hideable, false);
    }

    /**
     * Constructor with a destination URI string and a visible component to bind to.
     * The response will not be sent to the callback if the component is not visible.
     * There can be only one parallel exclusive request.
     * @param uri the target URI
     * @param hideable the visible component to bind to
     * @param exclusive whether the request is exclusive
     * @see #exlusiveIndex
     */
    public RestHandler(final String uri, final Hideable hideable, final boolean exclusive) {
        this.uri = App.get().getBaseUrl() + uri;
        this.hideable = hideable;
        if (exclusive) {
            this.exlusiveIndex = exclusiveCounter;
            exclusiveCounter++;
            if (exclusiveCounter == Long.MAX_VALUE) { exclusiveCounter = 1L; }
        } else {
            this.exlusiveIndex = 0L;
        }
    }

    /**
     * Callback setter.
     * @param callback the callback
     */
    public void setCallback(final RestCallback callback) {
        this.callback = callback;
    }

    /**
     * Sends request using the HTTP UPDATE method.
     * @param dataToSend
     *            encoded string map to be sent to the server as
     *            application/x-www-form-urlencoded
     */
    public void put(final Map<String, String> dataToSend) {
        encodedDataToSend = encodeDataToSend(dataToSend);

        send(RequestBuilder.PUT);
    }

    /**
     * Sends request using the HTTP POST method.
     * @param dataToSend
     *            encoded string map to be sent to the server as
     *            application/x-www-form-urlencoded
     */
    public void post(final Map<String, String> dataToSend) {
        encodedDataToSend = encodeDataToSend(dataToSend);
        send(RequestBuilder.POST);
    }

    /**
     * Sends request using the HTTP DELETE method.
     */
    public void delete() {
        send(RequestBuilder.DELETE);
    }

    /**
     * Sends request using the HTTP GET method.
     */
    public void get() {
        send(RequestBuilder.GET);
    }

    /**
     * Gets index of exclusivity (> 0 for exclusive requests). See {@link #exlusiveValue} for more info.
     * @return index bigger than 0 if the request is exclusive
     * @see #exlusiveIndex
     */
    public long getExclusiveIndex() {
        return exlusiveIndex;
    }

    /**
     * Gets flag whether the response should be ignored.
     * @return <i>true</i> if the response should be ignored
     */
    public boolean isIgnoreResponse() {
        return ignoreResponse;
    }
    /**
     * Sets flag whether the response should be ignored.
     * @param ignoreResponse <i>true</i> if the response should be ignored
     */
    public void setIgnoreResponse(final boolean ignoreResponse) {
        this.ignoreResponse = ignoreResponse;
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Encodes the input dataToSend map into a format for HTTP
     * application/x-www-form-urlencoded.
     * @param dataToSend map to be encoded
     * @return encoded string
     */
    private String encodeDataToSend(final Map< String, String > dataToSend) {
        StringBuilder encodedString = new StringBuilder();

        // iterate the data
        if (dataToSend != null) {
            for (Entry<String, String> entry : dataToSend.entrySet()) {
                // join data using ampersand "&"
                if (encodedString.length() > 0) {
                    encodedString.append("&");
                }

                // encode the data
                encodedString
                    .append(URL.encodeQueryString(entry.getKey()))
                    .append("=").append(URL.encodeQueryString(entry.getValue()));
            }
        }

        return encodedString.toString();
    }

    /**
     * Performs the actual HTTP request.
     * @param method HTTP method
     */
    private void send(final Method method) {
        // create request
        final RequestBuilder builder = new RequestBuilder(method, uri);
        builder.setHeader(
            "Accept-Language", LocaleInfo.getCurrentLocale().getLocaleName());
        builder.setHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
        if (encodedDataToSend != null) {
            builder.setRequestData(encodedDataToSend);
        }

        // add callback
        builder.setCallback(new RequestCallback() {
            @Override
            public void onError(final Request request, final Throwable ex) {
                // error state: data cannot be sent
                disableLoadingSignal();
                App.get().getFailureHandler().handleClientErrorWithAction(
                        ex, "callback.onError", "Request cannot be sent, uri=" + uri);
            }
            @Override
            public void onResponseReceived(final Request request, final Response response) {
                // request sent, now check the response
                disableLoadingSignal();
                processReceivedResponse(response);
            }

        });

        // send the data
        try {
            enableLoadingSignal();
            builder.send();
        } catch (final RequestException ex) {
            disableLoadingSignal();
            App.get().getFailureHandler().handleClientErrorWithAction(
                    ex, "exception by sending request", "Failed to send request, uri=" + uri);
        }
    }

    /**
     * Process the received response.
     * @param response the received response
     */
    private void processReceivedResponse(final Response response) {
        int responseStatusCode = response.getStatusCode();
        String responseText = response.getText();

        if (200 == responseStatusCode) {
            // OK state, call onSuccess()
            if (callback != null && (null == hideable || hideable.isVisible())) {

                // check the exclusivity
                if (exlusiveIndex > 0L) {
                    // process only if all other request in buffer have a lesser exclusive index
                    boolean skip = false;
                    for (RestHandler parallel : runningRequests) {
                        if (parallel.getExclusiveIndex() > exlusiveIndex) {
                            skip = true;
                        }
                        // requests 1, 2 / response 2, 1
                        // response 1 has to be ignored
                        if (parallel.getExclusiveIndex() > 0 && parallel.getExclusiveIndex() < exlusiveIndex) {
                            parallel.setIgnoreResponse(true);
                        }
                    }
                    // found other exclusive request with bigger index
                    // or
                    // response should be ignored
                    // => do NOT process the response
                    if (skip || isIgnoreResponse()) { return; }
                }

                try {
                    callback.onSuccess(responseText);
                } catch (Exception ex) {
                    App.get().getFailureHandler().handleClientErrorWithAction(
                        ex, "processing response", "statusCode=" + responseStatusCode + ", response=" + responseText);
                }
            }
        } else if (403 == responseStatusCode) {
            // Unauthorized state, fireEvent()
            SingletonEventBus.get().fireEvent(new UnauthorizedEvent());
        } else if (404 == responseStatusCode) {
            // Not Found
            ExceptionJsonWrapper exWrapper = new ExceptionJsonWrapper();
            exWrapper.setMessage("Not Found (HTTP 404)");
            exWrapper.setClassName("SourceNotFoundException");
            try {
                callback.onFailure(exWrapper);
            } catch (Exception ex) {
                App.get().getFailureHandler().handleClientErrorWithAction(
                        ex, "processing response", "statusCode=" + responseStatusCode + ", response=" + responseText);
            }
        } else if (406 == responseStatusCode) {
            // WaveExceptionResolve
            try {
                final ExceptionJsonWrapper exWrapper = App.get().getJsonDeserializer().deserialize(
                        ExceptionJsonWrapper.class, "exception", responseText);
                callback.onFailure(exWrapper);
            } catch (Exception ex) {
                App.get().getFailureHandler().handleClientErrorWithAction(
                        ex, "processing response", "statusCode=" + responseStatusCode + ", response=" + responseText);
            }
        } else {
            // error state, call onFailure()
            if (null != callback) {
                try {
                    ExceptionJsonWrapper exWrapper = new ExceptionJsonWrapper();
                    exWrapper.setMessage("HTTP " + responseStatusCode);
                    exWrapper.setClassName("ServerException");
                    callback.onFailure(exWrapper);
                } catch (Exception ex) {
                    App.get().getFailureHandler().handleClientErrorWithAction(
                            ex, "processing response",
                            "statusCode=" + responseStatusCode + ", response=" + responseText);
                }
            }
        }
    }

    // ---------------------------------------------- UI Loading Bar Processing

    /**
     * Sends a signal of starting server communication.
     */
    private void enableLoadingSignal() {
        if (runningRequests.isEmpty()) {
            SingletonEventBus.get().fireEvent(new AjaxEvent(AjaxEvent.Sort.REQUEST_SENT));
        }
        runningRequests.add(this);
        // start always the timer (and cancel the old one before)
        runningRequestsCleaner.cancel();
        runningRequestsCleaner.schedule(DISABLE_LOADING_BAR_TIMEOUT);
    }

    /**
     * Sends a signal of ending server communication.
     */
    private void disableLoadingSignal() {
        if (runningRequests.contains(this)) {
            runningRequests.remove(this);
        }
        if (runningRequests.isEmpty()) {
            SingletonEventBus.get().fireEvent(new AjaxEvent(AjaxEvent.Sort.RESPONSE_RECEIVED));
            runningRequestsCleaner.cancel();
        }
    }

    /**
     * Send a signal to disable the loading bar after defined timeout.
     */
    private static void checkRunninRequests() {
        if (!runningRequests.isEmpty()) {
            runningRequests.clear();
            SingletonEventBus.get().fireEvent(new AjaxEvent(AjaxEvent.Sort.RESPONSE_RECEIVED));
            // it's "never should state" because of 30s request timeout on GAE
            Window.alert("Loading Bar Timeout.\n(alert for testing purposes, remove it later)");
        }
    }

}
