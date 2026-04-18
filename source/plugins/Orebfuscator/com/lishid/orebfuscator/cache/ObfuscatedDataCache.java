package com.lishid.orebfuscator.cache;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.internal.IChunkCache;
import com.lishid.orebfuscator.internal.InternalAccessor;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;

public class ObfuscatedDataCache {
   private static IChunkCache internalCache;

   public ObfuscatedDataCache() {
      super();
   }

   private static IChunkCache getInternalCache() {
      if (internalCache == null) {
         internalCache = InternalAccessor.Instance.newChunkCache();
      }

      return internalCache;
   }

   public static void clearCache() {
      getInternalCache().clearCache();
   }

   public static DataInputStream getInputStream(File folder, int x, int z) {
      return getInternalCache().getInputStream(folder, x, z);
   }

   public static DataOutputStream getOutputStream(File folder, int x, int z) {
      return getInternalCache().getOutputStream(folder, x, z);
   }

   public static void ClearCache() {
      getInternalCache().clearCache();

      try {
         DeleteDir(OrebfuscatorConfig.getCacheFolder());
      } catch (Exception e) {
         Orebfuscator.log((Throwable)e);
      }

   }

   private static void DeleteDir(File dir) {
      try {
         if (!dir.exists()) {
            return;
         }

         if (dir.isDirectory()) {
            File[] var4;
            for(File f : var4 = dir.listFiles()) {
               DeleteDir(f);
            }
         }

         dir.delete();
      } catch (Exception e) {
         Orebfuscator.log((Throwable)e);
      }

   }
}
