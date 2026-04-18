package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class WorldConfig extends ConfigFile {
   public ArrayList CustomBiomes = new ArrayList();
   public HashMap CustomBiomeIds = new HashMap();
   public List customObjects = new ArrayList();
   public ArrayList NormalBiomes = new ArrayList();
   public ArrayList IceBiomes = new ArrayList();
   public ArrayList IsleBiomes = new ArrayList();
   public ArrayList BorderBiomes = new ArrayList();
   public BiomeConfig[] biomeConfigs;
   public int biomesCount;
   public byte[] ReplaceMatrixBiomes = new byte[256];
   public boolean HaveBiomeReplace = false;
   public double oldBiomeSize;
   public float minMoisture;
   public float maxMoisture;
   public float minTemperature;
   public float maxTemperature;
   public int GenerationDepth;
   public int BiomeRarityScale;
   public int LandRarity;
   public int LandSize;
   public int LandFuzzy;
   public int IceRarity;
   public int IceSize;
   public int RiverRarity;
   public int RiverSize;
   public boolean RiversEnabled;
   public boolean FrozenOcean;
   public String imageFile;
   public ImageMode imageMode;
   public String imageFillBiome;
   public int imageXOffset;
   public int imageZOffset;
   public HashMap biomeColorMap;
   public int WorldFog;
   public float WorldFogR;
   public float WorldFogG;
   public float WorldFogB;
   public int WorldNightFog;
   public float WorldNightFogR;
   public float WorldNightFogG;
   public float WorldNightFogB;
   public int caveRarity;
   public int caveFrequency;
   public int caveMinAltitude;
   public int caveMaxAltitude;
   public int individualCaveRarity;
   public int caveSystemFrequency;
   public int caveSystemPocketChance;
   public int caveSystemPocketMinSize;
   public int caveSystemPocketMaxSize;
   public boolean evenCaveDistribution;
   public int canyonRarity;
   public int canyonMinAltitude;
   public int canyonMaxAltitude;
   public int canyonMinLength;
   public int canyonMaxLength;
   public double canyonDepth;
   public boolean strongholdsEnabled;
   public double strongholdDistance;
   public int strongholdCount;
   public int strongholdSpread;
   public boolean villagesEnabled;
   public int villageSize;
   public int villageDistance;
   public boolean rareBuildingsEnabled;
   public int minimumDistanceBetweenRareBuildings;
   public int maximumDistanceBetweenRareBuildings;
   public boolean mineshaftsEnabled;
   public boolean netherFortressesEnabled;
   public boolean oldTerrainGenerator;
   public int waterLevelMax;
   public int waterLevelMin;
   public int waterBlock;
   public int iceBlock;
   public double fractureHorizontal;
   public double fractureVertical;
   public boolean disableBedrock;
   public boolean flatBedrock;
   public boolean ceilingBedrock;
   public int bedrockBlock;
   public boolean removeSurfaceStone;
   public int objectSpawnRatio;
   public File customObjectsDirectory;
   public File SettingsDir;
   public ConfigMode SettingsMode;
   public boolean isDeprecated = false;
   public WorldConfig newSettings = null;
   public String WorldName;
   public TerrainMode ModeTerrain;
   public Class biomeMode;
   public boolean BiomeConfigsHaveReplacement = false;
   public int normalBiomesRarity;
   public int iceBiomesRarity;
   public int worldHeightBits;
   public int WorldHeight;
   public long resourcesSeed;

   public WorldConfig(File settingsDir, LocalWorld world, boolean checkOnly) {
      super();
      this.SettingsDir = settingsDir;
      this.WorldName = world.getName();
      File settingsFile = new File(this.SettingsDir, TCDefaultValues.WorldSettingsName.stringValue());
      this.readSettingsFile(settingsFile);
      this.renameOldSettings();
      this.readConfigSettings();
      this.correctSettings();
      this.ReadWorldCustomObjects();

      for(String biomeName : this.CustomBiomes) {
         if ((Integer)this.CustomBiomeIds.get(biomeName) == -1) {
            this.CustomBiomeIds.put(biomeName, world.getFreeBiomeId());
         }
      }

      if (this.SettingsMode != WorldConfig.ConfigMode.WriteDisable) {
         this.writeSettingsFile(settingsFile, this.SettingsMode == WorldConfig.ConfigMode.WriteAll);
      }

      world.setHeightBits(this.worldHeightBits);
      File BiomeFolder = new File(this.SettingsDir, TCDefaultValues.WorldBiomeConfigDirectoryName.stringValue());
      if (!BiomeFolder.exists() && !BiomeFolder.mkdir()) {
         TerrainControl.log(Level.WARNING, "Error creating biome configs directory, working with defaults");
      } else {
         ArrayList<LocalBiome> localBiomes = new ArrayList(world.getDefaultBiomes());

         for(String biomeName : this.CustomBiomes) {
            if (checkOnly) {
               localBiomes.add(world.getNullBiome(biomeName));
            } else {
               localBiomes.add(world.AddBiome(biomeName, (Integer)this.CustomBiomeIds.get(biomeName)));
            }
         }

         for(int i = 0; i < this.ReplaceMatrixBiomes.length; ++i) {
            this.ReplaceMatrixBiomes[i] = (byte)i;
         }

         this.biomeConfigs = new BiomeConfig[world.getMaxBiomesCount()];
         this.biomesCount = 0;
         String LoadedBiomeNames = "";

         for(LocalBiome localBiome : localBiomes) {
            BiomeConfig config = new BiomeConfig(BiomeFolder, localBiome, this);
            if (!checkOnly) {
               if (!config.ReplaceBiomeName.equals("")) {
                  this.HaveBiomeReplace = true;
                  this.ReplaceMatrixBiomes[config.Biome.getId()] = (byte)world.getBiomeIdByName(config.ReplaceBiomeName);
               }

               if (this.NormalBiomes.contains(config.name)) {
                  this.normalBiomesRarity += config.BiomeRarity;
               }

               if (this.IceBiomes.contains(config.name)) {
                  this.iceBiomesRarity += config.BiomeRarity;
               }

               if (!this.BiomeConfigsHaveReplacement) {
                  this.BiomeConfigsHaveReplacement = config.ReplaceCount > 0;
               }

               if (this.biomesCount != 0) {
                  LoadedBiomeNames = LoadedBiomeNames + ", ";
               }

               LoadedBiomeNames = LoadedBiomeNames + localBiome.getName();
               if (this.biomeConfigs[localBiome.getId()] == null) {
                  ++this.biomesCount;
               } else {
                  TerrainControl.log(Level.WARNING, "Duplicate biome id " + localBiome.getId() + " (" + this.biomeConfigs[localBiome.getId()].name + " and " + config.name + ")!");
               }

               this.biomeConfigs[localBiome.getId()] = config;
               if (this.biomeMode == TerrainControl.getBiomeModeManager().FROM_IMAGE) {
                  if (this.biomeColorMap == null) {
                     this.biomeColorMap = new HashMap();
                  }

                  try {
                     int color = Integer.decode(config.BiomeColor);
                     if (color <= 16777215) {
                        this.biomeColorMap.put(color, config.Biome.getId());
                     }
                  } catch (NumberFormatException var12) {
                     TerrainControl.log(Level.WARNING, "Wrong color in " + config.Biome.getName());
                  }
               }
            }
         }

         TerrainControl.log("Loaded biomes - " + LoadedBiomeNames);
      }
   }

   private void ReadWorldCustomObjects() {
      this.customObjectsDirectory = new File(this.SettingsDir, TCDefaultValues.BO_WorldDirectoryName.stringValue());
      File oldCustomObjectsDirectory = new File(this.SettingsDir, "BOBPlugins");
      if (oldCustomObjectsDirectory.exists() && !oldCustomObjectsDirectory.renameTo(new File(this.SettingsDir, TCDefaultValues.BO_WorldDirectoryName.stringValue()))) {
         TerrainControl.log(Level.WARNING, "Fould old BOBPlugins folder, but it cannot be renamed to WorldObjects.");
         TerrainControl.log(Level.WARNING, "Please move the BO2s manually and delete BOBPlugins afterwards.");
      }

      if (!this.customObjectsDirectory.exists() && !this.customObjectsDirectory.mkdirs()) {
         TerrainControl.log(Level.WARNING, "Can`t create WorldObjects folder. No write permissions?");
      } else {
         this.customObjects = new ArrayList(TerrainControl.getCustomObjectManager().loadObjects(this.customObjectsDirectory).values());
         TerrainControl.log(this.customObjects.size() + " world custom objects loaded.");
      }
   }

   protected void renameOldSettings() {
      this.renameOldSetting("WaterLevel", TCDefaultValues.WaterLevelMax);
      this.renameOldSetting("ModeTerrain", TCDefaultValues.TerrainMode);
      this.renameOldSetting("ModeBiome", TCDefaultValues.BiomeMode);
      this.renameOldSetting("NetherFortressEnabled", TCDefaultValues.NetherFortressesEnabled);
      this.renameOldSetting("PyramidsEnabled", TCDefaultValues.RareBuildingsEnabled);
   }

   protected void correctSettings() {
      this.oldBiomeSize = this.applyBounds(this.oldBiomeSize, 0.1, (double)10.0F);
      this.GenerationDepth = this.applyBounds(this.GenerationDepth, 1, 20);
      this.BiomeRarityScale = this.applyBounds(this.BiomeRarityScale, 1, Integer.MAX_VALUE);
      this.LandRarity = this.applyBounds(this.LandRarity, 1, 100);
      this.LandSize = this.applyBounds(this.LandSize, 0, this.GenerationDepth);
      this.LandFuzzy = this.applyBounds(this.LandFuzzy, 0, this.GenerationDepth - this.LandSize);
      this.IceRarity = this.applyBounds(this.IceRarity, 1, 100);
      this.IceSize = this.applyBounds(this.IceSize, 0, this.GenerationDepth);
      this.RiverRarity = this.applyBounds(this.RiverRarity, 0, this.GenerationDepth);
      this.RiverSize = this.applyBounds(this.RiverSize, 0, this.GenerationDepth - this.RiverRarity);
      this.NormalBiomes = this.filterBiomes(this.NormalBiomes, this.CustomBiomes);
      this.IceBiomes = this.filterBiomes(this.IceBiomes, this.CustomBiomes);
      this.IsleBiomes = this.filterBiomes(this.IsleBiomes, this.CustomBiomes);
      this.BorderBiomes = this.filterBiomes(this.BorderBiomes, this.CustomBiomes);
      if (this.biomeMode == TerrainControl.getBiomeModeManager().FROM_IMAGE) {
         File mapFile = new File(this.SettingsDir, this.imageFile);
         if (!mapFile.exists()) {
            TerrainControl.log("Biome map file not found. Switching BiomeMode to Normal");
            this.biomeMode = TerrainControl.getBiomeModeManager().NORMAL;
         }
      }

      this.imageFillBiome = !DefaultBiome.Contain(this.imageFillBiome) && !this.CustomBiomes.contains(this.imageFillBiome) ? TCDefaultValues.ImageFillBiome.stringValue() : this.imageFillBiome;
      this.minMoisture = this.applyBounds(this.minMoisture, 0.0F, 1.0F);
      this.maxMoisture = this.applyBounds(this.maxMoisture, 0.0F, 1.0F, this.minMoisture);
      this.minTemperature = this.applyBounds(this.minTemperature, 0.0F, 1.0F);
      this.maxTemperature = this.applyBounds(this.maxTemperature, 0.0F, 1.0F, this.minTemperature);
      this.caveRarity = this.applyBounds(this.caveRarity, 0, 100);
      this.caveFrequency = this.applyBounds(this.caveFrequency, 0, 200);
      this.caveMinAltitude = this.applyBounds(this.caveMinAltitude, 0, this.WorldHeight);
      this.caveMaxAltitude = this.applyBounds(this.caveMaxAltitude, 0, this.WorldHeight, this.caveMinAltitude);
      this.individualCaveRarity = this.applyBounds(this.individualCaveRarity, 0, 100);
      this.caveSystemFrequency = this.applyBounds(this.caveSystemFrequency, 0, 200);
      this.caveSystemPocketChance = this.applyBounds(this.caveSystemPocketChance, 0, 100);
      this.caveSystemPocketMinSize = this.applyBounds(this.caveSystemPocketMinSize, 0, 100);
      this.caveSystemPocketMaxSize = this.applyBounds(this.caveSystemPocketMaxSize, 0, 100, this.caveSystemPocketMinSize);
      this.canyonRarity = this.applyBounds(this.canyonRarity, 0, 100);
      this.canyonMinAltitude = this.applyBounds(this.canyonMinAltitude, 0, this.WorldHeight);
      this.canyonMaxAltitude = this.applyBounds(this.canyonMaxAltitude, 0, this.WorldHeight, this.canyonMinAltitude);
      this.canyonMinLength = this.applyBounds(this.canyonMinLength, 1, 500);
      this.canyonMaxLength = this.applyBounds(this.canyonMaxLength, 1, 500, this.canyonMinLength);
      this.canyonDepth = this.applyBounds(this.canyonDepth, 0.1, (double)15.0F);
      this.waterLevelMin = this.applyBounds(this.waterLevelMin, 0, this.WorldHeight - 1);
      this.waterLevelMax = this.applyBounds(this.waterLevelMax, 0, this.WorldHeight - 1, this.waterLevelMin);
      this.villageDistance = this.applyBounds(this.villageDistance, 9, Integer.MAX_VALUE);
      this.minimumDistanceBetweenRareBuildings = this.applyBounds(this.minimumDistanceBetweenRareBuildings, 1, Integer.MAX_VALUE);
      this.maximumDistanceBetweenRareBuildings = this.applyBounds(this.maximumDistanceBetweenRareBuildings, this.minimumDistanceBetweenRareBuildings, Integer.MAX_VALUE);
      if (this.biomeMode == TerrainControl.getBiomeModeManager().OLD_GENERATOR && this.ModeTerrain != WorldConfig.TerrainMode.OldGenerator) {
         TerrainControl.log("Old biome generator works only with old terrain generator!");
         this.biomeMode = TerrainControl.getBiomeModeManager().NORMAL;
      }

   }

   protected void readConfigSettings() {
      this.SettingsMode = (ConfigMode)this.readSettings(TCDefaultValues.SettingsMode);
      this.ModeTerrain = (TerrainMode)this.readSettings(TCDefaultValues.TerrainMode);
      this.biomeMode = TerrainControl.getBiomeModeManager().getBiomeManager((String)this.readSettings(TCDefaultValues.BiomeMode));
      this.worldHeightBits = (Integer)this.readSettings(TCDefaultValues.WorldHeightBits);
      this.worldHeightBits = this.applyBounds(this.worldHeightBits, 5, 8);
      this.WorldHeight = 1 << this.worldHeightBits;
      this.waterLevelMax = this.WorldHeight / 2 - 1;
      this.GenerationDepth = (Integer)this.readSettings(TCDefaultValues.GenerationDepth);
      this.BiomeRarityScale = (Integer)this.readSettings(TCDefaultValues.BiomeRarityScale);
      this.LandRarity = (Integer)this.readSettings(TCDefaultValues.LandRarity);
      this.LandSize = (Integer)this.readSettings(TCDefaultValues.LandSize);
      this.LandFuzzy = (Integer)this.readSettings(TCDefaultValues.LandFuzzy);
      this.IceRarity = (Integer)this.readSettings(TCDefaultValues.IceRarity);
      this.IceSize = (Integer)this.readSettings(TCDefaultValues.IceSize);
      this.RiverRarity = (Integer)this.readSettings(TCDefaultValues.RiverRarity);
      this.RiverSize = (Integer)this.readSettings(TCDefaultValues.RiverSize);
      this.RiversEnabled = (Boolean)this.readSettings(TCDefaultValues.RiversEnabled);
      this.FrozenOcean = (Boolean)this.readSettings(TCDefaultValues.FrozenOcean);
      this.NormalBiomes = (ArrayList)this.readSettings(TCDefaultValues.NormalBiomes);
      this.IceBiomes = (ArrayList)this.readSettings(TCDefaultValues.IceBiomes);
      this.IsleBiomes = (ArrayList)this.readSettings(TCDefaultValues.IsleBiomes);
      this.BorderBiomes = (ArrayList)this.readSettings(TCDefaultValues.BorderBiomes);
      this.ReadCustomBiomes();
      this.imageMode = (ImageMode)this.readSettings(TCDefaultValues.ImageMode);
      this.imageFile = (String)this.readSettings(TCDefaultValues.ImageFile);
      this.imageFillBiome = (String)this.readSettings(TCDefaultValues.ImageFillBiome);
      this.imageXOffset = (Integer)this.readSettings(TCDefaultValues.ImageXOffset);
      this.imageZOffset = (Integer)this.readSettings(TCDefaultValues.ImageZOffset);
      this.oldBiomeSize = (Double)this.readSettings(TCDefaultValues.oldBiomeSize);
      this.minMoisture = (Float)this.readSettings(TCDefaultValues.minMoisture);
      this.maxMoisture = (Float)this.readSettings(TCDefaultValues.maxMoisture);
      this.minTemperature = (Float)this.readSettings(TCDefaultValues.minTemperature);
      this.maxTemperature = (Float)this.readSettings(TCDefaultValues.maxTemperature);
      this.WorldFog = (Integer)this.readSettings(TCDefaultValues.WorldFog);
      this.WorldNightFog = (Integer)this.readSettings(TCDefaultValues.WorldNightFog);
      this.WorldFogR = (float)((this.WorldFog & 16711680) >> 16) / 255.0F;
      this.WorldFogG = (float)((this.WorldFog & '\uff00') >> 8) / 255.0F;
      this.WorldFogB = (float)(this.WorldFog & 255) / 255.0F;
      this.WorldNightFogR = (float)((this.WorldNightFog & 16711680) >> 16) / 255.0F;
      this.WorldNightFogG = (float)((this.WorldNightFog & '\uff00') >> 8) / 255.0F;
      this.WorldNightFogB = (float)(this.WorldNightFog & 255) / 255.0F;
      this.strongholdsEnabled = (Boolean)this.readSettings(TCDefaultValues.StrongholdsEnabled);
      this.strongholdCount = (Integer)this.readSettings(TCDefaultValues.StrongholdCount);
      this.strongholdDistance = (Double)this.readSettings(TCDefaultValues.StrongholdDistance);
      this.strongholdSpread = (Integer)this.readSettings(TCDefaultValues.StrongholdSpread);
      this.villagesEnabled = (Boolean)this.readSettings(TCDefaultValues.VillagesEnabled);
      this.villageDistance = (Integer)this.readSettings(TCDefaultValues.VillageDistance);
      this.villageSize = (Integer)this.readSettings(TCDefaultValues.VillageSize);
      this.rareBuildingsEnabled = (Boolean)this.readSettings(TCDefaultValues.RareBuildingsEnabled);
      this.minimumDistanceBetweenRareBuildings = (Integer)this.readSettings(TCDefaultValues.MinimumDistanceBetweenRareBuildings);
      this.maximumDistanceBetweenRareBuildings = (Integer)this.readSettings(TCDefaultValues.MaximumDistanceBetweenRareBuildings);
      this.mineshaftsEnabled = (Boolean)this.readSettings(TCDefaultValues.MineshaftsEnabled);
      this.netherFortressesEnabled = (Boolean)this.readSettings(TCDefaultValues.NetherFortressesEnabled);
      this.caveRarity = (Integer)this.readSettings(TCDefaultValues.caveRarity);
      this.caveFrequency = (Integer)this.readSettings(TCDefaultValues.caveFrequency);
      this.caveMinAltitude = (Integer)this.readSettings(TCDefaultValues.caveMinAltitude);
      this.caveMaxAltitude = (Integer)this.readSettings(TCDefaultValues.caveMaxAltitude);
      this.individualCaveRarity = (Integer)this.readSettings(TCDefaultValues.individualCaveRarity);
      this.caveSystemFrequency = (Integer)this.readSettings(TCDefaultValues.caveSystemFrequency);
      this.caveSystemPocketChance = (Integer)this.readSettings(TCDefaultValues.caveSystemPocketChance);
      this.caveSystemPocketMinSize = (Integer)this.readSettings(TCDefaultValues.caveSystemPocketMinSize);
      this.caveSystemPocketMaxSize = (Integer)this.readSettings(TCDefaultValues.caveSystemPocketMaxSize);
      this.evenCaveDistribution = (Boolean)this.readSettings(TCDefaultValues.evenCaveDistribution);
      this.canyonRarity = (Integer)this.readSettings(TCDefaultValues.canyonRarity);
      this.canyonMinAltitude = (Integer)this.readSettings(TCDefaultValues.canyonMinAltitude);
      this.canyonMaxAltitude = (Integer)this.readSettings(TCDefaultValues.canyonMaxAltitude);
      this.canyonMinLength = (Integer)this.readSettings(TCDefaultValues.canyonMinLength);
      this.canyonMaxLength = (Integer)this.readSettings(TCDefaultValues.canyonMaxLength);
      this.canyonDepth = (Double)this.readSettings(TCDefaultValues.canyonDepth);
      this.waterLevelMax = (Integer)this.readSettings(TCDefaultValues.WaterLevelMax);
      this.waterLevelMin = (Integer)this.readSettings(TCDefaultValues.WaterLevelMin);
      this.waterBlock = (Integer)this.readSettings(TCDefaultValues.WaterBlock);
      this.iceBlock = (Integer)this.readSettings(TCDefaultValues.IceBlock);
      this.fractureHorizontal = (Double)this.readSettings(TCDefaultValues.FractureHorizontal);
      this.fractureVertical = (Double)this.readSettings(TCDefaultValues.FractureVertical);
      this.disableBedrock = (Boolean)this.readSettings(TCDefaultValues.DisableBedrock);
      this.ceilingBedrock = (Boolean)this.readSettings(TCDefaultValues.CeilingBedrock);
      this.flatBedrock = (Boolean)this.readSettings(TCDefaultValues.FlatBedrock);
      this.bedrockBlock = (Integer)this.readSettings(TCDefaultValues.BedrockobBlock);
      this.removeSurfaceStone = (Boolean)this.readSettings(TCDefaultValues.RemoveSurfaceStone);
      this.objectSpawnRatio = (Integer)this.readSettings(TCDefaultValues.objectSpawnRatio);
      this.resourcesSeed = (Long)this.readSettings(TCDefaultValues.ResourcesSeed);
      this.oldTerrainGenerator = this.ModeTerrain == WorldConfig.TerrainMode.OldGenerator;
   }

   private void ReadCustomBiomes() {
      for(String biome : (ArrayList)this.readSettings(TCDefaultValues.CustomBiomes)) {
         try {
            String[] keys = biome.split(":");
            int id = -1;
            if (keys.length == 2) {
               id = Integer.valueOf(keys[1]);
            }

            this.CustomBiomes.add(keys[0]);
            this.CustomBiomeIds.put(keys[0], id);
         } catch (NumberFormatException var6) {
            System.out.println("Wrong custom biome id settings: '" + biome + "'");
         }
      }

   }

   protected void writeConfigSettings() throws IOException {
      this.writeBigTitle("The modes");
      this.writeComment("What Terrain Control does with the config files.");
      this.writeComment("Possible modes: WriteAll, WriteWithoutComments, WriteDisable");
      this.writeComment("   WriteAll - default");
      this.writeComment("   WriteWithoutComments - write config files without help comments");
      this.writeComment("   WriteDisable - doesn't write to the config files, it only reads. Doesn't auto-update the configs. Use with care!");
      this.writeValue(TCDefaultValues.SettingsMode.name(), this.SettingsMode.name());
      this.writeNewLine();
      this.writeComment("Possible terrain modes: Normal, OldGenerator, TerrainTest, NotGenerate, Default");
      this.writeComment("   Normal - use all features");
      this.writeComment("   OldGenerator - generate land like 1.7.3 generator");
      this.writeComment("   TerrainTest - generate only terrain without any resources");
      this.writeComment("   NotGenerate - generate empty chunks");
      this.writeComment("   Default - use default terrain generator");
      this.writeValue(TCDefaultValues.TerrainMode.name(), this.ModeTerrain.name());
      this.writeNewLine();
      this.writeComment("Possible biome modes: Normal, OldGenerator, Default");
      this.writeComment("   Normal - use all features");
      this.writeComment("   FromImage - get biomes from image file");
      this.writeComment("   OldGenerator - generate biome like the Beta 1.7.3 generator");
      this.writeComment("   Default - use default Notch biome generator");
      this.writeValue(TCDefaultValues.BiomeMode.name(), TerrainControl.getBiomeModeManager().getName(this.biomeMode));
      this.writeBigTitle("Custom biomes");
      this.writeComment("You need to register your custom biomes here. This setting will make Terrain Control");
      this.writeComment("generate setting files for them. However, it won't place them in the world automatically.");
      this.writeComment("See the settings for your BiomeMode below on how to add them to the world.");
      this.writeComment("");
      this.writeComment("Syntax: CustomBiomes:BiomeName:id[,AnotherBiomeName:id[,...]]");
      this.writeComment("Example: CustomBiomes:TestBiome1:30,BiomeTest2:31");
      this.writeComment("This will add two biomes and generate the BiomeConfigs for them.");
      this.writeComment("All changes here need a server restart.");
      this.writeComment("");
      this.writeComment("Due to the way Mojang's loading code works, all biome ids need to be unique");
      this.writeComment("on the server. If you don't do this, the client will display the biomes just fine,");
      this.writeComment("but the server can think it is another biome with the same id. This will cause saplings,");
      this.writeComment("snowfall and mobs to work as in the other biome.");
      this.writeComment("");
      this.writeComment("The available ids range from 0 to 255 and the ids 0 to " + (DefaultBiome.values().length - 1) + " are occupied by vanilla minecraft");
      this.writeComment("biomes. To leave room for new vanilla biomes, it is recommend to not use ids below 30.");
      this.WriteCustomBiomes();
      this.writeBigTitle("Settings for BiomeMode:Normal");
      this.writeComment("Also applies if you are using BiomeMode:FromImage and ImageMode:ContinueNormal.");
      this.writeNewLine();
      this.writeComment("IMPORTANT value for generation. Bigger values appear to zoom out. All 'Sizes' must be smaller than this.");
      this.writeComment("Large %/total area biomes (Continents) must be set small, (limit=0)");
      this.writeComment("Small %/total area biomes (Oasis,Mountain Peaks) must be larger (limit=GenerationDepth)");
      this.writeComment("This could also represent \"Total number of biome sizes\" ");
      this.writeComment("Small values (about 1-2) and Large values (about 20) may affect generator performance.");
      this.writeValue(TCDefaultValues.GenerationDepth.name(), this.GenerationDepth);
      this.writeNewLine();
      this.writeComment("Max biome rarity from 1 to infinity. By default this is 100, but you can raise it for");
      this.writeComment("fine-grained control, or to create biomes with a chance of occurring smaller than 1/100.");
      this.writeValue(TCDefaultValues.BiomeRarityScale.name(), this.BiomeRarityScale);
      this.writeNewLine();
      this.writeSmallTitle("Biome lists");
      this.writeComment("Don't forget to register your custom biomes first in CustomBiomes!");
      this.writeNewLine();
      this.writeComment("Biomes which used in normal biome algorithm. Biome name is case sensitive.");
      this.writeValue(TCDefaultValues.NormalBiomes.name(), this.NormalBiomes);
      this.writeNewLine();
      this.writeComment("Biomes which used in ice biome algorithm. Biome name is case sensitive.");
      this.writeValue(TCDefaultValues.IceBiomes.name(), this.IceBiomes);
      this.writeNewLine();
      this.writeComment("Biomes which used as isles. You must set IsleInBiome in biome config for each biome here. Biome name is case sensitive.");
      this.writeValue(TCDefaultValues.IsleBiomes.name(), this.IsleBiomes);
      this.writeNewLine();
      this.writeComment("Biomes which used as borders. You must set BiomeIsBorder in biome config for each biome here. Biome name is case sensitive.");
      this.writeValue(TCDefaultValues.BorderBiomes.name(), this.BorderBiomes);
      this.writeNewLine();
      this.writeSmallTitle("Landmass settings (for NormalBiomes)");
      this.writeComment("Land rarity from 100 to 1. If you set smaller than 90 and LandSize near 0 beware Big oceans.");
      this.writeValue(TCDefaultValues.LandRarity.name(), this.LandRarity);
      this.writeNewLine();
      this.writeComment("Land size from 0 to GenerationDepth.");
      this.writeValue(TCDefaultValues.LandSize.name(), this.LandSize);
      this.writeNewLine();
      this.writeComment("Make land more fuzzy and make lakes. Must be from 0 to GenerationDepth - LandSize");
      this.writeValue(TCDefaultValues.LandFuzzy.name(), this.LandFuzzy);
      this.writeNewLine();
      this.writeSmallTitle("Ice area settings (for IceBiomes)");
      this.writeComment("Ice areas rarity from 100 to 1. If you set smaller than 90 and IceSize near 0 beware ice world");
      this.writeValue(TCDefaultValues.IceRarity.name(), this.IceRarity);
      this.writeNewLine();
      this.writeComment("Ice area size from 0 to GenerationDepth.");
      this.writeValue(TCDefaultValues.IceSize.name(), this.IceSize);
      this.writeNewLine();
      this.writeValue(TCDefaultValues.FrozenOcean.name(), this.FrozenOcean);
      this.writeNewLine();
      this.writeSmallTitle("River settings");
      this.writeComment("River rarity.Must be from 0 to GenerationDepth.");
      this.writeValue(TCDefaultValues.RiverRarity.name(), this.RiverRarity);
      this.writeNewLine();
      this.writeComment("River size from 0 to GenerationDepth - RiverRarity");
      this.writeValue(TCDefaultValues.RiverSize.name(), this.RiverSize);
      this.writeNewLine();
      this.writeValue(TCDefaultValues.RiversEnabled.name(), this.RiversEnabled);
      this.writeNewLine();
      this.writeBigTitle("Settings for BiomeMode:FromImage");
      this.writeComment("Possible modes when generator outside image boundaries: Repeat, ContinueNormal, FillEmpty");
      this.writeComment("   Repeat - repeat image");
      this.writeComment("   ContinueNormal - continue normal generation");
      this.writeComment("   FillEmpty - fill by biome in \"ImageFillBiome settings\" ");
      this.writeValue(TCDefaultValues.ImageMode.name(), this.imageMode.name());
      this.writeNewLine();
      this.writeComment("Source png file for FromImage biome mode.");
      this.writeValue(TCDefaultValues.ImageFile.name(), this.imageFile);
      this.writeNewLine();
      this.writeComment("Biome name for fill outside image boundaries with FillEmpty mode.");
      this.writeValue(TCDefaultValues.ImageFillBiome.name(), this.imageFillBiome);
      this.writeNewLine();
      this.writeComment("Shifts map position from x=0 and z=0 coordinates.");
      this.writeValue(TCDefaultValues.ImageXOffset.name(), this.imageXOffset);
      this.writeValue(TCDefaultValues.ImageZOffset.name(), this.imageZOffset);
      this.writeBigTitle("Terrain Generator Variables");
      this.writeComment("Height bits determinate generation height. Min 5, max 8");
      this.writeComment("For example 7 = 128 height, 8 = 256 height");
      this.writeValue(TCDefaultValues.WorldHeightBits.name(), this.worldHeightBits);
      this.writeNewLine();
      this.writeComment("Set water level. Every empty block under this level will be fill water or another block from WaterBlock ");
      this.writeValue(TCDefaultValues.WaterLevelMax.name(), this.waterLevelMax);
      this.writeValue(TCDefaultValues.WaterLevelMin.name(), this.waterLevelMin);
      this.writeNewLine();
      this.writeComment("BlockId used as water in WaterLevel");
      this.writeValue(TCDefaultValues.WaterBlock.name(), this.waterBlock);
      this.writeNewLine();
      this.writeComment("BlockId used as ice");
      this.writeValue(TCDefaultValues.IceBlock.name(), this.iceBlock);
      this.writeNewLine();
      this.writeComment("Can increase (values greater than 0) or decrease (values less than 0) how much the landscape is fractured horizontally.");
      this.writeValue(TCDefaultValues.FractureHorizontal.name(), this.fractureHorizontal);
      this.writeNewLine();
      this.writeComment("Can increase (values greater than 0) or decrease (values less than 0) how much the landscape is fractured vertically.");
      this.writeComment("Positive values will lead to large cliffs/overhangs, floating islands, and/or a cavern world depending on other settings.");
      this.writeValue(TCDefaultValues.FractureVertical.name(), this.fractureVertical);
      this.writeNewLine();
      this.writeComment("Attempts to replace all surface stone with biome surface block");
      this.writeValue(TCDefaultValues.RemoveSurfaceStone.name(), this.removeSurfaceStone);
      this.writeNewLine();
      this.writeComment("Disable bottom of map bedrock generation");
      this.writeValue(TCDefaultValues.DisableBedrock.name(), this.disableBedrock);
      this.writeNewLine();
      this.writeComment("Enable ceiling of map bedrock generation");
      this.writeValue(TCDefaultValues.CeilingBedrock.name(), this.ceilingBedrock);
      this.writeNewLine();
      this.writeComment("Make bottom layer of bedrock flat");
      this.writeValue(TCDefaultValues.FlatBedrock.name(), this.flatBedrock);
      this.writeNewLine();
      this.writeComment("BlockId used as bedrock");
      this.writeValue(TCDefaultValues.BedrockobBlock.name(), this.bedrockBlock);
      this.writeNewLine();
      this.writeComment("Seed used for the resource generation. Can only be numeric. Leave blank to use the world seed.");
      if (this.resourcesSeed == 0L) {
         this.writeValue(TCDefaultValues.ResourcesSeed.name(), "");
      } else {
         this.writeValue(TCDefaultValues.ResourcesSeed.name(), (float)this.resourcesSeed);
      }

      if (this.objectSpawnRatio != 1) {
         this.writeNewLine();
         this.writeComment("LEGACY setting for compability with old worlds. This setting should be kept at 1.");
         this.writeComment("If the setting is set at 1, the setting will vanish from the config file. Readd it");
         this.writeComment("manually with another value and it will be back.");
         this.writeComment("");
         this.writeComment("When using the UseWorld or UseBiome keyword for spawning custom objects, Terrain Control");
         this.writeComment("spawns one of the possible custom objects. There is of course a chance that");
         this.writeComment("the chosen object cannot spawn. This setting tells TC how many times it should");
         this.writeComment("try to spawn that object.");
         this.writeComment("This setting doesn't affect growing saplings anymore.");
         this.writeValue(TCDefaultValues.objectSpawnRatio.name(), this.objectSpawnRatio);
      }

      this.writeBigTitle("Strongholds");
      this.writeComment("Not much is known about these settings. They are directly passed to the stronghold generator.");
      this.writeValue(TCDefaultValues.StrongholdsEnabled.name(), this.strongholdsEnabled);
      this.writeNewLine();
      this.writeValue(TCDefaultValues.StrongholdCount.name(), this.strongholdCount);
      this.writeNewLine();
      this.writeValue(TCDefaultValues.StrongholdDistance.name(), this.strongholdDistance);
      this.writeNewLine();
      this.writeValue(TCDefaultValues.StrongholdSpread.name(), this.strongholdSpread);
      this.writeBigTitle("Villages");
      this.writeComment("Whether the villages are enabled or not.");
      this.writeValue(TCDefaultValues.VillagesEnabled.name(), this.villagesEnabled);
      this.writeNewLine();
      this.writeComment("The size of the village. Larger is bigger. Normal worlds have 0 as default, superflat worlds 1.");
      this.writeValue(TCDefaultValues.VillageSize.name(), this.villageSize);
      this.writeNewLine();
      this.writeComment("The minimum distance between the village centers in chunks. Minimum value is 9.");
      this.writeValue(TCDefaultValues.VillageDistance.name(), this.villageDistance);
      this.writeBigTitle("Rare buildings");
      this.writeComment("Rare buildings are either desert pyramids, jungle temples or swamp huts.");
      this.writeNewLine();
      this.writeComment("Whether rare buildings are enabled.");
      this.writeValue(TCDefaultValues.RareBuildingsEnabled.name(), this.rareBuildingsEnabled);
      this.writeNewLine();
      this.writeComment("The minimum distance between rare buildings in chunks.");
      this.writeValue(TCDefaultValues.MinimumDistanceBetweenRareBuildings.name(), this.minimumDistanceBetweenRareBuildings);
      this.writeNewLine();
      this.writeComment("The maximum distance between rare buildings in chunks.");
      this.writeValue(TCDefaultValues.MaximumDistanceBetweenRareBuildings.name(), this.maximumDistanceBetweenRareBuildings);
      this.writeBigTitle("Other structures");
      this.writeValue(TCDefaultValues.MineshaftsEnabled.name(), this.mineshaftsEnabled);
      this.writeValue(TCDefaultValues.NetherFortressesEnabled.name(), this.netherFortressesEnabled);
      this.writeBigTitle("Visual settings");
      this.writeComment("Warning this section will work only for players with the single version of Terrain Control installed.");
      this.writeComment("World fog color");
      this.writeColorValue(TCDefaultValues.WorldFog.name(), this.WorldFog);
      this.writeNewLine();
      this.writeComment("World night fog color");
      this.writeColorValue(TCDefaultValues.WorldNightFog.name(), this.WorldNightFog);
      this.writeNewLine();
      this.writeBigTitle("Cave settings");
      this.writeComment("This controls the odds that a given chunk will host a single cave and/or the start of a cave system.");
      this.writeValue(TCDefaultValues.caveRarity.name(), this.caveRarity);
      this.writeNewLine();
      this.writeComment("The number of times the cave generation algorithm will attempt to create single caves and cave");
      this.writeComment("systems in the given chunk. This value is larger because the likelihood for the cave generation");
      this.writeComment("algorithm to bailout is fairly high and it is used in a randomizer that trends towards lower");
      this.writeComment("random numbers. With an input of 40 (default) the randomizer will result in an average random");
      this.writeComment("result of 5 to 6. This can be turned off by setting evenCaveDistribution (below) to true.");
      this.writeValue(TCDefaultValues.caveFrequency.name(), this.caveFrequency);
      this.writeNewLine();
      this.writeComment("Sets the minimum and maximum altitudes at which caves will be generated. These values are");
      this.writeComment("used in a randomizer that trends towards lower numbers so that caves become more frequent");
      this.writeComment("the closer you get to the bottom of the map. Setting even cave distribution (above) to true");
      this.writeComment("will turn off this randomizer and use a flat random number generator that will create an even");
      this.writeComment("density of caves at all altitudes.");
      this.writeValue(TCDefaultValues.caveMinAltitude.name(), this.caveMinAltitude);
      this.writeValue(TCDefaultValues.caveMaxAltitude.name(), this.caveMaxAltitude);
      this.writeNewLine();
      this.writeComment("The odds that the cave generation algorithm will generate a single cavern without an accompanying");
      this.writeComment("cave system. Note that whenever the algorithm generates an individual cave it will also attempt to");
      this.writeComment("generate a pocket of cave systems in the vicinity (no guarantee of connection or that the cave system");
      this.writeComment("will actually be created).");
      this.writeValue(TCDefaultValues.individualCaveRarity.name(), this.individualCaveRarity);
      this.writeNewLine();
      this.writeComment("The number of times the algorithm will attempt to start a cave system in a given chunk per cycle of");
      this.writeComment("the cave generation algorithm (see cave frequency setting above). Note that setting this value too");
      this.writeComment("high with an accompanying high cave frequency value can cause extremely long world generation time.");
      this.writeValue(TCDefaultValues.caveSystemFrequency.name(), this.caveSystemFrequency);
      this.writeNewLine();
      this.writeComment("This can be set to create an additional chance that a cave system pocket (a higher than normal");
      this.writeComment("density of cave systems) being started in a given chunk. Normally, a cave pocket will only be");
      this.writeComment("attempted if an individual cave is generated, but this will allow more cave pockets to be generated");
      this.writeComment("in addition to the individual cave trigger.");
      this.writeValue(TCDefaultValues.caveSystemPocketChance.name(), this.caveSystemPocketChance);
      this.writeNewLine();
      this.writeComment("The minimum and maximum size that a cave system pocket can be. This modifies/overrides the");
      this.writeComment("cave system frequency setting (above) when triggered.");
      this.writeValue(TCDefaultValues.caveSystemPocketMinSize.name(), this.caveSystemPocketMinSize);
      this.writeValue(TCDefaultValues.caveSystemPocketMaxSize.name(), this.caveSystemPocketMaxSize);
      this.writeNewLine();
      this.writeComment("Setting this to true will turn off the randomizer for cave frequency (above). Do note that");
      this.writeComment("if you turn this on you will probably want to adjust the cave frequency down to avoid long");
      this.writeComment("load times at world creation.");
      this.writeValue(TCDefaultValues.evenCaveDistribution.name(), this.evenCaveDistribution);
      this.writeBigTitle("Canyon settings");
      this.writeValue(TCDefaultValues.canyonRarity.name(), this.canyonRarity);
      this.writeValue(TCDefaultValues.canyonMinAltitude.name(), this.canyonMinAltitude);
      this.writeValue(TCDefaultValues.canyonMaxAltitude.name(), this.canyonMaxAltitude);
      this.writeValue(TCDefaultValues.canyonMinLength.name(), this.canyonMinLength);
      this.writeValue(TCDefaultValues.canyonMaxLength.name(), this.canyonMaxLength);
      this.writeValue(TCDefaultValues.canyonDepth.name(), this.canyonDepth);
      this.writeBigTitle("Settings for BiomeMode:OldGenerator");
      this.writeComment("This generator works only with old terrain generator!");
      this.writeValue(TCDefaultValues.oldBiomeSize.name(), this.oldBiomeSize);
      this.writeValue(TCDefaultValues.minMoisture.name(), this.minMoisture);
      this.writeValue(TCDefaultValues.maxMoisture.name(), this.maxMoisture);
      this.writeValue(TCDefaultValues.minTemperature.name(), this.minTemperature);
      this.writeValue(TCDefaultValues.maxTemperature.name(), this.maxTemperature);
   }

   private void WriteCustomBiomes() throws IOException {
      String output = "";
      boolean first = true;

      for(String biome : this.CustomBiomes) {
         if (!first) {
            output = output + ",";
         }

         first = false;
         output = output + biome + ":" + this.CustomBiomeIds.get(biome);
      }

      this.writeValue(TCDefaultValues.CustomBiomes.name(), output);
   }

   public double getFractureHorizontal() {
      return this.fractureHorizontal < (double)0.0F ? (double)1.0F / (Math.abs(this.fractureHorizontal) + (double)1.0F) : this.fractureHorizontal + (double)1.0F;
   }

   public double getFractureVertical() {
      return this.fractureVertical < (double)0.0F ? (double)1.0F / (Math.abs(this.fractureVertical) + (double)1.0F) : this.fractureVertical + (double)1.0F;
   }

   public boolean createAdminium(int y) {
      return !this.disableBedrock && (!this.flatBedrock || y == 0);
   }

   public void Serialize(DataOutputStream stream) throws IOException {
      writeStringToStream(stream, this.WorldName);
      stream.writeInt(this.WorldFog);
      stream.writeInt(this.WorldNightFog);
      stream.writeInt(this.CustomBiomes.size());

      for(String name : this.CustomBiomes) {
         writeStringToStream(stream, name);
         stream.writeInt((Integer)this.CustomBiomeIds.get(name));
      }

      stream.writeInt(this.biomesCount);

      for(BiomeConfig config : this.biomeConfigs) {
         if (config != null) {
            stream.writeInt(config.Biome.getId());
            config.Serialize(stream);
         }
      }

   }

   public WorldConfig(DataInputStream stream, LocalWorld world) throws IOException {
      super();
      this.WorldName = readStringFromStream(stream);
      this.WorldFog = stream.readInt();
      this.WorldNightFog = stream.readInt();
      this.WorldFogR = (float)((this.WorldFog & 16711680) >> 16) / 255.0F;
      this.WorldFogG = (float)((this.WorldFog & '\uff00') >> 8) / 255.0F;
      this.WorldFogB = (float)(this.WorldFog & 255) / 255.0F;
      this.WorldNightFogR = (float)((this.WorldNightFog & 16711680) >> 16) / 255.0F;
      this.WorldNightFogG = (float)((this.WorldNightFog & '\uff00') >> 8) / 255.0F;
      this.WorldNightFogB = (float)(this.WorldNightFog & 255) / 255.0F;
      int count = stream.readInt();

      while(count-- > 0) {
         String name = readStringFromStream(stream);
         int id = stream.readInt();
         world.AddBiome(name, id);
         this.CustomBiomes.add(name);
         this.CustomBiomeIds.put(name, id);
      }

      this.biomeConfigs = new BiomeConfig[world.getMaxBiomesCount()];

      int id;
      BiomeConfig config;
      for(int var6 = stream.readInt(); var6-- > 0; this.biomeConfigs[id] = config) {
         id = stream.readInt();
         config = new BiomeConfig(stream, this, world.getBiomeById(id));
      }

   }

   public static enum TerrainMode {
      Normal,
      OldGenerator,
      TerrainTest,
      NotGenerate,
      Default;

      private TerrainMode() {
      }
   }

   public static enum ImageMode {
      Repeat,
      ContinueNormal,
      FillEmpty;

      private ImageMode() {
      }
   }

   public static enum ConfigMode {
      WriteAll,
      WriteDisable,
      WriteWithoutComments;

      private ConfigMode() {
      }
   }
}
