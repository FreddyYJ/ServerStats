package nl.lolmewn.stats.signs;

import java.util.Objects;

/**
 *
 * @author Lolmewn
 */
public class SignLocation {
    
    private final String world;
    private final int x, y, z;

    public SignLocation(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.world);
        hash = 43 * hash + this.x;
        hash = 43 * hash + this.y;
        hash = 43 * hash + this.z;
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
        final SignLocation other = (SignLocation) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        if (this.z != other.z) {
            return false;
        }
        return Objects.equals(this.world, other.world);
    }

}
