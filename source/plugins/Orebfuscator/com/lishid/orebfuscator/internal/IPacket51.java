package com.lishid.orebfuscator.internal;

import java.util.zip.Deflater;

public interface IPacket51 {
   void setPacket(Object var1);

   int getX();

   int getZ();

   int getChunkMask();

   int getExtraMask();

   byte[] getBuffer();

   void compress(Deflater var1);
}
