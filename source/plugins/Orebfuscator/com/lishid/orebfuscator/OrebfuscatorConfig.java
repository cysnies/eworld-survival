package com.lishid.orebfuscator;

import com.lishid.orebfuscator.cache.ObfuscatedDataCache;
import com.lishid.orebfuscator.internal.IBlockTransparency;
import com.lishid.orebfuscator.internal.InternalAccessor;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class OrebfuscatorConfig {
   private static final int CONFIG_VERSION = 10;
   private static Random random = new Random();
   private static int AvailableProcessors = Runtime.getRuntime().availableProcessors();
   public static boolean Enabled = true;
   public static boolean UpdateOnDamage = true;
   public static int EngineMode = 2;
   public static int InitialRadius = 1;
   public static int UpdateRadius = 2;
   public static int OrebfuscatorPriority = 1;
   public static int CompressionLevel = 1;
   public static int ProcessingThreads;
   public static boolean DarknessHideBlocks;
   public static boolean UseCache;
   public static int MaxLoadedCacheFiles;
   public static String CacheLocation;
   public static File CacheFolder;
   public static int ProximityHiderRate;
   public static int ProximityHiderDistance;
   public static int ProximityHiderID;
   public static int ProximityHiderEnd;
   public static boolean UseProximityHider;
   public static boolean UseSpecialBlockForProximityHider;
   public static boolean AntiTexturePackAndFreecam;
   public static int AirGeneratorMaxChance;
   public static boolean NoObfuscationForOps;
   public static boolean NoObfuscationForPermission;
   public static boolean LoginNotification;
   public static boolean CheckForUpdates;
   public static int AntiHitHackDecrementFactor;
   public static int AntiHitHackMaxViolation;
   private static boolean[] ObfuscateBlocks;
   private static boolean[] NetherObfuscateBlocks;
   private static boolean[] DarknessBlocks;
   private static boolean[] ProximityHiderBlocks;
   private static Integer[] RandomBlocks;
   private static Integer[] NetherRandomBlocks;
   private static Integer[] RandomBlocks2;
   private static List DisabledWorlds;
   private static IBlockTransparency blockTransparencyChecker;
   private static boolean[] TransparentBlocks;
   private static boolean TransparentCached;

   static {
      ProcessingThreads = AvailableProcessors - 1;
      DarknessHideBlocks = false;
      UseCache = true;
      MaxLoadedCacheFiles = 64;
      CacheLocation = "orebfuscator_cache";
      CacheFolder = new File(Bukkit.getServer().getWorldContainer(), CacheLocation);
      ProximityHiderRate = 500;
      ProximityHiderDistance = 8;
      ProximityHiderID = 1;
      ProximityHiderEnd = 255;
      UseProximityHider = true;
      UseSpecialBlockForProximityHider = true;
      AntiTexturePackAndFreecam = true;
      AirGeneratorMaxChance = 43;
      NoObfuscationForOps = false;
      NoObfuscationForPermission = false;
      LoginNotification = true;
      CheckForUpdates = true;
      AntiHitHackDecrementFactor = 1000;
      AntiHitHackMaxViolation = 15;
      ObfuscateBlocks = new boolean[256];
      NetherObfuscateBlocks = new boolean[256];
      DarknessBlocks = new boolean[256];
      ProximityHiderBlocks = new boolean[256];
      RandomBlocks = new Integer[]{1, 4, 5, 14, 15, 16, 21, 46, 48, 49, 56, 73, 82, 129};
      NetherRandomBlocks = new Integer[]{13, 87, 88, 112, 153};
      RandomBlocks2 = RandomBlocks;
      DisabledWorlds = new ArrayList();
      TransparentBlocks = new boolean[256];
      TransparentCached = false;
   }

   public OrebfuscatorConfig() {
      super();
   }

   public static File getCacheFolder() {
      if (!CacheFolder.exists()) {
         CacheFolder.mkdirs();
      }

      if (!CacheFolder.exists()) {
         CacheFolder = new File("orebfuscator_cache");
      }

      return CacheFolder;
   }

   public static boolean isBlockTransparent(short id) {
      if (blockTransparencyChecker == null) {
         blockTransparencyChecker = InternalAccessor.Instance.newBlockTransparency();
      }

      if (!TransparentCached) {
         generateTransparentBlocks();
      }

      if (id < 0) {
         id = (short)(id + 256);
      }

      return TransparentBlocks[id];
   }

   private static void generateTransparentBlocks() {
      for(int i = 0; i < TransparentBlocks.length; ++i) {
         TransparentBlocks[i] = blockTransparencyChecker.isBlockTransparent(i);
      }

      TransparentCached = true;
   }

   public static boolean isObfuscated(short id, boolean nether) {
      if (id < 0) {
         id = (short)(id + 256);
      }

      if (nether) {
         return id == 87 ? true : NetherObfuscateBlocks[id];
      } else {
         return id == 1 ? true : ObfuscateBlocks[id];
      }
   }

   public static boolean isDarknessObfuscated(short id) {
      if (id < 0) {
         id = (short)(id + 256);
      }

      return DarknessBlocks[id];
   }

   public static boolean isProximityObfuscated(short id) {
      if (id < 0) {
         id = (short)(id + 256);
      }

      return ProximityHiderBlocks[id];
   }

   public static boolean isWorldDisabled(String name) {
      for(String world : DisabledWorlds) {
         if (world.equalsIgnoreCase(name)) {
            return true;
         }
      }

      return false;
   }

   public static String getDisabledWorlds() {
      String retval = "";

      for(String world : DisabledWorlds) {
         retval = retval + world + ", ";
      }

      return retval.length() > 1 ? retval.substring(0, retval.length() - 2) : retval;
   }

   public static byte getRandomBlock(int index, boolean alternate, boolean nether) {
      return nether ? (byte)NetherRandomBlocks[index] : (byte)alternate ? RandomBlocks2[index] : RandomBlocks[index];
   }

   public static Integer[] getRandomBlocks(boolean alternate, boolean nether) {
      if (nether) {
         return NetherRandomBlocks;
      } else {
         return alternate ? RandomBlocks2 : RandomBlocks;
      }
   }

   public static void shuffleRandomBlocks() {
      synchronized(RandomBlocks) {
         Collections.shuffle(Arrays.asList(RandomBlocks));
         Collections.shuffle(Arrays.asList(RandomBlocks2));
      }
   }

   public static int random(int max) {
      return random.nextInt(max);
   }

   public static void setEngineMode(int data) {
      setData("Integers.EngineMode", data);
      EngineMode = data;
   }

   public static void setUpdateRadius(int data) {
      setData("Integers.UpdateRadius", data);
      UpdateRadius = data;
   }

   public static void setInitialRadius(int data) {
      setData("Integers.InitialRadius", data);
      InitialRadius = data;
   }

   public static void setProcessingThreads(int data) {
      setData("Integers.ProcessingThreads", data);
      ProcessingThreads = data;
   }

   public static void setProximityHiderDistance(int data) {
      setData("Integers.ProximityHiderDistance", data);
      ProximityHiderDistance = data;
   }

   public static void setAirGeneratorMaxChance(int data) {
      setData("Integers.AirGeneratorMaxChance", data);
      AirGeneratorMaxChance = data;
   }

   public static void setUseProximityHider(boolean data) {
      setData("Booleans.UseProximityHider", data);
      UseProximityHider = data;
   }

   public static void setDarknessHideBlocks(boolean data) {
      setData("Booleans.DarknessHideBlocks", data);
      DarknessHideBlocks = data;
   }

   public static void setNoObfuscationForOps(boolean data) {
      setData("Booleans.NoObfuscationForOps", data);
      NoObfuscationForOps = data;
   }

   public static void setNoObfuscationForPermission(boolean data) {
      setData("Booleans.NoObfuscationForPermission", data);
      NoObfuscationForPermission = data;
   }

   public static void setLoginNotification(boolean data) {
      setData("Booleans.LoginNotification", data);
      LoginNotification = data;
   }

   public static void setAntiTexturePackAndFreecam(boolean data) {
      setData("Booleans.AntiTexturePackAndFreecam", data);
      AntiTexturePackAndFreecam = data;
   }

   public static void setUseCache(boolean data) {
      setData("Booleans.UseCache", data);
      UseCache = data;
   }

   public static void setEnabled(boolean data) {
      setData("Booleans.Enabled", data);
      Enabled = data;
   }

   public static void setDisabledWorlds(String name, boolean data) {
      if (!data) {
         DisabledWorlds.remove(name);
      } else {
         DisabledWorlds.add(name);
      }

      setData("Lists.DisabledWorlds", DisabledWorlds);
   }

   private static boolean getBoolean(String path, boolean defaultData) {
      if (getConfig().get(path) == null) {
         setData(path, defaultData);
      }

      return getConfig().getBoolean(path, defaultData);
   }

   private static String getString(String path, String defaultData) {
      if (getConfig().get(path) == null) {
         setData(path, defaultData);
      }

      return getConfig().getString(path, defaultData);
   }

   private static int getInt(String path, int defaultData) {
      if (getConfig().get(path) == null) {
         setData(path, defaultData);
      }

      return getConfig().getInt(path, defaultData);
   }

   private static List getIntList(String path, List defaultData) {
      if (getConfig().get(path) == null) {
         setData(path, defaultData);
      }

      return getConfig().getIntegerList(path);
   }

   private static Integer[] getIntList2(String path, List defaultData) {
      if (getConfig().get(path) == null) {
         setData(path, defaultData);
      }

      return (Integer[])getConfig().getIntegerList(path).toArray(new Integer[1]);
   }

   private static List getStringList(String path, List defaultData) {
      if (getConfig().get(path) == null) {
         setData(path, defaultData);
      }

      return getConfig().getStringList(path);
   }

   private static void setData(String path, Object data) {
      try {
         getConfig().set(path, data);
         save();
      } catch (Exception e) {
         Orebfuscator.log((Throwable)e);
      }

   }

   private static void setBlockValues(boolean[] boolArray, List blocks, boolean transparent) {
      for(int i = 0; i < boolArray.length; ++i) {
         boolArray[i] = blocks.contains(i);
         if (transparent != isBlockTransparent((short)i)) {
            boolArray[i] = false;
         }
      }

   }

   private static void setBlockValues(boolean[] boolArray, List blocks) {
      for(int i = 0; i < boolArray.length; ++i) {
         boolArray[i] = blocks.contains(i);
      }

   }

   public static void load() {
      int version = getInt("ConfigVersion", 10);
      if (version < 10) {
         ObfuscatedDataCache.ClearCache();
         setData("ConfigVersion", 10);
      }

      EngineMode = getInt("Integers.EngineMode", EngineMode);
      if (EngineMode != 1 && EngineMode != 2) {
         EngineMode = 2;
         Orebfuscator.log("EngineMode must be 1 or 2.");
      }

      InitialRadius = clamp(getInt("Integers.InitialRadius", InitialRadius), 0, 2);
      if (InitialRadius == 0) {
         Orebfuscator.log("Warning, InitialRadius is 0. This will cause all exposed blocks to be obfuscated.");
      }

      UpdateRadius = clamp(getInt("Integers.UpdateRadius", UpdateRadius), 1, 5);
      ProcessingThreads = clamp(getInt("Integers.ProcessingThreads", ProcessingThreads), 1, AvailableProcessors);
      MaxLoadedCacheFiles = clamp(getInt("Integers.MaxLoadedCacheFiles", MaxLoadedCacheFiles), 16, 128);
      ProximityHiderDistance = clamp(getInt("Integers.ProximityHiderDistance", ProximityHiderDistance), 2, 64);
      ProximityHiderID = getInt("Integers.ProximityHiderID", ProximityHiderID);
      ProximityHiderEnd = clamp(getInt("Integers.ProximityHiderEnd", ProximityHiderEnd), 0, 255);
      AirGeneratorMaxChance = clamp(getInt("Integers.AirGeneratorMaxChance", AirGeneratorMaxChance), 40, 100);
      OrebfuscatorPriority = clamp(getInt("Integers.OrebfuscatorPriority", OrebfuscatorPriority), 1, 10);
      CompressionLevel = clamp(getInt("Integers.CompressionLevel", CompressionLevel), 1, 9);
      UseProximityHider = getBoolean("Booleans.UseProximityHider", UseProximityHider);
      UseSpecialBlockForProximityHider = getBoolean("Booleans.UseSpecialBlockForProximityHider", UseSpecialBlockForProximityHider);
      UpdateOnDamage = getBoolean("Booleans.UpdateOnDamage", UpdateOnDamage);
      DarknessHideBlocks = getBoolean("Booleans.DarknessHideBlocks", DarknessHideBlocks);
      NoObfuscationForOps = getBoolean("Booleans.NoObfuscationForOps", NoObfuscationForOps);
      NoObfuscationForPermission = getBoolean("Booleans.NoObfuscationForPermission", NoObfuscationForPermission);
      UseCache = getBoolean("Booleans.UseCache", UseCache);
      LoginNotification = getBoolean("Booleans.LoginNotification", LoginNotification);
      AntiTexturePackAndFreecam = getBoolean("Booleans.AntiTexturePackAndFreecam", AntiTexturePackAndFreecam);
      Enabled = getBoolean("Booleans.Enabled", Enabled);
      CheckForUpdates = getBoolean("Booleans.CheckForUpdates", CheckForUpdates);
      setBlockValues(ObfuscateBlocks, getIntList("Lists.ObfuscateBlocks", Arrays.asList(14, 15, 16, 21, 54, 56, 73, 74, 129, 130)), false);
      setBlockValues(NetherObfuscateBlocks, getIntList("Lists.NetherObfuscateBlocks", Arrays.asList(87, 153)), false);
      setBlockValues(DarknessBlocks, getIntList("Lists.DarknessBlocks", Arrays.asList(52, 54)));
      setBlockValues(ProximityHiderBlocks, getIntList("Lists.ProximityHiderBlocks", Arrays.asList(23, 52, 54, 56, 58, 61, 62, 116, 129, 130, 145, 146)));
      DisabledWorlds = getStringList("Lists.DisabledWorlds", DisabledWorlds);
      CacheLocation = getString("Strings.CacheLocation", CacheLocation);
      CacheFolder = new File(CacheLocation);
      RandomBlocks = getIntList2("Lists.RandomBlocks", Arrays.asList(RandomBlocks));
      NetherRandomBlocks = getIntList2("Lists.NetherRandomBlocks", Arrays.asList(NetherRandomBlocks));

      for(int i = 0; i < RandomBlocks.length; ++i) {
         if (RandomBlocks[i] == null || isBlockTransparent((short)RandomBlocks[i])) {
            RandomBlocks[i] = 1;
         }
      }

      RandomBlocks2 = RandomBlocks;
      save();
   }

   public static void reload() {
      Orebfuscator.instance.reloadConfig();
      load();
   }

   public static void save() {
      Orebfuscator.instance.saveConfig();
   }

   public static boolean obfuscateForPlayer(Player player) {
      return !playerBypassOp(player) && !playerBypassPerms(player);
   }

   public static boolean playerBypassOp(Player player) {
      boolean ret = false;

      try {
         ret = NoObfuscationForOps && player.isOp();
      } catch (Exception e) {
         Orebfuscator.log("Error while obtaining Operator status for player" + player.getName() + ": " + e.getMessage());
         e.printStackTrace();
      }

      return ret;
   }

   public static boolean playerBypassPerms(Player player) {
      boolean ret = false;

      try {
         ret = NoObfuscationForPermission && player.hasPermission("Orebfuscator.deobfuscate");
      } catch (Exception e) {
         Orebfuscator.log("Error while obtaining permissions for player" + player.getName() + ": " + e.getMessage());
         e.printStackTrace();
      }

      return ret;
   }

   private static FileConfiguration getConfig() {
      return Orebfuscator.instance.getConfig();
   }

   public static int clamp(int value, int min, int max) {
      if (value < min) {
         value = min;
      }

      if (value > max) {
         value = max;
      }

      return value;
   }
}
