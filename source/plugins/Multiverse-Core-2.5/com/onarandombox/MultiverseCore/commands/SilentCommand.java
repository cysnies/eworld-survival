package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public class SilentCommand extends MultiverseCommand {
   public SilentCommand(MultiverseCore plugin) {
      super(plugin);
      this.setName("Turn silent mode on/off?");
      this.setCommandUsage("/mv silent" + ChatColor.GOLD + " [true|false|on|off]");
      this.setArgRange(0, 1);
      this.addKey("mv silent");
      this.addKey("mvsilent");
      this.addCommandExample("/mv silent " + ChatColor.GOLD + "true");
      this.setPermission("multiverse.core.silent", "Reduces the amount of startup messages.", PermissionDefault.OP);
   }

   public void runCommand(CommandSender sender, List args) {
      if (args.size() == 1) {
         if (((String)args.get(0)).equalsIgnoreCase("on")) {
            args.set(0, "true");
         }

         this.plugin.getMVConfig().setSilentStart(Boolean.valueOf((String)args.get(0)));
         this.plugin.saveMVConfigs();
      }

      this.displaySilentMode(sender);
   }

   private void displaySilentMode(CommandSender sender) {
      if (this.plugin.getMVConfig().getSilentStart()) {
         sender.sendMessage("Multiverse Silent Start mode is " + ChatColor.GREEN + "ON");
      } else {
         sender.sendMessage("Multiverse Silent Start mode is " + ChatColor.RED + "OFF");
      }

   }
}
