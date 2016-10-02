package nl.lolmewn.stats.bukkit.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import nl.lolmewn.stats.Condition;
import nl.lolmewn.stats.api.stat.Stat;

/**
 *
 * @author Lolmewn
 */
public class StatDescriptor {

    private final Stat stat;
    private final Set<Condition> conditions = new HashSet<>();

    public StatDescriptor(Stat stat, Condition... conditions) {
        this.stat = stat;
        this.conditions.addAll(Arrays.asList(conditions));
    }

    public Stat getStat() {
        return stat;
    }

    public Set<Condition> getConditions() {
        return conditions;
    }

    public void addCondition(Condition cond) {
        this.conditions.add(cond);
    }

}
