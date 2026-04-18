package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.biomegenerators.BiomeGenerator;
import com.khorn.terraincontrol.biomegenerators.OldBiomeGenerator;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.Tag;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.customobjects.CustomObjectStructureCache;
import com.khorn.terraincontrol.forge.structuregens.MineshaftGen;
import com.khorn.terraincontrol.forge.structuregens.NetherFortressGen;
import com.khorn.terraincontrol.forge.structuregens.RareBuildingGen;
import com.khorn.terraincontrol.forge.structuregens.StrongholdGen;
import com.khorn.terraincontrol.forge.structuregens.VillageGen;
import com.khorn.terraincontrol.forge.util.NBTHelper;
import com.khorn.terraincontrol.generator.resourcegens.TreeType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenForest;
import net.minecraft.world.gen.feature.WorldGenHugeTrees;
import net.minecraft.world.gen.feature.WorldGenShrub;
import net.minecraft.world.gen.feature.WorldGenSwamp;
import net.minecraft.world.gen.feature.WorldGenTaiga1;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenTrees;

public class SingleWorld implements LocalWorld {
   private ChunkProvider generator;
   private World world;
   private WorldConfig settings;
   private CustomObjectStructureCache structureCache;
   private String name;
   private long seed;
   private BiomeGenerator biomeManager;
   private static int nextBiomeId = 0;
   private static int maxBiomeCount = 256;
   private static Biome[] biomes;
   private static BiomeGenBase[] biomesToRestore;
   private HashMap biomeNames = new HashMap();
   private static ArrayList defaultBiomes;
   public StrongholdGen strongholdGen;
   public VillageGen villageGen;
   public MineshaftGen mineshaftGen;
   public RareBuildingGen rareBuildingGen;
   public NetherFortressGen netherFortressGen;
   private WorldGenDungeons dungeonGen;
   private WorldGenTrees tree;
   private WorldGenTrees cocoaTree;
   private WorldGenBigTree bigTree;
   private WorldGenForest forest;
   private WorldGenSwamp swampTree;
   private WorldGenTaiga1 taigaTree1;
   private WorldGenTaiga2 taigaTree2;
   private WorldGenBigMushroom hugeMushroom;
   private WorldGenHugeTrees jungleTree;
   private WorldGenShrub groundBush;
   private boolean createNewChunks;
   private Chunk[] chunkCache;
   private Chunk cachedChunk;
   private int currentChunkX;
   private int currentChunkZ;
   private BiomeGenBase[] biomeGenBaseArray;
   private int[] biomeIntArray;
   private int worldHeight = 128;
   private int heightBits = 7;

   public static void restoreBiomes() {
      for(BiomeGenBase oldBiome : biomesToRestore) {
         if (oldBiome != null) {
            BiomeGenBase.field_76773_a[oldBiome.field_76756_M] = oldBiome;
         }
      }

      nextBiomeId = 0;
      defaultBiomes.clear();
   }

   public SingleWorld(String _name) {
      super();
      this.name = _name;

      for(int i = 0; i < DefaultBiome.values().length; ++i) {
         BiomeGenBase oldBiome = BiomeGenBase.field_76773_a[i];
         biomesToRestore[i] = oldBiome;
         BiomeGenCustom custom = new BiomeGenCustom(nextBiomeId++, oldBiome.field_76791_y);
         custom.CopyBiome(oldBiome);
         Biome biome = new Biome(custom);
         biomes[biome.getId()] = biome;
         defaultBiomes.add(biome);
         this.biomeNames.put(biome.getName(), biome);
      }

   }

   public LocalBiome getNullBiome(String name) {
      return null;
   }

   public LocalBiome AddBiome(String name, int id) {
      Biome biome = new Biome(new BiomeGenCustom(id, name));
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
         this.biomeGenBaseArray = this.world.field_73011_w.field_76578_c.func_76937_a(this.biomeGenBaseArray, x, z, x_size, z_size);
         if (biomeArray == null || biomeArray.length < x_size * z_size) {
            biomeArray = new int[x_size * z_size];
         }

         for(int i = 0; i < x_size * z_size; ++i) {
            biomeArray[i] = this.biomeGenBaseArray[i].field_76756_M;
         }

         return biomeArray;
      }
   }

   public float[] getTemperatures(int x, int z, int x_size, int z_size) {
      return this.biomeManager != null ? this.biomeManager.getTemperatures((float[])null, x, z, x_size, z_size) : this.world.field_73011_w.field_76578_c.func_76934_b(new float[0], x, z, x_size, z_size);
   }

   public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size) {
      if (this.biomeManager != null) {
         return this.biomeManager.getBiomes(biomeArray, x, z, x_size, z_size);
      } else {
         this.biomeGenBaseArray = this.world.field_73011_w.field_76578_c.func_76931_a(this.biomeGenBaseArray, x, z, x_size, z_size, true);
         if (biomeArray == null || biomeArray.length < x_size * z_size) {
            biomeArray = new int[x_size * z_size];
         }

         for(int i = 0; i < x_size * z_size; ++i) {
            biomeArray[i] = this.biomeGenBaseArray[i].field_76756_M;
         }

         return biomeArray;
      }
   }

   public int getCalculatedBiomeId(int x, int z) {
      return this.biomeManager != null ? this.biomeManager.getBiome(x, z) : this.world.field_73011_w.field_76578_c.func_76935_a(x, z).field_76756_M;
   }

   public double getBiomeFactorForOldBM(int index) {
      OldBiomeGenerator oldBiomeGenerator = (OldBiomeGenerator)this.biomeManager;
      return oldBiomeGenerator.oldTemperature1[index] * oldBiomeGenerator.oldWetness[index];
   }

   public void PrepareTerrainObjects(int x, int z, byte[] chunkArray, boolean dry) {
      if (this.settings.strongholdsEnabled) {
         this.strongholdGen.func_75036_a((IChunkProvider)null, this.world, x, z, chunkArray);
      }

      if (this.settings.mineshaftsEnabled) {
         this.mineshaftGen.func_75036_a((IChunkProvider)null, this.world, x, z, chunkArray);
      }

      if (this.settings.villagesEnabled && dry) {
         this.villageGen.func_75036_a((IChunkProvider)null, this.world, x, z, chunkArray);
      }

      if (this.settings.rareBuildingsEnabled) {
         this.rareBuildingGen.func_75036_a((IChunkProvider)null, this.world, x, z, chunkArray);
      }

      if (this.settings.netherFortressesEnabled) {
         this.netherFortressGen.func_75036_a((IChunkProvider)null, this.world, x, z, chunkArray);
      }

   }

   public void PlaceDungeons(Random rand, int x, int y, int z) {
      this.dungeonGen.func_76484_a(this.world, rand, x, y, z);
   }

   public boolean PlaceTree(TreeType type, Random rand, int x, int y, int z) {
      switch (type) {
         case Tree:
            return this.tree.func_76484_a(this.world, rand, x, y, z);
         case BigTree:
            this.bigTree.func_76487_a((double)1.0F, (double)1.0F, (double)1.0F);
            return this.bigTree.func_76484_a(this.world, rand, x, y, z);
         case Forest:
            return this.forest.func_76484_a(this.world, rand, x, y, z);
         case HugeMushroom:
            this.hugeMushroom.func_76487_a((double)1.0F, (double)1.0F, (double)1.0F);
            return this.hugeMushroom.func_76484_a(this.world, rand, x, y, z);
         case SwampTree:
            return this.swampTree.func_76484_a(this.world, rand, x, y, z);
         case Taiga1:
            return this.taigaTree1.func_76484_a(this.world, rand, x, y, z);
         case Taiga2:
            return this.taigaTree2.func_76484_a(this.world, rand, x, y, z);
         case JungleTree:
            return this.jungleTree.func_76484_a(this.world, rand, x, y, z);
         case GroundBush:
            return this.groundBush.func_76484_a(this.world, rand, x, y, z);
         case CocoaTree:
            return this.cocoaTree.func_76484_a(this.world, rand, x, y, z);
         default:
            return false;
      }
   }

   public boolean PlaceTerrainObjects(Random rand, int chunk_x, int chunk_z) {
      boolean isVillagePlaced = false;
      if (this.settings.strongholdsEnabled) {
         this.strongholdGen.func_75051_a(this.world, rand, chunk_x, chunk_z);
      }

      if (this.settings.mineshaftsEnabled) {
         this.mineshaftGen.func_75051_a(this.world, rand, chunk_x, chunk_z);
      }

      if (this.settings.villagesEnabled) {
         isVillagePlaced = this.villageGen.func_75051_a(this.world, rand, chunk_x, chunk_z);
      }

      if (this.settings.rareBuildingsEnabled) {
         this.rareBuildingGen.func_75051_a(this.world, rand, chunk_x, chunk_z);
      }

      if (this.settings.netherFortressesEnabled) {
         this.netherFortressGen.func_75051_a(this.world, rand, chunk_x, chunk_z);
      }

      return isVillagePlaced;
   }

   public void replaceBlocks() {
      if (this.settings.BiomeConfigsHaveReplacement) {
         Chunk rawChunk = this.chunkCache[0];
         ExtendedBlockStorage[] sectionsArray = rawChunk.func_76587_i();
         byte[] ChunkBiomes = rawChunk.func_76605_m();
         int x = this.currentChunkX * 16;
         int z = this.currentChunkZ * 16;

         for(ExtendedBlockStorage section : sectionsArray) {
            if (section != null) {
               for(int sectionX = 0; sectionX < 16; ++sectionX) {
                  for(int sectionZ = 0; sectionZ < 16; ++sectionZ) {
                     BiomeConfig biomeConfig = this.settings.biomeConfigs[ChunkBiomes[sectionZ << 4 | sectionX] & 255];
                     if (biomeConfig.ReplaceCount > 0) {
                        for(int sectionY = 0; sectionY < 16; ++sectionY) {
                           int blockId = section.func_76656_a(sectionX, sectionY, sectionZ);
                           if (biomeConfig.replaceMatrixBlocks[blockId] != null) {
                              int replaceTo = biomeConfig.replaceMatrixBlocks[blockId][section.func_76662_d() + sectionY];
                              if (replaceTo != -1) {
                                 section.func_76655_a(sectionX, sectionY, sectionZ, replaceTo >> 4);
                                 section.func_76654_b(sectionX, sectionY, sectionZ, replaceTo & 15);
                                 this.world.func_72883_k(x + sectionX, section.func_76662_d() + sectionY, z + sectionZ);
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
         byte[] ChunkBiomes = this.chunkCache[0].func_76605_m();

         for(int i = 0; i < ChunkBiomes.length; ++i) {
            ChunkBiomes[i] = (byte)(this.settings.ReplaceMatrixBiomes[ChunkBiomes[i] & 255] & 255);
         }
      }

   }

   public void placePopulationMobs(BiomeConfig config, Random random, int chunkX, int chunkZ) {
      SpawnerAnimals.func_77191_a(this.getWorld(), ((Biome)config.Biome).getHandle(), chunkX * 16 + 8, chunkZ * 16 + 8, 16, 16, random);
   }

   public void LoadChunk(int x, int z) {
      this.currentChunkX = x;
      this.currentChunkZ = z;
      this.chunkCache[0] = this.world.func_72964_e(x, z);
      this.chunkCache[1] = this.world.func_72964_e(x + 1, z);
      this.chunkCache[2] = this.world.func_72964_e(x, z + 1);
      this.chunkCache[3] = this.world.func_72964_e(x + 1, z + 1);
      this.createNewChunks = true;
   }

   private Chunk getChunk(int x, int y, int z) {
      if (y >= 0 && y < this.worldHeight) {
         x >>= 4;
         z >>= 4;
         if (this.cachedChunk != null && this.cachedChunk.field_76635_g == x && this.cachedChunk.field_76647_h == z) {
            return this.cachedChunk;
         } else {
            int index_x = x - this.currentChunkX;
            int index_z = z - this.currentChunkZ;
            if (index_x != 0 && index_x != 1 || index_z != 0 && index_z != 1) {
               return !this.createNewChunks && !this.world.func_72863_F().func_73149_a(x, z) ? null : (this.cachedChunk = this.world.func_72938_d(x, z));
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
            int id = chunk.func_76610_a(x, y, z);
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
            int id = chunk.func_76610_a(x & 15, y, z & 15);
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
         return chunk.func_76610_a(x, y, z);
      }
   }

   public byte getTypeData(int x, int y, int z) {
      Chunk chunk = this.getChunk(x, y, z);
      if (chunk == null) {
         return 0;
      } else {
         z &= 15;
         x &= 15;
         return (byte)chunk.func_76628_c(x, y, z);
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
            oldBlockId = chunk.func_76610_a(x & 15, y, z & 15);
         }

         if (applyPhysics) {
            chunk.func_76592_a(x & 15, y, z & 15, typeId, data);
         } else {
            boolean oldStatic = this.world.field_72995_K;
            this.world.field_72995_K = true;
            chunk.func_76592_a(x & 15, y, z & 15, typeId, data);
            this.world.field_72995_K = oldStatic;
         }

         if (updateLight) {
            this.world.func_72969_x(x, y, z);
         }

         if (notifyPlayers && !this.world.field_72995_K) {
            this.world.func_72845_h(x, y, z);
         }

         if (!this.world.field_72995_K && applyPhysics) {
            this.world.func_72851_f(x, y, z, oldBlockId);
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
         for(y = chunk.func_76611_b(x, z); chunk.func_76610_a(x, y, z) != DefaultMaterial.AIR.id && y <= this.worldHeight; ++y) {
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
      return this.world.func_72883_k(x, y, z);
   }

   public boolean isLoaded(int x, int y, int z) {
      if (y >= 0 && y < this.worldHeight) {
         x >>= 4;
         z >>= 4;
         return this.world.func_72863_F().func_73149_a(x, z);
      } else {
         return false;
      }
   }

   public WorldConfig getSettings() {
      return this.settings;
   }

   public String getName() {
      return this.name;
   }

   public long getSeed() {
      return this.seed;
   }

   public int getHeight() {
      return this.worldHeight;
   }

   public int getHeightBits() {
      return this.heightBits;
   }

   public ChunkProvider getChunkGenerator() {
      return this.generator;
   }

   public void InitM(World world, WorldConfig config) {
      this.settings = config;
      this.world = world;
      this.seed = world.func_72905_C();

      for(Biome biome : biomes) {
         if (biome != null && config.biomeConfigs[biome.getId()] != null) {
            biome.setEffects(config.biomeConfigs[biome.getId()]);
         }
      }

   }

   public void Init(World world, WorldConfig config) {
      this.settings = config;
      this.world = world;
      this.seed = world.func_72905_C();
      this.structureCache = new CustomObjectStructureCache(this);
      this.dungeonGen = new WorldGenDungeons();
      this.strongholdGen = new StrongholdGen(config);
      this.villageGen = new VillageGen(config);
      this.mineshaftGen = new MineshaftGen();
      this.rareBuildingGen = new RareBuildingGen(config);
      this.netherFortressGen = new NetherFortressGen();
      this.tree = new WorldGenTrees(false);
      this.cocoaTree = new WorldGenTrees(false, 5, 3, 3, true);
      this.bigTree = new WorldGenBigTree(false);
      this.forest = new WorldGenForest(false);
      this.swampTree = new WorldGenSwamp();
      this.taigaTree1 = new WorldGenTaiga1();
      this.taigaTree2 = new WorldGenTaiga2(false);
      this.hugeMushroom = new WorldGenBigMushroom();
      this.jungleTree = new WorldGenHugeTrees(false, 15, 3, 3);
      this.groundBush = new WorldGenShrub(3, 0);
      this.chunkCache = new Chunk[4];
      this.generator = new ChunkProvider(this);
   }

   public void setBiomeManager(BiomeGenerator manager) {
      this.biomeManager = manager;
   }

   public World getWorld() {
      return this.world;
   }

   public void setHeightBits(int heightBits) {
      this.heightBits = heightBits;
      this.worldHeight = 1 << heightBits;
   }

   public void FillChunkForBiomes(Chunk chunk, int x, int z) {
      byte[] arrayOfByte2 = chunk.func_76605_m();
      this.biomeIntArray = this.getBiomes(this.biomeIntArray, x * 16, z * 16, 16, 16);

      for(int i1 = 0; i1 < arrayOfByte2.length; ++i1) {
         arrayOfByte2[i1] = (byte)this.biomeIntArray[i1];
      }

   }

   public LocalBiome getCalculatedBiome(int x, int z) {
      return this.getBiomeById(this.getCalculatedBiomeId(x, z));
   }

   public int getBiomeId(int x, int z) {
      return this.world.func_72807_a(x, z).field_76756_M;
   }

   public LocalBiome getBiome(int x, int z) {
      return this.getBiomeById(this.world.func_72807_a(x, z).field_76756_M);
   }

   public void attachMetadata(int x, int y, int z, Tag tag) {
      NBTTagCompound nmsTag = NBTHelper.getNMSFromNBTTagCompound(tag);
      nmsTag.func_74768_a("x", x);
      nmsTag.func_74768_a("y", y);
      nmsTag.func_74768_a("z", z);
      TileEntity tileEntity = this.world.func_72796_p(x, y, z);
      if (tileEntity != null) {
         tileEntity.func_70307_a(nmsTag);
      } else {
         TerrainControl.log("Skipping tile entity with id " + nmsTag.func_74779_i("id") + ", cannot be placed at " + x + "," + y + "," + z + " on id " + this.world.func_72798_a(x, y, z));
      }

   }

   public Tag getMetadata(int x, int y, int z) {
      TileEntity tileEntity = this.world.func_72796_p(x, y, z);
      if (tileEntity == null) {
         return null;
      } else {
         NBTTagCompound nmsTag = new NBTTagCompound();
         tileEntity.func_70310_b(nmsTag);
         nmsTag.func_82580_o("x");
         nmsTag.func_82580_o("y");
         nmsTag.func_82580_o("z");
         return NBTHelper.getNBTFromNMSTagCompound(nmsTag);
      }
   }

   public CustomObjectStructureCache getStructureCache() {
      return this.structureCache;
   }

   static {
      biomes = new Biome[maxBiomeCount];
      biomesToRestore = new BiomeGenBase[maxBiomeCount];
      defaultBiomes = new ArrayList();
   }
}
