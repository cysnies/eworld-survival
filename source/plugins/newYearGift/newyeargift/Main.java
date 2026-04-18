package newyeargift;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import lib.util.UtilEco;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
   private static final Random r = new Random();
   private boolean enable;
   private String format;
   private int price;
   private String priceShow;
   private boolean tipEnable;
   private int tipDelay;
   private String tipShow;
   private int amountMin;
   private String amountMinShow;
   private int amountMax;
   private String amountMaxShow;
   private int base;
   private int luckCondition;
   private int luckAmountMin;
   private int luckAmountMax;
   private String luckShow;
   private String successShow;
   private String failLess;
   private int failBackMin;
   private int failBackMax;
   private String failBackShow;
   private String countPath;
   private int total;

   public Main() {
      super();
   }

   public void onEnable() {
      this.countPath = this.getFile().getParentFile().getAbsolutePath() + File.separator + this.getDescription().getName() + File.separator + "count.yml";
      YamlConfiguration config = new YamlConfiguration();

      try {
         config.load(new File(this.countPath));
         this.total = config.getInt("amount");
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

      this.loadConfig();
      Bukkit.getPluginManager().registerEvents(this, this);
   }

   public void onDisable() {
      this.save();
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
         this.loadConfig();
         sender.sendMessage("重新读取配置文件完毕.");
      }

      return true;
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      if (this.enable && this.tipEnable) {
         Show show = new Show(e.getPlayer());
         Bukkit.getScheduler().scheduleSyncDelayedTask(this, show, (long)this.tipDelay);
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
      if (this.enable) {
         String msg = e.getMessage().toLowerCase();
         String check = this.format.replace("{0}", "");
         if (msg.indexOf(check) != -1) {
            String[] ss = msg.split(check);
            if (ss != null && ss.length >= 1) {
               try {
                  int need = Integer.parseInt(ss[ss.length - 1].split(" ")[0]);
                  if (need < this.amountMin) {
                     e.getPlayer().sendMessage(this.amountMinShow);
                     return;
                  }

                  if (need > this.amountMax) {
                     e.getPlayer().sendMessage(this.amountMaxShow);
                     return;
                  }

                  if (UtilEco.get(e.getPlayer().getName()) < (double)this.price) {
                     e.getPlayer().sendMessage(this.failLess);
                     return;
                  }

                  UtilEco.del(e.getPlayer().getName(), (double)this.price);
                  e.getPlayer().sendMessage(this.priceShow);
                  this.total += this.price;
                  if (r.nextInt(need) < this.base) {
                     int give = Math.min(this.total, need);
                     this.total -= give;
                     UtilEco.add(e.getPlayer().getName(), (double)give);
                     Bukkit.broadcastMessage(this.successShow.replace("{0}", e.getPlayer().getName()).replace("{1}", String.valueOf(give)));
                     e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.ORB_PICKUP, 2.0F, 1.0F);
                  } else {
                     int give = r.nextInt(this.failBackMax - this.failBackMin + 1) + this.failBackMin;
                     this.total -= give;
                     UtilEco.add(e.getPlayer().getName(), (double)give);
                     e.getPlayer().sendMessage(this.failBackShow.replace("{0}", e.getPlayer().getName()).replace("{1}", String.valueOf(give)));
                  }

                  if (this.total >= this.luckCondition) {
                     int luck = r.nextInt(this.luckAmountMax - this.luckAmountMin + 1) + this.luckAmountMin;
                     this.total -= luck;
                     UtilEco.add(e.getPlayer().getName(), (double)luck);
                     Bukkit.broadcastMessage(this.luckShow.replace("{0}", e.getPlayer().getName()).replace("{1}", String.valueOf(luck)));
                     e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.ORB_PICKUP, 2.0F, 1.0F);
                  }
               } catch (NumberFormatException var7) {
               }
            }
         }
      }

   }

   private void save() {
      YamlConfiguration config = new YamlConfiguration();
      config.set("amount", this.total);

      try {
         config.save(this.countPath);
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   private void loadConfig() {
      FileConfiguration config = this.getConfig();
      this.enable = config.getBoolean("enable");
      this.format = config.getString("format").toLowerCase();
      this.price = config.getInt("price");
      this.priceShow = config.getString("priceShow").replace("&", "§").replace("{0}", String.valueOf(this.price));
      this.tipEnable = config.getBoolean("tip.enable");
      this.tipDelay = config.getInt("tip.delay");
      this.tipShow = config.getString("tip.show").replace("&", "§").replace("{0}", this.format.replace("{0}", "xxx")).replace("{1}", String.valueOf(this.price));
      this.amountMin = config.getInt("amount.min");
      this.amountMinShow = config.getString("amount.minShow").replace("&", "§").replace("{0}", String.valueOf(this.amountMin));
      this.amountMax = config.getInt("amount.max");
      this.amountMaxShow = config.getString("amount.maxShow").replace("&", "§").replace("{0}", String.valueOf(this.amountMax));
      this.base = config.getInt("base");
      this.luckCondition = config.getInt("luck.condition");
      this.luckAmountMin = config.getInt("luck.amount.min");
      this.luckAmountMax = config.getInt("luck.amount.max");
      this.luckShow = config.getString("luck.show").replace("&", "§");
      this.successShow = config.getString("success.show").replace("&", "§");
      this.failLess = config.getString("fail.less").replace("&", "§").replace("{0}", String.valueOf(this.price));
      this.failBackMin = config.getInt("fail.back.min");
      this.failBackMax = config.getInt("fail.back.max");
      this.failBackShow = config.getString("fail.back.show").replace("&", "§");
   }

   private class Show implements Runnable {
      private Player p;

      public Show(Player p) {
         super();
         this.p = p;
      }

      public void run() {
         if (this.p.isOnline()) {
            this.p.sendMessage(Main.this.tipShow);
         }

      }
   }
}
