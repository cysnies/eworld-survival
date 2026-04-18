package com.lishid.orebfuscator.internal;

import com.lishid.orebfuscator.Orebfuscator;
import org.bukkit.Server;

public class InternalAccessor {
   public static InternalAccessor Instance;
   private String version;

   public InternalAccessor() {
      super();
   }

   public static boolean Initialize(Server server) {
      Instance = new InternalAccessor();
      String packageName = server.getClass().getPackage().getName();
      Instance.version = packageName.substring(packageName.lastIndexOf(46) + 1);

      try {
         Class.forName("com.lishid.orebfuscator.internal." + Instance.version + ".PlayerHook");
         return true;
      } catch (Exception var3) {
         return false;
      }
   }

   public void PrintError() {
      Orebfuscator.log("Orebfuscator encountered an error with the CraftBukkit version \"" + Instance.version + "\". Please look for an updated version of Orebfuscator.");
   }

   public INBT newNBT() {
      return (INBT)this.createObject(INBT.class, "NBT");
   }

   public IChunkCache newChunkCache() {
      return (IChunkCache)this.createObject(IChunkCache.class, "ChunkCache");
   }

   public IPacket51 newPacket51() {
      return (IPacket51)this.createObject(IPacket51.class, "Packet51");
   }

   public IPacket56 newPacket56() {
      return (IPacket56)this.createObject(IPacket56.class, "Packet56");
   }

   public IPlayerHook newPlayerHook() {
      return (IPlayerHook)this.createObject(IPlayerHook.class, "PlayerHook");
   }

   public IBlockTransparency newBlockTransparency() {
      return (IBlockTransparency)this.createObject(IBlockTransparency.class, "BlockTransparency");
   }

   public IMinecraftWorldServer newMinecraftWorldServer() {
      return (IMinecraftWorldServer)this.createObject(IMinecraftWorldServer.class, "MinecraftWorldServer");
   }

   private Object createObject(Class assignableClass, String className) {
      try {
         Class<?> internalClass = Class.forName("com.lishid.orebfuscator.internal." + this.version + "." + className);
         if (assignableClass.isAssignableFrom(internalClass)) {
            return internalClass.getConstructor().newInstance();
         }
      } catch (Exception e) {
         this.PrintError();
         Orebfuscator.log((Throwable)e);
      }

      return null;
   }
}
