package nl.lolmewn.stats.mysql.api;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Lolmewn
 */
public class MySQLTableTest {

    /**
     * Test of getColumns method, of class MySQLTable.
     */
    @Test
    public void testGetColumns() {
        // gets from a Map
    }

    /**
     * Test of getName method, of class MySQLTable.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        MySQLTable instance = new MySQLTable("test");
        String expResult = "test";
        String result = instance.getName();
        assertEquals(expResult, result);
    }

}
