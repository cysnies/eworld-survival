package smelt;

import java.util.List;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Protect implements Listener {
   private Server server;
   private String pn;
   private String check;

   public Protect(Main main) {
      super();
      this.server = main.getServer();
      this.pn = main.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, main);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public boolean isProtect(ItemStack is) {
      if (is != null && is.getTypeId() != 0) {
         ItemMeta im = is.getItemMeta();
         if (im != null) {
            List<String> lore = im.getLore();
            if (lore != null && lore.size() > 0 && ((String)lore.get(0)).equals(this.check)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private void loadConfig(FileConfiguration config) {
      this.check = Util.convert(config.getString("protect.check"));
   }
}
