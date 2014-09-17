package veny.smevente.client.utils;


/**
 * A collection of text oriented utilities for GWT client side.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 18.9.2010
 */
public final class ClientTextUtils {

    /** Czech diacritics. */
    public static final char[][] CZ_2_ASCII = {
        { 'á', 'č', 'ď', 'é', 'ě', 'í', 'ň', 'ó', 'ř', 'š', 'ť', 'ú', 'ů', 'ý', 'ž',
          'Á', 'Č', 'Ď', 'É', 'Ě', 'Í', 'Ň', 'Ó', 'Ř', 'Š', 'Ť', 'Ú',      'Ý', 'Ž' },
        { 'a', 'c', 'd', 'e', 'e', 'i', 'n', 'o', 'r', 's', 't', 'u', 'u', 'y', 'z',
          'A', 'C', 'D', 'E', 'E', 'I', 'N', 'O', 'R', 'S', 'T', 'U',      'Y', 'Z' },
    };

    /** Suppresses default constructor, ensuring non-instantiability. */
    private ClientTextUtils() { }

    /**
     * Converts czech UTF characters to ASCII.
     *
     * @param s text to convert to ASCII
     * @return ASCII text
     */
    public static String convert2ascii(final String s) {
        final StringBuilder rslt = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            final char ch = s.charAt(i);
            int j = 0;
            boolean found = false;
            for (char d : CZ_2_ASCII[0]) {
                if (d == ch) {
                    rslt.append(CZ_2_ASCII[1][j]);
                    found = true;
                    break;
                }
                j++;
            }
            if (!found) {
                rslt.append(ch);
            }
        }
        return rslt.toString();
    }

    /**
     * For testing purposes.
     * @param args CLI arguments
     */
    public static void main(final String[] args) {
        System.out.println(convert2ascii("Žluťoučký kůň pěl ďábelské ódy")); //CSOFF
        System.out.println(convert2ascii("Vladimír Šťovíček")); //CSOFF
        System.out.println(convert2ascii("Špůr Josef")); //CSOFF
    }

}
