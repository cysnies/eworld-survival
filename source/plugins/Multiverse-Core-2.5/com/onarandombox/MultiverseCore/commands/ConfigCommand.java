package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public class ConfigCommand extends MultiverseCommand {
   public ConfigCommand(MultiverseCore plugin) {
      super(plugin);
      this.setName("Configuration");
      this.setCommandUsage("/mv config " + ChatColor.GREEN + "{PROPERTY} {VALUE}");
      this.setArgRange(1, 2);
      this.addKey("mv config");
      this.addKey("mvconfig");
      this.addKey("mv conf");
      this.addKey("mvconf");
      this.addCommandExample("/mv config show");
      this.addCommandExample("/mv config " + ChatColor.GREEN + "debug" + ChatColor.AQUA + " 3");
      this.addCommandExample("/mv config " + ChatColor.GREEN + "enforceaccess" + ChatColor.AQUA + " false");
      this.setPermission("multiverse.core.config", "Allows you to set Global MV Variables.", PermissionDefault.OP);
   }

   public void runCommand(CommandSender sender, List args) {
      if (args.size() > 1) {
         if (!this.plugin.getMVConfig().setConfigProperty(((String)args.get(0)).toLowerCase(), (String)args.get(1))) {
            sender.sendMessage(String.format("%sSetting '%s' to '%s' failed!", ChatColor.RED, ((String)args.get(0)).toLowerCase(), args.get(1)));
         } else {
            if (((String)args.get(0)).equalsIgnoreCase("firstspawnworld")) {
               this.plugin.getMVWorldManager().setFirstSpawnWorld((String)args.get(1));
            }

            if (this.plugin.saveMVConfigs()) {
               sender.sendMessage(ChatColor.GREEN + "SUCCESS!" + ChatColor.WHITE + " Values were updated successfully!");
               this.plugin.loadConfigs();
            } else {
               sender.sendMessage(ChatColor.RED + "FAIL!" + ChatColor.WHITE + " Check your console for details!");
            }

         }
      } else {
         StringBuilder builder = new StringBuilder();
         Map<String, Object> serializedConfig = this.plugin.getMVConfig().serialize();

         for(Map.Entry entry : serializedConfig.entrySet()) {
            builder.append(ChatColor.GREEN);
            builder.append((String)entry.getKey());
            builder.append(ChatColor.WHITE).append(" = ").append(ChatColor.GOLD);
            builder.append(entry.getValue().toString());
            builder.append(ChatColor.WHITE).append(", ");
         }

         String message = builder.toString();
         message = message.substring(0, message.length() - 2);
         sender.sendMessage(message);
      }
   }
}
