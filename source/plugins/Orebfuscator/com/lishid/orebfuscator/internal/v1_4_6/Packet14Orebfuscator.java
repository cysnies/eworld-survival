package com.lishid.orebfuscator.internal.v1_4_6;

import com.lishid.orebfuscator.hithack.BlockHitManager;
import net.minecraft.server.v1_4_6.Connection;
import net.minecraft.server.v1_4_6.Packet14BlockDig;
import net.minecraft.server.v1_4_6.PlayerConnection;
import org.bukkit.block.Block;

public class Packet14Orebfuscator extends Packet14BlockDig {
   public Packet14Orebfuscator() {
      super();
   }

   public void handle(Connection handler) {
      if (this.e == 1 && handler instanceof PlayerConnection) {
         boolean canHit = BlockHitManager.hitBlock(((PlayerConnection)handler).getPlayer(), (Block)null);
         if (!canHit) {
            return;
         }
      }

      super.handle(handler);
   }
}
