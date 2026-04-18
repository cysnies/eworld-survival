package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public class DebugCommand extends MultiverseCommand {
   public DebugCommand(MultiverseCore plugin) {
      super(plugin);
      this.setName("Turn Debug on/off?");
      this.setCommandUsage("/mv debug" + ChatColor.GOLD + " [1|2|3|off|silent]");
      this.setArgRange(0, 1);
      this.addKey("mv debug");
      this.addKey("mv d");
      this.addKey("mvdebug");
      this.addCommandExample("/mv debug " + ChatColor.GOLD + "2");
      this.setPermission("multiverse.core.debug", "Spams the console a bunch.", PermissionDefault.OP);
   }

   public void runCommand(CommandSender sender, List args) {
      if (args.size() == 1) {
         if (((String)args.get(0)).equalsIgnoreCase("off")) {
            this.plugin.getMVConfig().setGlobalDebug(0);
         } else {
            try {
               int debugLevel = Integer.parseInt((String)args.get(0));
               if (debugLevel > 3 || debugLevel < 0) {
                  throw new NumberFormatException();
               }

               this.plugin.getMVConfig().setGlobalDebug(debugLevel);
            } catch (NumberFormatException var4) {
               sender.sendMessage(ChatColor.RED + "Error" + ChatColor.WHITE + " setting debug level. Please use a number 0-3 " + ChatColor.AQUA + "(3 being many many messages!)");
            }
         }

         this.plugin.saveMVConfigs();
      }

      this.displayDebugMode(sender);
   }

   private void displayDebugMode(CommandSender sender) {
      int debugLevel = this.plugin.getMVConfig().getGlobalDebug();
      if (debugLevel == 0) {
         sender.sendMessage("Multiverse Debug mode is " + ChatColor.RED + "OFF");
      } else {
         sender.sendMessage("Multiverse Debug mode is " + ChatColor.GREEN + debugLevel);
         this.plugin.log(Level.FINE, "Multiverse Debug ENABLED");
      }

   }
}
