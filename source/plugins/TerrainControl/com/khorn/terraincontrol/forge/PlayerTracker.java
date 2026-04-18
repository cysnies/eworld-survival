package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import cpw.mods.fml.common.IPlayerTracker;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet250CustomPayload;

public class PlayerTracker implements IPlayerTracker {
   TCPlugin plugin;

   public PlayerTracker(TCPlugin plugin) {
      super();
      this.plugin = plugin;
   }

   public void onPlayerLogin(EntityPlayer player) {
      LocalWorld worldTC = WorldHelper.toLocalWorld(player.field_70170_p);
      if (worldTC != null) {
         WorldConfig config = worldTC.getSettings();
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         DataOutputStream stream = new DataOutputStream(outputStream);

         try {
            stream.writeInt(TCDefaultValues.ProtocolVersion.intValue());
            config.Serialize(stream);
         } catch (IOException e) {
            e.printStackTrace();
         }

         Packet250CustomPayload packet = new Packet250CustomPayload();
         packet.field_73630_a = TCDefaultValues.ChannelName.stringValue();
         packet.field_73629_c = outputStream.toByteArray();
         packet.field_73628_b = outputStream.size();
         ((EntityPlayerMP)player).field_71135_a.func_72567_b(packet);
         System.out.println("TerrainControl: sent config");
      }
   }

   public void onPlayerLogout(EntityPlayer player) {
   }

   public void onPlayerChangedDimension(EntityPlayer player) {
   }

   public void onPlayerRespawn(EntityPlayer player) {
   }
}
