package nl.lolmewn.stats.signs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import nl.lolmewn.stats.DefaultStatManager;
import nl.lolmewn.stats.api.StatManager;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.bukkit.signs.BukkitStatsSign;
import nl.lolmewn.stats.storage.InterfaceAdapter;
import nl.lolmewn.stats.storage.StatGsonAdapter;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Lolmewn
 */
public class SignManagerTest {

    private static final File TEST_FILE = new File("test/signs.json");
    private static final StatManager STAT_MANAGER = new DefaultStatManager();
    private static Gson GSON;

    public SignManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        System.out.println("setup");
        if (!TEST_FILE.exists()) {
            TEST_FILE.getParentFile().mkdirs();
            TEST_FILE.createNewFile();
        }
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeHierarchyAdapter(Stat.class, new StatGsonAdapter(STAT_MANAGER));
        builder.enableComplexMapKeySerialization();
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(StatsSign.class, new InterfaceAdapter<StatsSign>());
        
        GSON = builder.create();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of load method, of class SignManager.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testLoad() throws IOException {
        System.out.println("load");
        SignManager instance = new SignManager(TEST_FILE, STAT_MANAGER);
        BukkitStatsSign sign = new BukkitStatsSign(new SignLocation("world", 1, 2, 3), SignPlayerType.SINGLE, SignStatType.SINGLE);
        sign.addHolder(UUID.randomUUID());
        Stat mockStat = mock(Stat.class);
        when(mockStat.getName()).thenReturn("TestStat");
        sign.addStat(mockStat);
        STAT_MANAGER.addStat(mockStat);
        instance.addSign(sign);

        instance.save();
        instance = new SignManager(TEST_FILE, STAT_MANAGER);
        instance.load();

        assertEquals(1, instance.getSigns().size());
        StatsSign loaded = instance.getSigns().iterator().next();
        assertEquals(SignPlayerType.SINGLE, loaded.getSignType());
        assertEquals(1, loaded.getStats().size());
        assertEquals(1, loaded.getHolders().size());
    }

    @Test
    public void testSingleSign() {
        System.out.println("single");
        BukkitStatsSign sign = new BukkitStatsSign(new SignLocation("world", 1, 2, 3), SignPlayerType.SINGLE, SignStatType.SINGLE);
        sign.addHolder(UUID.randomUUID());
        Stat mockStat = mock(Stat.class);
        when(mockStat.getName()).thenReturn("TestStat");
        sign.addStat(mockStat);

        String gson = GSON.toJson(sign);

        BukkitStatsSign loaded = GSON.fromJson(gson, BukkitStatsSign.class);
        assertEquals(SignPlayerType.SINGLE, loaded.getSignType());
        assertEquals(1, loaded.getStats().size());
        assertEquals(1, loaded.getHolders().size());
    }

    @Test
    public void testListSigns() {
        System.out.println("map");
        SignLocation loc = new SignLocation("world", 1, 2, 3);
        BukkitStatsSign sign = new BukkitStatsSign(loc, SignPlayerType.SINGLE, SignStatType.SINGLE);
        sign.addHolder(UUID.randomUUID());
        Stat mockStat = mock(Stat.class);
        when(mockStat.getName()).thenReturn("TestStat");
        sign.addStat(mockStat);

        HashMap<SignLocation, StatsSign> signs = new HashMap<>();
        signs.put(loc, sign);
        
        Type type = new TypeToken<Map<SignLocation, StatsSign>>(){}.getType();

        String gson = GSON.toJson(signs, type);
        Map<SignLocation, StatsSign> loaded = GSON.fromJson(gson, type);
        assertEquals(1, loaded.size());
        assertEquals(1, loaded.get(loc).getStats().size());
    }

}
