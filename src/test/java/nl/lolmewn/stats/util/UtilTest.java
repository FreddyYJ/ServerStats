package nl.lolmewn.stats.util;

import java.util.Arrays;
import java.util.List;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Lolmewn
 */
public class UtilTest {

    public UtilTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of matches method, of class Util.
     */
    @Test
    public void testMatches() {
        System.out.println("matches");
        StatEntry entry = new DefaultStatEntry(0, new MetadataPair("key", "value"));
        MetadataPair pair = new MetadataPair("key", "value");
        boolean expResult = true;
        boolean result = Util.matches(entry, pair);
        assertEquals(expResult, result);

        pair = new MetadataPair("key", "different value");
        expResult = false;
        result = Util.matches(entry, pair);
        assertEquals(expResult, result);

        pair = new MetadataPair("diff key", "value");
        expResult = false;
        result = Util.matches(entry, pair);
        assertEquals(expResult, result);

        pair = new MetadataPair("diff key", "different value");
        expResult = false;
        result = Util.matches(entry, pair);
        assertEquals(expResult, result);
    }

    /**
     * Test of matchesAll method, of class Util.
     */
    @Test
    public void testMatchesAll_StatEntry_MetadataPairArr() {
        System.out.println("matchesAll");
        StatEntry entry = new DefaultStatEntry(0, new MetadataPair("key", "value"));
        MetadataPair[] pairs = null;
        boolean expResult = false;
        //boolean result = Util.matchesAll(entry, pairs);
        //assertEquals(expResult, result);
    }

    /**
     * Test of matchesAny method, of class Util.
     */
    @Test
    public void testMatchesAny_StatEntry_MetadataPairArr() {
        System.out.println("matchesAny");
        StatEntry entry = null;
        MetadataPair[] pairs = null;
        boolean expResult = false;
        //boolean result = Util.matchesAny(entry, pairs);
        //assertEquals(expResult, result);
    }

    /**
     * Test of matchesNone method, of class Util.
     */
    @Test
    public void testMatchesNone_StatEntry_MetadataPairArr() {
        System.out.println("matchesNone");
        StatEntry entry = null;
        MetadataPair[] pairs = null;
        boolean expResult = false;
        //boolean result = Util.matchesNone(entry, pairs);
        //assertEquals(expResult, result);
    }

    /**
     * Test of matchesAll method, of class Util.
     */
    @Test
    public void testMatchesAll_StatEntry_List() {
        System.out.println("matchesAll");
        StatEntry entry = null;
        List<MetadataPair> pairs = null;
        boolean expResult = false;
        //boolean result = Util.matchesAll(entry, pairs);
        //assertEquals(expResult, result);
    }

    /**
     * Test of matchesAny method, of class Util.
     */
    @Test
    public void testMatchesAny_StatEntry_List() {
        System.out.println("matchesAny");
        StatEntry entry = null;
        List<MetadataPair> pairs = null;
        boolean expResult = false;
        //boolean result = Util.matchesAny(entry, pairs);
        //assertEquals(expResult, result);
    }

    /**
     * Test of matchesNone method, of class Util.
     */
    @Test
    public void testMatchesNone_StatEntry_List() {
        System.out.println("matchesNone");
        MetadataPair pair = new MetadataPair("SomeKey", "someValue");
        StatEntry entry = new DefaultStatEntry(1, pair);
        List<MetadataPair> pairs = Arrays.asList(new MetadataPair[]{
            new MetadataPair("key", "value"),
            new MetadataPair("otherKey", "otherValue")
        });
        boolean expResult = true;
        boolean result = Util.matchesNone(entry, pairs);
        assertEquals(expResult, result);
        
        assertEquals(false, Util.matchesNone(entry, pair));
    }

}
