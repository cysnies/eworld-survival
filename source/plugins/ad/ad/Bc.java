package ad;

import java.util.ArrayList;
import java.util.List;
import lib.config.ReloadConfigEvent;
import lib.time.TimeEvent;
import lib.util.Util;
import lib.util.UtilBar;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import ticket.Ticket;

public class Bc implements Listener {
   private Ticket t;
   private String pn = Ad.getPn();
   private String per_ad_admin;
   private int bigCost;
   private int bigLast;
   private int bigMax;
   private List waitList = new ArrayList();
   private int left;

   public Bc(Ad ad) {
      super();
      this.loadConfig(UtilConfig.getConfig(Ad.getPn()));
      ad.getPm().registerEvents(this, ad);
      this.addBig(this.get(275));
   }

   public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      try {
         Player p = null;
         if (sender instanceof Player) {
            p = (Player)sender;
         }

         String cmdName = cmd.getName();
         int length = args.length;
         if (cmdName.equalsIgnoreCase("bc")) {
            if (length != 1 || !args[0].equalsIgnoreCase("?")) {
               if (length == 1 && args[0].equalsIgnoreCase("reload")) {
                  if (p != null && !UtilPer.checkPer(p, this.per_ad_admin)) {
                     return;
                  }

                  sender.sendMessage(this.get(225));
                  sender.sendMessage(this.get(230));
                  return;
               }

               if (p != null && length > 0) {
                  try {
                     String msg = Util.convert(Util.combine(args, " ", 0, length)).trim();
                     msg = msg.substring(0, Math.min(this.bigMax, msg.length()));
                     if (!msg.isEmpty()) {
                        this.big(p, msg);
                     }
                  } catch (Exception var9) {
                  }

                  return;
               }
            }

            sender.sendMessage(UtilFormat.format(Ad.getPn(), "cmdHelpHeader", new Object[]{this.get(200)}));
            sender.sendMessage(UtilFormat.format(Ad.getPn(), "cmdHelpItem", new Object[]{this.get(255), UtilFormat.format(this.pn, "big", new Object[]{this.bigCost, this.bigLast})}));
         }
      } catch (NumberFormatException var10) {
         sender.sendMessage(UtilFormat.format(Ad.getPn(), "fail", new Object[]{this.get(35)}));
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(Ad.getPn())) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onTime(TimeEvent e) {
      if (this.left <= 0) {
         if (!this.waitList.isEmpty()) {
            this.left = this.bigLast;
            UtilBar.clearMsg();
            UtilBar.addMsg((String)this.waitList.remove(0));
         }
      } else {
         --this.left;
      }

   }

   private void checkInitTicket() {
      if (this.t == null) {
         this.t = (Ticket)Bukkit.getPluginManager().getPlugin("ticket");
      }

   }

   private void big(Player p, String msg) {
      int ticket = Ticket.getTicket(p.getName());
      if (ticket < this.bigCost) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(260)}));
      } else {
         this.checkInitTicket();
         if (this.t.del(Bukkit.getConsoleSender(), p.getName(), this.bigCost, this.pn, this.get(265))) {
            this.addBig(UtilFormat.format(this.pn, "bigShow", new Object[]{p.getName(), msg}));
            p.sendMessage(UtilFormat.format(this.pn, "tip", new Object[]{this.get(270)}));
         }

      }
   }

   private void addBig(String msg) {
      this.waitList.add(msg);
   }

   private void loadConfig(YamlConfiguration config) {
      this.per_ad_admin = config.getString("per_ad_admin");
      this.bigCost = config.getInt("big.cost");
      this.bigLast = config.getInt("big.last");
      this.bigMax = config.getInt("big.max");
   }

   private String get(int id) {
      return UtilFormat.format(Ad.getPn(), id);
   }
}
