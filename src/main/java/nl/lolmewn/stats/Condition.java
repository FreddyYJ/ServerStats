package nl.lolmewn.stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import nl.lolmewn.stats.api.stat.StatEntry;

/**
 *
 * @author Lolmewn
 */
public class Condition {

    private final String metadataName;
    private final List<String> matches = new ArrayList<>();
    private final MatchingMode mode;

    private Condition(String metadataName, MatchingMode mode) {
        this.metadataName = metadataName;
        this.mode = mode;
    }

    public static class Builder {

        private String buildName;
        private final List<String> buildMatches = new ArrayList<>();
        private MatchingMode buildMode;

        public Builder() {
        }

        public Builder named(String name) {
            this.buildName = name;
            return this;
        }

        public Builder matching(String... matches) {
            this.buildMatches.addAll(Arrays.asList(matches));
            return this;
        }

        public Builder inMode(MatchingMode mode) {
            this.buildMode = mode;
            return this;
        }

        public Condition build() {
            if (buildName == null || buildMode == null || buildMatches.isEmpty()) {
                throw new IllegalStateException("Cannot build Condition, no parameter can be null or empty (" + this.toString() + ")");
            }
            Condition cond = new Condition(buildName, buildMode);
            cond.matches.addAll(buildMatches);
            return cond;
        }

        @Override
        public String toString() {
            return "Builder{" + "buildName=" + buildName + ", buildMatches=" + buildMatches + ", buildMode=" + buildMode + '}';
        }

    }

    public enum MatchingMode {
        CONTAINING, EXCLUDING
    }

    public boolean matches(StatEntry entry) {
        switch (mode) {
            case CONTAINING:
                return entry.getMetadata().containsKey(metadataName) && this.matches.contains(entry.getMetadata().get(metadataName).toString());
            case EXCLUDING:
                return !entry.getMetadata().containsKey(metadataName) || this.matches.stream().noneMatch(str -> Objects.equals(str, entry.getMetadata().get(metadataName)));
        }
        throw new IllegalStateException("Unimplemented case " + mode);
    }

    public String getName() {
        return metadataName;
    }

    public List<String> getMatches() {
        return matches;
    }

    public MatchingMode getMode() {
        return mode;
    }

    public static Condition parse(String cond) {
        if (cond == null) {
            System.err.println("Condition was null, cannot parse");
            return null;
        }
        if (!cond.contains("=")) {
            System.err.println("Condition does not contain '=': " + cond);
            return null;
        }
        String[] vals = cond.split("=");
        if (vals.length != 2) {
            System.err.println("Condition contains too many or too little '=' to be parsed: " + cond);
            return null;
        }
        Condition.Builder builder = new Condition.Builder();
        builder.buildName = vals[0];
        String val = vals[1];
        if (val.contains("|")) {
            // Multiple values
            String[] params = val.split(Pattern.quote("|"));
            for (String param : params) {
                if (param.startsWith("!")) {
                    builder.buildMatches.add(param.substring(1));
                    builder.buildMode = MatchingMode.EXCLUDING;
                } else {
                    builder.buildMatches.add(param);
                    builder.buildMode = MatchingMode.CONTAINING;
                }
            }
        } else if (val.startsWith("!")) {
            builder.buildMatches.add(val.substring(1));
            builder.buildMode = MatchingMode.EXCLUDING;
        } else {
            builder.buildMatches.add(val);
            builder.buildMode = MatchingMode.CONTAINING;
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return metadataName + "=" + matches + ", mode=" + mode;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.metadataName);
        hash = 29 * hash + Objects.hashCode(this.matches);
        hash = 29 * hash + Objects.hashCode(this.mode);
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
        final Condition other = (Condition) obj;
        if (!Objects.equals(this.metadataName, other.metadataName)) {
            return false;
        }
        if (!Objects.equals(this.matches, other.matches)) {
            return false;
        }
        return this.mode == other.mode;
    }
}
