package com.lishid.orebfuscator.cache;

import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.internal.INBT;
import com.lishid.orebfuscator.internal.InternalAccessor;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;

public class ObfuscatedCachedChunk {
   File path;
   int x;
   int z;
   public byte[] data;
   public int[] proximityList;
   public long hash = 0L;
   private boolean loaded = false;
   private static final ThreadLocal nbtAccessor = new ThreadLocal() {
      protected INBT initialValue() {
         return InternalAccessor.Instance.newNBT();
      }
   };

   public ObfuscatedCachedChunk(File file, int x, int z) {
      super();
      this.x = x;
      this.z = z;
      this.path = new File(file, "data");
      this.path.mkdirs();
   }

   public void Invalidate() {
      this.Write(0L, new byte[0], new int[0]);
   }

   public void free() {
      this.data = null;
      this.proximityList = null;
   }

   public long getHash() {
      this.Read();
      return !this.loaded ? 0L : this.hash;
   }

   public void Read() {
      if (!this.loaded) {
         try {
            DataInputStream stream = ObfuscatedDataCache.getInputStream(this.path, this.x, this.z);
            if (stream != null) {
               INBT nbt = (INBT)nbtAccessor.get();
               nbt.Read(stream);
               if (nbt.getInt("X") == this.x && nbt.getInt("Z") == this.z) {
                  if (OrebfuscatorConfig.UseProximityHider == nbt.getBoolean("PH") && OrebfuscatorConfig.InitialRadius == nbt.getInt("IR")) {
                     this.hash = nbt.getLong("Hash");
                     this.data = nbt.getByteArray("Data");
                     this.proximityList = nbt.getIntArray("ProximityList");
                     this.loaded = true;
                     return;
                  }

                  return;
               }

               return;
            }
         } catch (Exception var3) {
            this.loaded = false;
         }

      }
   }

   public void Write(long hash, byte[] data, int[] proximityList) {
      try {
         INBT nbt = (INBT)nbtAccessor.get();
         nbt.reset();
         nbt.setInt("X", this.x);
         nbt.setInt("Z", this.z);
         nbt.setInt("IR", OrebfuscatorConfig.InitialRadius);
         nbt.setBoolean("PH", OrebfuscatorConfig.UseProximityHider);
         nbt.setLong("Hash", hash);
         nbt.setByteArray("Data", data);
         nbt.setIntArray("ProximityList", proximityList);
         DataOutputStream stream = ObfuscatedDataCache.getOutputStream(this.path, this.x, this.z);
         nbt.Write(stream);

         try {
            stream.close();
         } catch (Exception var8) {
         }
      } catch (Exception var9) {
      }

   }
}
