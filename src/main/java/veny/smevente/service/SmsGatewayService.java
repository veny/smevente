package veny.smevente.service;

import java.util.Map;

import veny.smevente.shared.SmeventeException;

/**
 * SMS service.
 *
 * (old: keytool -import -alias test -file sms-sluzba-cz.cert -keystore TS)
 * keytool -import -file /tmp/sms.sluzba.cz.cert -keystore war/WEB-INF/sms-services.keystore
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
public interface SmsGatewayService {

    /** Metadata key of login name to the service. */
    String METADATA_USERNAME = "smsGatewayUsername";
    /** Metadata key of password to the service. */
    String METADATA_PASSWORD = "smsGatewayPassword";

    /**
     * Enumeration of possible failure reasons.
     * @author Vaclav Sykora [vaclav.sykora@gmail.com]
     * @since 2.8.2010
     */
    public enum FailureType {
        // CHECKSTYLE:OFF
        BAD_AUTHENTICATION,
        BAD_PHONE_NUMBER,
        NO_CREDIT,
        BAD_PARAMETERS,
        CLIENT_ERROR,
        SERVICE_ERROR
        // CHECKSTYLE:ON
    }

    /**
     * Exception representing a problem by sending SMS.
     * @author Vaclav Sykora [vaclav.sykora@gmail.com]
     * @since 2.8.2010
     */
    @SuppressWarnings("serial")
    public class SmsException extends SmeventeException {
        /** Type of failure. */
        private FailureType failureType;
        /**
         * Constructor.
         * @param failureType type of failure
         * @param msg message
         */
        public SmsException(final FailureType failureType, final String msg) {
            super(msg);
            this.failureType = failureType;

        }
        /**
         * Gets type of failure.
         * @return type of failure
         */
        public FailureType getFailureType() {
            return failureType;
        }
        /** {@inheritDoc} */
        @Override
        public String toString() {
            return super.getMessage() + ", failureType=" + failureType;
        }
    }

    // CHECKSTYLE:OFF
    // Got an exception - java.lang.RuntimeException: Unable to get class information for @throws tag 'SmsException'.
    /**
     * Sends a SMS.
     *
     * @param number the phone number
     * @param msg the message
     * @param metadata metadata for a concrete SMS service implementation
     */
    void send(String number, String msg, Map<String, String> metadata) throws SmsException;
    // CHECKSTYLE:ON

}