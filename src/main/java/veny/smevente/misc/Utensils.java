package veny.smevente.misc;

import javax.annotation.Nullable;

/**
 * Implementation of various utilities.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 19.11.2016
 */
public final class Utensils {

    /**
     * Private constructor to avoid creating object of utility class.
     */
    private Utensils() {
    }

    /**
     * Returns {@code true} if the given string is null or is the empty string.
     *
     * @param string a string reference to check
     * @return {@code true} if the string is null or is the empty string
     */
    public static boolean stringIsBlank(final @Nullable String string) {
        return null == string || 0 == string.trim().length();
    }

}
