package veny.smevente.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * A collection of text oriented utilities.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 20.8.2010
 */
public final class TextUtils {

    /** Czech diacritics. */
    public static final String[][] CZ_2_ASCII = {
        { "á", "č", "ď", "é", "ě", "í", "ň", "ó", "ř", "š", "ť", "ú", "ů", "ý", "ž",
          "Á", "Č", "Ď", "É", "Ě", "Í", "Ň", "Ó", "Ř", "Š", "Ť", "Ú", "Ý", "Ž" },
        { "a", "c", "d", "e", "e", "i", "n", "o", "r", "s", "t", "u", "u", "y", "z",
          "A", "C", "D", "E", "E", "I", "N", "O", "R", "S", "T", "U", "Y", "Z" },
    };

    /** Suppresses default constructor, ensuring non-instantiability. */
    private TextUtils() { }

    /**
     * Replaces in SMS text predefined sequences.
     * @param sms SMS text
     * @param replaceConf map with replacement configuration
     * @return replaced SMS text
     */
    public static String formatSmsText(final String sms, final Map<String, String> replaceConf) {
        String rslt = sms;
        for (Map.Entry<String, String> e : replaceConf.entrySet()) {
            rslt = StringUtils.replace(rslt, e.getKey(), e.getValue());
        }
        return convert2ascii(rslt);
    }

    /**
     * Converts czech UTF characters to ASCII.
     *
     * @param s text to convert to ASCII
     * @return ASCII text
     */
    public static String convert2ascii(final String s) {
        return StringUtils.replaceEach(s, CZ_2_ASCII[0], CZ_2_ASCII[1]);
    }

    /**
     * Sanitizes number to be really a number.
     * @param n number as text
     * @return sanitized number
     */
    public static String sanitizeNumber(final String n) {
        String rslt = StringUtils.remove(n, ' ');
        rslt = StringUtils.remove(rslt, '/');
        return rslt;
    }

    /**
     * Converts Java Map to String.
     * @param map map to convert
     * @return textually representation of given map
     */
    public static String mapToString(final Map<String, String> map) {
        final StringBuilder rslt = new StringBuilder();

        for (String key : map.keySet()) {
            if (rslt.length() > 0) {
                rslt.append("&");
            }
            final String value = map.get(key);
            try {
                rslt.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
                rslt.append("=");
                rslt.append(value != null ? URLEncoder.encode(value, "UTF-8") : "");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("this method requires UTF-8 encoding support", e);
            }
        }
        return rslt.toString();
    }

    /**
     * Converts String to Java Map.
     * @param s textually representation of a map
     * @return map converted from text
     */
    public static Map<String, String> stringToMap(final String s) {
        if (null == s) { throw new NullPointerException("text to convert cannot be null"); }
        final Map<String, String> map = new HashMap<String, String>();

        String[] nameValuePairs = s.split("&");
        for (String nameValuePair : nameValuePairs) {
            final String[] nameValue = nameValuePair.split("=");
            try {
                map.put(URLDecoder.decode(nameValue[0], "UTF-8"),
                        nameValue.length > 1 ? URLDecoder.decode(nameValue[1], "UTF-8") : "");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("this method requires UTF-8 encoding support", e);
            }
        }
        return map;
    }

    /**
     * For testing purposes.
     * @param args CLI arguments
     */
    public static void main(final String[] args) {
        System.out.println(convert2ascii("Žluťoučký kůň pěl ďábelské ódy")); // CSOFF
        System.out.println(sanitizeNumber("608 346 123")); // CSOFF
        System.out.println("|" + sanitizeNumber("") + "|"); // CSOFF

        final Map<String, String> map = new HashMap<String, String>();
        map.put("color", "red");
        map.put("symbols", "{,=&*?}");
        map.put("empty", "");
        final String output = mapToString(map);
        final Map<String, String> parsedMap = stringToMap(output);
        for (String key : map.keySet()) {
            System.out.println(key + ":'" + parsedMap.get(key) + "'"); //CSOFF
        }
    }

}
