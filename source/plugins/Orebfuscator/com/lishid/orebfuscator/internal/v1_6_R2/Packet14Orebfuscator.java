package com.lishid.orebfuscator.internal.v1_6_R2;

import com.lishid.orebfuscator.hithack.BlockHitManager;
import net.minecraft.server.v1_6_R2.Connection;
import net.minecraft.server.v1_6_R2.Packet14BlockDig;
import net.minecraft.server.v1_6_R2.PlayerConnection;
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
