package veny.smevente.misc;

import static java.util.Locale.ENGLISH;

import java.lang.reflect.Method;

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

    /**
     * Return the value of the specified property of the specified bean.
     *
     * @param bean Bean whose property is to be extracted
     * @param name Name of the property to be extracted
     * @return the property value
     * @throws SecurityException
     *
     * @exception ReflectiveOperationException if invokation of the property accessor method fails
     */
    public static Object getBeanProperty(final Object bean, final String name) throws ReflectiveOperationException {
        if (bean == null) {
            throw new IllegalArgumentException("No bean specified");
        }
        if (name == null) {
            throw new IllegalArgumentException("No name specified for bean class '" + bean.getClass() + "'");
        }

        final String methodName = "get" + (name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1));
        final Method method = bean.getClass().getMethod(methodName);

        return method.invoke(bean);
    }


}
