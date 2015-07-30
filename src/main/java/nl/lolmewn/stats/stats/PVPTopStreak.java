package nl.lolmewn.stats.stats;

/**
 *
 * @author Lolmewn
 */
public class PVPTopStreak extends SimpleStat {

    public PVPTopStreak() {
        super("PVP top streak");
    }

    @Override
    public boolean isSummable() {
        return false;
    }

}
