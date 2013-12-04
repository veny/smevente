package veny.smevente.client.utils;


/**
 * Triple class.
 *
 * @param <A> type of first element
 * @param <B> type of second element
 * @param <C> type of third element
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 1.8.2010
 */
public class Triple<A, B, C> extends Pair<A, B> {

    /** Serial version UID. */
    private static final long serialVersionUID = -5421539782835612516L;

    /** Third element. */
    private final C c;

    /**
     * Constructor.
     * @param a first element
     * @param b second element
     * @param c third element
     */
    public Triple(final A a, final B b, final C c) {
        super(a, b);
        this.c = c;
    }

    /**
     * Gets third element.
     * @return third element
     */
    public C getC() {
        return c;
    }

}
