package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public class LoadCommand extends MultiverseCommand {
   public LoadCommand(MultiverseCore plugin) {
      super(plugin);
      this.setName("Load World");
      this.setCommandUsage("/mv load" + ChatColor.GREEN + " {WORLD}");
      this.setArgRange(1, 1);
      this.addKey("mvload");
      this.addKey("mv load");
      this.addCommandExample("/mv load " + ChatColor.GREEN + "MyUnloadedWorld");
      this.setPermission("multiverse.core.load", "Loads a world into Multiverse.", PermissionDefault.OP);
   }

   public void runCommand(CommandSender sender, List args) {
      if (this.plugin.getMVWorldManager().loadWorld((String)args.get(0))) {
         Command.broadcastCommandMessage(sender, "Loaded world '" + (String)args.get(0) + "'!");
      } else {
         sender.sendMessage("Error trying to load world '" + (String)args.get(0) + "'!");
      }

   }
}
