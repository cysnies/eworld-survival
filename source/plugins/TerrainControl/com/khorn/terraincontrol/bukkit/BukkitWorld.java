package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.biomegenerators.BiomeGenerator;
import com.khorn.terraincontrol.biomegenerators.OldBiomeGenerator;
import com.khorn.terraincontrol.bukkit.structuregens.MineshaftGen;
import com.khorn.terraincontrol.bukkit.structuregens.NetherFortressGen;
import com.khorn.terraincontrol.bukkit.structuregens.RareBuildingGen;
import com.khorn.terraincontrol.bukkit.structuregens.StrongholdGen;
import com.khorn.terraincontrol.bukkit.structuregens.VillageGen;
import com.khorn.terraincontrol.bukkit.util.NBTHelper;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.Tag;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.customobjects.CustomObjectStructureCache;
import com.khorn.terraincontrol.generator.resourcegens.TreeType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import net.minecraft.server.v1_6_R2.BiomeBase;
import net.minecraft.server.v1_6_R2.Chunk;
import net.minecraft.server.v1_6_R2.ChunkSection;
import net.minecraft.server.v1_6_R2.NBTTagCompound;
import net.minecraft.server.v1_6_R2.SpawnerCreature;
import net.minecraft.server.v1_6_R2.TileEntity;
import net.minecraft.server.v1_6_R2.World;
import net.minecraft.server.v1_6_R2.WorldGenBigTree;
import net.minecraft.server.v1_6_R2.WorldGenDungeons;
import net.minecraft.server.v1_6_R2.WorldGenForest;
import net.minecraft.server.v1_6_R2.WorldGenGroundBush;
import net.minecraft.server.v1_6_R2.WorldGenHugeMushroom;
import net.minecraft.server.v1_6_R2.WorldGenMegaTree;
import net.minecraft.server.v1_6_R2.WorldGenSwampTree;
import net.minecraft.server.v1_6_R2.WorldGenTaiga1;
import net.minecraft.server.v1_6_R2.WorldGenTaiga2;
import net.minecraft.server.v1_6_R2.WorldGenTrees;
import net.minecraft.server.v1_6_R2.WorldServer;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;

public class BukkitWorld implements LocalWorld {
   private boolean initialized;
   private TCChunkGenerator generator;
   private WorldServer world;
   private WorldConfig settings;
   private CustomObjectStructureCache structureCache;
   private String name;
   private BiomeGenerator biomeManager;
   private static int nextBiomeId = DefaultBiome.values().length;
   private static int maxBiomeCount = 256;
   private static LocalBiome[] biomes;
   private HashMap biomeNames = new HashMap();
   private static ArrayList defaultBiomes;
   public StrongholdGen strongholdGen;
   public VillageGen villageGen;
   public MineshaftGen mineshaftGen;
   public RareBuildingGen pyramidsGen;
   public NetherFortressGen netherFortress;
   private WorldGenTrees tree;
   private WorldGenTrees cocoaTree;
   private WorldGenBigTree bigTree;
   private WorldGenForest forest;
   private WorldGenSwampTree swampTree;
   private WorldGenTaiga1 taigaTree1;
   private WorldGenTaiga2 taigaTree2;
   private WorldGenHugeMushroom hugeMushroom;
   private WorldGenMegaTree jungleTree;
   private WorldGenGroundBush groundBush;
   private boolean createNewChunks;
   private Chunk[] chunkCache;
   private Chunk cachedChunk;
   private int currentChunkX;
   private int currentChunkZ;
   private BiomeBase[] biomeBaseArray;
   private int worldHeight = 256;
   private int heightBits = 8;
   private int customBiomesCount = 21;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$khorn$terraincontrol$generator$resourcegens$TreeType;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$khorn$terraincontrol$configuration$WorldConfig$TerrainMode;

   static {
      biomes = new LocalBiome[maxBiomeCount];
      defaultBiomes = new ArrayList();

      for(int i = 0; i < DefaultBiome.values().length; ++i) {
         biomes[i] = new BukkitBiome(BiomeBase.biomes[i]);
         defaultBiomes.add(biomes[i]);
      }

   }

   public BukkitWorld(String _name) {
      super();
      this.name = _name;

      for(LocalBiome biome : defaultBiomes) {
         this.biomeNames.put(biome.getName(), biome);
      }

   }

   public LocalBiome getNullBiome(String name) {
      return new NullBiome(name);
   }

   public LocalBiome AddBiome(String name, int id) {
      BukkitBiome biome = new BukkitBiome(new CustomBiome(id, name));
      biome.setCustomID(this.customBiomesCount++);
      biomes[biome.getId()] = biome;
      this.biomeNames.put(biome.getName(), biome);
      return biome;
   }

   public int getMaxBiomesCount() {
      return maxBiomeCount;
   }

   public int getFreeBiomeId() {
      return nextBiomeId++;
   }

   public LocalBiome getBiomeById(int id) {
      return biomes[id];
   }

   public int getBiomeIdByName(String name) {
      return ((LocalBiome)this.biomeNames.get(name)).getId();
   }

   public ArrayList getDefaultBiomes() {
      return defaultBiomes;
   }

   public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int x_size, int z_size) {
      if (this.biomeManager != null) {
         return this.biomeManager.getBiomesUnZoomed(biomeArray, x, z, x_size, z_size);
      } else {
         this.biomeBaseArray = this.world.worldProvider.e.getBiomes(this.biomeBaseArray, x, z, x_size, z_size);
         if (biomeArray == null || biomeArray.length < x_size * z_size) {
            biomeArray = new int[x_size * z_size];
         }

         for(int i = 0; i < x_size * z_size; ++i) {
            biomeArray[i] = this.biomeBaseArray[i].id;
         }

         return biomeArray;
      }
   }

   public float[] getTemperatures(int x, int z, int x_size, int z_size) {
      return this.biomeManager != null ? this.biomeManager.getTemperatures((float[])null, x, z, x_size, z_size) : this.world.worldProvider.e.getTemperatures((float[])null, x, z, x_size, z_size);
   }

   public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size) {
      if (this.biomeManager != null) {
         return this.biomeManager.getBiomes(biomeArray, x, z, x_size, z_size);
      } else {
         this.biomeBaseArray = this.world.worldProvider.e.a(this.biomeBaseArray, x, z, x_size, z_size, true);
         if (biomeArray == null || biomeArray.length < x_size * z_size) {
            biomeArray = new int[x_size * z_size];
         }

         for(int i = 0; i < x_size * z_size; ++i) {
            biomeArray[i] = this.biomeBaseArray[i].id;
         }

         return biomeArray;
      }
   }

   public int getCalculatedBiomeId(int x, int z) {
      return this.biomeManager != null ? this.biomeManager.getBiome(x, z) : this.world.worldProvider.e.getBiome(x, z).id;
   }

   public double getBiomeFactorForOldBM(int index) {
      OldBiomeGenerator oldBiomeGenerator = (OldBiomeGenerator)this.biomeManager;
      return oldBiomeGenerator.oldTemperature1[index] * oldBiomeGenerator.oldWetness[index];
   }

   public void PrepareTerrainObjects(int chunkX, int chunkZ, byte[] chunkArray, boolean dry) {
      if (this.settings.strongholdsEnabled) {
         this.strongholdGen.prepare(this.world, chunkX, chunkZ, chunkArray);
      }

      if (this.settings.mineshaftsEnabled) {
         this.mineshaftGen.prepare(this.world, chunkX, chunkZ, chunkArray);
      }

      if (this.settings.villagesEnabled && dry) {
         this.villageGen.prepare(this.world, chunkX, chunkZ, chunkArray);
      }

      if (this.settings.rareBuildingsEnabled) {
         this.pyramidsGen.prepare(this.world, chunkX, chunkZ, chunkArray);
      }

      if (this.settings.netherFortressesEnabled) {
         this.netherFortress.prepare(this.world, chunkX, chunkZ, chunkArray);
      }

   }

   public void PlaceDungeons(Random rand, int x, int y, int z) {
      (new WorldGenDungeons()).a(this.world, rand, x, y, z);
   }

   public boolean PlaceTree(TreeType type, Random rand, int x, int y, int z) {
      switch (type) {
         case Tree:
            return this.tree.a(this.world, rand, x, y, z);
         case BigTree:
            this.bigTree.a((double)1.0F, (double)1.0F, (double)1.0F);
            return this.bigTree.a(this.world, rand, x, y, z);
         case Forest:
            return this.forest.a(this.world, rand, x, y, z);
         case HugeMushroom:
            this.hugeMushroom.a((double)1.0F, (double)1.0F, (double)1.0F);
            return this.hugeMushroom.a(this.world, rand, x, y, z);
         case SwampTree:
            return this.swampTree.a(this.world, rand, x, y, z);
         case Taiga1:
            return this.taigaTree1.a(this.world, rand, x, y, z);
         case Taiga2:
            return this.taigaTree2.a(this.world, rand, x, y, z);
         case JungleTree:
            return this.jungleTree.a(this.world, rand, x, y, z);
         case GroundBush:
            return this.groundBush.a(this.world, rand, x, y, z);
         case CocoaTree:
            return this.cocoaTree.a(this.world, rand, x, y, z);
         default:
            return false;
      }
   }

   public boolean PlaceTerrainObjects(Random random, int chunkX, int chunkZ) {
      boolean villageGenerated = false;
      if (this.settings.strongholdsEnabled) {
         this.strongholdGen.place(this.world, random, chunkX, chunkZ);
      }

      if (this.settings.mineshaftsEnabled) {
         this.mineshaftGen.place(this.world, random, chunkX, chunkZ);
      }

      if (this.settings.villagesEnabled) {
         villageGenerated = this.villageGen.place(this.world, random, chunkX, chunkZ);
      }

      if (this.settings.rareBuildingsEnabled) {
         this.pyramidsGen.place(this.world, random, chunkX, chunkZ);
      }

      if (this.settings.netherFortressesEnabled) {
         this.netherFortress.place(this.world, random, chunkX, chunkZ);
      }

      return villageGenerated;
   }

   public void replaceBlocks() {
      if (this.settings.BiomeConfigsHaveReplacement) {
         Chunk rawChunk = this.chunkCache[0];
         ChunkSection[] sectionsArray = rawChunk.i();
         byte[] ChunkBiomes = rawChunk.m();
         int x = this.currentChunkX * 16;
         int z = this.currentChunkZ * 16;

         for(ChunkSection section : sectionsArray) {
            if (section != null) {
               for(int sectionX = 0; sectionX < 16; ++sectionX) {
                  for(int sectionZ = 0; sectionZ < 16; ++sectionZ) {
                     BiomeConfig biomeConfig = this.settings.biomeConfigs[ChunkBiomes[sectionZ << 4 | sectionX] & 255];
                     if (biomeConfig != null && biomeConfig.ReplaceCount > 0) {
                        for(int sectionY = 0; sectionY < 16; ++sectionY) {
                           int blockId = section.getTypeId(sectionX, sectionY, sectionZ);
                           if (biomeConfig.replaceMatrixBlocks[blockId] != null) {
                              int replaceTo = biomeConfig.replaceMatrixBlocks[blockId][section.getYPosition() + sectionY];
                              if (replaceTo != -1) {
                                 section.setTypeId(sectionX, sectionY, sectionZ, replaceTo >> 4);
                                 section.setData(sectionX, sectionY, sectionZ, replaceTo & 15);
                                 this.world.notify(x + sectionX, section.getYPosition() + sectionY, z + sectionZ);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

   }

   public void replaceBiomes() {
      if (this.settings.HaveBiomeReplace) {
         byte[] ChunkBiomes = this.chunkCache[0].m();

         for(int i = 0; i < ChunkBiomes.length; ++i) {
            ChunkBiomes[i] = (byte)(this.settings.ReplaceMatrixBiomes[ChunkBiomes[i] & 255] & 255);
         }
      }

   }

   public void placePopulationMobs(BiomeConfig config, Random random, int chunkX, int chunkZ) {
      SpawnerCreature.a(this.world, ((BukkitBiome)config.Biome).getHandle(), chunkX * 16 + 8, chunkZ * 16 + 8, 16, 16, random);
   }

   public void LoadChunk(Chunk chunk) {
      this.currentChunkX = chunk.x;
      this.currentChunkZ = chunk.z;
      this.chunkCache[0] = chunk;
      this.chunkCache[1] = this.world.getChunkAt(chunk.x + 1, chunk.z);
      this.chunkCache[2] = this.world.getChunkAt(chunk.x, chunk.z + 1);
      this.chunkCache[3] = this.world.getChunkAt(chunk.x + 1, chunk.z + 1);
      this.createNewChunks = true;
   }

   private Chunk getChunk(int x, int y, int z) {
      if (y >= 0 && y < this.worldHeight) {
         x >>= 4;
         z >>= 4;
         if (this.cachedChunk != null && this.cachedChunk.x == x && this.cachedChunk.z == z) {
            return this.cachedChunk;
         } else {
            int index_x = x - this.currentChunkX;
            int index_z = z - this.currentChunkZ;
            if (index_x != 0 && index_x != 1 || index_z != 0 && index_z != 1) {
               return !this.createNewChunks && !this.world.chunkProvider.isChunkLoaded(x, z) ? null : (this.cachedChunk = this.world.getChunkAt(x, z));
            } else {
               return this.cachedChunk = this.chunkCache[index_x | index_z << 1];
            }
         }
      } else {
         return null;
      }
   }

   public int getLiquidHeight(int x, int z) {
      Chunk chunk = this.getChunk(x, 0, z);
      if (chunk == null) {
         return -1;
      } else {
         z &= 15;
         x &= 15;

         for(int y = this.worldHeight - 1; y > 0; --y) {
            int id = chunk.getTypeId(x, y, z);
            if (DefaultMaterial.getMaterial(id).isLiquid()) {
               return y;
            }
         }

         return -1;
      }
   }

   public int getSolidHeight(int x, int z) {
      Chunk chunk = this.getChunk(x, 0, z);
      if (chunk == null) {
         return -1;
      } else {
         for(int y = this.getHighestBlockYAt(x, z) - 1; y > 0; --y) {
            int id = chunk.getTypeId(x & 15, y, z & 15);
            if (DefaultMaterial.getMaterial(id).isSolid()) {
               return y + 1;
            }
         }

         return -1;
      }
   }

   public boolean isEmpty(int x, int y, int z) {
      return this.getTypeId(x, y, z) == 0;
   }

   public int getTypeId(int x, int y, int z) {
      Chunk chunk = this.getChunk(x, y, z);
      if (chunk == null) {
         return 0;
      } else {
         z &= 15;
         x &= 15;
         return chunk.getTypeId(x, y, z);
      }
   }

   public byte getTypeData(int x, int y, int z) {
      Chunk chunk = this.getChunk(x, y, z);
      if (chunk == null) {
         return 0;
      } else {
         z &= 15;
         x &= 15;
         return (byte)chunk.getData(x, y, z);
      }
   }

   public void setBlock(int x, int y, int z, int typeId, int data, boolean updateLight, boolean applyPhysics, boolean notifyPlayers) {
      if (this.isLoaded(x, y, z)) {
         if (y < TerrainControl.worldDepth || y >= TerrainControl.worldHeight) {
            return;
         }

         Chunk chunk = this.getChunk(x, y, z);
         if (chunk == null) {
            return;
         }

         int oldBlockId = 0;
         if (applyPhysics) {
            oldBlockId = chunk.getTypeId(x & 15, y, z & 15);
         }

         if (applyPhysics) {
            chunk.a(x & 15, y, z & 15, typeId, data);
         } else {
            boolean oldStatic = this.world.isStatic;
            this.world.isStatic = true;
            chunk.a(x & 15, y, z & 15, typeId, data);
            this.world.isStatic = oldStatic;
         }

         if (updateLight) {
            this.world.A(x, y, z);
         }

         if (notifyPlayers && !this.world.isStatic) {
            this.world.notify(x, y, z);
         }

         if (!this.world.isStatic && applyPhysics) {
            this.world.update(x, y, z, oldBlockId);
         }
      }

   }

   public void setBlock(int x, int y, int z, int typeId, int data) {
      this.setBlock(x, y, z, typeId, data, true, false, true);
   }

   public int getHighestBlockYAt(int x, int z) {
      Chunk chunk = this.getChunk(x, 0, z);
      if (chunk == null) {
         return -1;
      } else {
         z &= 15;
         x &= 15;

         int y;
         for(y = chunk.b(x, z); chunk.getTypeId(x, y, z) != DefaultMaterial.AIR.id && y <= this.worldHeight; ++y) {
         }

         return y;
      }
   }

   public DefaultMaterial getMaterial(int x, int y, int z) {
      int id = this.getTypeId(x, y, z);
      return DefaultMaterial.getMaterial(id);
   }

   public void setChunksCreations(boolean createNew) {
      this.createNewChunks = createNew;
   }

   public int getLightLevel(int x, int y, int z) {
      return this.world.m(x, y, z);
   }

   public boolean isLoaded(int x, int y, int z) {
      return this.world.isLoaded(x, y, z);
   }

   public WorldConfig getSettings() {
      return this.settings;
   }

   public String getName() {
      return this.name;
   }

   public long getSeed() {
      return this.world.getSeed();
   }

   public int getHeight() {
      return this.worldHeight;
   }

   public int getHeightBits() {
      return this.heightBits;
   }

   public TCChunkGenerator getChunkGenerator() {
      return this.generator;
   }

   public World getWorld() {
      return this.world;
   }

   public void setSettings(WorldConfig worldConfig) {
      if (this.settings != null) {
         this.settings.newSettings = worldConfig;
         this.settings.isDeprecated = true;
      }

      this.settings = worldConfig;
   }

   public void enable(org.bukkit.World world) {
      WorldServer mcWorld = ((CraftWorld)world).getHandle();
      this.world = mcWorld;
      this.chunkCache = new Chunk[4];
      if (mcWorld.worldProvider.getName().equals("Overworld")) {
         mcWorld.worldProvider = new TCWorldProvider(this, this.world.worldProvider);
      }

      Class<? extends BiomeGenerator> biomeModeClass = this.settings.biomeMode;
      if (biomeModeClass != TerrainControl.getBiomeModeManager().VANILLA) {
         TCWorldChunkManager worldChunkManager = new TCWorldChunkManager(this);
         mcWorld.worldProvider.e = worldChunkManager;
         BiomeGenerator biomeManager = TerrainControl.getBiomeModeManager().create(biomeModeClass, this, new BiomeCacheWrapper(worldChunkManager));
         worldChunkManager.setBiomeManager(biomeManager);
         this.setBiomeManager(biomeManager);
      }

      if (!this.initialized) {
         this.structureCache = new CustomObjectStructureCache(this);
         switch (this.settings.ModeTerrain) {
            case Normal:
            case OldGenerator:
               this.strongholdGen = new StrongholdGen(this.settings);
               this.villageGen = new VillageGen(this.settings);
               this.mineshaftGen = new MineshaftGen();
               this.pyramidsGen = new RareBuildingGen(this.settings);
               this.netherFortress = new NetherFortressGen();
            case NotGenerate:
               this.tree = new WorldGenTrees(false);
               this.cocoaTree = new WorldGenTrees(false, 5, 3, 3, true);
               this.bigTree = new WorldGenBigTree(false);
               this.forest = new WorldGenForest(false);
               this.swampTree = new WorldGenSwampTree();
               this.taigaTree1 = new WorldGenTaiga1();
               this.taigaTree2 = new WorldGenTaiga2(false);
               this.hugeMushroom = new WorldGenHugeMushroom();
               this.jungleTree = new WorldGenMegaTree(false, 15, 3, 3);
               this.groundBush = new WorldGenGroundBush(3, 0);
            case TerrainTest:
               this.generator.Init(this);
            case Default:
            default:
               this.initialized = true;
         }
      } else {
         this.structureCache.reload(this);
      }

   }

   public void disable() {
      if (this.world.worldProvider instanceof TCWorldProvider) {
         this.world.worldProvider = ((TCWorldProvider)this.world.worldProvider).getOldWorldProvider();
      }

   }

   public void setChunkGenerator(TCChunkGenerator _generator) {
      this.generator = _generator;
   }

   public void setBiomeManager(BiomeGenerator manager) {
      this.biomeManager = manager;
   }

   public void setHeightBits(int heightBits) {
      this.heightBits = heightBits;
      this.worldHeight = 1 << heightBits;
   }

   public LocalBiome getCalculatedBiome(int x, int z) {
      return this.getBiomeById(this.getCalculatedBiomeId(x, z));
   }

   public int getBiomeId(int x, int z) {
      return this.world.getBiome(x, z).id;
   }

   public LocalBiome getBiome(int x, int z) {
      return this.getBiomeById(this.world.getBiome(x, z).id);
   }

   public void attachMetadata(int x, int y, int z, Tag tag) {
      NBTTagCompound nmsTag = NBTHelper.getNMSFromNBTTagCompound(tag);
      nmsTag.setInt("x", x);
      nmsTag.setInt("y", y);
      nmsTag.setInt("z", z);
      TileEntity tileEntity = this.world.getTileEntity(x, y, z);
      if (tileEntity != null) {
         tileEntity.a(nmsTag);
      } else {
         TerrainControl.log("Skipping tile entity with id " + nmsTag.getString("id") + ", cannot be placed at " + x + "," + y + "," + z + " on id " + this.world.getTypeId(x, y, z));
      }

   }

   public Tag getMetadata(int x, int y, int z) {
      TileEntity tileEntity = this.world.getTileEntity(x, y, z);
      if (tileEntity == null) {
         return null;
      } else {
         NBTTagCompound nmsTag = new NBTTagCompound();
         tileEntity.b(nmsTag);
         nmsTag.remove("x");
         nmsTag.remove("y");
         nmsTag.remove("z");
         return NBTHelper.getNBTFromNMSTagCompound(nmsTag);
      }
   }

   public CustomObjectStructureCache getStructureCache() {
      return this.structureCache;
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$khorn$terraincontrol$generator$resourcegens$TreeType() {
      int[] var10000 = $SWITCH_TABLE$com$khorn$terraincontrol$generator$resourcegens$TreeType;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[TreeType.values().length];

         try {
            var0[TreeType.BigTree.ordinal()] = 2;
         } catch (NoSuchFieldError var10) {
         }

         try {
            var0[TreeType.CocoaTree.ordinal()] = 10;
         } catch (NoSuchFieldError var9) {
         }

         try {
            var0[TreeType.Forest.ordinal()] = 3;
         } catch (NoSuchFieldError var8) {
         }

         try {
            var0[TreeType.GroundBush.ordinal()] = 9;
         } catch (NoSuchFieldError var7) {
         }

         try {
            var0[TreeType.HugeMushroom.ordinal()] = 4;
         } catch (NoSuchFieldError var6) {
         }

         try {
            var0[TreeType.JungleTree.ordinal()] = 8;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[TreeType.SwampTree.ordinal()] = 5;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[TreeType.Taiga1.ordinal()] = 6;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[TreeType.Taiga2.ordinal()] = 7;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[TreeType.Tree.ordinal()] = 1;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$khorn$terraincontrol$generator$resourcegens$TreeType = var0;
         return var0;
      }
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$khorn$terraincontrol$configuration$WorldConfig$TerrainMode() {
      int[] var10000 = $SWITCH_TABLE$com$khorn$terraincontrol$configuration$WorldConfig$TerrainMode;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[WorldConfig.TerrainMode.values().length];

         try {
            var0[WorldConfig.TerrainMode.Default.ordinal()] = 5;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[WorldConfig.TerrainMode.Normal.ordinal()] = 1;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[WorldConfig.TerrainMode.NotGenerate.ordinal()] = 4;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[WorldConfig.TerrainMode.OldGenerator.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[WorldConfig.TerrainMode.TerrainTest.ordinal()] = 3;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$khorn$terraincontrol$configuration$WorldConfig$TerrainMode = var0;
         return var0;
      }
   }
}
