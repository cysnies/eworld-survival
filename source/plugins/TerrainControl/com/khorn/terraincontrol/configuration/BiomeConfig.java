package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.UseBiome;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.generator.resourcegens.AboveWaterGen;
import com.khorn.terraincontrol.generator.resourcegens.CactusGen;
import com.khorn.terraincontrol.generator.resourcegens.CustomObjectGen;
import com.khorn.terraincontrol.generator.resourcegens.CustomStructureGen;
import com.khorn.terraincontrol.generator.resourcegens.DungeonGen;
import com.khorn.terraincontrol.generator.resourcegens.GrassGen;
import com.khorn.terraincontrol.generator.resourcegens.LiquidGen;
import com.khorn.terraincontrol.generator.resourcegens.OreGen;
import com.khorn.terraincontrol.generator.resourcegens.PlantGen;
import com.khorn.terraincontrol.generator.resourcegens.ReedGen;
import com.khorn.terraincontrol.generator.resourcegens.Resource;
import com.khorn.terraincontrol.generator.resourcegens.SaplingGen;
import com.khorn.terraincontrol.generator.resourcegens.SaplingType;
import com.khorn.terraincontrol.generator.resourcegens.SmallLakeGen;
import com.khorn.terraincontrol.generator.resourcegens.TreeGen;
import com.khorn.terraincontrol.generator.resourcegens.TreeType;
import com.khorn.terraincontrol.generator.resourcegens.UnderWaterOreGen;
import com.khorn.terraincontrol.generator.resourcegens.UndergroundLakeGen;
import com.khorn.terraincontrol.generator.resourcegens.VinesGen;
import com.khorn.terraincontrol.generator.resourcegens.WellGen;
import com.khorn.terraincontrol.util.StringHelper;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BiomeConfig extends ConfigFile {
   public short[][] replaceMatrixBlocks;
   public int ReplaceCount;
   public int BiomeSize;
   public int BiomeRarity;
   public String BiomeColor;
   public ArrayList BiomeIsBorder;
   public ArrayList IsleInBiome;
   public ArrayList NotBorderNear;
   public float BiomeHeight;
   public float BiomeVolatility;
   public float BiomeTemperature;
   public float BiomeWetness;
   public String RiverBiome;
   public byte SurfaceBlock;
   public byte GroundBlock;
   public String ReplaceBiomeName;
   public boolean UseWorldWaterLevel;
   public int waterLevelMax;
   public int waterLevelMin;
   public int waterBlock;
   public int iceBlock;
   public int SkyColor;
   public int WaterColor;
   public int GrassColor;
   public boolean GrassColorIsMultiplier;
   public int FoliageColor;
   public boolean FoliageColorIsMultiplier;
   public Resource[] ResourceSequence;
   private SaplingGen[] saplingTypes;
   private SaplingGen saplingResource;
   public ArrayList biomeObjects;
   public CustomStructureGen structureGen;
   public ArrayList biomeObjectStrings;
   public double maxAverageHeight;
   public double maxAverageDepth;
   public double volatility1;
   public double volatility2;
   public double volatilityWeight1;
   public double volatilityWeight2;
   private double volatilityRaw1;
   private double volatilityRaw2;
   private double volatilityWeightRaw1;
   private double volatilityWeightRaw2;
   public boolean disableNotchHeightControl;
   public double[] heightMatrix;
   public boolean strongholdsEnabled;
   public boolean netherFortressesEnabled;
   public VillageType villageType;
   public double mineshaftsRarity;
   public RareBuildingType rareBuildingType;
   public int ResourceCount;
   public LocalBiome Biome;
   public WorldConfig worldConfig;
   public String name;
   public boolean spawnMonstersAddDefaults;
   public List spawnMonsters;
   public boolean spawnCreaturesAddDefaults;
   public List spawnCreatures;
   public boolean spawnWaterCreaturesAddDefaults;
   public List spawnWaterCreatures;
   public boolean spawnAmbientCreaturesAddDefaults;
   public List spawnAmbientCreatures;
   protected boolean defaultWaterLakes;
   protected int defaultTrees;
   protected int defaultFlowers;
   protected int defaultGrass;
   protected int defaultDeadBrush;
   protected int defaultMushroom;
   protected int defaultReed;
   protected int defaultCactus;
   protected int defaultClay;
   protected Object[] defaultWell;
   protected float defaultBiomeSurface;
   protected float defaultBiomeVolatility;
   protected byte defaultSurfaceBlock;
   protected byte defaultGroundBlock;
   protected float defaultBiomeTemperature;
   protected float defaultBiomeWetness;
   protected ArrayList defaultIsle;
   protected ArrayList defaultBorder;
   protected ArrayList defaultNotBorderNear;
   protected String defaultRiverBiome;
   protected int defaultSize;
   protected int defaultRarity;
   protected String defaultColor;
   protected int defaultWaterLily;
   protected String defaultWaterColorMultiplier;
   protected String defaultGrassColor;
   protected String defaultFoliageColor;
   protected boolean defaultStrongholds;
   protected VillageType defaultVillageType;
   protected RareBuildingType defaultRareBuildingType;

   public BiomeConfig(File settingsDir, LocalBiome biome, WorldConfig config) {
      super();
      this.replaceMatrixBlocks = new short[TerrainControl.supportedBlockIds][];
      this.ReplaceCount = 0;
      this.ResourceSequence = new Resource[256];
      this.saplingTypes = new SaplingGen[20];
      this.saplingResource = null;
      this.ResourceCount = 0;
      this.spawnMonstersAddDefaults = true;
      this.spawnMonsters = new ArrayList();
      this.spawnCreaturesAddDefaults = true;
      this.spawnCreatures = new ArrayList();
      this.spawnWaterCreaturesAddDefaults = true;
      this.spawnWaterCreatures = new ArrayList();
      this.spawnAmbientCreaturesAddDefaults = true;
      this.spawnAmbientCreatures = new ArrayList();
      this.defaultWaterLakes = true;
      this.defaultTrees = 1;
      this.defaultFlowers = 2;
      this.defaultGrass = 10;
      this.defaultDeadBrush = 0;
      this.defaultMushroom = 0;
      this.defaultReed = 0;
      this.defaultCactus = 0;
      this.defaultClay = 1;
      this.defaultBiomeSurface = 0.1F;
      this.defaultBiomeVolatility = 0.3F;
      this.defaultSurfaceBlock = (byte)DefaultMaterial.GRASS.id;
      this.defaultGroundBlock = (byte)DefaultMaterial.DIRT.id;
      this.defaultBiomeTemperature = 0.5F;
      this.defaultBiomeWetness = 0.5F;
      this.defaultIsle = new ArrayList();
      this.defaultBorder = new ArrayList();
      this.defaultNotBorderNear = new ArrayList();
      this.defaultRiverBiome = DefaultBiome.RIVER.Name;
      this.defaultSize = 4;
      this.defaultRarity = 100;
      this.defaultColor = "0x000000";
      this.defaultWaterLily = 0;
      this.defaultWaterColorMultiplier = "0xFFFFFF";
      this.defaultGrassColor = "0xFFFFFF";
      this.defaultFoliageColor = "0xFFFFFF";
      this.defaultStrongholds = true;
      this.defaultVillageType = BiomeConfig.VillageType.disabled;
      this.defaultRareBuildingType = BiomeConfig.RareBuildingType.disabled;
      this.Biome = biome;
      this.name = biome.getName();
      this.worldConfig = config;
      this.initDefaults();
      File settingsFile = new File(settingsDir, this.name + TCDefaultValues.WorldBiomeConfigName.stringValue());
      this.readSettingsFile(settingsFile);
      this.renameOldSettings();
      this.readConfigSettings();
      this.correctSettings();
      if (!settingsFile.exists()) {
         this.createDefaultResources();
      }

      if (config.SettingsMode != WorldConfig.ConfigMode.WriteDisable) {
         this.writeSettingsFile(settingsFile, config.SettingsMode == WorldConfig.ConfigMode.WriteAll);
      }

      if (this.UseWorldWaterLevel) {
         this.waterLevelMax = this.worldConfig.waterLevelMax;
         this.waterLevelMin = this.worldConfig.waterLevelMin;
         this.waterBlock = this.worldConfig.waterBlock;
         this.iceBlock = this.worldConfig.iceBlock;
      }

      if (biome.isCustom()) {
         biome.setEffects(this);
      }

   }

   public int getTemperature() {
      return (int)(this.BiomeTemperature * 65536.0F);
   }

   public int getWetness() {
      return (int)(this.BiomeWetness * 65536.0F);
   }

   public SaplingGen getSaplingGen(SaplingType type) {
      SaplingGen gen = this.saplingTypes[type.getSaplingId()];
      if (gen == null && type.growsTree()) {
         gen = this.saplingResource;
      }

      return gen;
   }

   private void createDefaultResources() {
      if (this.defaultWaterLakes) {
         Resource resource = Resource.createResource(this, SmallLakeGen.class, DefaultMaterial.WATER.id, TCDefaultValues.SmallLakeWaterFrequency.intValue(), TCDefaultValues.SmallLakeWaterRarity.intValue(), TCDefaultValues.SmallLakeMinAltitude.intValue(), TCDefaultValues.SmallLakeMaxAltitude.intValue());
         this.ResourceSequence[this.ResourceCount++] = resource;
      }

      Resource resource = Resource.createResource(this, SmallLakeGen.class, DefaultMaterial.LAVA.id, TCDefaultValues.SmallLakeLavaFrequency.intValue(), TCDefaultValues.SmallLakeLavaRarity.intValue(), TCDefaultValues.SmallLakeMinAltitude.intValue(), TCDefaultValues.SmallLakeMaxAltitude.intValue());
      this.ResourceSequence[this.ResourceCount++] = resource;
      resource = Resource.createResource(this, UndergroundLakeGen.class, TCDefaultValues.undergroundLakeMinSize.intValue(), TCDefaultValues.undergroundLakeMaxSize.intValue(), TCDefaultValues.undergroundLakeFrequency.intValue(), TCDefaultValues.undergroundLakeRarity.intValue(), TCDefaultValues.undergroundLakeMinAltitude.intValue(), TCDefaultValues.undergroundLakeMaxAltitude.intValue());
      this.ResourceSequence[this.ResourceCount++] = resource;
      resource = Resource.createResource(this, DungeonGen.class, TCDefaultValues.dungeonFrequency.intValue(), TCDefaultValues.dungeonRarity.intValue(), TCDefaultValues.dungeonMinAltitude.intValue(), this.worldConfig.WorldHeight);
      this.ResourceSequence[this.ResourceCount++] = resource;
      resource = Resource.createResource(this, OreGen.class, DefaultMaterial.DIRT.id, TCDefaultValues.dirtDepositSize.intValue(), TCDefaultValues.dirtDepositFrequency.intValue(), TCDefaultValues.dirtDepositRarity.intValue(), TCDefaultValues.dirtDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.STONE.id);
      this.ResourceSequence[this.ResourceCount++] = resource;
      resource = Resource.createResource(this, OreGen.class, DefaultMaterial.GRAVEL.id, TCDefaultValues.gravelDepositSize.intValue(), TCDefaultValues.gravelDepositFrequency.intValue(), TCDefaultValues.gravelDepositRarity.intValue(), TCDefaultValues.gravelDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.STONE.id);
      this.ResourceSequence[this.ResourceCount++] = resource;
      resource = Resource.createResource(this, OreGen.class, DefaultMaterial.COAL_ORE.id, TCDefaultValues.coalDepositSize.intValue(), TCDefaultValues.coalDepositFrequency.intValue(), TCDefaultValues.coalDepositRarity.intValue(), TCDefaultValues.coalDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.STONE.id);
      this.ResourceSequence[this.ResourceCount++] = resource;
      resource = Resource.createResource(this, OreGen.class, DefaultMaterial.IRON_ORE.id, TCDefaultValues.ironDepositSize.intValue(), TCDefaultValues.ironDepositFrequency.intValue(), TCDefaultValues.ironDepositRarity.intValue(), TCDefaultValues.ironDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 2, DefaultMaterial.STONE.id);
      this.ResourceSequence[this.ResourceCount++] = resource;
      resource = Resource.createResource(this, OreGen.class, DefaultMaterial.GOLD_ORE.id, TCDefaultValues.goldDepositSize.intValue(), TCDefaultValues.goldDepositFrequency.intValue(), TCDefaultValues.goldDepositRarity.intValue(), TCDefaultValues.goldDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 4, DefaultMaterial.STONE.id);
      this.ResourceSequence[this.ResourceCount++] = resource;
      resource = Resource.createResource(this, OreGen.class, DefaultMaterial.REDSTONE_ORE.id, TCDefaultValues.redstoneDepositSize.intValue(), TCDefaultValues.redstoneDepositFrequency.intValue(), TCDefaultValues.redstoneDepositRarity.intValue(), TCDefaultValues.redstoneDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 8, DefaultMaterial.STONE.id);
      this.ResourceSequence[this.ResourceCount++] = resource;
      resource = Resource.createResource(this, OreGen.class, DefaultMaterial.DIAMOND_ORE.id, TCDefaultValues.diamondDepositSize.intValue(), TCDefaultValues.diamondDepositFrequency.intValue(), TCDefaultValues.diamondDepositRarity.intValue(), TCDefaultValues.diamondDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 8, DefaultMaterial.STONE.id);
      this.ResourceSequence[this.ResourceCount++] = resource;
      resource = Resource.createResource(this, OreGen.class, DefaultMaterial.LAPIS_ORE.id, TCDefaultValues.lapislazuliDepositSize.intValue(), TCDefaultValues.lapislazuliDepositFrequency.intValue(), TCDefaultValues.lapislazuliDepositRarity.intValue(), TCDefaultValues.lapislazuliDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 8, DefaultMaterial.STONE.id);
      this.ResourceSequence[this.ResourceCount++] = resource;
      DefaultBiome biome = DefaultBiome.getBiome(this.Biome.getId());
      if (biome != null && (biome == DefaultBiome.EXTREME_HILLS || biome == DefaultBiome.SMALL_MOUNTAINS)) {
         resource = Resource.createResource(this, OreGen.class, DefaultMaterial.EMERALD_ORE.id, TCDefaultValues.emeraldDepositSize.intValue(), TCDefaultValues.emeraldDepositFrequency.intValue(), TCDefaultValues.emeraldDepositRarity.intValue(), TCDefaultValues.emeraldDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 4, DefaultMaterial.STONE.id);
         this.ResourceSequence[this.ResourceCount++] = resource;
      }

      resource = Resource.createResource(this, UnderWaterOreGen.class, DefaultMaterial.SAND.id, TCDefaultValues.waterSandDepositSize.intValue(), TCDefaultValues.waterSandDepositFrequency.intValue(), TCDefaultValues.waterSandDepositRarity.intValue(), DefaultMaterial.DIRT.id, DefaultMaterial.GRASS.id);
      this.ResourceSequence[this.ResourceCount++] = resource;
      if (this.defaultClay > 0) {
         resource = Resource.createResource(this, UnderWaterOreGen.class, DefaultMaterial.CLAY.id, TCDefaultValues.waterClayDepositSize.intValue(), this.defaultClay, TCDefaultValues.waterClayDepositRarity.intValue(), DefaultMaterial.DIRT.id, DefaultMaterial.CLAY.id);
         this.ResourceSequence[this.ResourceCount++] = resource;
      }

      resource = Resource.createResource(this, CustomObjectGen.class, "UseWorld");
      this.ResourceSequence[this.ResourceCount++] = resource;
      if (biome != null) {
         switch (biome) {
            case OCEAN:
            case EXTREME_HILLS:
            case RIVER:
            case SMALL_MOUNTAINS:
               resource = Resource.createResource(this, TreeGen.class, this.defaultTrees, TreeType.BigTree, 1, TreeType.Tree, 9);
               this.ResourceSequence[this.ResourceCount++] = resource;
            case PLAINS:
            case DESERT:
            case DESERT_HILLS:
            default:
               break;
            case FOREST_HILLS:
            case FOREST:
               resource = Resource.createResource(this, TreeGen.class, this.defaultTrees, TreeType.Forest, 20, TreeType.BigTree, 10, TreeType.Tree, 100);
               this.ResourceSequence[this.ResourceCount++] = resource;
               break;
            case TAIGA_HILLS:
            case TAIGA:
               resource = Resource.createResource(this, TreeGen.class, this.defaultTrees, TreeType.Taiga1, 35, TreeType.Taiga2, 100);
               this.ResourceSequence[this.ResourceCount++] = resource;
               break;
            case SWAMPLAND:
               resource = Resource.createResource(this, TreeGen.class, this.defaultTrees, TreeType.SwampTree, 100);
               this.ResourceSequence[this.ResourceCount++] = resource;
               break;
            case MUSHROOM_ISLAND:
               resource = Resource.createResource(this, TreeGen.class, this.defaultTrees, TreeType.HugeMushroom, 100);
               this.ResourceSequence[this.ResourceCount++] = resource;
               break;
            case JUNGLE:
            case JUNGLE_HILLS:
               resource = Resource.createResource(this, TreeGen.class, this.defaultTrees, TreeType.BigTree, 10, TreeType.GroundBush, 50, TreeType.JungleTree, 35, TreeType.CocoaTree, 100);
               this.ResourceSequence[this.ResourceCount++] = resource;
         }
      }

      if (this.defaultWaterLily > 0) {
         resource = Resource.createResource(this, AboveWaterGen.class, DefaultMaterial.WATER_LILY.id, this.defaultWaterLily, 100);
         this.ResourceSequence[this.ResourceCount++] = resource;
      }

      if (this.defaultFlowers > 0) {
         resource = Resource.createResource(this, PlantGen.class, DefaultMaterial.RED_ROSE.id, this.defaultFlowers, TCDefaultValues.roseDepositRarity.intValue(), TCDefaultValues.roseDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id, DefaultMaterial.SOIL.id);
         this.ResourceSequence[this.ResourceCount++] = resource;
         resource = Resource.createResource(this, PlantGen.class, DefaultMaterial.YELLOW_FLOWER.id, this.defaultFlowers, TCDefaultValues.flowerDepositRarity.intValue(), TCDefaultValues.flowerDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id, DefaultMaterial.SOIL.id);
         this.ResourceSequence[this.ResourceCount++] = resource;
      }

      if (this.defaultMushroom > 0) {
         resource = Resource.createResource(this, PlantGen.class, DefaultMaterial.RED_MUSHROOM.id, this.defaultMushroom, TCDefaultValues.redMushroomDepositRarity.intValue(), TCDefaultValues.redMushroomDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, this.defaultSurfaceBlock, DefaultMaterial.DIRT.id);
         this.ResourceSequence[this.ResourceCount++] = resource;
         resource = Resource.createResource(this, PlantGen.class, DefaultMaterial.BROWN_MUSHROOM.id, this.defaultMushroom, TCDefaultValues.brownMushroomDepositRarity.intValue(), TCDefaultValues.brownMushroomDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, this.defaultSurfaceBlock, DefaultMaterial.DIRT.id);
         this.ResourceSequence[this.ResourceCount++] = resource;
      }

      if (this.defaultGrass > 0) {
         resource = Resource.createResource(this, GrassGen.class, DefaultMaterial.LONG_GRASS.id, 1, this.defaultGrass, TCDefaultValues.longGrassDepositRarity.intValue(), DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id);
         this.ResourceSequence[this.ResourceCount++] = resource;
      }

      if (this.defaultDeadBrush > 0) {
         resource = Resource.createResource(this, GrassGen.class, DefaultMaterial.DEAD_BUSH.id, 0, this.defaultDeadBrush, TCDefaultValues.deadBushDepositRarity.intValue(), DefaultMaterial.SAND.id);
         this.ResourceSequence[this.ResourceCount++] = resource;
      }

      resource = Resource.createResource(this, PlantGen.class, DefaultMaterial.PUMPKIN.id, TCDefaultValues.pumpkinDepositFrequency.intValue(), TCDefaultValues.pumpkinDepositRarity.intValue(), TCDefaultValues.pumpkinDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.GRASS.id);
      this.ResourceSequence[this.ResourceCount++] = resource;
      if (this.defaultReed > 0) {
         resource = Resource.createResource(this, ReedGen.class, DefaultMaterial.SUGAR_CANE_BLOCK.id, this.defaultReed, TCDefaultValues.reedDepositRarity.intValue(), TCDefaultValues.reedDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id, DefaultMaterial.SAND.id);
         this.ResourceSequence[this.ResourceCount++] = resource;
      }

      if (this.defaultCactus > 0) {
         resource = Resource.createResource(this, CactusGen.class, DefaultMaterial.CACTUS.id, this.defaultCactus, TCDefaultValues.cactusDepositRarity.intValue(), TCDefaultValues.cactusDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.SAND.id);
         this.ResourceSequence[this.ResourceCount++] = resource;
      }

      if (biome == DefaultBiome.JUNGLE || biome == DefaultBiome.JUNGLE_HILLS) {
         resource = Resource.createResource(this, VinesGen.class, TCDefaultValues.vinesFrequency.intValue(), TCDefaultValues.vinesRarity.intValue(), TCDefaultValues.vinesMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.VINE.id);
         this.ResourceSequence[this.ResourceCount++] = resource;
      }

      resource = Resource.createResource(this, LiquidGen.class, DefaultMaterial.WATER.id, TCDefaultValues.waterSourceDepositFrequency.intValue(), TCDefaultValues.waterSourceDepositRarity.intValue(), TCDefaultValues.waterSourceDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.STONE.id);
      this.ResourceSequence[this.ResourceCount++] = resource;
      resource = Resource.createResource(this, LiquidGen.class, DefaultMaterial.LAVA.id, TCDefaultValues.lavaSourceDepositFrequency.intValue(), TCDefaultValues.lavaSourceDepositRarity.intValue(), TCDefaultValues.lavaSourceDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.STONE.id);
      this.ResourceSequence[this.ResourceCount++] = resource;
      if (this.defaultWell != null) {
         resource = Resource.createResource(this, WellGen.class, this.defaultWell);
         this.ResourceSequence[this.ResourceCount++] = resource;
      }

   }

   protected void readConfigSettings() {
      this.BiomeSize = this.readModSettings(TCDefaultValues.BiomeSize.name(), this.defaultSize);
      this.BiomeRarity = this.readModSettings(TCDefaultValues.BiomeRarity.name(), this.defaultRarity);
      this.BiomeColor = this.readModSettings(TCDefaultValues.BiomeColor.name(), this.defaultColor);
      this.RiverBiome = this.readModSettings(TCDefaultValues.RiverBiome.name(), this.defaultRiverBiome);
      this.IsleInBiome = this.readModSettings(TCDefaultValues.IsleInBiome.name(), this.defaultIsle);
      this.BiomeIsBorder = this.readModSettings(TCDefaultValues.BiomeIsBorder.name(), this.defaultBorder);
      this.NotBorderNear = this.readModSettings(TCDefaultValues.NotBorderNear.name(), this.defaultNotBorderNear);
      this.BiomeTemperature = this.readModSettings(TCDefaultValues.BiomeTemperature.name(), this.defaultBiomeTemperature);
      this.BiomeWetness = this.readModSettings(TCDefaultValues.BiomeWetness.name(), this.defaultBiomeWetness);
      this.ReplaceBiomeName = (String)this.readSettings(TCDefaultValues.ReplaceToBiomeName);
      this.BiomeHeight = this.readModSettings(TCDefaultValues.BiomeHeight.name(), this.defaultBiomeSurface);
      this.BiomeVolatility = this.readModSettings(TCDefaultValues.BiomeVolatility.name(), this.defaultBiomeVolatility);
      this.SurfaceBlock = this.readModSettings(TCDefaultValues.SurfaceBlock.name(), this.defaultSurfaceBlock);
      this.GroundBlock = this.readModSettings(TCDefaultValues.GroundBlock.name(), this.defaultGroundBlock);
      this.UseWorldWaterLevel = (Boolean)this.readSettings(TCDefaultValues.UseWorldWaterLevel);
      this.waterLevelMax = (Integer)this.readSettings(TCDefaultValues.WaterLevelMax);
      this.waterLevelMin = (Integer)this.readSettings(TCDefaultValues.WaterLevelMin);
      this.waterBlock = (Integer)this.readSettings(TCDefaultValues.WaterBlock);
      this.iceBlock = (Integer)this.readSettings(TCDefaultValues.IceBlock);
      this.SkyColor = (Integer)this.readSettings(TCDefaultValues.SkyColor);
      this.WaterColor = this.readModSettingsColor(TCDefaultValues.WaterColor.name(), this.defaultWaterColorMultiplier);
      this.GrassColor = this.readModSettingsColor(TCDefaultValues.GrassColor.name(), this.defaultGrassColor);
      this.GrassColorIsMultiplier = (Boolean)this.readSettings(TCDefaultValues.GrassColorIsMultiplier);
      this.FoliageColor = this.readModSettingsColor(TCDefaultValues.FoliageColor.name(), this.defaultFoliageColor);
      this.FoliageColorIsMultiplier = (Boolean)this.readSettings(TCDefaultValues.FoliageColorIsMultiplier);
      this.volatilityRaw1 = (Double)this.readSettings(TCDefaultValues.Volatility1);
      this.volatilityRaw2 = (Double)this.readSettings(TCDefaultValues.Volatility2);
      this.volatilityWeightRaw1 = (Double)this.readSettings(TCDefaultValues.VolatilityWeight1);
      this.volatilityWeightRaw2 = (Double)this.readSettings(TCDefaultValues.VolatilityWeight2);
      this.disableNotchHeightControl = (Boolean)this.readSettings(TCDefaultValues.DisableBiomeHeight);
      this.maxAverageHeight = (Double)this.readSettings(TCDefaultValues.MaxAverageHeight);
      this.maxAverageDepth = (Double)this.readSettings(TCDefaultValues.MaxAverageDepth);
      this.strongholdsEnabled = this.readModSettings(TCDefaultValues.StrongholdsEnabled.name(), this.defaultStrongholds);
      this.netherFortressesEnabled = this.readModSettings(TCDefaultValues.NetherFortressesEnabled.name(), true);
      this.villageType = (VillageType)this.readModSettings(TCDefaultValues.VillageType.name(), this.defaultVillageType);
      this.mineshaftsRarity = (Double)this.readSettings(TCDefaultValues.MineshaftRarity);
      this.rareBuildingType = (RareBuildingType)this.readModSettings(TCDefaultValues.RareBuildingType.name(), this.defaultRareBuildingType);
      if (DefaultBiome.getBiome(this.Biome.getId()) == null) {
         this.spawnMonstersAddDefaults = this.readModSettings("spawnMonstersAddDefaults", true);
         this.spawnMonsters = this.readModSettings("spawnMonsters", new ArrayList());
         this.spawnCreaturesAddDefaults = this.readModSettings("spawnCreaturesAddDefaults", true);
         this.spawnCreatures = this.readModSettings("spawnCreatures", new ArrayList());
         this.spawnWaterCreaturesAddDefaults = this.readModSettings("spawnWaterCreaturesAddDefaults", true);
         this.spawnWaterCreatures = this.readModSettings("spawnWaterCreatures", new ArrayList());
         this.spawnAmbientCreaturesAddDefaults = this.readModSettings("spawnAmbientCreaturesAddDefaults", true);
         this.spawnAmbientCreatures = this.readModSettings("spawnAmbientCreatures", new ArrayList());
      }

      this.ReadCustomObjectSettings();
      this.ReadReplaceSettings();
      this.ReadResourceSettings();
      this.ReadHeightSettings();
   }

   private void ReadHeightSettings() {
      this.heightMatrix = new double[this.worldConfig.WorldHeight / 8 + 1];
      ArrayList<String> keys = (ArrayList)this.readSettings(TCDefaultValues.CustomHeightControl);

      try {
         if (keys.size() != this.worldConfig.WorldHeight / 8 + 1) {
            return;
         }

         for(int i = 0; i < this.worldConfig.WorldHeight / 8 + 1; ++i) {
            this.heightMatrix[i] = Double.valueOf((String)keys.get(i));
         }
      } catch (NumberFormatException var3) {
         System.out.println("Wrong height settings: '" + (String)this.settingsCache.get(TCDefaultValues.CustomHeightControl.name().toLowerCase()) + "'");
      }

   }

   private void ReadReplaceSettings() {
      String settingValue = this.readModSettings("ReplacedBlocks", "None");
      if (!settingValue.equals("") && !settingValue.equals("None")) {
         String[] keys = readComplexString(settingValue);

         try {
            for(String key : keys) {
               int start = key.indexOf("(");
               int end = key.lastIndexOf(")");
               if (start != -1 && end != -1) {
                  key = key.substring(start + 1, end);
                  String[] values = key.split(",");
                  if (values.length == 5) {
                     values = new String[]{values[0], values[1] + ":" + values[2], values[3], "" + (Integer.parseInt(values[4]) - 1)};
                  }

                  if (values.length == 2 || values.length == 4) {
                     short fromBlockId = (short)StringHelper.readBlockId(values[0]);
                     short toBlockId = (short)StringHelper.readBlockId(values[1]);
                     short blockData = (short)StringHelper.readBlockData(values[1]);
                     int minY = 0;
                     int maxY = this.worldConfig.WorldHeight - 1;
                     if (values.length == 4) {
                        int var19 = Integer.valueOf(values[2]);
                        maxY = Integer.valueOf(values[3]);
                        minY = this.applyBounds(var19, 0, this.worldConfig.WorldHeight - 1);
                        maxY = this.applyBounds(maxY, minY, this.worldConfig.WorldHeight - 1);
                     }

                     if (this.replaceMatrixBlocks[fromBlockId] == null) {
                        this.replaceMatrixBlocks[fromBlockId] = new short[this.worldConfig.WorldHeight];

                        for(int i = 0; i < this.worldConfig.WorldHeight; ++i) {
                           this.replaceMatrixBlocks[fromBlockId][i] = -1;
                        }
                     }

                     for(int y = minY; y <= maxY; ++y) {
                        this.replaceMatrixBlocks[fromBlockId][y] = (short)(toBlockId << 4 | blockData);
                     }

                     ++this.ReplaceCount;
                  }
               }
            }
         } catch (NumberFormatException var16) {
            TerrainControl.log("Wrong replace settings: '" + (String)this.settingsCache.get(settingValue) + "'");
         } catch (InvalidConfigException var17) {
            TerrainControl.log("Wrong replace settings: '" + (String)this.settingsCache.get(settingValue) + "'");
         }

      }
   }

   private void ReadResourceSettings() {
      ArrayList<Integer> LineNumbers = new ArrayList();

      for(Map.Entry entry : this.settingsCache.entrySet()) {
         String key = (String)entry.getKey();
         int start = key.indexOf("(");
         int end = key.lastIndexOf(")");
         if (start != -1 && end != -1) {
            String name = key.substring(0, start);
            String[] props = readComplexString(key.substring(start + 1, end));
            ConfigFunction<BiomeConfig> res = TerrainControl.getConfigFunctionsManager().getConfigFunction(name, this, this.name + " on line " + (String)entry.getValue(), Arrays.asList(props));
            if (res != null) {
               if (res instanceof SaplingGen && res.isValid()) {
                  SaplingGen sapling = (SaplingGen)res;
                  if (sapling.saplingType == SaplingType.All) {
                     this.saplingResource = sapling;
                  } else {
                     this.saplingTypes[sapling.saplingType.getSaplingId()] = sapling;
                  }
               } else if (res instanceof Resource) {
                  LineNumbers.add(Integer.valueOf((String)entry.getValue()));
                  this.ResourceSequence[this.ResourceCount++] = (Resource)res;
               }
            }
         }
      }

      for(int i = 0; i < this.ResourceCount; ++i) {
         Resource buffer = this.ResourceSequence[i];
         int intBuffer = (Integer)LineNumbers.get(i);
         int minimal = i;

         for(int t = i; t < this.ResourceCount; ++t) {
            if ((Integer)LineNumbers.get(t) < intBuffer) {
               intBuffer = (Integer)LineNumbers.get(t);
               minimal = t;
            }
         }

         this.ResourceSequence[i] = this.ResourceSequence[minimal];
         this.ResourceSequence[minimal] = buffer;
         LineNumbers.set(minimal, LineNumbers.get(i));
      }

   }

   private void ReadCustomObjectSettings() {
      this.biomeObjects = new ArrayList();
      this.biomeObjectStrings = new ArrayList();
      String biomeObjectsValue = this.readModSettings("biomeobjects", "");
      if (biomeObjectsValue.length() > 0) {
         String[] customObjectStrings = biomeObjectsValue.split(",");

         for(String customObjectString : customObjectStrings) {
            CustomObject object = TerrainControl.getCustomObjectManager().getObjectFromString(customObjectString, this.worldConfig);
            if (object != null && !(object instanceof UseBiome)) {
               this.biomeObjects.add(object);
               this.biomeObjectStrings.add(customObjectString);
            }
         }
      }

   }

   protected void writeConfigSettings() throws IOException {
      if (DefaultBiome.getBiome(this.Biome.getId()) != null) {
         this.writeComment("This is the biome config file of the " + this.name + " biome, which is one of the vanilla biomes.");
      } else {
         this.writeComment("This is the biome config file of the " + this.name + " biome, which is a custom biome.");
      }

      this.writeNewLine();
      this.writeBigTitle("Biome placement");
      this.writeComment("Biome size from 0 to GenerationDepth. Defines in which biome layer this biome will be generated (see GenerationDepth).");
      this.writeComment("Higher numbers give a smaller biome, lower numbers a larger biome.");
      this.writeComment("Oceans and rivers are generated using a dirrerent algorithm in the default settings,");
      this.writeComment("(they aren't in one of the biome lists), so this setting won't affect them.");
      this.writeValue(TCDefaultValues.BiomeSize.name(), this.BiomeSize);
      this.writeNewLine();
      this.writeComment("Biome rarity from 100 to 1. If this is normal or ice biome - chance for spawn this biome then others.");
      this.writeComment("Example for normal biome :");
      this.writeComment("  100 rarity mean 1/6 chance than other ( with 6 default normal biomes).");
      this.writeComment("  50 rarity mean 1/11 chance than other");
      this.writeComment("For isle biome this is chance to spawn isle in good place.");
      this.writeComment("Don`t work on Ocean and River (frozen versions too) biomes until not added as normal biome.");
      this.writeValue(TCDefaultValues.BiomeRarity.name(), this.BiomeRarity);
      this.writeNewLine();
      this.writeComment("The hexadecimal color value of this biome. Used in the output of the /tc map command,");
      this.writeComment("and used in the input of BiomeMode:FromImage.");
      this.writeValue(TCDefaultValues.BiomeColor.name(), this.BiomeColor);
      this.writeNewLine();
      this.writeComment("Biome name used as river in this biome. Leave empty to disable rivers.");
      this.writeValue(TCDefaultValues.RiverBiome.name(), this.RiverBiome);
      this.writeNewLine();
      this.writeComment("Replace this biome to specified after all generations. Warning this will cause saplings and mob spawning work as in specified biome");
      this.writeValue(TCDefaultValues.ReplaceToBiomeName.name(), this.ReplaceBiomeName);
      this.writeNewLine();
      this.writeSmallTitle("Isle biomes only");
      this.writeComment("Biome name list where this biome will be spawned as isle. Like Mushroom isle in Ocean.  This work only if this biome is in IsleBiomes in world config");
      this.writeValue(TCDefaultValues.IsleInBiome.name(), this.IsleInBiome);
      this.writeNewLine();
      this.writeSmallTitle("Border biomes only");
      this.writeComment("Biome name list where this biome will be border.Like Mushroom isle shore. Use is compared as IsleInBiome");
      this.writeValue(TCDefaultValues.BiomeIsBorder.name(), this.BiomeIsBorder);
      this.writeNewLine();
      this.writeComment("Biome name list near border is not applied. ");
      this.writeValue(TCDefaultValues.NotBorderNear.name(), this.NotBorderNear);
      this.writeNewLine();
      this.writeBigTitle("Biome height and volatility");
      this.writeComment("BiomeHeight mean how much height will be added in terrain generation");
      this.writeComment("It is double value from -10.0 to 10.0");
      this.writeComment("Value 0.0 equivalent half of map height with all other default settings");
      this.writeValue(TCDefaultValues.BiomeHeight.name(), this.BiomeHeight);
      this.writeNewLine();
      this.writeComment("Biome volatility.");
      this.writeValue(TCDefaultValues.BiomeVolatility.name(), this.BiomeVolatility);
      this.writeNewLine();
      this.writeComment("If this value is greater than 0, then it will affect how much, on average, the terrain will rise before leveling off when it begins to increase in elevation.");
      this.writeComment("If the value is less than 0, then it will cause the terrain to either increase to a lower height before leveling out or decrease in height if the value is a large enough negative.");
      this.writeValue(TCDefaultValues.MaxAverageHeight.name(), this.maxAverageHeight);
      this.writeNewLine();
      this.writeComment("If this value is greater than 0, then it will affect how much, on average, the terrain (usually at the ottom of the ocean) will fall before leveling off when it begins to decrease in elevation. ");
      this.writeComment("If the value is less than 0, then it will cause the terrain to either fall to a lesser depth before leveling out or increase in height if the value is a large enough negative.");
      this.writeValue(TCDefaultValues.MaxAverageDepth.name(), this.maxAverageDepth);
      this.writeNewLine();
      this.writeComment("Another type of noise. This noise is independent from biomes. The larger the values the more chaotic/volatile landscape generation becomes.");
      this.writeComment("Setting the values to negative will have the opposite effect and make landscape generation calmer/gentler.");
      this.writeValue(TCDefaultValues.Volatility1.name(), this.volatilityRaw1);
      this.writeValue(TCDefaultValues.Volatility2.name(), this.volatilityRaw2);
      this.writeNewLine();
      this.writeComment("Adjust the weight of the corresponding volatility settings. This allows you to change how prevalent you want either of the volatility settings to be in the terrain.");
      this.writeValue(TCDefaultValues.VolatilityWeight1.name(), this.volatilityWeightRaw1);
      this.writeValue(TCDefaultValues.VolatilityWeight2.name(), this.volatilityWeightRaw2);
      this.writeNewLine();
      this.writeComment("Disable all noises except Volatility1 and Volatility2. Also disable default block chance from height.");
      this.writeValue(TCDefaultValues.DisableBiomeHeight.name(), this.disableNotchHeightControl);
      this.writeNewLine();
      this.writeComment("List of custom height factor, 17 double entries, each entire control of about 7 blocks height from down. Positive entry - better chance of spawn blocks, negative - smaller");
      this.writeComment("Values which affect your configuration may be found only experimental. That may be very big, like ~3000.0 depends from height");
      this.writeComment("Example:");
      this.writeComment("  CustomHeightControl:0.0,-2500.0,0.0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0");
      this.writeComment("Make empty layer above bedrock layer. ");
      this.WriteHeightSettings();
      this.writeBigTitle("Blocks");
      this.writeNewLine();
      this.writeComment("Surface block id");
      this.writeValue(TCDefaultValues.SurfaceBlock.name(), this.SurfaceBlock);
      this.writeNewLine();
      this.writeComment("Block id from stone to surface, like dirt in plain biome ");
      this.writeValue(TCDefaultValues.GroundBlock.name(), this.GroundBlock);
      this.writeNewLine();
      this.writeComment("Replace Variable: (blockFrom,blockTo[:blockDataTo][,minHeight,maxHeight])");
      this.writeComment("Example :");
      this.writeComment("  ReplacedBlocks:(GRASS,DIRT,100,127),(GRAVEL,GLASS)");
      this.writeComment("Replace grass block to dirt from 100 to 127 height and replace gravel to glass on all height ");
      this.WriteModReplaceSettings();
      this.writeBigTitle("Water and ice");
      this.writeComment("Set this to false to use the water and ice settings of this biome.");
      this.writeValue(TCDefaultValues.UseWorldWaterLevel.name(), this.UseWorldWaterLevel);
      this.writeNewLine();
      this.writeComment("Set water level. Every empty between this levels will be fill water or another block from WaterBlock.");
      this.writeValue(TCDefaultValues.WaterLevelMax.name(), this.waterLevelMax);
      this.writeValue(TCDefaultValues.WaterLevelMin.name(), this.waterLevelMin);
      this.writeNewLine();
      this.writeComment("BlockId used as water in WaterLevelMax");
      this.writeValue(TCDefaultValues.WaterBlock.name(), this.waterBlock);
      this.writeNewLine();
      this.writeComment("BlockId used as ice. Ice only spawns if the BiomeTemperture is low enough.");
      this.writeValue(TCDefaultValues.IceBlock.name(), this.iceBlock);
      this.writeBigTitle("Visuals and weather");
      this.writeComment("Most of the settings here only have an effect on players with the client version of Terrain Control installed.");
      this.writeComment("Biome temperature. Float value from 0.0 to 1.0.");
      this.writeValue(TCDefaultValues.BiomeTemperature.name(), this.BiomeTemperature);
      this.writeNewLine();
      this.writeComment("Biome wetness. Float value from 0.0 to 1.0.");
      this.writeValue(TCDefaultValues.BiomeWetness.name(), this.BiomeWetness);
      this.writeNewLine();
      this.writeComment("Biome sky color.");
      this.writeColorValue(TCDefaultValues.SkyColor.name(), this.SkyColor);
      this.writeNewLine();
      this.writeComment("Biome water color multiplier.");
      this.writeColorValue(TCDefaultValues.WaterColor.name(), this.WaterColor);
      this.writeNewLine();
      this.writeComment("Biome grass color.");
      this.writeColorValue(TCDefaultValues.GrassColor.name(), this.GrassColor);
      this.writeNewLine();
      this.writeComment("Whether the grass color is a multiplier.");
      this.writeComment("If you set it to true, the color will be based on this value, the BiomeTemperature and the BiomeWetness.");
      this.writeComment("If you set it to false, the grass color will be just this color.");
      this.writeValue(TCDefaultValues.GrassColorIsMultiplier.name(), this.GrassColorIsMultiplier);
      this.writeNewLine();
      this.writeComment("Biome foliage color.");
      this.writeColorValue(TCDefaultValues.FoliageColor.name(), this.FoliageColor);
      this.writeNewLine();
      this.writeComment("Whether the foliage color is a multiplier. See GrassColorIsMultiplier for details.");
      this.writeValue(TCDefaultValues.FoliageColorIsMultiplier.name(), this.FoliageColorIsMultiplier);
      this.writeNewLine();
      this.writeBigTitle("Resource queue");
      this.writeComment("This section control all resources spawning after terrain generation.");
      this.writeComment("The resources will be placed in this order.");
      this.writeComment("");
      this.writeComment("Keep in mind that a high size, frequency or rarity might slow down terrain generation.");
      this.writeComment("");
      this.writeComment("Possible resources:");
      this.writeComment("SmallLake(Block[:Data],Frequency,Rarity,MinAltitude,MaxAltitude)");
      this.writeComment("Dungeon(Frequency,Rarity,MinAltitude,MaxAltitude)");
      this.writeComment("UnderGroundLake(MinSize,MaxSize,Frequency,Rarity,MinAltitude,MaxAltitude)");
      this.writeComment("Ore(Block[:Data],Size,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
      this.writeComment("UnderWaterOre(Block[:Data],Size,Frequency,Rarity,BlockSource[,BlockSource2,BlockSource3.....])");
      this.writeComment("CustomObject(Object[,AnotherObject[,...]])");
      this.writeComment("CustomStructure([Object,Object_Chance[,AnotherObject,Object_Chance[,...]]])");
      this.writeComment("Tree(Frequency,TreeType,TreeType_Chance[,Additional_TreeType,Additional_TreeType_Chance.....])");
      this.writeComment("Plant(Block[:Data],Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
      this.writeComment("Grass(Block,BlockData,Frequency,Rarity,BlockSource[,BlockSource2,BlockSource3.....])");
      this.writeComment("Reed(Block[:Data],Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
      this.writeComment("Cactus(Block[:Data],Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
      this.writeComment("Liquid(Block[:Data],Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
      this.writeComment("AboveWaterRes(Block[:Data],Frequency,Rarity)");
      this.writeComment("Vines(Frequency,Rarity,MinAltitude,MaxAltitude)");
      this.writeComment("Vein(Block[:Data],MinRadius,MaxRadius,Rarity,OreSize,OreFrequency,OreRarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,..])");
      this.writeComment("Well(BaseBlock[:Data],HalfSlabBlock[:Data],WaterBlock[:Data],Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,..])");
      this.writeComment("");
      this.writeComment("Block and BlockSource: can be id or name, Frequency - is count of attempts for place resource");
      this.writeComment("Rarity: chance for each attempt, Rarity:100 - mean 100% to pass, Rarity:1 - mean 1% to pass");
      this.writeComment("MinAltitude and MaxAltitude: height limits");
      this.writeComment("BlockSource: mean where or whereupon resource will be placed ");
      this.writeComment("TreeType: Tree - BigTree - Forest (a birch tree) - HugeMushroom (not a tree but still counts)");
      this.writeComment("   Taiga1 - Taiga2 - JungleTree (the huge jungle tree) - GroundBush - CocoaTree");
      this.writeComment("   You can also use your own custom objects, as long as they have set Tree to true in their settings.");
      this.writeComment("TreeType_Chance: similar Rarity. Example:");
      this.writeComment("  Tree(10,Taiga1,35,Taiga2,100) - plugin tries 10 times, for each attempt it tries to place Taiga1 (35% chance),");
      this.writeComment("  if that fails, it attempts to place Taiga2 (100% chance).");
      this.writeComment("Object: can be a any kind of custom object (bo2 or bo3) but without the file extension. You can");
      this.writeComment("also use UseWorld to spawn one of the object in the WorldObjects folder and UseBiome to spawn");
      this.writeComment("one of the objects in the BiomeObjects setting. When using BO2s for UseWorld, the BO2 must have");
      this.writeComment("this biome in their spawnInBiome setting.");
      this.writeComment("Object_Chance: Like TreeType_Chance.");
      this.writeComment("");
      this.writeComment("Plant and Grass resource: both a resource of one block. Plant can place blocks underground, Grass cannot.");
      this.writeComment("Liquid resource: a one-block water or lava source");
      this.writeComment("SmallLake and UnderGroundLake resources: small lakes of about 8x8 blocks");
      this.writeComment("Vein resource: not in vanilla. Starts an area where ores will spawn. Can be slow, so use a low Rarity (smaller than 1).");
      this.writeComment("CustomStructure resource: starts a BO3 structure in the chunk.");
      this.writeComment("");
      this.WriteResources();
      this.writeNewLine();
      this.writeBigTitle("Sapling resource");
      this.writeComment("Terrain Control allows you to grow your custom objects from saplings, instead");
      this.writeComment("of the vanilla trees. Add one or more Sapling functions here to override vanilla");
      this.writeComment("spawning for that sapling.");
      this.writeComment("");
      this.writeComment("The syntax is: Sapling(SaplingType,TreeType,TreeType_Chance[,Additional_TreeType,Additional_TreeType_Chance.....])");
      this.writeComment("Works like Tree resource instead first parameter.");
      this.writeComment("");
      this.writeComment("Sapling types: " + StringHelper.join((Object[])SaplingType.values(), ", "));
      this.writeComment("All - will make the tree spawn from all saplings, but not from mushrooms.");
      this.writeComment("BigJungle - for when 4 jungle saplings grow at once.");
      this.writeComment("RedMushroom/BrownMushroom - will only grow when bonemeal is used.");
      this.WriteSaplingSettings();
      this.writeBigTitle("Custom objects");
      this.writeComment("These objects will spawn when using the UseBiome keyword.");
      this.WriteCustomObjects();
      this.writeBigTitle("Structures");
      this.writeComment("Here you can change, enable or disable the stuctures.");
      this.writeComment("If you have disabled the structure in the WorldConfig, it won't spawn,");
      this.writeComment("regardless of these settings.");
      this.writeNewLine();
      this.writeComment("Disables strongholds for this biome. If there is no suitable biome nearby,");
      this.writeComment("Minecraft will ignore this setting.");
      this.writeValue(TCDefaultValues.StrongholdsEnabled.name(), this.strongholdsEnabled);
      this.writeNewLine();
      this.writeComment("Whether a Nether Fortress can start in this biome. Might extend to neighbor biomes.");
      this.writeValue(TCDefaultValues.NetherFortressesEnabled.name(), this.netherFortressesEnabled);
      this.writeNewLine();
      this.writeComment("The village type in this biome. Can be wood, sandstone or disabled.");
      this.writeValue(TCDefaultValues.VillageType.name(), this.villageType.toString());
      this.writeNewLine();
      this.writeComment("The mineshaft rarity from 0 to 100. 0 = no mineshafts, 1 = default rarity, 100 = a wooden chaos.");
      this.writeValue(TCDefaultValues.MineshaftRarity.name(), this.mineshaftsRarity);
      this.writeNewLine();
      this.writeComment("The type of the aboveground rare building in this biome. Can be desertPyramid, jungleTemple, swampHut or disabled.");
      this.writeValue(TCDefaultValues.RareBuildingType.name(), this.rareBuildingType.toString());
      this.writeNewLine();
      this.writeBigTitle("Mob spawning");
      if (DefaultBiome.getBiome(this.Biome.getId()) != null) {
         this.writeComment("Mob spawning control doesn't work in default biomes.");
      } else {
         this.writeComment("========<TUTORIAL>========");
         this.writeComment("This is where you configure mob spawning. Changing this section is optional.");
         this.writeComment("");
         this.writeComment("#STEP1: Understanding what a mobgroup is.");
         this.writeComment("A mobgroups is made of four parts. They are mob, weight, min and max.");
         this.writeComment("The mob is one of the Minecraft internal mob names.");
         this.writeComment("See http://www.minecraftwiki.net/wiki/Chunk_format#Mobs");
         this.writeComment("The weight is used for a random selection. This is a positive integer.");
         this.writeComment("The min is the minimum amount of mobs spawning as a group. This is a positive integer.");
         this.writeComment("The max is the maximum amount of mobs spawning as a group. This is a positive integer.");
         this.writeComment("");
         this.writeComment("#STEP2: Understanding how write a mobgroup as JSON as well as lists of them.");
         this.writeComment("Json is a tree document format: http://en.wikipedia.org/wiki/JSON");
         this.writeComment("Write a mobgroup like this: {\"mob\": \"mobname\", \"weight\": integer, \"min\": integer, \"max\": integer}");
         this.writeComment("For example: {\"mob\": \"Ocelot\", \"weight\": 10, \"min\": 2, \"max\": 6}");
         this.writeComment("For example: {\"mob\": \"MushroomCow\", \"weight\": 5, \"min\": 2, \"max\": 2}");
         this.writeComment("A json list of mobgroups looks like this: [mobgroup, mobgroup, mobgroup...]");
         this.writeComment("This would be an ampty list: []");
         this.writeComment("You can validate your json here: http://jsonlint.com/");
         this.writeComment("");
         this.writeComment("#STEP3: Understanding what to do with all this info");
         this.writeComment("There are three categories of mobs: monsters, creatures and watercreatures.");
         this.writeComment("These list may be populated with Default values if thee booleans bellow is set to true");
         this.writeComment("You may also add your own mobgroups in the lists below");
         this.writeComment("");
         this.writeComment("#STEP4: What is in the default mob groups?");
         this.writeComment("The default mob groups are controlled by vanilla minecraft.");
         this.writeComment("At 2012-03-24 you could find them here: https://github.com/Bukkit/mc-dev/blob/master/net/minecraft/server/BiomeBase.java#L75");
         this.writeComment("In simple terms:");
         this.writeComment("default creatures: [{\"mob\": \"Sheep\", \"weight\": 12, \"min\": 4, \"max\": 4}, {\"mob\": \"Pig\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Chicken\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Cow\", \"weight\": 8, \"min\": 4, \"max\": 4}]");
         this.writeComment("default monsters: [{\"mob\": \"Spider\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Zombie\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Skeleton\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Creeper\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Slime\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Enderman\", \"weight\": 1, \"min\": 1, \"max\": 4}]");
         this.writeComment("default watercreatures: [{\"mob\": \"Squid\", \"weight\": 10, \"min\": 4, \"max\": 4}]");
         this.writeComment("");
         this.writeComment("So for example ocelots wont spawn unless you add them below.");
         this.writeNewLine();
         this.writeComment("========<CONFIGURATION>========");
         this.writeComment("Should we add the default monster spawn groups?");
         this.writeValue("spawnMonstersAddDefaults", this.spawnMonstersAddDefaults);
         this.writeComment("Add extra monster spawn groups here");
         this.writeValue("spawnMonsters", this.spawnMonsters);
         this.writeNewLine();
         this.writeComment("Should we add the default creature spawn groups?");
         this.writeValue("spawnCreaturesAddDefaults", this.spawnCreaturesAddDefaults);
         this.writeComment("Add extra creature spawn groups here");
         this.writeValue("spawnCreatures", this.spawnCreatures);
         this.writeNewLine();
         this.writeComment("Should we add the default watercreature spawn groups?");
         this.writeValue("spawnWaterCreaturesAddDefaults", this.spawnWaterCreaturesAddDefaults);
         this.writeComment("Add extra watercreature spawn groups here");
         this.writeValue("spawnWaterCreatures", this.spawnWaterCreatures);
         this.writeNewLine();
         this.writeComment("Should we add the default ambient creature spawn groups? (Currently only bats)");
         this.writeValue("spawnAmbientCreaturesAddDefaults", this.spawnAmbientCreaturesAddDefaults);
         this.writeComment("Add extra ambient creature spawn groups here");
         this.writeValue("spawnAmbientCreatures", this.spawnAmbientCreatures);
         this.writeNewLine();
      }
   }

   private void WriteHeightSettings() throws IOException {
      String output = Double.toString(this.heightMatrix[0]);

      for(int i = 1; i < this.heightMatrix.length; ++i) {
         output = output + "," + Double.toString(this.heightMatrix[i]);
      }

      this.writeValue(TCDefaultValues.CustomHeightControl.name(), output);
   }

   private void WriteModReplaceSettings() throws IOException {
      if (this.ReplaceCount == 0) {
         this.writeValue("ReplacedBlocks", "None");
      } else {
         String output = "";

         for(int blockIdFrom = 0; blockIdFrom < this.replaceMatrixBlocks.length; ++blockIdFrom) {
            if (this.replaceMatrixBlocks[blockIdFrom] != null) {
               int previousReplaceTo = -1;
               int yStart = 0;

               for(int y = 0; y <= this.replaceMatrixBlocks[blockIdFrom].length; ++y) {
                  int currentReplaceTo = y == this.replaceMatrixBlocks[blockIdFrom].length ? -1 : this.replaceMatrixBlocks[blockIdFrom][y];
                  if (currentReplaceTo != previousReplaceTo) {
                     if (previousReplaceTo != -1) {
                        output = output + "(" + StringHelper.makeMaterial(blockIdFrom) + ",";
                        if (yStart == 0 && y == this.replaceMatrixBlocks[blockIdFrom].length) {
                           output = output + StringHelper.makeMaterial(previousReplaceTo >> 4, previousReplaceTo & 15);
                        } else {
                           output = output + StringHelper.makeMaterial(previousReplaceTo >> 4, previousReplaceTo & 15) + "," + yStart + "," + (y - 1);
                        }

                        output = output + "),";
                        previousReplaceTo = -1;
                     }

                     if (previousReplaceTo == -1) {
                        yStart = y;
                        previousReplaceTo = currentReplaceTo;
                     }
                  }
               }
            }
         }

         this.writeValue("ReplacedBlocks", output.substring(0, output.length() - 1));
      }
   }

   private void WriteResources() throws IOException {
      for(int i = 0; i < this.ResourceCount; ++i) {
         this.writeValue(this.ResourceSequence[i].write());
      }

   }

   private void WriteCustomObjects() throws IOException {
      StringBuilder builder = new StringBuilder();

      for(String objectString : this.biomeObjectStrings) {
         builder.append(objectString);
         builder.append(',');
      }

      if (builder.length() > 0) {
         builder.deleteCharAt(builder.length() - 1);
      }

      this.writeValue("BiomeObjects", builder.toString());
   }

   private void WriteSaplingSettings() throws IOException {
      if (this.saplingResource != null) {
         this.writeValue(this.saplingResource.write());
      }

      for(SaplingGen res : this.saplingTypes) {
         if (res != null) {
            this.writeValue(res.write());
         }
      }

   }

   protected void correctSettings() {
      this.BiomeSize = this.applyBounds(this.BiomeSize, 0, this.worldConfig.GenerationDepth);
      this.BiomeHeight = (float)this.applyBounds((double)this.BiomeHeight, (double)-10.0F, (double)10.0F);
      this.BiomeRarity = this.applyBounds(this.BiomeRarity, 1, this.worldConfig.BiomeRarityScale);
      this.BiomeTemperature = this.applyBounds(this.BiomeTemperature, 0.0F, 1.0F);
      this.BiomeWetness = this.applyBounds(this.BiomeWetness, 0.0F, 1.0F);
      this.IsleInBiome = this.filterBiomes(this.IsleInBiome, this.worldConfig.CustomBiomes);
      this.BiomeIsBorder = this.filterBiomes(this.BiomeIsBorder, this.worldConfig.CustomBiomes);
      this.NotBorderNear = this.filterBiomes(this.NotBorderNear, this.worldConfig.CustomBiomes);
      this.volatility1 = this.volatilityRaw1 < (double)0.0F ? (double)1.0F / (Math.abs(this.volatilityRaw1) + (double)1.0F) : this.volatilityRaw1 + (double)1.0F;
      this.volatility2 = this.volatilityRaw2 < (double)0.0F ? (double)1.0F / (Math.abs(this.volatilityRaw2) + (double)1.0F) : this.volatilityRaw2 + (double)1.0F;
      this.volatilityWeight1 = (this.volatilityWeightRaw1 - (double)0.5F) * (double)24.0F;
      this.volatilityWeight2 = ((double)0.5F - this.volatilityWeightRaw2) * (double)24.0F;
      this.waterLevelMin = this.applyBounds(this.waterLevelMin, 0, this.worldConfig.WorldHeight - 1);
      this.waterLevelMax = this.applyBounds(this.waterLevelMax, 0, this.worldConfig.WorldHeight - 1, this.waterLevelMin);
      this.ReplaceBiomeName = !DefaultBiome.Contain(this.ReplaceBiomeName) && !this.worldConfig.CustomBiomes.contains(this.ReplaceBiomeName) ? "" : this.ReplaceBiomeName;
      this.RiverBiome = !DefaultBiome.Contain(this.RiverBiome) && !this.worldConfig.CustomBiomes.contains(this.RiverBiome) ? "" : this.RiverBiome;
   }

   protected void renameOldSettings() {
      TCDefaultValues[] copyFromWorld = new TCDefaultValues[]{TCDefaultValues.MaxAverageHeight, TCDefaultValues.MaxAverageDepth, TCDefaultValues.Volatility1, TCDefaultValues.Volatility2, TCDefaultValues.VolatilityWeight1, TCDefaultValues.VolatilityWeight2, TCDefaultValues.DisableBiomeHeight, TCDefaultValues.CustomHeightControl};

      for(TCDefaultValues value : copyFromWorld) {
         if (this.worldConfig.settingsCache.containsKey(value.name().toLowerCase())) {
            this.settingsCache.put(value.name().toLowerCase(), this.worldConfig.settingsCache.get(value.name().toLowerCase()));
         }
      }

      if (this.settingsCache.containsKey("disableNotchPonds".toLowerCase()) && !this.readModSettings("disableNotchPonds".toLowerCase(), false)) {
         this.settingsCache.put("SmallLake(WATER,4,7,8," + this.worldConfig.WorldHeight + ")", "0");
         this.settingsCache.put("SmallLake(LAVA,2,3,8," + (this.worldConfig.WorldHeight - 8) + ")", "1");
      }

      int customTreeChance = 0;
      if (this.worldConfig.settingsCache.containsKey("customtreechance")) {
         try {
            customTreeChance = Integer.parseInt((String)this.worldConfig.settingsCache.get("customtreechance"));
         } catch (NumberFormatException var20) {
         }
      }

      if (customTreeChance == 100) {
         this.settingsCache.put("Sapling(All,UseWorld,100)", "-");
      }

      if (customTreeChance > 0 && customTreeChance < 100) {
         this.settingsCache.put("Sapling(0,UseWorld," + customTreeChance + ",BigTree,10,Tree,100)", "-");
         this.settingsCache.put("Sapling(1,UseWorld," + customTreeChance + ",Taiga2,100)", "-");
         this.settingsCache.put("Sapling(2,UseWorld," + customTreeChance + ",Forest,100)", "-");
         this.settingsCache.put("Sapling(3,UseWorld," + customTreeChance + ",CocoaTree,100)", "-");
      }

      if (!this.readModSettings("BiomeRivers", true)) {
         this.settingsCache.put("riverbiome", "");
      }

      String replacedBlocksValue = this.readModSettings("ReplacedBlocks", "None");
      if (replacedBlocksValue.contains("=")) {
         String[] values = replacedBlocksValue.split(",");
         String output = "";

         for(String replacedBlock : values) {
            try {
               String fromId = replacedBlock.split("=")[0];
               String toId = replacedBlock.split("=")[1];
               String toData = "0";
               String minHeight = "0";
               int maxHeight = this.worldConfig.WorldHeight;
               boolean longForm = false;
               int start = toId.indexOf("(");
               int end = toId.indexOf(")");
               if (start != -1 && end != -1) {
                  String[] ranges = toId.substring(start + 1, end).split("-");
                  toId = toId.substring(0, start);
                  minHeight = ranges[0];
                  maxHeight = Integer.parseInt(ranges[1]);
                  longForm = true;
               }

               if (toId.contains(".")) {
                  String[] temp = toId.split("\\.");
                  toId = temp[0];
                  toData = temp[1];
                  longForm = true;
               }

               if (longForm) {
                  output = output + "(" + fromId + "," + toId + ":" + toData + "," + minHeight + "," + (maxHeight - 1) + "),";
               } else {
                  output = output + "(" + fromId + "," + toId + "),";
               }
            } catch (Exception var19) {
            }
         }

         this.settingsCache.put("replacedblocks", output.substring(0, output.length() - 1));
      }

   }

   protected void initDefaults() {
      this.defaultBiomeSurface = this.Biome.getSurfaceHeight();
      this.defaultBiomeVolatility = this.Biome.getSurfaceVolatility();
      this.defaultSurfaceBlock = this.Biome.getSurfaceBlock();
      this.defaultGroundBlock = this.Biome.getGroundBlock();
      this.defaultBiomeTemperature = this.Biome.getTemperature();
      this.defaultBiomeWetness = this.Biome.getWetness();
      switch (this.Biome.getId()) {
         case 0:
            this.defaultColor = "0x3333FF";
            this.defaultStrongholds = false;
            this.defaultRiverBiome = "";
            break;
         case 1:
            this.defaultTrees = 0;
            this.defaultFlowers = 4;
            this.defaultGrass = 100;
            this.defaultColor = "0x999900";
            this.defaultStrongholds = false;
            this.defaultVillageType = BiomeConfig.VillageType.wood;
            break;
         case 2:
            this.defaultWaterLakes = false;
            this.defaultTrees = 0;
            this.defaultDeadBrush = 4;
            this.defaultGrass = 0;
            this.defaultReed = 10;
            this.defaultCactus = 10;
            this.defaultColor = "0xFFCC33";
            this.defaultWell = new Object[]{DefaultMaterial.SANDSTONE, DefaultMaterial.STEP + ":1", DefaultMaterial.WATER, 1, 0.1, 2, this.worldConfig.WorldHeight, DefaultMaterial.SAND};
            this.defaultVillageType = BiomeConfig.VillageType.sandstone;
            this.defaultRareBuildingType = BiomeConfig.RareBuildingType.desertPyramid;
            break;
         case 3:
            this.defaultColor = "0x333300";
            break;
         case 4:
            this.defaultTrees = 10;
            this.defaultGrass = 15;
            this.defaultColor = "0x00FF00";
            break;
         case 5:
            this.defaultTrees = 10;
            this.defaultGrass = 10;
            this.defaultColor = "0x007700";
            break;
         case 6:
            this.defaultTrees = 2;
            this.defaultFlowers = -999;
            this.defaultDeadBrush = 1;
            this.defaultMushroom = 8;
            this.defaultReed = 10;
            this.defaultClay = 1;
            this.defaultWaterLily = 1;
            this.defaultColor = "0x99CC66";
            this.defaultWaterColorMultiplier = "0xe0ffae";
            this.defaultGrassColor = "0x7E6E7E";
            this.defaultFoliageColor = "0x7E6E7E";
            this.defaultRareBuildingType = BiomeConfig.RareBuildingType.swampHut;
            break;
         case 7:
            this.defaultSize = 8;
            this.defaultRarity = 95;
            this.defaultIsle.add(DefaultBiome.SWAMPLAND.Name);
            this.defaultColor = "0x00CCCC";
            this.defaultStrongholds = false;
         case 8:
         case 9:
         default:
            break;
         case 10:
            this.defaultColor = "0xFFFFFF";
            this.defaultStrongholds = false;
            this.defaultRiverBiome = "";
            break;
         case 11:
            this.defaultColor = "0x66FFFF";
            this.defaultStrongholds = false;
            break;
         case 12:
            this.defaultColor = "0xCCCCCC";
            if (this.worldConfig.readModSettings("FrozenRivers", true)) {
               this.defaultRiverBiome = DefaultBiome.FROZEN_RIVER.Name;
            }
            break;
         case 13:
            this.defaultColor = "0xCC9966";
            if (this.worldConfig.readModSettings("FrozenRivers", true)) {
               this.defaultRiverBiome = DefaultBiome.FROZEN_RIVER.Name;
            }
            break;
         case 14:
            this.defaultSurfaceBlock = (byte)DefaultMaterial.MYCEL.id;
            this.defaultMushroom = 1;
            this.defaultGrass = 0;
            this.defaultFlowers = 0;
            this.defaultTrees = 0;
            this.defaultRarity = 1;
            this.defaultRiverBiome = "";
            this.defaultSize = 6;
            this.defaultIsle.add(DefaultBiome.OCEAN.Name);
            this.defaultColor = "0xFF33CC";
            this.defaultWaterLily = 1;
            this.defaultStrongholds = false;
            break;
         case 15:
            this.defaultRiverBiome = "";
            this.defaultSize = 9;
            this.defaultBorder.add(DefaultBiome.MUSHROOM_ISLAND.Name);
            this.defaultColor = "0xFF9999";
            this.defaultStrongholds = false;
            break;
         case 16:
            this.defaultTrees = 0;
            this.defaultSize = 8;
            this.defaultBorder.add(DefaultBiome.OCEAN.Name);
            this.defaultNotBorderNear.add(DefaultBiome.RIVER.Name);
            this.defaultNotBorderNear.add(DefaultBiome.SWAMPLAND.Name);
            this.defaultNotBorderNear.add(DefaultBiome.EXTREME_HILLS.Name);
            this.defaultNotBorderNear.add(DefaultBiome.MUSHROOM_ISLAND.Name);
            this.defaultColor = "0xFFFF00";
            this.defaultStrongholds = false;
            break;
         case 17:
            this.defaultWaterLakes = false;
            this.defaultSize = 6;
            this.defaultRarity = 97;
            this.defaultIsle.add(DefaultBiome.DESERT.Name);
            this.defaultTrees = 0;
            this.defaultDeadBrush = 4;
            this.defaultGrass = 0;
            this.defaultReed = 50;
            this.defaultCactus = 10;
            this.defaultColor = "0x996600";
            this.defaultWell = new Object[]{DefaultMaterial.SANDSTONE, DefaultMaterial.STEP + ":1", DefaultMaterial.WATER, 1, 0.1, 2, this.worldConfig.WorldHeight, DefaultMaterial.SAND};
            this.defaultVillageType = BiomeConfig.VillageType.sandstone;
            this.defaultRareBuildingType = BiomeConfig.RareBuildingType.desertPyramid;
            break;
         case 18:
            this.defaultSize = 6;
            this.defaultRarity = 97;
            this.defaultIsle.add(DefaultBiome.FOREST.Name);
            this.defaultTrees = 10;
            this.defaultGrass = 15;
            this.defaultColor = "0x009900";
            break;
         case 19:
            this.defaultSize = 6;
            this.defaultRarity = 97;
            this.defaultIsle.add(DefaultBiome.TAIGA.Name);
            this.defaultTrees = 10;
            this.defaultGrass = 10;
            this.defaultColor = "0x003300";
            this.defaultRiverBiome = DefaultBiome.FROZEN_RIVER.Name;
            break;
         case 20:
            this.defaultSize = 8;
            this.defaultBorder.add(DefaultBiome.EXTREME_HILLS.Name);
            this.defaultColor = "0x666600";
            break;
         case 21:
            this.defaultTrees = 50;
            this.defaultGrass = 25;
            this.defaultFlowers = 4;
            this.defaultColor = "0xCC6600";
            this.defaultRareBuildingType = BiomeConfig.RareBuildingType.jungleTemple;
            break;
         case 22:
            this.defaultTrees = 50;
            this.defaultGrass = 25;
            this.defaultFlowers = 4;
            this.defaultColor = "0x663300";
            this.defaultIsle.add(DefaultBiome.JUNGLE.Name);
            this.defaultRareBuildingType = BiomeConfig.RareBuildingType.jungleTemple;
      }

   }

   public void Serialize(DataOutputStream stream) throws IOException {
      writeStringToStream(stream, this.name);
      stream.writeFloat(this.BiomeTemperature);
      stream.writeFloat(this.BiomeWetness);
      stream.writeInt(this.SkyColor);
      stream.writeInt(this.WaterColor);
      stream.writeInt(this.GrassColor);
      stream.writeBoolean(this.GrassColorIsMultiplier);
      stream.writeInt(this.FoliageColor);
      stream.writeBoolean(this.FoliageColorIsMultiplier);
   }

   public BiomeConfig(DataInputStream stream, WorldConfig config, LocalBiome biome) throws IOException {
      super();
      this.replaceMatrixBlocks = new short[TerrainControl.supportedBlockIds][];
      this.ReplaceCount = 0;
      this.ResourceSequence = new Resource[256];
      this.saplingTypes = new SaplingGen[20];
      this.saplingResource = null;
      this.ResourceCount = 0;
      this.spawnMonstersAddDefaults = true;
      this.spawnMonsters = new ArrayList();
      this.spawnCreaturesAddDefaults = true;
      this.spawnCreatures = new ArrayList();
      this.spawnWaterCreaturesAddDefaults = true;
      this.spawnWaterCreatures = new ArrayList();
      this.spawnAmbientCreaturesAddDefaults = true;
      this.spawnAmbientCreatures = new ArrayList();
      this.defaultWaterLakes = true;
      this.defaultTrees = 1;
      this.defaultFlowers = 2;
      this.defaultGrass = 10;
      this.defaultDeadBrush = 0;
      this.defaultMushroom = 0;
      this.defaultReed = 0;
      this.defaultCactus = 0;
      this.defaultClay = 1;
      this.defaultBiomeSurface = 0.1F;
      this.defaultBiomeVolatility = 0.3F;
      this.defaultSurfaceBlock = (byte)DefaultMaterial.GRASS.id;
      this.defaultGroundBlock = (byte)DefaultMaterial.DIRT.id;
      this.defaultBiomeTemperature = 0.5F;
      this.defaultBiomeWetness = 0.5F;
      this.defaultIsle = new ArrayList();
      this.defaultBorder = new ArrayList();
      this.defaultNotBorderNear = new ArrayList();
      this.defaultRiverBiome = DefaultBiome.RIVER.Name;
      this.defaultSize = 4;
      this.defaultRarity = 100;
      this.defaultColor = "0x000000";
      this.defaultWaterLily = 0;
      this.defaultWaterColorMultiplier = "0xFFFFFF";
      this.defaultGrassColor = "0xFFFFFF";
      this.defaultFoliageColor = "0xFFFFFF";
      this.defaultStrongholds = true;
      this.defaultVillageType = BiomeConfig.VillageType.disabled;
      this.defaultRareBuildingType = BiomeConfig.RareBuildingType.disabled;
      this.name = readStringFromStream(stream);
      this.Biome = biome;
      this.worldConfig = config;
      this.BiomeTemperature = stream.readFloat();
      this.BiomeWetness = stream.readFloat();
      this.SkyColor = stream.readInt();
      this.WaterColor = stream.readInt();
      this.GrassColor = stream.readInt();
      this.GrassColorIsMultiplier = stream.readBoolean();
      this.FoliageColor = stream.readInt();
      this.FoliageColorIsMultiplier = stream.readBoolean();
   }

   public static enum VillageType {
      disabled,
      wood,
      sandstone;

      private VillageType() {
      }
   }

   public static enum RareBuildingType {
      disabled,
      desertPyramid,
      jungleTemple,
      swampHut;

      private RareBuildingType() {
      }
   }
}
