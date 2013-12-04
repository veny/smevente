package veny.smevente.client.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A collection of SMS and phone number oriented utilities.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 15.9.2010
 *
 * @link http://mobilni-operatori.info/predvolby-mobilnich-operatoru.html
 * @link http://cs.wikipedia.org/wiki/Mobiln%C3%AD_oper%C3%A1tor
 */
public final class SmsUtils {

    /** Textually representation of Czech locale. */
    public static final String LOCALE_CS = "cs";
    /** Message of an exception to signal that SMS limit of an unit has exceeded. */
    public static final String SMS_LIMIT_EXCEEDE = "smsLimitExceeded";

    /** Map with national GSM info. */
    private static Map<String, Pair<String, List<String>>> prefixes = null;

    /** Suppresses default constructor, ensuring non-instantiability. */
    private SmsUtils() { }

    /**
     * Checks whether the given phone number is a number from Czech Republic.
     * @param pn number to check
     * @param locale nation
     * @return <i>true</i> if it is a czech number
     */
    public static boolean isNationalNumber(final String pn, final String locale) {
        if (null == pn) {
            throw new NullPointerException("phone number cannot be blank");
        }
        final Map<String, Pair<String, List<String>>> p = getPrefixes();
        return (p.containsKey(locale) && pn.startsWith(p.get(locale).getA()));
    }

    /**
     * Checks whether the given phone number is a valid GSM number (because of SMS sending)
     * for given nation.
     * @param pn phone number to check
     * @param locale nation
     * @return <i>true</i> if it is a valid GSM number
     */
    public static boolean isValidGsmPhoneNumber(final String pn, final String locale) {
        final Map<String, Pair<String, List<String>>> p = getPrefixes();
        if (!p.containsKey(locale)) { return false; }

        // cut the national prefix if necessary
        String number;
        if (isNationalNumber(pn, locale)) {
            number = pn.substring(p.get(locale).getA().length());
        } else {
            number = pn;
        }

        boolean rslt = false;
        final List<String> pnPrefixes = p.get(locale).getB();
        for (String prefix : pnPrefixes) {
            if (number.startsWith(prefix)) {
                rslt = true;
                break;
            }
        }
        return rslt;
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Gets initialized map of national prefixes.
     * @return initialized map of national prefixes
     */
    private static synchronized Map<String, Pair<String, List<String>>> getPrefixes() {
        if (null == prefixes) {
            prefixes = new HashMap<String, Pair<String, List<String>>>();
            // CS
            prefixes.put(LOCALE_CS, new Pair<String, List<String>>("00420", Arrays.asList(new String[] {
                // O2
                "601", "602", "606", "607", "72",
                // T-Mobile
                "603", "604", "605", "73",
                // Vodafone
                "608", "77",
                // U:fon
                "79"
            })));
        }
        return prefixes;
    }

    /**
     * For testing purposes.
     * @param args CLI arguments
     */
    public static void main(final String[] args) {
        final String cs = LOCALE_CS;
        System.out.println(isNationalNumber("00420606146177", cs)); //CSOFF
        System.out.println(isNationalNumber("606146177", cs)); //CSOFF

        System.out.println(isValidGsmPhoneNumber("00420606146177", cs)); //CSOFF
        System.out.println(isValidGsmPhoneNumber("606146177", cs)); //CSOFF
    }

}
