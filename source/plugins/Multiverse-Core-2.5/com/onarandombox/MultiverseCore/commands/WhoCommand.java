package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class WhoCommand extends MultiverseCommand {
   private MVWorldManager worldManager;

   public WhoCommand(MultiverseCore plugin) {
      super(plugin);
      this.setName("Who?");
      this.setCommandUsage("/mv who" + ChatColor.GOLD + " [WORLD|--all]");
      this.setArgRange(0, 1);
      this.addKey("mv who");
      this.addKey("mvw");
      this.addKey("mvwho");
      this.addCommandExample("/mv who");
      this.addCommandExample(String.format("/mv who %s--all", ChatColor.GREEN));
      this.addCommandExample(String.format("/mv who %smyworld", ChatColor.GOLD));
      this.setPermission("multiverse.core.list.who", "States who is in what world.", PermissionDefault.OP);
      this.worldManager = this.plugin.getMVWorldManager();
   }

   public void runCommand(CommandSender sender, List args) {
      Player p = null;
      boolean showAll = true;
      if (sender instanceof Player) {
         p = (Player)sender;
         showAll = false;
      }

      Player[] onlinePlayers = this.plugin.getServer().getOnlinePlayers();
      Collection<Player> visiblePlayers = new HashSet(onlinePlayers.length);

      for(Player player : onlinePlayers) {
         if (p == null || p.canSee(player)) {
            visiblePlayers.add(player);
         }
      }

      if (args.size() == 1) {
         if (!((String)args.get(0)).equalsIgnoreCase("--all") && !((String)args.get(0)).equalsIgnoreCase("-a")) {
            MultiverseWorld world = this.worldManager.getMVWorld((String)args.get(0));
            if (world == null) {
               sender.sendMessage(ChatColor.RED + "That world does not exist.");
               return;
            }

            if (!this.plugin.getMVPerms().canEnterWorld(p, world)) {
               sender.sendMessage(ChatColor.RED + "You aren't allowed to access to this world!");
               return;
            }

            sender.sendMessage(String.format("%s--- Players in %s%s ---", ChatColor.AQUA, world.getColoredWorldString(), ChatColor.AQUA));
            sender.sendMessage(buildPlayerString(world, p, visiblePlayers));
            return;
         }

         showAll = true;
      }

      sender.sendMessage(ChatColor.AQUA + "--- Worlds and their players --- " + visiblePlayers.size() + "/" + this.plugin.getServer().getMaxPlayers());
      boolean shownOne = false;

      for(MultiverseWorld world : this.worldManager.getMVWorlds()) {
         if (this.plugin.getMVPerms().canEnterWorld(p, world) && (showAll || !world.getCBWorld().getPlayers().isEmpty())) {
            sender.sendMessage(String.format("%s%s - %s", world.getColoredWorldString(), ChatColor.WHITE, buildPlayerString(world, p, visiblePlayers)));
            shownOne = true;
         }
      }

      if (!shownOne) {
         sender.sendMessage("No worlds found.");
      }

   }

   private static String buildPlayerString(MultiverseWorld world, Player viewer, Collection visiblePlayers) {
      List<Player> players = world.getCBWorld().getPlayers();
      StringBuilder playerBuilder = new StringBuilder();

      for(Player player : players) {
         if (visiblePlayers.contains(player)) {
            playerBuilder.append(player.getDisplayName()).append(", ");
         }
      }

      String bString = playerBuilder.toString();
      return bString.length() == 0 ? "No players found." : bString.substring(0, bString.length() - 2);
   }
}
