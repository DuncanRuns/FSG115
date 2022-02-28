package me.duncanruns.fsg;

import kaptainwutax.mcutils.rand.seed.WorldSeed;
import kaptainwutax.mcutils.util.data.SeedIterator;

import java.util.Random;

public class FSG115 {

    public static void main(String[] args) {
        System.out.println(findSeed());
    }

    public static Long findSeed() {

        Random random = new Random();
        CoastalSeedFilterer filterer = new CoastalSeedFilterer();

        while (true) {
            long seed = random.nextLong();
            if (filterer.testAndLocateStructures(seed)) {
                SeedIterator seedIterator = WorldSeed.getSisterSeeds(seed);
                System.out.print(",");
                while (seedIterator.hasNext()) {
                    if (filterer.testBiomes(seedIterator.next())) {
                        System.out.println();
                        return filterer.getSeed();
                    }
                }

            }
        }
    }
}
