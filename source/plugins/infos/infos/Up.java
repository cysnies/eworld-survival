package infos;

import level.Main;
import lib.config.ReloadConfigEvent;
import lib.time.TimeEvent;
import lib.util.UtilConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class Up implements Listener {
   private int interval;

   public Up(Infos infos) {
      super();
      this.loadConfig(UtilConfig.getConfig(Infos.getPn()));
      infos.getPm().registerEvents(this, infos);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(Infos.getPn())) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onTime(TimeEvent e) {
      if (TimeEvent.getTime() % (long)this.interval == 0L) {
         Player[] var5;
         for(Player p : var5 = Bukkit.getServer().getOnlinePlayers()) {
            PlayerInfo pi = Infos.getPlayerInfoManager().checkInit(p.getName());
            int id = 50;
            if (!Main.getLevelManager().hasLevel(p.getName(), id) && pi.getOnlineTime() > 1440) {
               Main.getLevelManager().addLevel((CommandSender)null, p.getName(), id);
            }

            id = 51;
            if (!Main.getLevelManager().hasLevel(p.getName(), id) && pi.getOnlineTime() > 7200) {
               Main.getLevelManager().addLevel((CommandSender)null, p.getName(), id);
            }

            id = 52;
            if (!Main.getLevelManager().hasLevel(p.getName(), id) && pi.getOnlineTime() > 36000) {
               Main.getLevelManager().addLevel((CommandSender)null, p.getName(), id);
            }

            id = 53;
            if (!Main.getLevelManager().hasLevel(p.getName(), id) && pi.getMineNum() > 10000) {
               Main.getLevelManager().addLevel((CommandSender)null, p.getName(), id);
            }

            id = 54;
            if (!Main.getLevelManager().hasLevel(p.getName(), id) && pi.getMineNum() > 100000) {
               Main.getLevelManager().addLevel((CommandSender)null, p.getName(), id);
            }

            id = 55;
            if (!Main.getLevelManager().hasLevel(p.getName(), id) && pi.getMineNum() > 1000000) {
               Main.getLevelManager().addLevel((CommandSender)null, p.getName(), id);
            }

            id = 56;
            if (!Main.getLevelManager().hasLevel(p.getName(), id) && pi.getBreakNum() > 100000) {
               Main.getLevelManager().addLevel((CommandSender)null, p.getName(), id);
            }

            id = 57;
            if (!Main.getLevelManager().hasLevel(p.getName(), id) && pi.getPlaceNum() > 100000) {
               Main.getLevelManager().addLevel((CommandSender)null, p.getName(), id);
            }

            id = 58;
            if (!Main.getLevelManager().hasLevel(p.getName(), id) && pi.getKillMonsterNum() > 1000) {
               Main.getLevelManager().addLevel((CommandSender)null, p.getName(), id);
            }

            id = 59;
            if (!Main.getLevelManager().hasLevel(p.getName(), id) && pi.getKillMonsterNum() > 10000) {
               Main.getLevelManager().addLevel((CommandSender)null, p.getName(), id);
            }

            id = 60;
            if (!Main.getLevelManager().hasLevel(p.getName(), id) && pi.getKillMonsterNum() > 100000) {
               Main.getLevelManager().addLevel((CommandSender)null, p.getName(), id);
            }

            id = 61;
            if (!Main.getLevelManager().hasLevel(p.getName(), id) && pi.getKillAnimalNum() > 10000) {
               Main.getLevelManager().addLevel((CommandSender)null, p.getName(), id);
            }

            id = 62;
            if (!Main.getLevelManager().hasLevel(p.getName(), id) && pi.getKillPlayerNum() > 500) {
               Main.getLevelManager().addLevel((CommandSender)null, p.getName(), id);
            }

            id = 63;
            if (!Main.getLevelManager().hasLevel(p.getName(), id) && pi.getKillPlayerNum() > 2500) {
               Main.getLevelManager().addLevel((CommandSender)null, p.getName(), id);
            }

            id = 64;
            if (!Main.getLevelManager().hasLevel(p.getName(), id) && pi.getKillPlayerNum() > 12500) {
               Main.getLevelManager().addLevel((CommandSender)null, p.getName(), id);
            }
         }
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.interval = config.getInt("up.interval");
   }
}
