package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public class EnvironmentCommand extends MultiverseCommand {
   public EnvironmentCommand(MultiverseCore plugin) {
      super(plugin);
      this.setName("List Environments");
      this.setCommandUsage("/mv env");
      this.setArgRange(0, 0);
      this.addKey("mvenv");
      this.addKey("mv env");
      this.addKey("mv type");
      this.addKey("mv environment");
      this.addKey("mv environments");
      this.addCommandExample("/mv env");
      this.setPermission("multiverse.core.list.environments", "Lists valid known environments/world types.", PermissionDefault.OP);
   }

   public static void showEnvironments(CommandSender sender) {
      sender.sendMessage(ChatColor.YELLOW + "Valid Environments are:");
      sender.sendMessage(ChatColor.GREEN + "NORMAL");
      sender.sendMessage(ChatColor.RED + "NETHER");
      sender.sendMessage(ChatColor.AQUA + "END");
   }

   public static void showWorldTypes(CommandSender sender) {
      sender.sendMessage(ChatColor.YELLOW + "Valid World Types are:");
      sender.sendMessage(String.format("%sNORMAL%s, %sFLAT, %sLARGEBIOMES %sor %sVERSION_1_1", ChatColor.GREEN, ChatColor.WHITE, ChatColor.AQUA, ChatColor.RED, ChatColor.WHITE, ChatColor.GOLD));
   }

   public void runCommand(CommandSender sender, List args) {
      showEnvironments(sender);
      showWorldTypes(sender);
   }

   public static WorldType getWorldTypeFromString(String type) {
      if (type.equalsIgnoreCase("normal")) {
         type = "NORMAL";
      }

      if (type.equalsIgnoreCase("flat")) {
         type = "FLAT";
      }

      if (type.equalsIgnoreCase("largebiomes")) {
         type = "LARGE_BIOMES";
      }

      try {
         return WorldType.valueOf(type);
      } catch (IllegalArgumentException var2) {
         return null;
      }
   }

   public static World.Environment getEnvFromString(String env) {
      env = env.toUpperCase();
      if (env.equalsIgnoreCase("HELL") || env.equalsIgnoreCase("NETHER")) {
         env = "NETHER";
      }

      if (env.equalsIgnoreCase("END") || env.equalsIgnoreCase("THEEND") || env.equalsIgnoreCase("STARWARS")) {
         env = "THE_END";
      }

      if (env.equalsIgnoreCase("NORMAL") || env.equalsIgnoreCase("WORLD")) {
         env = "NORMAL";
      }

      try {
         return Environment.valueOf(env);
      } catch (IllegalArgumentException var2) {
         return null;
      }
   }
}
