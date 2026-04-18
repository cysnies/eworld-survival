package com.lishid.orebfuscator.internal;

import java.util.zip.Deflater;

public interface IPacket56 {
   void setPacket(Object var1);

   int getPacketChunkNumber();

   int[] getX();

   int[] getZ();

   int[] getChunkMask();

   int[] getExtraMask();

   Object getFieldData(String var1);

   void setFieldData(String var1, Object var2);

   String getInflatedBuffers();

   String getBuildBuffer();

   String getOutputBuffer();

   void compress(Deflater var1);
}
