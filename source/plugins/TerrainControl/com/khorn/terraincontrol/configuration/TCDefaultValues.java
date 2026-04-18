package com.khorn.terraincontrol.configuration;

import java.util.ArrayList;
import java.util.Collections;

public enum TCDefaultValues implements TCSetting {
   WorldSettingsName("WorldConfig.ini"),
   BO_WorldDirectoryName("WorldObjects"),
   WorldBiomeConfigDirectoryName("BiomeConfigs"),
   WorldBiomeConfigName("BiomeConfig.ini"),
   ChannelName("TerrainControl"),
   ProtocolVersion(5),
   snowAndIceMaxTemp(0.15F),
   SettingsMode(WorldConfig.ConfigMode.WriteAll),
   TerrainMode(WorldConfig.TerrainMode.Normal),
   BiomeMode("Normal"),
   WorldHeightBits(7),
   GenerationDepth(10),
   BiomeRarityScale(100),
   LandRarity(97),
   LandSize(0),
   LandFuzzy(6),
   IceRarity(90),
   IceSize(3),
   RiverRarity(4),
   RiverSize(0),
   RiversEnabled(true),
   FrozenOcean(true),
   NormalBiomes("Desert,Forest,Extreme Hills,Swampland,Plains,Taiga,Jungle", TCSetting.SettingsType.StringArray),
   IceBiomes("Ice Plains", TCSetting.SettingsType.StringArray),
   IsleBiomes("MushroomIsland,Ice Mountains,DesertHills,ForestHills,TaigaHills,River,JungleHills", TCSetting.SettingsType.StringArray),
   BorderBiomes("MushroomIslandShore,Beach,Extreme Hills Edge", TCSetting.SettingsType.StringArray),
   CustomBiomes("", TCSetting.SettingsType.StringArray),
   ImageMode(WorldConfig.ImageMode.Repeat),
   ImageFile("map.png"),
   ImageFillBiome("Ocean"),
   ImageXOffset(0),
   ImageZOffset(0),
   oldBiomeSize((double)1.5F),
   minMoisture(0.0F),
   maxMoisture(1.0F),
   minTemperature(0.0F),
   maxTemperature(1.0F),
   WorldFog("0xC0D8FF", TCSetting.SettingsType.Color),
   WorldNightFog("0x0B0D17", TCSetting.SettingsType.Color),
   NetherFortressesEnabled(false),
   StrongholdsEnabled(true),
   StrongholdCount(3),
   StrongholdDistance((double)32.0F),
   StrongholdSpread(3),
   VillagesEnabled(true),
   VillageDistance(32),
   VillageSize(0),
   VillageType(BiomeConfig.VillageType.disabled),
   MineshaftsEnabled(true),
   MineshaftRarity((double)1.0F),
   RareBuildingsEnabled(true),
   MinimumDistanceBetweenRareBuildings(9),
   MaximumDistanceBetweenRareBuildings(32),
   RareBuildingType(BiomeConfig.RareBuildingType.disabled),
   caveRarity(7),
   caveFrequency(40),
   caveMinAltitude(8),
   caveMaxAltitude(128),
   individualCaveRarity(25),
   caveSystemFrequency(1),
   caveSystemPocketChance(0),
   caveSystemPocketMinSize(0),
   caveSystemPocketMaxSize(4),
   evenCaveDistribution(false),
   canyonRarity(2),
   canyonMinAltitude(20),
   canyonMaxAltitude(68),
   canyonMinLength(84),
   canyonMaxLength(112),
   canyonDepth((double)3.0F),
   WaterLevelMax(63),
   WaterLevelMin(0),
   WaterBlock(9),
   IceBlock(79),
   FractureHorizontal((double)0.0F),
   FractureVertical((double)0.0F),
   DisableBedrock(false),
   CeilingBedrock(false),
   FlatBedrock(false),
   BedrockobBlock(7),
   RemoveSurfaceStone(false),
   objectSpawnRatio(1),
   ResourcesSeed(0L),
   BiomeSize(5),
   BiomeRarity(100),
   BiomeColor("", TCSetting.SettingsType.Color),
   RiverBiome("River"),
   IsleInBiome("Ocean", TCSetting.SettingsType.StringArray),
   BiomeIsBorder("", TCSetting.SettingsType.StringArray),
   NotBorderNear("", TCSetting.SettingsType.StringArray),
   BiomeTemperature(0.5F),
   BiomeWetness(0.5F),
   ReplaceToBiomeName(""),
   BiomeHeight(0.1),
   BiomeVolatility(0.3),
   SurfaceBlock(2),
   GroundBlock(3),
   UseWorldWaterLevel(true),
   SkyColor("0x7BA5FF", TCSetting.SettingsType.Color),
   WaterColor("0xFFFFFF", TCSetting.SettingsType.Color),
   GrassColor("0x000000", TCSetting.SettingsType.Color),
   GrassColorIsMultiplier(true),
   FoliageColor("0x000000", TCSetting.SettingsType.Color),
   FoliageColorIsMultiplier(true),
   Volatility1((double)0.0F),
   Volatility2((double)0.0F),
   VolatilityWeight1((double)0.5F),
   VolatilityWeight2(0.45),
   DisableBiomeHeight(false),
   MaxAverageHeight((double)0.0F),
   MaxAverageDepth((double)0.0F),
   CustomHeightControl("0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0", TCSetting.SettingsType.StringArray),
   SmallLakeWaterFrequency(4),
   SmallLakeLavaFrequency(2),
   SmallLakeWaterRarity(7),
   SmallLakeLavaRarity(1),
   SmallLakeMinAltitude(8),
   SmallLakeMaxAltitude(120),
   undergroundLakeFrequency(2),
   undergroundLakeRarity(5),
   undergroundLakeMinSize(50),
   undergroundLakeMaxSize(60),
   undergroundLakeMinAltitude(0),
   undergroundLakeMaxAltitude(50),
   dungeonRarity(100),
   dungeonFrequency(8),
   dungeonMinAltitude(0),
   dirtDepositRarity(100),
   dirtDepositFrequency(20),
   dirtDepositSize(32),
   dirtDepositMinAltitude(0),
   dirtDepositMaxAltitude(128),
   gravelDepositRarity(100),
   gravelDepositFrequency(10),
   gravelDepositSize(32),
   gravelDepositMinAltitude(0),
   gravelDepositMaxAltitude(128),
   clayDepositRarity(100),
   clayDepositFrequency(1),
   clayDepositSize(32),
   clayDepositMinAltitude(0),
   clayDepositMaxAltitude(128),
   coalDepositRarity(100),
   coalDepositFrequency(20),
   coalDepositSize(16),
   coalDepositMinAltitude(0),
   coalDepositMaxAltitude(128),
   ironDepositRarity(100),
   ironDepositFrequency(20),
   ironDepositSize(8),
   ironDepositMinAltitude(0),
   ironDepositMaxAltitude(64),
   goldDepositRarity(100),
   goldDepositFrequency(2),
   goldDepositSize(8),
   goldDepositMinAltitude(0),
   goldDepositMaxAltitude(32),
   redstoneDepositRarity(100),
   redstoneDepositFrequency(8),
   redstoneDepositSize(7),
   redstoneDepositMinAltitude(0),
   redstoneDepositMaxAltitude(16),
   diamondDepositRarity(100),
   diamondDepositFrequency(1),
   diamondDepositSize(7),
   diamondDepositMinAltitude(0),
   diamondDepositMaxAltitude(16),
   lapislazuliDepositRarity(100),
   lapislazuliDepositFrequency(1),
   lapislazuliDepositSize(7),
   lapislazuliDepositMinAltitude(0),
   lapislazuliDepositMaxAltitude(16),
   emeraldDepositRarity(100),
   emeraldDepositFrequency(1),
   emeraldDepositSize(5),
   emeraldDepositMinAltitude(4),
   emeraldDepositMaxAltitude(32),
   waterClayDepositRarity(100),
   waterClayDepositSize(4),
   waterSandDepositRarity(100),
   waterSandDepositFrequency(4),
   waterSandDepositSize(7),
   roseDepositRarity(100),
   roseDepositMinAltitude(0),
   roseDepositMaxAltitude(128),
   flowerDepositRarity(100),
   flowerDepositMinAltitude(0),
   flowerDepositMaxAltitude(128),
   redMushroomDepositRarity(100),
   redMushroomDepositMinAltitude(0),
   redMushroomDepositMaxAltitude(128),
   brownMushroomDepositRarity(100),
   brownMushroomDepositMinAltitude(0),
   brownMushroomDepositMaxAltitude(128),
   longGrassDepositRarity(100),
   deadBushDepositRarity(100),
   pumpkinDepositRarity(3),
   pumpkinDepositFrequency(1),
   pumpkinDepositMinAltitude(0),
   pumpkinDepositMaxAltitude(128),
   reedDepositRarity(100),
   reedDepositMinAltitude(0),
   reedDepositMaxAltitude(128),
   cactusDepositRarity(100),
   cactusDepositMinAltitude(0),
   cactusDepositMaxAltitude(128),
   vinesRarity(100),
   vinesFrequency(50),
   vinesMinAltitude(64),
   waterSourceDepositRarity(100),
   waterSourceDepositFrequency(20),
   waterSourceDepositMinAltitude(8),
   waterSourceDepositMaxAltitude(128),
   lavaSourceDepositRarity(100),
   lavaSourceDepositFrequency(10),
   lavaSourceDepositMinAltitude(8),
   lavaSourceDepositMaxAltitude(128);

   private int iValue;
   private long lValue;
   private double dValue;
   private float fValue;
   private String sValue;
   private boolean bValue;
   private Enum eValue;
   private TCSetting.SettingsType returnType;
   private ArrayList sArrayValue;

   private TCDefaultValues(int i) {
      this.iValue = i;
      this.returnType = TCSetting.SettingsType.Int;
   }

   private TCDefaultValues(double d) {
      this.dValue = d;
      this.returnType = TCSetting.SettingsType.Double;
   }

   private TCDefaultValues(float f) {
      this.fValue = f;
      this.returnType = TCSetting.SettingsType.Float;
   }

   private TCDefaultValues(long l) {
      this.lValue = l;
      this.returnType = TCSetting.SettingsType.Long;
   }

   private TCDefaultValues(String s) {
      this.sValue = s;
      this.returnType = TCSetting.SettingsType.String;
   }

   private TCDefaultValues(String s, TCSetting.SettingsType type) {
      this.returnType = type;
      if (type == TCSetting.SettingsType.StringArray) {
         this.sArrayValue = new ArrayList();
         if (s.contains(",")) {
            Collections.addAll(this.sArrayValue, s.split(","));
         } else if (!s.equals("")) {
            this.sArrayValue.add(s);
         }

      } else {
         this.sValue = s;
      }
   }

   private TCDefaultValues(Enum e) {
      this.eValue = e;
      this.returnType = TCSetting.SettingsType.Enum;
   }

   private TCDefaultValues(boolean b) {
      this.bValue = b;
      this.returnType = TCSetting.SettingsType.Boolean;
   }

   public int intValue() {
      return this.iValue;
   }

   public long longValue() {
      return this.lValue;
   }

   public double doubleValue() {
      return this.dValue;
   }

   public float floatValue() {
      return this.fValue;
   }

   public Enum enumValue() {
      return this.eValue;
   }

   public TCSetting.SettingsType getReturnType() {
      return this.returnType;
   }

   public String stringValue() {
      return this.sValue;
   }

   public ArrayList stringArrayListValue() {
      return this.sArrayValue;
   }

   public boolean booleanValue() {
      return this.bValue;
   }
}
