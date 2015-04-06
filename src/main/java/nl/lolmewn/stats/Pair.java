package nl.lolmewn.stats;

/**
 * @author Sybren
 * @param <String> Key for this Pair. Whereever the key is found, it will be replaced by the value
 * @param <T> Value to replace key with. Can be anything, honestly.
 */
public class Pair<String, T> {

    private final String find;
    private final T replace;

    public Pair(String find, T replace) {
        this.find = find;
        this.replace = replace;
    }

    public String getKey() {
        return find;
    }

    public T getValue() {
        return replace;
    }

}
