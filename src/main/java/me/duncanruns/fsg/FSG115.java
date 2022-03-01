package me.duncanruns.fsg;

import kaptainwutax.mcutils.rand.seed.WorldSeed;
import kaptainwutax.mcutils.util.data.SeedIterator;

import java.util.Random;

public class FSG115 {

    public static void main(String[] args) {
        System.out.println("--------------------\nMinecraft 1.15.2 FSG by DuncanRuns\nRunning on Java " + System.getProperty("java.version") + "\n--------------------");
        System.out.println(findSeed(true));
    }

    public static Long findSeed(boolean printProgress) {

        Random random = new Random();
        CoastalSeedFilterer filterer = new CoastalSeedFilterer();

        while (true) {
            long seed = random.nextLong();
            if (filterer.testAndLocateStructures(seed)) {
                SeedIterator seedIterator = WorldSeed.getSisterSeeds(seed);
                if (printProgress)
                    System.out.print(",");
                while (seedIterator.hasNext()) {
                    if (filterer.testBiomes(seedIterator.next())) {
                        if (printProgress)
                            System.out.println("!");
                        return filterer.getSeed();
                    }
                }

            }
        }
    }
}
