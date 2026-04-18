package com.lishid.orebfuscator.internal.craftbukkit;

import com.lishid.orebfuscator.hithack.BlockHitManager;
import net.minecraft.server.NetHandler;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.Packet14BlockDig;
import org.bukkit.block.Block;

public class Packet14Orebfuscator extends Packet14BlockDig {
   public Packet14Orebfuscator() {
      super();
   }

   public void handle(NetHandler handler) {
      if (this.e == 1 && handler instanceof NetServerHandler) {
         boolean canHit = BlockHitManager.hitBlock(((NetServerHandler)handler).getPlayer(), (Block)null);
         if (!canHit) {
            return;
         }
      }

      super.handle(handler);
   }
}
