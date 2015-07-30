package nl.lolmewn.stats.user;

import java.util.HashMap;
import java.util.UUID;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.api.storage.DataType;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author Lolmewn
 */
public class StatsStatHolderTest {

    private final UUID uuid = UUID.randomUUID();
    private final String name = "test";
    @Mock
    private Stat exampleStat1, exampleStat2;

    public StatsStatHolderTest() {
        MockitoAnnotations.initMocks(this);
        when(exampleStat1.getName()).thenReturn("testStat");
        when(exampleStat1.getDataTypes()).thenReturn(new HashMap<String, DataType>() {
            {
                this.put("key", DataType.STRING);
            }
        });
        when(exampleStat2.getName()).thenReturn("testStat2");
        when(exampleStat2.getDataTypes()).thenReturn(new HashMap<String, DataType>() {
            {
                this.put("key", DataType.STRING);
            }
        });
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getName method, of class StatsStatHolder.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        StatsStatHolder instance = new StatsStatHolder(uuid, name);
        assertEquals(name, instance.getName());
    }

    /**
     * Test of addEntry method, of class StatsStatHolder.
     */
    @Test
    public void testAddEntry() {
        System.out.println("addEntry");
        StatsStatHolder instance = new StatsStatHolder(uuid, name);
        StatEntry entry = new DefaultStatEntry(10, new MetadataPair("key", "value"));
        instance.addEntry(exampleStat1, entry);
        assertTrue(instance.getStats(exampleStat1).size() == 1);
        assertEquals(entry, instance.getStats(exampleStat1).iterator().next());

        instance.addEntry(exampleStat1, entry);
        assertTrue(instance.getStats(exampleStat1).size() == 1);
        assertEquals(20, instance.getStats(exampleStat1).iterator().next().getValue(), 0.1);
    }

    /**
     * Test of getStats method, of class StatsStatHolder.
     */
    @Test
    public void testGetStats_0args() {
        System.out.println("getStats");
        StatsStatHolder instance = new StatsStatHolder(uuid, name);
        StatEntry entry = new DefaultStatEntry(5);
        instance.addEntry(exampleStat1, entry);
        instance.addEntry(exampleStat2, entry);
        assertEquals(2, instance.getStats().size());
    }

    /**
     * Test of getStats method, of class StatsStatHolder.
     */
    @Test
    public void testGetStats_Stat() {
        System.out.println("getStats");
        StatsStatHolder instance = new StatsStatHolder(uuid, name);
        StatEntry entry = new DefaultStatEntry(5);
        instance.addEntry(exampleStat1, entry);
        assertFalse(instance.getStats().isEmpty());
        assertFalse(instance.getStats(exampleStat1).isEmpty());
        assertEquals(entry, instance.getStats(exampleStat1).iterator().next());
    }

    /**
     * Test of getUuid method, of class StatsStatHolder.
     */
    @Test
    public void testGetUuid() {
        System.out.println("getUuid");
        StatsStatHolder instance = new StatsStatHolder(uuid, name);
        assertEquals(uuid, instance.getUuid());
    }

    /**
     * Test of hasStat method, of class StatsStatHolder.
     */
    @Test
    public void testHasStat() {
        System.out.println("hasStat");
        StatsStatHolder instance = new StatsStatHolder(uuid, name);
        assertFalse(instance.hasStat(exampleStat1));
        instance.addEntry(exampleStat1, new DefaultStatEntry(5));
        assertTrue(instance.hasStat(exampleStat1));
    }

    /**
     * Test of removeStat method, of class StatsStatHolder.
     */
    @Test
    public void testRemoveStat() {
        System.out.println("removeStat");
        StatsStatHolder instance = new StatsStatHolder(uuid, name);
        instance.addEntry(exampleStat1, new DefaultStatEntry());
        assertTrue(instance.hasStat(exampleStat1));
        instance.removeStat(exampleStat1);
        assertFalse(instance.hasStat(exampleStat1));
    }

    /**
     * Test of removeEntry method, of class StatsStatHolder.
     */
    @Test
    public void testRemoveEntry() {
        System.out.println("removeEntry");
        StatsStatHolder instance = new StatsStatHolder(uuid, name);
        StatEntry entry = new DefaultStatEntry(5);
        instance.addEntry(exampleStat1, entry);
        assertTrue(instance.hasStat(exampleStat1));
        instance.removeEntry(exampleStat1, entry);
        assertFalse(instance.hasStat(exampleStat1));
    }

}
