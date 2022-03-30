package me.duncanruns.fsg.runner;

import kaptainwutax.mcutils.rand.seed.WorldSeed;
import kaptainwutax.mcutils.util.data.SeedIterator;
import me.duncanruns.fsg.cryptography.DRandInfo;
import me.duncanruns.fsg.filter.CoastalSeedFilterer;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Random;

/*
 * An individual seed finding thread. Runs the main filter methods of CoastalSeedFilterer.
 * @see me.duncanruns.fsg.CoastalSeedFilterer
 */

public class FSGThread extends Thread {

    private final int threadNum;
    private final boolean printProgress;
    private final long initialSeed;
    private final long startTime;
    private final DRandInfo dRandInfo;
    private volatile FilterResult filterResult = null;

    public FSGThread(boolean printProgress, int threadNum, DRandInfo dRandInfo) throws NoSuchAlgorithmException {
        this.dRandInfo = dRandInfo;
        this.printProgress = printProgress;
        this.threadNum = threadNum;
        Instant instant = Instant.now();
        this.startTime = (instant.getEpochSecond() * (1_000_000_000)) + instant.getNano() + threadNum;
        String seedString = startTime + dRandInfo.randomness;

        // Java SHA256
        // https://stackoverflow.com/questions/5531455/how-to-hash-some-string-with-sha256-in-java
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(seedString.getBytes(StandardCharsets.UTF_8));

        initialSeed = new BigInteger(hash).longValue();
    }

    private FilterResult findSeed(boolean printProgress) {

        int structureChecks = 0;

        Random random = new Random(initialSeed);
        CoastalSeedFilterer filterer = new CoastalSeedFilterer(10);

        while (true) {
            structureChecks++;
            long seed = random.nextLong();
            if (filterer.testAndLocateStructures(seed)) {
                int sisterChecks = 0;
                SeedIterator seedIterator = WorldSeed.getSisterSeeds(seed);
                if (printProgress) System.out.print(",");
                while (seedIterator.hasNext()) {
                    sisterChecks++;
                    if (filterer.testBiomes(seedIterator.next())) {
                        if (printProgress) System.out.println("!");
                        return new FilterResult(filterer.getSeed(), startTime, structureChecks, sisterChecks, dRandInfo.round);
                    }
                }
                if (printProgress) System.out.print(".");
            }
        }
        //return null;
    }

    public void run() {
        super.run();
        if (printProgress) System.out.println("Thread #" + (threadNum + 1) + " started");
        FilterResult filterResult = findSeed(printProgress);
        this.filterResult = filterResult;
        FSG115.foundSeed.set(true);
    }

    public FilterResult getFilterResult() {
        return filterResult;
    }
}
