package veny.smevente.service;

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
     * Replaces predefined sequences in message text .
     * @param msg message text
     * @param replaceConf map with replacement configuration
     * @return replaced message text
     */
    public static String formatEventText(final String msg, final Map<String, String> replaceConf) {
        String rslt = msg;
        for (Map.Entry<String, String> e : replaceConf.entrySet()) {
            rslt = StringUtils.replace(rslt, e.getKey(), e.getValue());
        }
        return rslt;
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
     * For testing purposes.
     * @param args CLI arguments
     */
    public static void main(final String[] args) {
        System.out.println(convert2ascii("Žluťoučký kůň pěl ďábelské ódy")); //CSOFF
        System.out.println(sanitizeNumber("608 346 123")); //CSOFF
        System.out.println("|" + sanitizeNumber("") + "|"); //CSOFF
    }

}
