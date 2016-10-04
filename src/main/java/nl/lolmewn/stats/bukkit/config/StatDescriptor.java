package nl.lolmewn.stats.bukkit.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import nl.lolmewn.stats.Condition;
import nl.lolmewn.stats.api.StatManager;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.util.Util;

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

    public static StatDescriptor parse(String line, StatManager statManager) {
        if (line.contains(",")) {
            String[] split = line.split(",");
            Stat stat = Util.findStat(statManager, split[0]);
            if (stat == null) {
                System.err.println("Stat could not be found, cannot parse descriptor");
                return null;
            }
            StatDescriptor sd = new StatDescriptor(stat);
            for (int i = 1; i < split.length; i++) {
                sd.addCondition(Condition.parse(split[i]));
            }
            return sd;
        } else {
            Stat stat = Util.findStat(statManager, line);
            if (stat == null) {
                System.err.println("Stat could not be found, cannot parse descriptor");
                return null;
            }
            return new StatDescriptor(stat);
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.stat);
        hash = 97 * hash + Objects.hashCode(this.conditions);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StatDescriptor other = (StatDescriptor) obj;
        if (!Objects.equals(this.stat, other.stat)) {
            return false;
        }
        return Objects.equals(this.conditions, other.conditions);
    }

}
