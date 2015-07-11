package nl.lolmewn.stats.signs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import nl.lolmewn.stats.api.StatManager;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.storage.InterfaceAdapter;
import nl.lolmewn.stats.storage.StatGsonAdapter;

/**
 *
 * @author Lolmewn
 */
public class SignManager {

    private final Gson gson;
    private final File signFile;
    private final HashMap<SignLocation, StatsSign> signs = new HashMap<>();
    private final Type type = new TypeToken<Map<SignLocation, StatsSign>>() {
    }.getType();

    public SignManager(File signFile, StatManager statManager) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeHierarchyAdapter(Stat.class, new StatGsonAdapter(statManager));
        builder.enableComplexMapKeySerialization();
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(StatsSign.class, new InterfaceAdapter<StatsSign>());
        this.gson = builder.create();
        this.signFile = signFile;
    }

    public void load() throws FileNotFoundException, IOException {
        if (!signFile.exists()) {
            signFile.createNewFile();
            return; // nothing to load
        }
        BufferedReader br = new BufferedReader(new FileReader(signFile));

        Map<SignLocation, StatsSign> map = gson.fromJson(br, type);
        signs.putAll(map);
    }

    public void save() throws FileNotFoundException, IOException {
        if (!signFile.exists()) {
            signFile.createNewFile();
        }
        try (PrintWriter writer = new PrintWriter(signFile)) {
            String json = gson.toJson(signs, type);
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
