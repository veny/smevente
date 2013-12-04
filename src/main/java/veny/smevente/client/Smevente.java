package veny.smevente.client;

import com.google.gwt.core.client.EntryPoint;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 10.7.2010
 */
public class Smevente implements EntryPoint {
//    /**
//     * The message displayed to the user when the server cannot be reached or
//     * returns an error.
//     */
//    private static final String SERVER_ERROR = "An error occurred while "
//            + "attempting to contact the server. Please check your network "
//            + "connection and try again.";

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        // set the client side error handler
        App.get().setFailureHandler(new AlertFailureHandler());

        // if the session is not valid -> login
        // if yes -> the presenter from history stack will be activated
        //        -> or the default one if no entry in the history stack
        App.get().checkWithServerIfSessionIdIsStillLegal();
    }
}
