package nl.lolmewn.stats.mysql.api;

import nl.lolmewn.stats.api.storage.DataType;
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
public class MySQLTableTest {

    public MySQLTableTest() {
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
     * Test of addColumn method, of class MySQLTable.
     */
    @Test
    public void testAddColumn() {
        System.out.println("addColumn");
        String name = "";
        DataType type = null;
        MySQLTable instance = new MySQLTable("test");
        MySQLColumn expResult = new MySQLColumn(name, type);
        MySQLColumn result = instance.addColumn(name, type);
        assertEquals(expResult, result);
    }

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

    /**
     * Test of getColumn method, of class MySQLTable.
     */
    @Test
    public void testGetColumn() {
        System.out.println("getColumn");
        String name = "testColumn";
        MySQLTable instance = new MySQLTable("test");
        MySQLColumn expResult = new MySQLColumn(name, DataType.LONG);
        instance.addColumn(name, DataType.LONG);
        MySQLColumn result = instance.getColumn(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of generateCreateQuery method, of class MySQLTable.
     */
    @Test
    public void testGenerateCreateQuery() {
        System.out.println("generateCreateQuery");
        MySQLTable players = new MySQLTable("players");
        MySQLColumn col = players.addColumn("uuid", DataType.STRING);
        MySQLTable instance = new MySQLTable("test");
        instance.addColumn("uuid", DataType.STRING).references(players, col).setDefault("'someDefaultValue'");
        instance.addColumn("statvalue", DataType.INTEGER).addAttribute(MySQLAttribute.NOT_NULL);
        String result = instance.generateCreateQuery();
        System.out.println(result);
    }

}
