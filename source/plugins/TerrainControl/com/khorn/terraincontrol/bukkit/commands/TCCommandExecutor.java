package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.bukkit.TCPlugin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TCCommandExecutor implements CommandExecutor {
   protected final TCPlugin plugin;
   protected HashMap commandHashMap = new HashMap();
   protected HelpCommand helpCommand;

   public TCCommandExecutor(TCPlugin plugin) {
      super();
      this.plugin = plugin;
      this.helpCommand = new HelpCommand(plugin);
      this.RegisterCommands();
   }

   public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
      ArrayList<String> arg = new ArrayList(Arrays.asList(strings));
      BaseCommand cmd = this.helpCommand;
      if (arg.size() != 0 && this.commandHashMap.containsKey(arg.get(0))) {
         cmd = (BaseCommand)this.commandHashMap.get(arg.get(0));
         arg.remove(0);
      }

      if (!commandSender.hasPermission(cmd.perm)) {
         commandSender.sendMessage(ChatColor.RED.toString() + "You don't have permission to " + cmd.getHelp() + "!");
         return true;
      } else {
         return cmd.onCommand(commandSender, arg);
      }
   }

   private void RegisterCommands() {
      this.AddCommand(new ReloadCommand(this.plugin));
      this.AddCommand(new ListCommand(this.plugin));
      this.AddCommand(new CheckCommand(this.plugin));
      this.AddCommand(new BiomeCommand(this.plugin));
      this.AddCommand(new SpawnCommand(this.plugin));
      this.AddCommand(new MapCommand(this.plugin));
      this.AddCommand(new ReplaceBiomeCommand(this.plugin));
      this.AddCommand(this.helpCommand);
   }

   private void AddCommand(BaseCommand command) {
      this.commandHashMap.put(command.name, command);
   }
}
