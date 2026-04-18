package fix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lib.config.ReloadConfigEvent;
import lib.hashList.ChanceHashList;
import lib.hashList.ChanceHashListImpl;
import lib.time.TimeEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilNames;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;

public class Fish implements Listener {
   private Random r = new Random();
   private String pn;
   private String path;
   private boolean enable;
   private int player;
   private int interval;
   private int chance;
   private boolean check;
   private ChanceHashList itemList;

   public Fish(Fix fix) {
      super();
      this.pn = fix.getPn();
      this.path = fix.getPluginPath() + File.separator + this.pn + File.separator + "fish.yml";
      this.loadConfig(UtilConfig.getConfig(this.pn));
      fix.getPm().registerEvents(this, fix);
      this.load();
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
         this.load();
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerFish(PlayerFishEvent e) {
      if (this.enable && e.getState().equals(State.CAUGHT_FISH) && this.check) {
         this.check = false;
         Entity entity = e.getCaught();
         if (entity != null) {
            try {
               Item item = (Item)entity;
               ItemStack result = this.addFish(e.getPlayer());
               if (result != null) {
                  item.setItemStack(result);
               }
            } catch (Exception var5) {
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onTime(TimeEvent e) {
      if (this.enable && !this.check && TimeEvent.getTime() % (long)this.interval == 0L && Bukkit.getOnlinePlayers().length >= this.player && this.r.nextInt(100) < this.chance) {
         this.check = true;
      }

   }

   private ItemStack addFish(Player p) {
      if (!this.itemList.isEmpty()) {
         String s = (String)this.itemList.getRandom();
         String[] ss = s.split(":");
         int type;
         short damage;
         if (ss.length == 2) {
            type = Integer.parseInt(ss[0]);
            damage = (short)Integer.parseInt(ss[1]);
         } else {
            type = Integer.parseInt(s);
            damage = 0;
         }

         ItemStack result = new ItemStack(type, 1, damage);
         Bukkit.broadcastMessage(UtilFormat.format(this.pn, "catch", new Object[]{p.getName(), UtilNames.getItemName(result)}));
         int amount = this.itemList.getChance(s);
         if (amount <= 1) {
            this.itemList.remove(s);
         } else {
            this.itemList.setChance(s, amount - 1);
         }

         this.save();
         return result;
      } else {
         return null;
      }
   }

   private void load() {
      this.itemList = new ChanceHashListImpl();
      YamlConfiguration config = new YamlConfiguration();

      try {
         config.load(this.path);

         for(String s : config.getStringList("items")) {
            String type = s.split(" ")[0];
            int amount = Integer.parseInt(s.split(" ")[1]);
            this.itemList.addChance(type, amount);
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

   }

   private void save() {
      YamlConfiguration config = new YamlConfiguration();
      List<String> list = new ArrayList();

      for(String type : this.itemList) {
         int amount = this.itemList.getChance(type);
         list.add(type + " " + amount);
      }

      config.set("items", list);

      try {
         config.save(this.path);
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.enable = config.getBoolean("fish.enable");
      this.player = config.getInt("fish.player");
      this.interval = config.getInt("fish.interval");
      this.chance = config.getInt("fish.chance");
   }
}
