package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public class RemoveCommand extends MultiverseCommand {
   public RemoveCommand(MultiverseCore plugin) {
      super(plugin);
      this.setName("Remove World");
      this.setCommandUsage("/mv remove" + ChatColor.GREEN + " {WORLD}");
      this.setArgRange(1, 1);
      this.addKey("mvremove");
      this.addKey("mv remove");
      this.addCommandExample("/mv remove " + ChatColor.GREEN + "MyWorld");
      this.setPermission("multiverse.core.remove", "Unloads a world from Multiverse and removes it from worlds.yml, this does NOT DELETE the world folder.", PermissionDefault.OP);
   }

   public void runCommand(CommandSender sender, List args) {
      if (this.plugin.getMVWorldManager().removeWorldFromConfig((String)args.get(0))) {
         sender.sendMessage("World removed from config!");
      } else {
         sender.sendMessage("Error trying to remove world from config!");
      }

   }
}
