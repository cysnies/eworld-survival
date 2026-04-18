package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigFile;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.customobjects.CustomObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BO3Config extends ConfigFile {
   public File file;
   public String name;
   public Map otherObjectsInDirectory;
   public String author;
   public String description;
   public WorldConfig.ConfigMode settingsMode;
   public boolean tree;
   public int frequency;
   public double rarity;
   public boolean rotateRandomly;
   public BO3Settings.SpawnHeightSetting spawnHeight;
   public int minHeight;
   public int maxHeight;
   public ArrayList excludedBiomes;
   public int sourceBlock;
   public int maxPercentageOutsideSourceBlock;
   public BO3Settings.OutsideSourceBlock outsideSourceBlock;
   public BlockFunction[][] blocks = new BlockFunction[4][];
   public BO3Check[][] bo3Checks = new BO3Check[4][];
   public int maxBranchDepth;
   public BranchFunction[][] branches = new BranchFunction[4][];

   public BO3Config(String name, File file, Map otherObjectsInDirectory) {
      super();
      this.file = file;
      this.name = name;
      this.otherObjectsInDirectory = otherObjectsInDirectory;
      this.readSettingsFile(file);
      this.init();
   }

   public BO3Config(BO3 oldObject, Map extraSettings) {
      super();
      this.file = oldObject.getSettings().file;
      this.name = oldObject.getName();
      this.settingsCache = oldObject.getSettings().settingsCache;
      this.settingsCache.putAll(extraSettings);
      this.settingsCache.put(TCDefaultValues.SettingsMode.toString().toLowerCase(), WorldConfig.ConfigMode.WriteDisable.toString());
      this.init();
   }

   private void init() {
      this.readConfigSettings();
      this.correctSettings();
      if (this.settingsMode != WorldConfig.ConfigMode.WriteDisable) {
         this.writeSettingsFile(this.file, this.settingsMode == WorldConfig.ConfigMode.WriteAll);
      }

      this.rotateBlocksAndChecks();
   }

   public void sayFileNotFound(File file) {
   }

   public Map getSettingsCache() {
      return this.settingsCache;
   }

   protected void writeConfigSettings() throws IOException {
      this.writeBigTitle("BO3 object");
      this.writeComment("This is the config file of a custom object.");
      this.writeComment("If you add this object correctly to your BiomeConfigs, it will spawn in the world.");
      this.writeComment("");
      this.writeComment("This is the creator of this BO3 object");
      this.writeValue("Author", this.author);
      this.writeNewLine();
      this.writeComment("A short description of this BO3 object");
      this.writeValue("Description", this.description);
      this.writeNewLine();
      this.writeComment("The BO3 version, don't change this! It can be used by external applications to do a version check.");
      this.writeValue("Version", 3);
      this.writeNewLine();
      this.writeComment("The settings mode, WriteAll, WriteWithoutComments or WriteDisable. See WorldConfig.");
      this.writeValue("SettingsMode", this.settingsMode.toString());
      this.writeBigTitle("Main settings");
      this.writeComment("This needs to be set to true to spawn the object in the Tree and Sapling resources.");
      this.writeValue("Tree", this.tree);
      this.writeNewLine();
      this.writeComment("The frequency of the BO3 from 1 to 200. Tries this many times to spawn this BO3 when using the CustomObject(...) resource.");
      this.writeComment("Ignored by Tree(..), Sapling(..) and CustomStructure(..)");
      this.writeValue("Frequency", this.frequency);
      this.writeNewLine();
      this.writeComment("The rarity of the BO3 from 0 to 100. Each spawn attempt has rarity% chance to succeed when using the CustomObject(...) resource.");
      this.writeComment("Ignored by Tree(..), Sapling(..) and CustomStructure(..)");
      this.writeValue("Rarity", this.rarity);
      this.writeNewLine();
      this.writeComment("If you set this to true, the BO3 will be placed with a random rotation.");
      this.writeValue("RotateRandomly", this.rotateRandomly);
      this.writeNewLine();
      this.writeComment("The spawn height of the BO3 - randomY, highestBlock or highestSolidBlock.");
      this.writeValue("SpawnHeight", this.spawnHeight.toString());
      this.writeNewLine();
      this.writeComment("The height limits for the BO3.");
      this.writeValue("MinHeight", this.minHeight);
      this.writeValue("MaxHeight", this.maxHeight);
      this.writeNewLine();
      this.writeComment("Objects can have other objects attacthed to it: branches. Branches can also");
      this.writeComment("have branches attached to it, which can also have branches, etc. This is the");
      this.writeComment("maximum branch depth for this objects.");
      this.writeValue("MaxBranchDepth", this.maxBranchDepth);
      this.writeNewLine();
      this.writeComment("When spawned with the UseWorld keyword, this BO3 should NOT spawn in the following biomes.");
      this.writeComment("If you write the BO3 name directly in the BiomeConfigs, this will be ignored.");
      this.writeValue("ExcludedBiomes", this.excludedBiomes);
      this.writeBigTitle("Source block settings");
      this.writeComment("The block the BO3 should spawn in");
      this.writeValue("SourceBlock", this.sourceBlock);
      this.writeNewLine();
      this.writeComment("The maximum percentage of the BO3 that can be outside the SourceBlock.");
      this.writeComment("The BO3 won't be placed on a location with more blocks outside the SourceBlock than this percentage.");
      this.writeValue("MaxPercentageOutsideSourceBlock", this.maxPercentageOutsideSourceBlock);
      this.writeNewLine();
      this.writeComment("What to do when a block is about to be placed outside the SourceBlock? (dontPlace, placeAnyway)");
      this.writeValue("OutsideSourceBlock", this.outsideSourceBlock.toString());
      this.writeResources();
   }

   protected void readConfigSettings() {
      this.author = (String)this.readSettings(BO3Settings.author);
      this.description = (String)this.readSettings(BO3Settings.description);
      this.settingsMode = (WorldConfig.ConfigMode)this.readSettings(TCDefaultValues.SettingsMode);
      this.tree = (Boolean)this.readSettings(BO3Settings.tree);
      this.frequency = (Integer)this.readSettings(BO3Settings.frequency);
      this.rarity = (Double)this.readSettings(BO3Settings.rarity);
      this.rotateRandomly = (Boolean)this.readSettings(BO3Settings.rotateRandomly);
      this.spawnHeight = (BO3Settings.SpawnHeightSetting)this.readSettings(BO3Settings.spawnHeight);
      this.minHeight = (Integer)this.readSettings(BO3Settings.minHeight);
      this.maxHeight = (Integer)this.readSettings(BO3Settings.maxHeight);
      this.maxBranchDepth = (Integer)this.readSettings(BO3Settings.maxBranchDepth);
      this.excludedBiomes = (ArrayList)this.readSettings(BO3Settings.excludedBiomes);
      this.sourceBlock = (Integer)this.readSettings(BO3Settings.sourceBlock);
      this.maxPercentageOutsideSourceBlock = (Integer)this.readSettings(BO3Settings.maxPercentageOutsideSourceBlock);
      this.outsideSourceBlock = (BO3Settings.OutsideSourceBlock)this.readSettings(BO3Settings.outsideSourceBlock);
      this.readResources();
   }

   private void readResources() {
      List<BlockFunction> tempBlocksList = new ArrayList();
      List<BO3Check> tempChecksList = new ArrayList();
      List<BranchFunction> tempBranchesList = new ArrayList();

      for(Map.Entry entry : this.settingsCache.entrySet()) {
         String key = (String)entry.getKey();
         int start = key.indexOf("(");
         int end = key.lastIndexOf(")");
         if (start != -1 && end != -1) {
            String name = key.substring(0, start);
            String[] props = readComplexString(key.substring(start + 1, end));
            ConfigFunction<BO3Config> res = TerrainControl.getConfigFunctionsManager().getConfigFunction(name, this, this.name + " on line " + (String)entry.getValue(), Arrays.asList(props));
            if (res != null && res.isValid()) {
               if (res instanceof BlockFunction) {
                  tempBlocksList.add(res);
               } else if (res instanceof BO3Check) {
                  tempChecksList.add(res);
               } else if (res instanceof BranchFunction) {
                  tempBranchesList.add(res);
               }
            }
         }
      }

      this.blocks[0] = (BlockFunction[])tempBlocksList.toArray(new BlockFunction[tempBlocksList.size()]);
      this.bo3Checks[0] = (BO3Check[])tempChecksList.toArray(new BO3Check[tempChecksList.size()]);
      this.branches[0] = (BranchFunction[])tempBranchesList.toArray(new BranchFunction[tempBranchesList.size()]);
   }

   public void writeResources() throws IOException {
      this.writeBigTitle("Blocks");
      this.writeComment("All the blocks used in the BO3 are listed here. Possible blocks:");
      this.writeComment("Block(x,y,z,id[.data][,nbtfile.nbt)");
      this.writeComment("RandomBlock(x,y,z,id[:data][,nbtfile.nbt],chance[,id[:data][,nbtfile.nbt],chance[,...]])");
      this.writeComment("So RandomBlock(0,0,0,CHEST,chest.nbt,50,CHEST,anotherchest.nbt,100) will spawn a chest at");
      this.writeComment("the BO3 origin, and give it a 50% chance to have the contents of chest.nbt, or, if that");
      this.writeComment("fails, a 100% percent chance to have the contents of anotherchest.nbt.");

      for(BlockFunction block : this.blocks[0]) {
         this.writeValue(block.write());
      }

      this.writeBigTitle("BO3 checks");
      this.writeComment("Require a condition at a certain location in order for the BO3 to be spawned.");
      this.writeComment("BlockCheck(x,y,z,id[:data][,id[:data][,...]])");
      this.writeComment("LightCheck(x,y,z,minLightLevel,maxLightLevel)");

      for(BO3Check check : this.bo3Checks[0]) {
         this.writeValue(check.write());
      }

      this.writeBigTitle("Branches");
      this.writeComment("Branches are objects that will spawn when this object spawns when it is used in");
      this.writeComment("the CustomStructure resource. Branches can also have branches, making complex");
      this.writeComment("structures possible.");
      this.writeComment("Branch(x,y,z,branchName,rotation,chance[,anotherBranchName,rotation,chance[,...]])");
      this.writeComment("branchName - name of the object to spawn.");
      this.writeComment("rotation - NORTH, SOUTH, EAST or WEST.");

      for(BranchFunction branch : this.branches[0]) {
         this.writeValue(branch.makeString());
      }

   }

   protected void correctSettings() {
      this.frequency = this.applyBounds(this.frequency, 1, 200);
      this.rarity = this.applyBounds(this.rarity, 1.0E-6, (double)100.0F);
      this.minHeight = this.applyBounds(this.minHeight, TerrainControl.worldDepth, TerrainControl.worldHeight - 1);
      this.maxHeight = this.applyBounds(this.maxHeight, this.minHeight, TerrainControl.worldHeight);
      this.maxBranchDepth = this.applyBounds(this.maxBranchDepth, 1, Integer.MAX_VALUE);
      this.sourceBlock = this.applyBounds(this.sourceBlock, 0, TerrainControl.supportedBlockIds);
      this.maxPercentageOutsideSourceBlock = this.applyBounds(this.maxPercentageOutsideSourceBlock, 0, 100);
   }

   protected void renameOldSettings() {
   }

   public void rotateBlocksAndChecks() {
      for(int i = 1; i < 4; ++i) {
         this.blocks[i] = new BlockFunction[this.blocks[i - 1].length];

         for(int j = 0; j < this.blocks[i].length; ++j) {
            this.blocks[i][j] = this.blocks[i - 1][j].rotate();
         }

         this.bo3Checks[i] = new BO3Check[this.bo3Checks[i - 1].length];

         for(int j = 0; j < this.bo3Checks[i].length; ++j) {
            this.bo3Checks[i][j] = this.bo3Checks[i - 1][j].rotate();
         }

         this.branches[i] = new BranchFunction[this.branches[i - 1].length];

         for(int j = 0; j < this.branches[i].length; ++j) {
            this.branches[i][j] = this.branches[i - 1][j].rotate();
         }
      }

   }
}
