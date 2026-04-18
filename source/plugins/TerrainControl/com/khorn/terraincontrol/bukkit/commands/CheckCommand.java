package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import com.khorn.terraincontrol.configuration.WorldConfig;
import java.io.File;
import java.util.List;
import org.bukkit.command.CommandSender;

public class CheckCommand extends BaseCommand {
   public CheckCommand(TCPlugin _plugin) {
      super(_plugin);
      this.name = "check";
      this.perm = TCPerm.CMD_CHECK.node;
      this.usage = "check World_Name";
      this.workOnConsole = true;
   }

   public boolean onCommand(CommandSender sender, List args) {
      if (args.size() == 0) {
         sender.sendMessage(ERROR_COLOR + "You need to select world");
         return true;
      } else {
         String worldName = (String)args.get(0);
         File settingsFolder = this.plugin.getWorldSettingsFolder(worldName);
         new WorldConfig(settingsFolder, new BukkitWorld(worldName), true);
         sender.sendMessage(MESSAGE_COLOR + "Done!");
         return true;
      }
   }
}
