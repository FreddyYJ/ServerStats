package nl.lolmewn.stats;

import java.util.List;

/**
 *
 * @author Lolmewn
 */
public interface Config {
    
    public String getString(String key);
    public String getString(String key, String def);
    public int getInteger(String key);
    public int getInteger(String key, int def);
    public List<String> getStringList(String key);

}
