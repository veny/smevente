package veny.smevente.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import veny.smevente.service.SmsGatewayService;


/**
 * Implementation of SMS service.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 * {@link http://stackoverflow.com/questions/2793150/how-to-use-java-net-urlconnection-to-fire-and-handle-http-requests}
 */
public class SmsGatewayServiceSmsSluzbaCz implements SmsGatewayService {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(SmsGatewayServiceSmsSluzbaCz.class.getName());

    /** Service endpoint to send a SMS. */
    private static final String ENDPOINT_SEND = "https://smsgateapi.sms-sluzba.cz/apipost10/sms";

    /**
     * See 'SMS_Gate_API_POST_10.pdf' for this limit.
     */
    private static final int MAX_SMS_LEN = 459;

    /** Message Digest to generate password hash. */
    private final MessageDigest md5;

    /** Constructor. */
    public SmsGatewayServiceSmsSluzbaCz() {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("failed to initialize message digest", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean send(final String number, final String msg, final Map<String, String> metadata)
        throws IOException, SmsException {

        if (null == number) {
            throw new NullPointerException("phone number cannot be null");
        }
        if (null == msg) {
            throw new NullPointerException("message cannot be null");
        }
        if (msg.length() > MAX_SMS_LEN) {
            throw new IllegalArgumentException("message longer than " + MAX_SMS_LEN + " characters");
        }
        if (null == metadata) {
            throw new NullPointerException("metadata cannot be null");
        }
        if (!metadata.containsKey(METADATA_USERNAME)) {
            throw new IllegalArgumentException("metadata does not contain key: " + METADATA_USERNAME);
        }
        if (!metadata.containsKey(METADATA_PASSWORD)) {
            throw new IllegalArgumentException("metadata does not contain key: " + METADATA_PASSWORD);
        }

        final String login = metadata.get(METADATA_USERNAME);
        final String password = metadata.get(METADATA_PASSWORD);
        final String data = new StringBuilder("msg=")
            .append(urlEncode(msg))
            .append("&msisdn=")
            .append(sanitize(number))
            .append("&act=send&login=")
            .append(login)
            .append("&auth=")
            .append(getAuth(msg, login, password))
            .toString();

        // do NOT send the SMS physically if system property 'sms.gateway.fake' set to 'true'
        // OR if phone number equals "-=NO_SMS=-"
        // [it's mostly for JUnit test that do not want to send a SMS.]
        if ("true".equalsIgnoreCase(System.getProperty("sms.gateway.fake", "false")) || "-=NO_SMS=-".equals(number)) {
            LOG.fine("fake sms (not sent), phNumber=" + number);
            return true;
        }

        final URL url = new URL(System.getProperty("sms.endpoint.url", ENDPOINT_SEND));
        LOG.info("initialized with service endpoint: " + url);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        final OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(data);
        writer.close();
        final int returnCode = connection.getResponseCode();

            // HTTP response headers
//            for (Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
//                LOG.finest(header.getKey() + "=" + header.getValue());
//            }


        if (returnCode != HttpURLConnection.HTTP_OK) {
            LOG.info("failed to send SMS, number=" + number
                    + ", returnCode=" + returnCode + ", data=" + data);
            throw new SmsException(FailureType.SERVICE_ERROR, "HTTP request return code: " + returnCode);
        }

        // HTTP response body
        BufferedReader reader = null;
        try {
            final StringBuilder body = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line = reader.readLine();
            while (null != line) {
                body.append(line);
                line = reader.readLine();
            }

            // is response OK?
            final SmsException failure = assertResponse(body.toString());
            if (null == failure) {
                LOG.info("SMS sent, number=" + number + ", msg=" + msg + ", data=" + data);
            } else {
                LOG.severe("failed to send SMS, number=" + number
                        + ", data=" + data + ", failure=" + failure.getMessage());
                throw failure;
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    LOG.log(Level.WARNING, "failed to close the read buffer", ioe);
                }
            }
        }

        return true;
    }

    // ----------------------------------------------------------- Helper Stuff

    /**
     * Sanitizes (removes all white spaces) phone number.
     * @param phoneNumber phone number to sanitize
     * @return sanitized phone number
     */
    private String sanitize(final String phoneNumber) {
        return phoneNumber.replaceAll("\\s", "");
    }

    /**
     * Gets 'Auth' according to the 'SMS_Gate_API_POST_10.pdf' algorithm.
     * @param text text to be sent
     * @param login login name to the service
     * @param password password
     * @return value of 'Auth' parameter
     */
    private String getAuth(final String text, final String login, final String password) {
        String text31 = text;
        if (text.length() > 31) {
            text31 = text.substring(0, 31);
        }
        final String s = md5(password) + login + "send" + text31;
        return md5(s);
    }

    /**
     * Gets MD5 from given string.
     * @param s string to calculate MD5 hash
     * @return MD5 hash
     */
    private String md5(final String s) {
        md5.reset();
        md5.update(s.getBytes(), 0, s.length());
        return convertToHex(md5.digest());
    }

    /**
     * Converts byte array to hex string.
     * @param data byte array to convert
     * @return converted hex string
     */
    private static String convertToHex(final byte[] data) {
        final StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int twoHalfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9)) {
                    buf.append((char) ('0' + halfbyte));
                } else {
                    buf.append((char) ('a' + (halfbyte - 10)));
                }
                halfbyte = data[i] & 0x0F;
            } while(twoHalfs++ < 1);
        }
        return buf.toString();
    }

    /**
     * Translates a string into URL encoded form without exception handling.
     * @param s string to encode
     * @return encoded string
     */
    private String urlEncode(final String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            LOG.severe("failed to URL encode data" + ", failure=" + e1.getMessage());
            throw new IllegalStateException("failed to URL encode data", e1);
        }
    }

    /**
     * Asserts response whether it represents success or failure.
     * @param response text to parse
     * @return <i>null</i> if success or corresponding <code>SmsException</code> instance
     */
    private SmsException assertResponse(final String response) {
        // id
        int idStart = response.indexOf("<id>");
        int idEnd = response.indexOf("</id>");
        String id = response.substring(idStart + 4, idEnd);
        // message
        int msgStart = response.indexOf("<message>");
        int msgEnd = response.indexOf("</message>");
        String msg = response.substring(msgStart + "<message>".length(), msgEnd);
        // failure type
        FailureType type;
        if ("200".equals(id)) {
            return null;
        } else if ("400".equals(id)) {
            type = FailureType.CLIENT_ERROR; // unknown action
        } else if ("400;1".equals(id)) {
            type = FailureType.BAD_PHONE_NUMBER;
        } else if ("400;2".equals(id)) {
            type = FailureType.CLIENT_ERROR; // no SMS text
        } else if ("401".equals(id)) {
            type = FailureType.BAD_AUTHENTICATION;
        } else if ("402".equals(id)) {
            type = FailureType.NO_CREDIT;
        } else {
            type = FailureType.SERVICE_ERROR;
        }
        return new SmsException(type, msg);
    }

    /**
     * Entry point for testing purposes.
     * @param args CLI parameters
     * @throws Exception if some problem occurs
     */
    public static void main(final String[] args) throws Exception {
//        System.setProperty("javax.net.ssl.trustStore", "war/WEB-INF/sms-services.keystore"); // only for GAE
//        System.setProperty("javax.net.ssl.trustStorePassword", "smevente73");

        SmsGatewayServiceSmsSluzbaCz service = new SmsGatewayServiceSmsSluzbaCz();
        final Map<String, String> metadata = new HashMap<String, String>();
        metadata.put(METADATA_USERNAME, args[0]);
        metadata.put(METADATA_PASSWORD, args[1]);

        service.send("606 146 177", "ahoj, toto je z aplikace", metadata);

//        System.out.println(//CSOFF
//                service.assertResponse(
//                        "<status><id>200</id><message>Zprava byla uspesne odeslana</message></status>"));
//        System.out.println(//CSOFF
//                service.assertResponse(
//                        "<status><id>400;1</id><message>Chybne telefonni cislo  Prefix don't exists. "
//                        + "500146177</message></status>"));
//        System.out.println(//CSOFF
//                service.assertResponse("<status><id>401</id><message>Chybne prihlaseni</message></status>"));
    }

}
