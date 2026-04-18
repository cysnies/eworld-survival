package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.pneumaticraft.commandhandler.multiverse.CommandHandler;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public class CreateCommand extends MultiverseCommand {
   private MVWorldManager worldManager;

   public CreateCommand(MultiverseCore plugin) {
      super(plugin);
      this.setName("Create World");
      this.setCommandUsage(String.format("/mv create %s{NAME} {ENV} %s-s [SEED] -g [GENERATOR[:ID]] -t [WORLDTYPE] [-n] -a [true|false]", ChatColor.GREEN, ChatColor.GOLD));
      this.setArgRange(2, 11);
      this.addKey("mvcreate");
      this.addKey("mvc");
      this.addKey("mv create");
      this.setPermission("multiverse.core.create", "Creates a new world and loads it.", PermissionDefault.OP);
      this.addCommandExample("/mv create " + ChatColor.GOLD + "world" + ChatColor.GREEN + " normal");
      this.addCommandExample("/mv create " + ChatColor.GOLD + "lavaland" + ChatColor.RED + " nether");
      this.addCommandExample("/mv create " + ChatColor.GOLD + "starwars" + ChatColor.AQUA + " end");
      this.addCommandExample("/mv create " + ChatColor.GOLD + "flatroom" + ChatColor.GREEN + " normal" + ChatColor.AQUA + " -t flat");
      this.addCommandExample("/mv create " + ChatColor.GOLD + "gargamel" + ChatColor.GREEN + " normal" + ChatColor.DARK_AQUA + " -s gargamel");
      this.addCommandExample("/mv create " + ChatColor.GOLD + "moonworld" + ChatColor.GREEN + " normal" + ChatColor.DARK_AQUA + " -g BukkitFullOfMoon");
      this.worldManager = this.plugin.getMVWorldManager();
   }

   public void runCommand(CommandSender sender, List args) {
      String worldName = (String)args.get(0);
      File worldFile = new File(this.plugin.getServer().getWorldContainer(), worldName);
      String env = (String)args.get(1);
      String seed = CommandHandler.getFlag("-s", args);
      String generator = CommandHandler.getFlag("-g", args);
      boolean allowStructures = true;
      String structureString = CommandHandler.getFlag("-a", args);
      if (structureString != null) {
         allowStructures = Boolean.parseBoolean(structureString);
      }

      String typeString = CommandHandler.getFlag("-t", args);
      boolean useSpawnAdjust = true;

      for(String s : args) {
         if (s.equalsIgnoreCase("-n")) {
            useSpawnAdjust = false;
         }
      }

      if (this.worldManager.isMVWorld(worldName)) {
         sender.sendMessage(ChatColor.RED + "Multiverse cannot create " + ChatColor.GOLD + ChatColor.UNDERLINE + "another" + ChatColor.RESET + ChatColor.RED + " world named " + worldName);
      } else if (worldFile.exists()) {
         sender.sendMessage(ChatColor.RED + "A Folder/World already exists with this name!");
         sender.sendMessage(ChatColor.RED + "If you are confident it is a world you can import with /mvimport");
      } else {
         World.Environment environment = EnvironmentCommand.getEnvFromString(env);
         if (environment == null) {
            sender.sendMessage(ChatColor.RED + "That is not a valid environment.");
            EnvironmentCommand.showEnvironments(sender);
         } else {
            if (typeString == null) {
               typeString = "NORMAL";
            }

            WorldType type = EnvironmentCommand.getWorldTypeFromString(typeString);
            if (type == null) {
               sender.sendMessage(ChatColor.RED + "That is not a valid World Type.");
               EnvironmentCommand.showWorldTypes(sender);
            } else {
               if (generator != null) {
                  List<String> genarray = new ArrayList(Arrays.asList(generator.split(":")));
                  if (genarray.size() < 2) {
                     genarray.add("");
                  }

                  if (this.worldManager.getChunkGenerator((String)genarray.get(0), (String)genarray.get(1), "test") == null) {
                     sender.sendMessage("Invalid generator! '" + generator + "'. " + ChatColor.RED + "Aborting world creation.");
                     return;
                  }
               }

               Command.broadcastCommandMessage(sender, "Starting creation of world '" + worldName + "'...");
               if (this.worldManager.addWorld(worldName, environment, seed, type, allowStructures, generator, useSpawnAdjust)) {
                  Command.broadcastCommandMessage(sender, "Complete!");
               } else {
                  Command.broadcastCommandMessage(sender, "FAILED.");
               }

            }
         }
      }
   }
}
