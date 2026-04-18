package com.khorn.terraincontrol;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.Tag;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.customobjects.CustomObjectStructureCache;
import com.khorn.terraincontrol.generator.resourcegens.TreeType;
import java.util.ArrayList;
import java.util.Random;

public interface LocalWorld {
   LocalBiome AddBiome(String var1, int var2);

   LocalBiome getNullBiome(String var1);

   int getMaxBiomesCount();

   int getFreeBiomeId();

   LocalBiome getBiomeById(int var1);

   int getBiomeIdByName(String var1);

   ArrayList getDefaultBiomes();

   int[] getBiomesUnZoomed(int[] var1, int var2, int var3, int var4, int var5);

   float[] getTemperatures(int var1, int var2, int var3, int var4);

   int[] getBiomes(int[] var1, int var2, int var3, int var4, int var5);

   int getCalculatedBiomeId(int var1, int var2);

   LocalBiome getCalculatedBiome(int var1, int var2);

   int getBiomeId(int var1, int var2);

   LocalBiome getBiome(int var1, int var2);

   double getBiomeFactorForOldBM(int var1);

   void PrepareTerrainObjects(int var1, int var2, byte[] var3, boolean var4);

   void PlaceDungeons(Random var1, int var2, int var3, int var4);

   boolean PlaceTree(TreeType var1, Random var2, int var3, int var4, int var5);

   boolean PlaceTerrainObjects(Random var1, int var2, int var3);

   void replaceBlocks();

   void replaceBiomes();

   void placePopulationMobs(BiomeConfig var1, Random var2, int var3, int var4);

   int getTypeId(int var1, int var2, int var3);

   byte getTypeData(int var1, int var2, int var3);

   boolean isEmpty(int var1, int var2, int var3);

   void setBlock(int var1, int var2, int var3, int var4, int var5, boolean var6, boolean var7, boolean var8);

   void setBlock(int var1, int var2, int var3, int var4, int var5);

   void attachMetadata(int var1, int var2, int var3, Tag var4);

   Tag getMetadata(int var1, int var2, int var3);

   int getLiquidHeight(int var1, int var2);

   int getSolidHeight(int var1, int var2);

   int getHighestBlockYAt(int var1, int var2);

   DefaultMaterial getMaterial(int var1, int var2, int var3);

   void setChunksCreations(boolean var1);

   int getLightLevel(int var1, int var2, int var3);

   boolean isLoaded(int var1, int var2, int var3);

   WorldConfig getSettings();

   CustomObjectStructureCache getStructureCache();

   String getName();

   long getSeed();

   int getHeight();

   int getHeightBits();

   void setHeightBits(int var1);
}
