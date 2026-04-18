package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.configuration.WorldConfig;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

public class PacketHandler implements IPacketHandler {
   TCPlugin plugin;

   public PacketHandler(TCPlugin plugin) {
      super();
      this.plugin = plugin;
   }

   public void onPacketData(INetworkManager manager, Packet250CustomPayload receivedPacket, Player player) {
      if (receivedPacket.field_73630_a.equals(TCDefaultValues.ChannelName.stringValue())) {
         ByteArrayInputStream inputStream = new ByteArrayInputStream(receivedPacket.field_73629_c);
         DataInputStream stream = new DataInputStream(inputStream);

         try {
            int serverProtocolVersion = stream.readInt();
            int clientProtocolVersion = TCDefaultValues.ProtocolVersion.intValue();
            if (serverProtocolVersion == clientProtocolVersion) {
               SingleWorld.restoreBiomes();
               if (receivedPacket.field_73628_b > 4) {
                  WorldClient worldMC = FMLClientHandler.instance().getClient().field_71441_e;
                  SingleWorld worldTC = new SingleWorld("external");
                  WorldConfig config = new WorldConfig(stream, worldTC);
                  worldTC.InitM(worldMC, config);
               }

               System.out.println("TerrainControl: config received from server");
            } else {
               System.out.println("TerrainControl: server has different protocol version! Client: " + TCDefaultValues.ProtocolVersion.intValue() + " Server: " + serverProtocolVersion);
            }
         } catch (IOException e) {
            e.printStackTrace();
         }

      }
   }
}
