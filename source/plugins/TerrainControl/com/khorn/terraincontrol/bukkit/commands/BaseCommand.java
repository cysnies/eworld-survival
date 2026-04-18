package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public abstract class BaseCommand {
   public String name;
   public String perm;
   public String usage;
   public boolean workOnConsole;
   protected TCPlugin plugin;
   public static final String ERROR_COLOR;
   public static final String MESSAGE_COLOR;
   public static final String VALUE_COLOR;

   static {
      ERROR_COLOR = ChatColor.RED.toString();
      MESSAGE_COLOR = ChatColor.GREEN.toString();
      VALUE_COLOR = ChatColor.DARK_GREEN.toString();
   }

   public BaseCommand(TCPlugin _plugin) {
      super();
      this.plugin = _plugin;
   }

   public abstract boolean onCommand(CommandSender var1, List var2);

   protected BukkitWorld getWorld(CommandSender sender, String arg) {
      if (arg.equals("")) {
         if (sender instanceof ConsoleCommandSender) {
            return null;
         } else {
            return sender instanceof Player && this.plugin.worlds.containsKey(((Player)sender).getWorld().getUID()) ? (BukkitWorld)this.plugin.worlds.get(((Player)sender).getWorld().getUID()) : null;
         }
      } else {
         World world = Bukkit.getWorld(arg);
         return world != null && this.plugin.worlds.containsKey(world.getUID()) ? (BukkitWorld)this.plugin.worlds.get(world.getUID()) : null;
      }
   }

   protected void ListMessage(CommandSender sender, List lines, int page, String... headers) {
      int pageCount = (lines.size() >> 3) + 1;
      if (page > pageCount) {
         page = pageCount;
      }

      sender.sendMessage(ChatColor.AQUA.toString() + headers[0] + " - page " + page + "/" + pageCount);

      for(int headerId = 1; headerId < headers.length; ++headerId) {
         sender.sendMessage(ChatColor.AQUA + headers[headerId]);
      }

      --page;

      for(int i = page * 8; i < lines.size() && i < page * 8 + 8; ++i) {
         sender.sendMessage((String)lines.get(i));
      }

   }

   public String getHelp() {
      String ret = "do that";
      Permission permission = Bukkit.getPluginManager().getPermission(this.perm);
      if (permission != null) {
         String desc = permission.getDescription();
         if (desc != null && desc.trim().length() > 0) {
            ret = desc.trim();
         }
      }

      return ret;
   }
}
