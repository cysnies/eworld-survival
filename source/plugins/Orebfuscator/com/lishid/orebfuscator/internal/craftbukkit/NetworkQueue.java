package com.lishid.orebfuscator.internal.craftbukkit;

import com.lishid.orebfuscator.internal.IPacket51;
import com.lishid.orebfuscator.internal.InternalAccessor;
import com.lishid.orebfuscator.obfuscation.Calculations;
import java.util.ArrayList;
import net.minecraft.server.Packet;
import org.bukkit.entity.Player;

public class NetworkQueue extends ArrayList {
   private static final long serialVersionUID = 4252847662044263527L;
   private Player player;

   public NetworkQueue(Player player) {
      super();
      this.player = player;
   }

   public boolean add(Packet packet) {
      if (packet.k() == 51) {
         IPacket51 packet51 = InternalAccessor.Instance.newPacket51();
         packet51.setPacket(packet);
         Calculations.Obfuscate(packet51, this.player);
      }

      return super.add(packet);
   }
}
