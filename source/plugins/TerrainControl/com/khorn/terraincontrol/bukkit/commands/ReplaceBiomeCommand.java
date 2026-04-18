package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.bukkit.BiomeReplace;
import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.entity.Player;

public class ReplaceBiomeCommand extends BaseCommand {
   public ReplaceBiomeCommand(TCPlugin _plugin) {
      super(_plugin);
      this.name = "replace";
      this.perm = TCPerm.CMD_REPLACE.node;
      this.usage = "replace biomeIDFrom biomeIdTo [World]";
      this.workOnConsole = true;
   }

   public boolean onCommand(CommandSender sender, List args) {
      CraftWorld world = null;
      if (args.size() == 3) {
         world = (CraftWorld)Bukkit.getWorld((String)args.get(0));
         args.remove(0);
         if (world == null) {
            sender.sendMessage(ERROR_COLOR + "You need to select world");
            return true;
         }
      }

      if (world == null) {
         if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(ERROR_COLOR + "You need to select world");
            return true;
         }

         world = (CraftWorld)((Player)sender).getWorld();
      }

      int biomeIdFrom;
      int biomeIdTo;
      try {
         biomeIdFrom = Integer.parseInt((String)args.get(0));
         biomeIdTo = Integer.parseInt((String)args.get(1));
         args.remove(0);
         args.remove(0);
      } catch (Exception var7) {
         sender.sendMessage(ERROR_COLOR + "Wrong biome ids ");
         return true;
      }

      BiomeReplace replace = new BiomeReplace(world, biomeIdFrom, biomeIdTo, sender);
      this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, replace);
      return true;
   }
}
