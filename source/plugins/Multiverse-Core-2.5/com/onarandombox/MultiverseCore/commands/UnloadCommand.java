package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public class UnloadCommand extends MultiverseCommand {
   public UnloadCommand(MultiverseCore plugin) {
      super(plugin);
      this.setName("Unload World");
      this.setCommandUsage("/mv unload" + ChatColor.GREEN + " {WORLD}");
      this.setArgRange(1, 1);
      this.addKey("mvunload");
      this.addKey("mv unload");
      this.setPermission("multiverse.core.unload", "Unloads a world from Multiverse. This does NOT remove the world folder. This does NOT remove it from the config file.", PermissionDefault.OP);
   }

   public void runCommand(CommandSender sender, List args) {
      if (this.plugin.getMVWorldManager().unloadWorld((String)args.get(0))) {
         Command.broadcastCommandMessage(sender, "Unloaded world '" + (String)args.get(0) + "'!");
      } else {
         sender.sendMessage("Error trying to unload world '" + (String)args.get(0) + "'!");
      }

   }
}
