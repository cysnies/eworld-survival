package com.lishid.orebfuscator.internal;

import java.io.DataInput;
import java.io.DataOutput;

public interface INBT {
   void reset();

   void setInt(String var1, int var2);

   void setLong(String var1, long var2);

   void setBoolean(String var1, boolean var2);

   void setByteArray(String var1, byte[] var2);

   void setIntArray(String var1, int[] var2);

   int getInt(String var1);

   long getLong(String var1);

   boolean getBoolean(String var1);

   byte[] getByteArray(String var1);

   int[] getIntArray(String var1);

   void Read(DataInput var1);

   void Write(DataOutput var1);
}
