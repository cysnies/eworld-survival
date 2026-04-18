package com.lishid.orebfuscator.hook;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.lishid.orebfuscator.hithack.BlockHitManager;
import com.lishid.orebfuscator.internal.IPacket51;
import com.lishid.orebfuscator.internal.InternalAccessor;
import com.lishid.orebfuscator.obfuscation.Calculations;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

public class ProtocolLibHook {
   private ProtocolManager manager;

   public ProtocolLibHook() {
      super();
   }

   public void register(Plugin plugin) {
      this.manager = ProtocolLibrary.getProtocolManager();
      Integer[] packets = new Integer[]{51, 56};
      this.manager.addPacketListener(new PacketAdapter(plugin, ConnectionSide.SERVER_SIDE, packets) {
         public void onPacketSending(PacketEvent event) {
            if (event.getPacketID() == 51) {
               IPacket51 packet = InternalAccessor.Instance.newPacket51();
               packet.setPacket(event.getPacket().getHandle());
               Calculations.Obfuscate(packet, event.getPlayer());
            }

         }
      });
      Integer[] packets2 = new Integer[]{14};
      this.manager.addPacketListener(new PacketAdapter(plugin, ConnectionSide.CLIENT_SIDE, packets2) {
         public void onPacketReceiving(PacketEvent event) {
            if (event.getPacketID() == 14) {
               int status = (Integer)event.getPacket().getIntegers().read(4);
               if (status == 1 && !BlockHitManager.hitBlock(event.getPlayer(), (Block)null)) {
                  event.setCancelled(true);
               }
            }

         }
      });
   }
}
