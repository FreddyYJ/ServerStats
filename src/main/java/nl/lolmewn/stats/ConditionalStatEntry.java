package nl.lolmewn.stats;

import java.util.Map;
import java.util.Set;
import nl.lolmewn.stats.api.stat.StatEntry;

/**
 *
 * @author Lolmewn
 */
public class ConditionalStatEntry implements StatEntry {

    private final StatEntry entry;
    private final Set<Condition> conditions;

    public ConditionalStatEntry(StatEntry entry, Set<Condition> conditions) {
        this.entry = entry;
        this.conditions = conditions;
    }

    @Override
    public double getValue() {
        return entry.getValue();
    }

    @Override
    public Map<String, Object> getMetadata() {
        return entry.getMetadata();
    }

    @Override
    public void setValue(double value) {
        entry.setValue(value);
    }

    public Set<Condition> getConditions() {
        return conditions;
    }

}
