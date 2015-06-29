package nl.lolmewn.stats.signs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.stats.api.StatManager;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.storage.StatGsonAdapter;

/**
 *
 * @author Lolmewn
 */
public class SignManager {

    private final Gson gson;
    private final File signFile;
    private final HashMap<SignLocation, StatsSign> signs = new HashMap<>();

    public SignManager(File signFile, StatManager statManager, final Class<? extends StatsSign> signImpl) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeHierarchyAdapter(Stat.class, new StatGsonAdapter(statManager));
        builder.enableComplexMapKeySerialization();
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(StatsSign.class, new InstanceCreator() {

            @Override
            public Object createInstance(Type type) {
                try {
                    return signImpl.getConstructor(SignLocation.class, SignPlayerType.class, SignStatType.class).newInstance(new SignLocation("", 0, 0, 0), SignPlayerType.SINGLE, SignStatType.SINGLE);
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Logger.getLogger(SignManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }
        });
        this.gson = builder.create();
        this.signFile = signFile;
    }

    public void load() throws FileNotFoundException, IOException {
        if (!signFile.exists()) {
            signFile.createNewFile();
            return; // nothing to load
        }
        BufferedReader br = new BufferedReader(new FileReader(signFile));
        Type type = new TypeToken<Map<SignLocation, StatsSign>>() {
        }.getType();
        Map<SignLocation, StatsSign> map = gson.fromJson(br, type);
        signs.putAll(map);
    }

    public void save() throws FileNotFoundException, IOException {
        if (!signFile.exists()) {
            signFile.createNewFile();
        }
        try (PrintWriter writer = new PrintWriter(signFile)) {
            String json = gson.toJson(signs);
            writer.write(json);
        }
    }

    public void addSign(StatsSign sign) {
        this.signs.put(sign.getLocation(), sign);
    }

    public Collection<StatsSign> getSigns() {
        return signs.values();
    }

}
