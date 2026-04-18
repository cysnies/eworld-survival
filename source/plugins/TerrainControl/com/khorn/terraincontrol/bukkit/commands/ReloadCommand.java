package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import com.khorn.terraincontrol.configuration.WorldConfig;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand extends BaseCommand {
   public ReloadCommand(TCPlugin _plugin) {
      super(_plugin);
      this.name = "reload";
      this.perm = TCPerm.CMD_RELOAD.node;
      this.usage = "reload [world_name]";
      this.workOnConsole = true;
   }

   public boolean onCommand(CommandSender sender, List args) {
      BukkitWorld world = this.getWorld(sender, args.size() > 0 ? (String)args.get(0) : "");
      if (world == null) {
         sender.sendMessage(ERROR_COLOR + "World not found. Either you are not in a world with Terrain Control, or you are the console.");
         return false;
      } else {
         WorldConfig newSettings = new WorldConfig(this.plugin.getWorldSettingsFolder(world.getName()), world, false);
         world.setSettings(newSettings);
         sender.sendMessage(MESSAGE_COLOR + "Configs for world '" + world.getName() + "' reloaded");
         if (sender instanceof Player) {
            TerrainControl.log(sender.getName() + " reloaded the config files for world '" + world.getName() + "'.");
         }

         return true;
      }
   }
}
