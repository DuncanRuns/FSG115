package me.duncanruns.fsg;

import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.loot.ChestContent;
import kaptainwutax.featureutils.loot.item.ItemStack;
import kaptainwutax.featureutils.loot.item.Items;
import kaptainwutax.featureutils.misc.SpawnPoint;
import kaptainwutax.featureutils.structure.*;
import kaptainwutax.featureutils.structure.generator.structure.ShipwreckGenerator;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.util.math.DistanceMetric;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.mcutils.version.MCVersion;
import kaptainwutax.terrainutils.terrain.OverworldTerrainGenerator;

import java.util.List;

public class CoastalSeedFilterer {
    private static final MCVersion MCVERSION = MCVersion.v1_15_2;
    private static final Village VILLAGE = new Village(MCVERSION);
    private static final Stronghold STRONGHOLD = new Stronghold(MCVERSION);
    private static final Monument MONUMENT = new Monument(MCVERSION);
    private static final Shipwreck SHIPWRECK = new Shipwreck(MCVERSION);
    private static final Fortress FORTRESS = new Fortress(MCVERSION);
    private static final List<String> GOOD_WRECKS = List.of("with_mast", "upsidedown_full", "upsidedown_backhalf", "sideways_full", "sideways_backhalf", "rightsideup_full", "rightsideup_backhalf", "with_mast_degraded", "upsidedown_full_degraded", "upsidedown_backhalf_degraded", "sideways_full_degraded", "sideways_backhalf_degraded", "rightsideup_full_degraded", "rightsideup_backhalf_degraded");
    private static final SpawnPoint SPAWN_POINT = new SpawnPoint();

    //private List<FoundShip> shipwrecks;
    private long seed;
    private ChunkRand chunkRand;
    private CPos villagePos;
    private CPos monumentPos;
    private CPos mainShipwreckPos;
    private Integer mainShipwreckEmeralds;

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
        this.chunkRand = new ChunkRand();
    }

    public boolean testAndLocateStructures() {

        testVillageS();

        if (villagePos == null) {
            // No village
            return false;
        }

        testMonumentS();

        if (monumentPos == null) {
            // No monument
            return false;
        }

        testMainShipwreckS();

        if (mainShipwreckPos == null) {
            // No shipwreck
            return false;
        }

        testMainShipwreckG();

        if (mainShipwreckEmeralds == null) {
            // Bad shipwreck
            return false;
        }

        return true;
    }

    private void testVillageS() {
        villagePos = null;

        // Find a close enough village to 0,0
        CPos pos = VILLAGE.getInRegion(seed, 0, 0, chunkRand);
        if (pos != null && pos.distanceTo(CPos.ZERO, DistanceMetric.EUCLIDEAN_SQ) < 87.890625) {
            villagePos = pos;
        }
    }

    private void testMonumentS() {
        monumentPos = null;

        CPos cPos = MONUMENT.getInRegion(seed, 0, 0, chunkRand);
        double d = villagePos.distanceTo(cPos, DistanceMetric.EUCLIDEAN_SQ);
        if (d < 351.5625 && d > 87.890625
                && villagePos.distanceTo(CPos.ZERO, DistanceMetric.EUCLIDEAN_SQ)
                < cPos.distanceTo(CPos.ZERO, DistanceMetric.EUCLIDEAN_SQ)) {
            monumentPos = cPos;
        }


    }

    private void testMainShipwreckS() {
        mainShipwreckPos = null;

        CPos middlePos = new CPos((monumentPos.getX() + villagePos.getX()) / 2, (monumentPos.getZ() + villagePos.getZ()) / 2);
        CPos pos = SHIPWRECK.getInRegion(seed, 0, 0, chunkRand);

        //System.out.println(middlePos.toBlockPos() + " - " + middlePos.distanceTo(pos, DistanceMetric.EUCLIDEAN_SQ) + " - " + pos.toBlockPos());

        if (middlePos.distanceTo(pos, DistanceMetric.EUCLIDEAN_SQ) < 9.765625) {
            mainShipwreckPos = pos;
        }
    }

    private void testMainShipwreckG() {
        mainShipwreckEmeralds = null;

        ShipwreckGenerator shipwreckGenerator = new ShipwreckGenerator(MCVERSION);
        shipwreckGenerator.generate(seed, Dimension.OVERWORLD, mainShipwreckPos.getX(), mainShipwreckPos.getZ());
        double iron = 0;
        int emeralds = 0;
        for (ChestContent chestContent : SHIPWRECK.getLoot(seed, shipwreckGenerator, chunkRand, false)) {
            for (ItemStack itemStack : chestContent.getItems()) {
                if (itemStack.getItem().equals(Items.IRON_INGOT)) {
                    iron += itemStack.getCount();
                } else if (itemStack.getItem().equals(Items.IRON_NUGGET)) {
                    iron += itemStack.getCount() / 9.0D;
                } else if (itemStack.getItem().equals(Items.EMERALD)) {
                    emeralds += itemStack.getCount();
                }
            }
        }

        if (emeralds >= 10 && iron >= 7) {
            mainShipwreckEmeralds = emeralds;
        }
    }

    public boolean testBiomes() {
        OverworldBiomeSource overworldBiomeSource = new OverworldBiomeSource(MCVERSION, seed);
        if (VILLAGE.canSpawn(villagePos, overworldBiomeSource)
                && MONUMENT.canSpawn(monumentPos, overworldBiomeSource)
                && SHIPWRECK.canSpawn(mainShipwreckPos, overworldBiomeSource)
                && villagePos.toBlockPos(64).distanceTo(SPAWN_POINT.getApproximateSpawnPoint(overworldBiomeSource), DistanceMetric.EUCLIDEAN_SQ) < 2500) {
            BPos spawnPos = SPAWN_POINT.getSpawnPoint(new OverworldTerrainGenerator(overworldBiomeSource));
            return villagePos.toBlockPos(spawnPos.getY()).distanceTo(spawnPos, DistanceMetric.EUCLIDEAN_SQ) < 2500;
        }
        return false;
    }


}
