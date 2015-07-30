package nl.lolmewn.stats.stats;

/**
 *
 * @author Lolmewn
 */
public class PVPStreak extends SimpleStat {

    public PVPStreak() {
        super("PVP streak");
    }

    @Override
    public boolean isSummable() {
        return false;
    }

}
