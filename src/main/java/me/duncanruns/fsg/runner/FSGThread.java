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
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

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
    private final Consumer<FilterResult> resultConsumer;
    private final BooleanSupplier continueSupplier;

    public FSGThread(boolean printProgress, int threadNum, DRandInfo dRandInfo, Instant instant, Consumer<FilterResult> resultConsumer, BooleanSupplier continueSupplier) throws NoSuchAlgorithmException {
        this.dRandInfo = dRandInfo;
        this.printProgress = printProgress;
        this.threadNum = threadNum;
        this.startTime = (instant.getEpochSecond() * (1_000_000_000)) + instant.getNano() + threadNum;
        this.resultConsumer = resultConsumer;
        this.continueSupplier = continueSupplier;
        String seedString = startTime + dRandInfo.randomness;

        // Java SHA256
        // https://stackoverflow.com/questions/5531455/how-to-hash-some-string-with-sha256-in-java
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(seedString.getBytes(StandardCharsets.UTF_8));

        initialSeed = new BigInteger(hash).longValue();
    }

    public void run() {
        super.run();
        if (printProgress) System.out.println("Thread #" + (threadNum + 1) + " started");

        int structureChecks = 0;

        Random random = new Random(initialSeed);
        CoastalSeedFilterer filterer = new CoastalSeedFilterer(10);

        while (continueSupplier.getAsBoolean()) {
            structureChecks++;
            long seed = random.nextLong();
            if (filterer.testAndLocateStructures(seed)) {
                int sisterChecks = 0;
                SeedIterator seedIterator = WorldSeed.getSisterSeeds(seed);
                if (printProgress) System.out.print(",");
                while (seedIterator.hasNext() && continueSupplier.getAsBoolean()) {
                    sisterChecks++;
                    if (filterer.testBiomes(seedIterator.next())) {
                        if (printProgress) System.out.println("!");
                        resultConsumer.accept(new FilterResult(filterer.getSeed(), startTime, structureChecks, sisterChecks, dRandInfo.round));
                        break;
                    }
                }
                if (printProgress) System.out.print(".");
            }
        }
    }
}
