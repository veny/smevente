package veny.smevente.client.utils;

import java.io.Serializable;

/**
 * Simple pair class.
 *
 * @param <A> type of first element
 * @param <B> type of second element
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 1.8.2010
 */
public class Pair<A, B> implements Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 3090191326317997517L;

    /** First Element. */
    private final A a;
    /** Second element. */
    private final B b;

    /**
     * Constructor.
     * @param a first element
     * @param b second element
     */
    public Pair(final A a, final B b) {
        this.a = a;
        this.b = b;
    }

    /**
     * @return first element
     */
    public A getA() {
        return a;
    }

    /**
     * @return second element
     */
    public B getB() {
        return b;
    }

}
