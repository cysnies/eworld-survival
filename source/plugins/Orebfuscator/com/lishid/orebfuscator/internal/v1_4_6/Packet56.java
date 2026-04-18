package com.lishid.orebfuscator.internal.v1_4_6;

import com.lishid.orebfuscator.commands.OrebfuscatorCommandExecutor;
import com.lishid.orebfuscator.internal.IPacket56;
import com.lishid.orebfuscator.internal.InternalAccessor;
import com.lishid.orebfuscator.utils.ReflectionHelper;
import java.util.zip.Deflater;
import net.minecraft.server.v1_4_6.Packet56MapChunkBulk;

public class Packet56 implements IPacket56 {
   Packet56MapChunkBulk packet;

   public Packet56() {
      super();
   }

   public void setPacket(Object packet) {
      if (packet instanceof Packet56MapChunkBulk) {
         this.packet = (Packet56MapChunkBulk)packet;
      } else {
         InternalAccessor.Instance.PrintError();
      }

   }

   public int getPacketChunkNumber() {
      return this.packet.d();
   }

   public int[] getX() {
      return (int[])ReflectionHelper.getPrivateField(this.packet, "c");
   }

   public int[] getZ() {
      return (int[])ReflectionHelper.getPrivateField(this.packet, "d");
   }

   public int[] getChunkMask() {
      return this.packet.a;
   }

   public int[] getExtraMask() {
      return this.packet.b;
   }

   public Object getFieldData(String field) {
      return ReflectionHelper.getPrivateField(Packet56MapChunkBulk.class, this.packet, field);
   }

   public void setFieldData(String field, Object data) {
      ReflectionHelper.setPrivateField(Packet56MapChunkBulk.class, this.packet, field, data);
   }

   public String getInflatedBuffers() {
      return "inflatedBuffers";
   }

   public String getBuildBuffer() {
      return "buildBuffer";
   }

   public String getOutputBuffer() {
      return "buffer";
   }

   public void compress(Deflater deflater) {
      if (this.getFieldData(this.getOutputBuffer()) == null) {
         byte[] buildBuffer = (byte[])this.getFieldData(this.getBuildBuffer());
         deflater.reset();
         deflater.setInput(buildBuffer);
         deflater.finish();
         byte[] buffer = new byte[buildBuffer.length + 100];
         ReflectionHelper.setPrivateField(this.packet, "buffer", buffer);
         int size = deflater.deflate(buffer);
         ReflectionHelper.setPrivateField(this.packet, "size", size);
         ReflectionHelper.setPrivateField(this.packet, "buildBuffer", (Object)null);
         ReflectionHelper.setPrivateField(this.packet, "inflatedBuffers", (Object)null);
         if (OrebfuscatorCommandExecutor.DebugMode) {
            System.out.println("Packet size: " + size);
         }

      }
   }
}
