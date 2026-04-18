package com.lishid.orebfuscator.internal.v1_4_6;

import com.lishid.orebfuscator.internal.IPacket51;
import com.lishid.orebfuscator.internal.InternalAccessor;
import com.lishid.orebfuscator.utils.ReflectionHelper;
import java.util.zip.Deflater;
import net.minecraft.server.v1_4_6.Packet51MapChunk;

public class Packet51 implements IPacket51 {
   private static Class packetClass = Packet51MapChunk.class;
   Packet51MapChunk packet;

   public Packet51() {
      super();
   }

   public void setPacket(Object packet) {
      if (packet instanceof Packet51MapChunk) {
         this.packet = (Packet51MapChunk)packet;
      } else {
         InternalAccessor.Instance.PrintError();
      }

   }

   public int getX() {
      return this.packet.a;
   }

   public int getZ() {
      return this.packet.b;
   }

   public int getChunkMask() {
      return this.packet.c;
   }

   public int getExtraMask() {
      return this.packet.d;
   }

   public byte[] getBuffer() {
      return (byte[])ReflectionHelper.getPrivateField(packetClass, this.packet, "inflatedBuffer");
   }

   private byte[] getOutputBuffer() {
      return (byte[])ReflectionHelper.getPrivateField(packetClass, this.packet, "buffer");
   }

   public void compress(Deflater deflater) {
      byte[] chunkInflatedBuffer = this.getBuffer();
      byte[] chunkBuffer = this.getOutputBuffer();
      deflater.reset();
      deflater.setInput(chunkInflatedBuffer, 0, chunkInflatedBuffer.length);
      deflater.finish();
      ReflectionHelper.setPrivateField(packetClass, this.packet, "size", deflater.deflate(chunkBuffer));
   }
}
