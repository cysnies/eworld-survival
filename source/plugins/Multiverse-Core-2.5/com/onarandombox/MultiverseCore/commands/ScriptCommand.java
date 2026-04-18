package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import java.io.File;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class ScriptCommand extends MultiverseCommand {
   public ScriptCommand(MultiverseCore plugin) {
      super(plugin);
      this.setName("Runs a script.");
      this.setCommandUsage("/mv script" + ChatColor.GOLD + " {script} [target]");
      this.setArgRange(1, 2);
      this.addKey("mv script");
      this.addKey("mvscript");
      this.addCommandExample(String.format("/mv script %sscript.txt", ChatColor.GOLD));
      this.addCommandExample(String.format("/mv script %stest.txt %ssomeplayer", ChatColor.GOLD, ChatColor.GREEN));
      this.setPermission("multiverse.core.script", "Runs a script.", PermissionDefault.OP);
   }

   public void runCommand(CommandSender sender, List args) {
      File file = new File(this.plugin.getScriptAPI().getScriptFolder(), (String)args.get(0));
      if (!file.exists()) {
         sender.sendMessage("That script file does not exist in the Multiverse-Core scripts directory!");
      } else {
         Player player = null;
         if (sender instanceof Player) {
            player = (Player)sender;
         }

         String target = null;
         if (args.size() == 2) {
            target = (String)args.get(1);
         }

         this.plugin.getScriptAPI().executeScript(file, target, player);
         sender.sendMessage(String.format("Script '%s%s%s' finished!", ChatColor.GOLD, file.getName(), ChatColor.WHITE));
      }
   }
}
