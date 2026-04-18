package com.lishid.orebfuscator.internal.v1_6_R2;

import com.lishid.orebfuscator.internal.INBT;
import java.io.DataInput;
import java.io.DataOutput;
import net.minecraft.server.v1_6_R2.NBTCompressedStreamTools;
import net.minecraft.server.v1_6_R2.NBTTagCompound;

public class NBT implements INBT {
   NBTTagCompound nbt = new NBTTagCompound();

   public NBT() {
      super();
   }

   public void reset() {
      this.nbt = new NBTTagCompound();
   }

   public void setInt(String tag, int value) {
      this.nbt.setInt(tag, value);
   }

   public void setLong(String tag, long value) {
      this.nbt.setLong(tag, value);
   }

   public void setBoolean(String tag, boolean value) {
      this.nbt.setBoolean(tag, value);
   }

   public void setByteArray(String tag, byte[] value) {
      this.nbt.setByteArray(tag, value);
   }

   public void setIntArray(String tag, int[] value) {
      this.nbt.setIntArray(tag, value);
   }

   public int getInt(String tag) {
      return this.nbt.getInt(tag);
   }

   public long getLong(String tag) {
      return this.nbt.getLong(tag);
   }

   public boolean getBoolean(String tag) {
      return this.nbt.getBoolean(tag);
   }

   public byte[] getByteArray(String tag) {
      return this.nbt.getByteArray(tag);
   }

   public int[] getIntArray(String tag) {
      return this.nbt.getIntArray(tag);
   }

   public void Read(DataInput stream) {
      this.nbt = NBTCompressedStreamTools.a(stream);
   }

   public void Write(DataOutput stream) {
      NBTCompressedStreamTools.a(this.nbt, stream);
   }
}
