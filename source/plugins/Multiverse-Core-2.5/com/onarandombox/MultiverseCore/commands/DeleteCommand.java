package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public class DeleteCommand extends MultiverseCommand {
   public DeleteCommand(MultiverseCore plugin) {
      super(plugin);
      this.setName("Delete World");
      this.setCommandUsage("/mv delete" + ChatColor.GREEN + " {WORLD}");
      this.setArgRange(1, 1);
      this.addKey("mvdelete");
      this.addKey("mv delete");
      this.addCommandExample("/mv create " + ChatColor.GOLD + "MyWorld");
      this.setPermission("multiverse.core.delete", "Deletes a world on your server. " + ChatColor.RED + "PERMANENTLY.", PermissionDefault.OP);
   }

   public void runCommand(CommandSender sender, List args) {
      Class<?>[] paramTypes = new Class[]{String.class};
      List<Object> objectArgs = new ArrayList(args);
      this.plugin.getCommandHandler().queueCommand(sender, "mvdelete", "deleteWorld", objectArgs, paramTypes, ChatColor.GREEN + "World Deleted!", ChatColor.RED + "World could NOT be deleted!");
   }
}
