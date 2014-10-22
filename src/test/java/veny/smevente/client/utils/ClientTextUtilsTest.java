package veny.smevente.client.utils;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test of <code>ClientTextUtils</code>.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 22.10.2014
 */
public class ClientTextUtilsTest {

    /** ClientTextUtilsTest.convert2ascii. */
    @Test
    public void convert2ascii() {
        assertEquals("Zlutoucky kun pel dabelske ody", ClientTextUtils.convert2ascii("Žluťoučký kůň pěl ďábelské ódy"));
        assertEquals("Vladimir Stovicek", ClientTextUtils.convert2ascii("Vladimír Šťovíček"));
        assertEquals("Spur Josef", ClientTextUtils.convert2ascii("Špůr Josef"));
        assertEquals("Eva Smulova", ClientTextUtils.convert2ascii("Eva Šmůlová"));
        assertEquals("Janine Trumpi", ClientTextUtils.convert2ascii("Janine Trümpi"));
        assertEquals("Iva Thondlova", ClientTextUtils.convert2ascii("Iva Thöndlová"));
        assertEquals("Ales Glanzner", ClientTextUtils.convert2ascii("Aleš Glänzner"));
    }

}
