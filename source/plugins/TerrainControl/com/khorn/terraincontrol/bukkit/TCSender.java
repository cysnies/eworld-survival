package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.configuration.WorldConfig;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class TCSender {
   private TCPlugin plugin;

   public TCSender(TCPlugin plugin) {
      super();
      this.plugin = plugin;
   }

   public void send(Player player) {
      World world = player.getWorld();
      if (this.plugin.worlds.containsKey(world.getUID())) {
         WorldConfig config = ((BukkitWorld)this.plugin.worlds.get(world.getUID())).getSettings();
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         DataOutputStream stream = new DataOutputStream(outputStream);

         try {
            stream.writeInt(TCDefaultValues.ProtocolVersion.intValue());
            config.Serialize(stream);
            stream.flush();
         } catch (IOException e) {
            e.printStackTrace();
         }

         byte[] data = outputStream.toByteArray();
         player.sendPluginMessage(this.plugin, TCDefaultValues.ChannelName.stringValue(), data);
      }

   }
}
