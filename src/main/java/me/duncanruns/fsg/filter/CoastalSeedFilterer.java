package me.duncanruns.fsg.filter;

import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.biomeutils.biome.Biomes;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// Class to hold information and perform actual logic to filter seeds.

public class CoastalSeedFilterer {
    private static final MCVersion MCVERSION = MCVersion.v1_15_2;
    private static final Village VILLAGE = new Village(MCVERSION);
    private static final Stronghold STRONGHOLD = new Stronghold(MCVERSION);
    private static final Monument MONUMENT = new Monument(MCVERSION);
    private static final Shipwreck SHIPWRECK = new Shipwreck(MCVERSION);
    private static final Fortress FORTRESS = new Fortress(MCVERSION);
    private static final List<String> GOOD_WRECKS = getGoodWrecks();
    private static final List<Biome> GOOD_VILLAGE_BIOMES = getGoodVillageBiomes();
    private static final SpawnPoint SPAWN_POINT = new SpawnPoint();
    private static final double MAX_ANGLE_DIFF = (Math.PI * 2) / 36;
    private static final double BASE_ANGLE = Math.atan2(1, 1);
    private final int minimumEmeralds;
    private long seed;
    private ChunkRand chunkRand;
    private CPos villagePos, monumentPos, mainShipwreckPos, strongholdPos, fortressPos;
    private int strongholdNum;
    private OverworldBiomeSource overworldBiomeSource;
    //private List<FoundShip> shipwrecks;
    private BPos spawnPos;

    public CoastalSeedFilterer(int minimumEmeralds) {
        this.minimumEmeralds = minimumEmeralds;
    }


    private static List<String> getGoodWrecks() {
        List<String> goodWrecks = new ArrayList<>();
        goodWrecks.add("with_mast");
        goodWrecks.add("upsidedown_full");
        goodWrecks.add("upsidedown_backhalf");
        goodWrecks.add("sideways_full");
        goodWrecks.add("sideways_backhalf");
        goodWrecks.add("rightsideup_full");
        goodWrecks.add("rightsideup_backhalf");
        goodWrecks.add("with_mast_degraded");
        goodWrecks.add("upsidedown_full_degraded");
        goodWrecks.add("upsidedown_backhalf_degraded");
        goodWrecks.add("sideways_full_degraded");
        goodWrecks.add("sideways_backhalf_degraded");
        goodWrecks.add("rightsideup_full_degraded");
        goodWrecks.add("rightsideup_backhalf_degraded");
        return Collections.unmodifiableList(goodWrecks);
    }

    private static List<Biome> getGoodVillageBiomes() {
        List<Biome> goodVillageBiomes = new ArrayList<>();
        goodVillageBiomes.add(Biomes.PLAINS);
        goodVillageBiomes.add(Biomes.SAVANNA);
        goodVillageBiomes.add(Biomes.SNOWY_TUNDRA);
        goodVillageBiomes.add(Biomes.TAIGA);
        return Collections.unmodifiableList(goodVillageBiomes);
    }

    public boolean testVillageS() {

        // Find a close enough village to 0,0
        CPos pos = VILLAGE.getInRegion(seed, 0, 0, chunkRand);
        if (pos.distanceTo(CPos.ZERO, DistanceMetric.EUCLIDEAN_SQ) < 87.890625) {
            villagePos = pos;
            return true;
        }
        return false;
    }

    public boolean testMonumentS() {
        CPos cPos = MONUMENT.getInRegion(seed, 0, 0, chunkRand);
        double d = villagePos.distanceTo(cPos, DistanceMetric.EUCLIDEAN_SQ);
        if (d < 351.5625 && d > 100
                && villagePos.distanceTo(CPos.ZERO, DistanceMetric.EUCLIDEAN_SQ)
                < cPos.distanceTo(CPos.ZERO, DistanceMetric.EUCLIDEAN_SQ)
                && Math.abs(villagePos.getX() - cPos.getX()) > 10
                && Math.abs(villagePos.getZ() - cPos.getZ()) > 10) {
            monumentPos = cPos;
            return true;
        }
        return false;
    }

    public boolean testFortressS() {
        CPos fPos = FORTRESS.getInRegion(seed, 0, 0, chunkRand);
        if (fPos != null && fPos.getX() <= 5 && fPos.getZ() <= 5) {
            fortressPos = fPos;
            return true;
        }
        return false;
    }

    public boolean testStrongholdS() {
        Random random = new Random();
        random.setSeed(seed);
        double sh1Angle = random.nextDouble() * Math.PI * 2.0D;

        if (AngleMathHelper.getAngleDifference(BASE_ANGLE, sh1Angle) < MAX_ANGLE_DIFF) {
            double distance = STRONGHOLD.getDistance();
            double distanceRing = (4.0D * distance) + (random.nextDouble() - 0.5D) * distance * 2.5D;
            if (distanceRing * 16 < 1600) {
                strongholdNum = 1;
                return true;
            }
        }
        return false;
    }

    public boolean testMainShipwreckS() {
        CPos middlePos = new CPos((monumentPos.getX() + villagePos.getX()) / 2, (monumentPos.getZ() + villagePos.getZ()) / 2);
        CPos pos = SHIPWRECK.getInRegion(seed, 0, 0, chunkRand);
        if (middlePos.distanceTo(pos, DistanceMetric.EUCLIDEAN_SQ) < 9.765625) {
            mainShipwreckPos = pos;
            return true;
        }
        return false;
    }

    public boolean testMainShipwreckGen(int minimumEmeralds, int minimumIron) {
        ShipwreckGenerator shipwreckGenerator = new ShipwreckGenerator(MCVERSION);
        shipwreckGenerator.generate(seed, Dimension.OVERWORLD, mainShipwreckPos.getX(), mainShipwreckPos.getZ());
        if (GOOD_WRECKS.contains(shipwreckGenerator.getType())) {
            int iron = 0;
            int emeralds = 0;
            for (ChestContent chestContent : SHIPWRECK.getLoot(seed, shipwreckGenerator, chunkRand, false)) {
                for (ItemStack itemStack : chestContent.getItems()) {
                    if (itemStack.getItem().equals(Items.IRON_INGOT)) {
                        iron += itemStack.getCount() * 9;
                    } else if (itemStack.getItem().equals(Items.IRON_NUGGET)) {
                        iron += itemStack.getCount();
                    } else if (itemStack.getItem().equals(Items.EMERALD)) {
                        emeralds += itemStack.getCount();
                    }
                }
            }
            return emeralds >= minimumEmeralds && iron >= minimumIron;
        }
        return false;
    }

    public boolean testMainStructureBiomes() {
        overworldBiomeSource = new OverworldBiomeSource(MCVERSION, seed);
        return GOOD_VILLAGE_BIOMES.contains(overworldBiomeSource.getBiome(villagePos.toBlockPos().add(9, 0, 9)))
                && MONUMENT.canSpawn(monumentPos, overworldBiomeSource)
                && SHIPWRECK.canSpawn(mainShipwreckPos, overworldBiomeSource);
    }

    public boolean testVillageOceanAccess() {
        BPos middle = villagePos.toBlockPos().add(-20, 0, -20);
        for (int z = 0; z <= 1; z++) {
            for (int x = 0; x <= 1; x++) {
                if (overworldBiomeSource.getBiome(middle.add(x * 40, 0, z * 40)).getCategory().equals(Biome.Category.OCEAN)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean testStrongholdBiome() {
        strongholdPos = STRONGHOLD.getStarts(overworldBiomeSource, strongholdNum, chunkRand)[strongholdNum - 1];
        if (strongholdPos.distanceTo(CPos.ZERO, DistanceMetric.EUCLIDEAN_SQ) < 10000) {
            for (int z = 1; z >= -1; z -= 2) {
                for (int x = 1; x >= -1; x -= 2) {
                    if (overworldBiomeSource.getBiome(strongholdPos.toBlockPos().add(x * 20, 0, z * 20)).getCategory().equals(Biome.Category.OCEAN)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean testOceanPercent() {
        int startX = monumentPos.toBlockPos().getX();
        int startZ = monumentPos.toBlockPos().getZ();
        int endX = strongholdPos.toBlockPos().getX();
        int endZ = strongholdPos.toBlockPos().getZ();

        double diffX = (endX - startX) / 11.0;
        double diffZ = (endZ - startZ) / 11.0;


        int oceans = 0;
        for (int diff = 1; diff <= 10; diff++) {
            if (overworldBiomeSource.getBiome((int) (startX + diffX * diff), 0, (int) (startZ + diffZ * diff)).getCategory().equals(Biome.Category.OCEAN))
                oceans++;
        }
        return oceans >= 7;
    }

    public boolean testApproxSpawnPoint() {
        return villagePos.toBlockPos(64).distanceTo(SPAWN_POINT.getApproximateSpawnPoint(overworldBiomeSource), DistanceMetric.EUCLIDEAN_SQ) < 2500;
    }

    public boolean testExactSpawnPoint() {

        spawnPos = SPAWN_POINT.getSpawnPoint(new OverworldTerrainGenerator(overworldBiomeSource));
        return villagePos.toBlockPos(spawnPos.getY()).distanceTo(spawnPos, DistanceMetric.EUCLIDEAN_SQ) < 2500;
    }

    public boolean testAndLocateStructures() {
        return testVillageS()
                && testMonumentS()
                && testFortressS()
                && testStrongholdS()
                && testMainShipwreckS()
                && testMainShipwreckGen(minimumEmeralds,72);
    }

    public boolean testAndLocateStructures(long seed) {
        setSeed(seed);
        return testAndLocateStructures();
    }

    public boolean testBiomes() {
        return testMainStructureBiomes()
                && testVillageOceanAccess()
                && testApproxSpawnPoint()
                && testStrongholdBiome()
                && testOceanPercent()
                && testExactSpawnPoint();
    }

    public boolean testBiomes(long seed) {
        setSeed(seed);
        return testBiomes();
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
        this.chunkRand = new ChunkRand();
    }
}
