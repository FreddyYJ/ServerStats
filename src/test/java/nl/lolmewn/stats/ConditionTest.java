package nl.lolmewn.stats;

import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import org.junit.Assert;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Lolmewn
 */
public class ConditionTest {

    public ConditionTest() {
    }

    @Test
    public void testPositiveParse() {
        System.out.println("Test positive parse values");
        
        String line = "world=world|world_nether";
        Condition cond = Condition.parse(line);
        assertTrue(cond.matches(new DefaultStatEntry(0, new MetadataPair("world", "world_nether"))));
        assertTrue(cond.matches(new DefaultStatEntry(0, new MetadataPair("world", "world"))));
        assertFalse(cond.matches(new DefaultStatEntry(0, new MetadataPair("world", "world_the_end"))));
        
        line = "world=world";
        cond = Condition.parse(line);
        assertFalse(cond.matches(new DefaultStatEntry(0, new MetadataPair("world", "world_nether"))));
        assertTrue(cond.matches(new DefaultStatEntry(0, new MetadataPair("world", "world"))));
        assertFalse(cond.matches(new DefaultStatEntry(0, new MetadataPair("world", "world_the_end"))));
    }

    @Test
    public void testNegativeParse() {
        System.out.println("Test negative parse values");
        
        String line = "world=!world|!world_nether";
        Condition cond = Condition.parse(line);
        assertFalse(cond.matches(new DefaultStatEntry(0, new MetadataPair("world", "world_nether"))));
        assertFalse(cond.matches(new DefaultStatEntry(0, new MetadataPair("world", "world"))));
        assertTrue(cond.matches(new DefaultStatEntry(0, new MetadataPair("world", "world_the_end"))));
        
        line = "world=!world";
        cond = Condition.parse(line);
        assertTrue(cond.matches(new DefaultStatEntry(0, new MetadataPair("world", "world_nether"))));
        assertFalse(cond.matches(new DefaultStatEntry(0, new MetadataPair("world", "world"))));
        assertTrue(cond.matches(new DefaultStatEntry(0, new MetadataPair("world", "world_the_end"))));
    }

    @Test
    public void testParseInput() {
        System.out.println("Test parse input");
        String line = "world";
        Assert.assertNull(Condition.parse(line));
        
        line = null;
        Assert.assertNull(Condition.parse(line));
        
        line = "world=world=world";
        Assert.assertNull(Condition.parse(line));
    }

}
