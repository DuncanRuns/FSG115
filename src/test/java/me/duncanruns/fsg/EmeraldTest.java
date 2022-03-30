package me.duncanruns.fsg;

import me.duncanruns.fsg.filter.CoastalSeedFilterer;

import java.util.Random;

public class EmeraldTest {
    public static void main(String[] args) {
        StringBuilder sheet = new StringBuilder();
        for (int minEmeralds = 0; minEmeralds <= 20; minEmeralds++) {
            int seedsFound = 0;
            long seedsChecked = 0L;
            while (seedsFound < 100) {
                seedsChecked += runUntilStructureSeed(minEmeralds);
                seedsFound++;
                System.out.print(".");
            }
            System.out.println("\nFor " + minEmeralds + " emeralds, it took " + seedsChecked + " seeds.");
            if (!sheet.toString().equals("")) {
                sheet.append("\n");
            }
            sheet.append(minEmeralds).append("\t").append(seedsChecked);
        }
        System.out.println(sheet);
    }

    private static long runUntilStructureSeed(int minEmeralds) {
        CoastalSeedFilterer filterer = new CoastalSeedFilterer(minEmeralds);
        Random random = new Random();
        boolean gettingOtherStructurePos = true;
        while (gettingOtherStructurePos) {
            filterer.setSeed(random.nextLong());
            gettingOtherStructurePos = !(filterer.testVillageS() && filterer.testMonumentS() && filterer.testMainShipwreckS());
        }
        long structureChecks = 0;
        while (true) {
            structureChecks++;
            filterer.setSeed(random.nextLong());
            if (filterer.testMainShipwreckGen(minEmeralds,0)) {
                return structureChecks;
            }
        }
    }
}
