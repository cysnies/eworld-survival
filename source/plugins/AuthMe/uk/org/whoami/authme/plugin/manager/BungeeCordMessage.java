package uk.org.whoami.authme.plugin.manager;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import uk.org.whoami.authme.AuthMe;

public class BungeeCordMessage implements PluginMessageListener {
   public AuthMe plugin;

   public BungeeCordMessage(AuthMe plugin) {
      super();
      this.plugin = plugin;
   }

   public void onPluginMessageReceived(String channel, Player player, byte[] message) {
      try {
         DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
         if (in.readUTF().equals("IP")) {
            this.plugin.realIp.put(player.getName().toLowerCase(), in.readUTF());
         }
      } catch (IOException var5) {
      }

   }
}
