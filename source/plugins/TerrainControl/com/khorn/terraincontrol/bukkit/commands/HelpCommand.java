package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;

public class HelpCommand extends BaseCommand {
   public HelpCommand(TCPlugin _plugin) {
      super(_plugin);
      this.name = "help";
      this.perm = TCPerm.CMD_HELP.node;
      this.usage = "help";
      this.workOnConsole = false;
   }

   public boolean onCommand(CommandSender sender, List args) {
      List<String> lines = new ArrayList();

      for(BaseCommand command : this.plugin.commandExecutor.commandHashMap.values()) {
         lines.add(MESSAGE_COLOR + "/tc " + command.usage + " - " + command.getHelp());
      }

      int page = 1;
      if (args.size() > 0) {
         try {
            page = Integer.parseInt((String)args.get(0));
         } catch (NumberFormatException var6) {
            sender.sendMessage(ERROR_COLOR + "Wrong page number " + (String)args.get(0));
         }
      }

      this.ListMessage(sender, lines, page, new String[]{"Available commands"});
      return true;
   }
}
