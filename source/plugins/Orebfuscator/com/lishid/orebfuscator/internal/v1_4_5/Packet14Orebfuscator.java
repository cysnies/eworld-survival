package com.lishid.orebfuscator.internal.v1_4_5;

import com.lishid.orebfuscator.hithack.BlockHitManager;
import net.minecraft.server.v1_4_5.NetHandler;
import net.minecraft.server.v1_4_5.NetServerHandler;
import net.minecraft.server.v1_4_5.Packet14BlockDig;
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
