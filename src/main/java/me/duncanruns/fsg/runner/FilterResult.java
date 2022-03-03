package me.duncanruns.fsg.runner;

// Info class which holds all relevant info to make a token. Also makes the token.

public class FilterResult {

    private final long worldSeed;
    private final long startTime;
    private final int structureChecks;
    private final int sisterChecks;
    private final int round;

    public FilterResult(long worldSeed, long startTime, int structureChecks, int sisterChecks, int round) {
        this.worldSeed = worldSeed;
        this.startTime = startTime;
        this.structureChecks = structureChecks;
        this.sisterChecks = sisterChecks;
        this.round = round;
    }

    public long getWorldSeed() {
        return worldSeed;
    }

    public String toToken() {
        return Long.toHexString(startTime) + "g" + Long.toHexString(worldSeed) + "g" + Integer.toHexString(structureChecks) + "g" + Integer.toHexString(sisterChecks) + "g" + Integer.toHexString(round);
    }

    @Override
    public String toString() {
        return "FilterResult{" +
                "worldSeed=" + worldSeed +
                ", initialSeed=" + startTime +
                ", structureChecks=" + structureChecks +
                ", sisterChecks=" + sisterChecks +
                '}';
    }
}
