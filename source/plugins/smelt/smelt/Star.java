package smelt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilNames;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Star implements Listener {
   private static final ItemMeta IM = (new ItemStack(1)).getItemMeta();
   private Server server;
   private String pn;
   private String empty;
   private String fill;
   private String defaultColor;
   private HashMap colors;

   public Star(Main main) {
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

   public StarInfo getStarInfo(ItemStack is) {
      try {
         ItemMeta im = is.getItemMeta();
         if (im == null) {
            return null;
         } else {
            List<String> lore = im.getLore();
            if (lore != null && lore.size() != 0) {
               int total = ((String)lore.get(0)).length() - 2;
               if (total <= 0) {
                  return null;
               } else {
                  int fill = 0;
                  String star = ((String)lore.get(0)).substring(2);

                  for(int i = 0; i < total; ++i) {
                     String check = star.substring(i, i + 1);
                     if (check.equalsIgnoreCase(this.fill)) {
                        ++fill;
                     } else if (!check.equalsIgnoreCase(this.empty)) {
                        return null;
                     }
                  }

                  StarInfo starInfo = new StarInfo(total, fill);
                  return starInfo;
               }
            } else {
               return null;
            }
         }
      } catch (Exception var9) {
         return null;
      }
   }

   public void setStar(ItemStack is, StarInfo starInfo) {
      StarInfo si = this.getStarInfo(is);
      ItemMeta im = is.getItemMeta();
      if (im == null) {
         im = IM.clone();
         im.setDisplayName(UtilNames.getItemName(is.getTypeId(), is.getDurability()));
      }

      List<String> lore = im.getLore();
      if (lore == null) {
         lore = new ArrayList();
      }

      String color = this.defaultColor;
      int total = starInfo.getTotal();
      int fill = starInfo.getFill();
      int empty = total - fill;
      if (total > 0) {
         if (this.colors.containsKey(total)) {
            color = (String)this.colors.get(total);
         }

         String info = "§" + color;

         for(int i = 0; i < fill; ++i) {
            info = info + this.fill;
         }

         for(int i = 0; i < empty; ++i) {
            info = info + this.empty;
         }

         if (lore.size() == 0) {
            lore.add(info);
         } else if (si != null) {
            lore.set(0, info);
         } else {
            lore.add(0, info);
         }
      } else if (si != null && lore.size() > 0) {
         lore.remove(0);
      }

      im.setLore(lore);
      is.setItemMeta(im);
   }

   private void loadConfig(FileConfiguration config) {
      this.empty = this.getStr(config.getString("star.empty"));
      this.fill = this.getStr(config.getString("star.fill"));
      this.defaultColor = config.getString("star.color.default");
      this.colors = new HashMap();

      for(String s : config.getStringList("star.color.levels")) {
         int level = Integer.parseInt(s.split(" ")[0]);
         String color = s.split(" ")[1];
         this.colors.put(level, color);
      }

   }

   private String getStr(String str) {
      String[] strs = str.split("&#x");
      StringBuilder sb = new StringBuilder();

      for(int i = 1; i < strs.length; ++i) {
         int temp = Integer.parseInt(strs[i].substring(0, strs[i].length() - 1), 16);
         sb.append((char)temp);
      }

      return sb.toString();
   }
}
