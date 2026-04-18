package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.pneumaticraft.commandhandler.multiverse.CommandHandler;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public class CloneCommand extends MultiverseCommand {
   private MVWorldManager worldManager;

   public CloneCommand(MultiverseCore plugin) {
      super(plugin);
      this.setName("Clone World");
      this.setCommandUsage("/mv clone" + ChatColor.GREEN + " {TARGET} {NAME}" + ChatColor.GOLD + " -g [GENERATOR[:ID]]");
      this.setArgRange(2, 4);
      this.addKey("mvclone");
      this.addKey("mvcl");
      this.addKey("mv cl");
      this.addKey("mv clone");
      this.addCommandExample("/mv clone " + ChatColor.GOLD + "world" + ChatColor.GREEN + " world_backup");
      this.addCommandExample("/mv clone " + ChatColor.GOLD + "skyblock_pristine" + ChatColor.GREEN + " skyblock");
      this.addCommandExample("To clone a world that uses a generator:");
      this.addCommandExample("/mv clone " + ChatColor.GOLD + "CleanRoom" + ChatColor.GREEN + " CleanRoomCopy" + ChatColor.DARK_AQUA + " -g CleanRoomGenerator");
      this.setPermission("multiverse.core.clone", "Clones a world.", PermissionDefault.OP);
      this.worldManager = this.plugin.getMVWorldManager();
   }

   public void runCommand(CommandSender sender, List args) {
      Class<?>[] paramTypes = new Class[]{String.class, String.class, String.class};
      List<Object> objectArgs = new ArrayList();
      objectArgs.add(args.get(0));
      objectArgs.add(args.get(1));
      objectArgs.add(CommandHandler.getFlag("-g", args));
      if (!this.worldManager.isMVWorld((String)args.get(0))) {
         sender.sendMessage("Sorry, Multiverse doesn't know about world " + (String)args.get(0) + ", so we can't clone it!");
         sender.sendMessage("Check the " + ChatColor.GREEN + "/mv list" + ChatColor.WHITE + " command to verify it is listed.");
      } else {
         this.plugin.getCommandHandler().queueCommand(sender, "mvclone", "cloneWorld", objectArgs, paramTypes, ChatColor.GREEN + "World Cloned!", ChatColor.RED + "World could NOT be cloned!");
      }
   }
}
