package com.lishid.orebfuscator.internal.v1_6_R1;

import com.lishid.orebfuscator.internal.IMinecraftWorldServer;
import com.lishid.orebfuscator.internal.InternalAccessor;
import net.minecraft.server.v1_6_R1.WorldServer;
import org.bukkit.craftbukkit.v1_6_R1.CraftWorld;

public class MinecraftWorldServer implements IMinecraftWorldServer {
   public MinecraftWorldServer() {
      super();
   }

   public void Notify(Object world, int x, int y, int z) {
      if (world instanceof CraftWorld) {
         WorldServer server = ((CraftWorld)world).getHandle();
         server.notify(x, y, z);
      } else {
         InternalAccessor.Instance.PrintError();
      }

   }
}
