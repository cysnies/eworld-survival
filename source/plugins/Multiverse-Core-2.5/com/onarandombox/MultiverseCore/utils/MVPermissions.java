package com.onarandombox.MultiverseCore.utils;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.pneumaticraft.commandhandler.multiverse.PermissionsInterface;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class MVPermissions implements PermissionsInterface {
   private MultiverseCore plugin;
   private MVWorldManager worldMgr;

   public MVPermissions(MultiverseCore plugin) {
      super();
      this.plugin = plugin;
      this.worldMgr = plugin.getMVWorldManager();
   }

   public boolean canIgnoreGameModeRestriction(Player p, MultiverseWorld w) {
      return p.hasPermission("mv.bypass.gamemode." + w.getName());
   }

   public boolean canTravelFromWorld(Player p, MultiverseWorld w) {
      List<String> blackList = w.getWorldBlacklist();
      boolean returnValue = true;

      for(String s : blackList) {
         if (s.equalsIgnoreCase(p.getWorld().getName())) {
            returnValue = false;
            break;
         }
      }

      return returnValue;
   }

   public boolean canTravelFromLocation(CommandSender sender, Location location) {
      if (location == null) {
         return false;
      } else if (!(sender instanceof Player)) {
         return true;
      } else {
         Player teleporter = (Player)sender;
         return !this.worldMgr.isMVWorld(location.getWorld().getName()) ? false : this.canTravelFromWorld(teleporter, this.worldMgr.getMVWorld(location.getWorld().getName()));
      }
   }

   public boolean canEnterWorld(Player p, MultiverseWorld w) {
      if (!this.plugin.getMVConfig().getEnforceAccess()) {
         this.plugin.log(Level.FINEST, "EnforceAccess is OFF. Player was allowed in " + w.getAlias());
         return true;
      } else {
         return this.hasPermission(p, "multiverse.access." + w.getName(), false);
      }
   }

   private boolean canEnterLocation(Player p, Location l) {
      if (l == null) {
         return false;
      } else {
         String worldName = l.getWorld().getName();
         return !this.plugin.getMVWorldManager().isMVWorld(worldName) ? false : this.hasPermission(p, "multiverse.access." + worldName, false);
      }
   }

   public boolean canEnterDestination(CommandSender sender, MVDestination d) {
      if (!(sender instanceof Player)) {
         return true;
      } else {
         Player p = (Player)sender;
         if (d != null && d.getLocation(p) != null) {
            String worldName = d.getLocation(p).getWorld().getName();
            if (!this.worldMgr.isMVWorld(worldName)) {
               return false;
            } else {
               return !this.canEnterLocation(p, d.getLocation(p)) ? false : this.hasPermission(p, d.getRequiredPermission(), false);
            }
         } else {
            return false;
         }
      }
   }

   public void tellMeWhyICantDoThis(CommandSender asker, CommandSender playerInQuestion, MVDestination d) {
      boolean cango = true;
      if (!(playerInQuestion instanceof Player)) {
         asker.sendMessage(String.format("The console can do %severything%s.", ChatColor.RED, ChatColor.WHITE));
      } else {
         Player p = (Player)playerInQuestion;
         if (d == null) {
            asker.sendMessage(String.format("The provided Destination is %sNULL%s, and therefore %sINVALID%s.", ChatColor.RED, ChatColor.WHITE, ChatColor.RED, ChatColor.WHITE));
            cango = false;
         }

         if (d.getLocation(p) == null) {
            asker.sendMessage(String.format("The player will spawn at an %sindeterminate location%s. Talk to the MV Devs if you see this", ChatColor.RED, ChatColor.WHITE));
            cango = false;
         }

         String worldName = d.getLocation(p).getWorld().getName();
         if (!this.worldMgr.isMVWorld(worldName)) {
            asker.sendMessage(String.format("The destination resides in a world(%s%s%s) that is not managed by Multiverse.", ChatColor.AQUA, worldName, ChatColor.WHITE));
            asker.sendMessage(String.format("Type %s/mv import ?%s to see the import command's help page.", ChatColor.DARK_AQUA, ChatColor.WHITE));
            cango = false;
         }

         if (!this.hasPermission(p, "multiverse.access." + worldName, false)) {
            asker.sendMessage(String.format("The player (%s%s%s) does not have the required world entry permission (%s%s%s) to go to the destination (%s%s%s).", ChatColor.AQUA, p.getDisplayName(), ChatColor.WHITE, ChatColor.GREEN, "multiverse.access." + worldName, ChatColor.WHITE, ChatColor.DARK_AQUA, d.getName(), ChatColor.WHITE));
            cango = false;
         }

         if (!this.hasPermission(p, d.getRequiredPermission(), false)) {
            asker.sendMessage(String.format("The player (%s%s%s) does not have the required entry permission (%s%s%s) to go to the destination (%s%s%s).", ChatColor.AQUA, p.getDisplayName(), ChatColor.WHITE, ChatColor.GREEN, d.getRequiredPermission(), ChatColor.WHITE, ChatColor.DARK_AQUA, d.getName(), ChatColor.WHITE));
            cango = false;
         }

         if (cango) {
            asker.sendMessage(String.format("The player (%s%s%s) CAN go to the destination (%s%s%s).", ChatColor.AQUA, p.getDisplayName(), ChatColor.WHITE, ChatColor.DARK_AQUA, d.getName(), ChatColor.WHITE));
            if (!this.hasPermission(p, "multiverse.teleport.self." + d.getIdentifier(), false)) {
               asker.sendMessage(String.format("The player (%s%s%s) does not have the required teleport permission (%s%s%s) to use %s/mvtp %s%s.", ChatColor.AQUA, p.getDisplayName(), ChatColor.WHITE, ChatColor.GREEN, "multiverse.teleport.self." + d.getIdentifier(), ChatColor.WHITE, ChatColor.DARK_AQUA, d.getName(), ChatColor.WHITE));
            } else {
               asker.sendMessage(String.format("The player (%s%s%s) has the required teleport permission (%s%s%s) to use %s/mvtp %s%s.", ChatColor.AQUA, p.getDisplayName(), ChatColor.WHITE, ChatColor.GREEN, "multiverse.teleport.self." + d.getIdentifier(), ChatColor.WHITE, ChatColor.DARK_AQUA, d.getName(), ChatColor.WHITE));
            }

            if (!this.hasPermission(p, "multiverse.teleport.other." + d.getIdentifier(), false)) {
               asker.sendMessage(String.format("The player (%s%s%s) does not have the required teleport permission (%s%s%s) to send others to %s%s%s via mvtp.", ChatColor.AQUA, p.getDisplayName(), ChatColor.WHITE, ChatColor.GREEN, "multiverse.teleport.other." + d.getIdentifier(), ChatColor.WHITE, ChatColor.DARK_AQUA, d.getName(), ChatColor.WHITE));
            } else {
               asker.sendMessage(String.format("The player (%s%s%s) has required teleport permission (%s%s%s) to send others to %s%s%s via mvtp.", ChatColor.AQUA, p.getDisplayName(), ChatColor.WHITE, ChatColor.GREEN, "multiverse.teleport.other." + d.getIdentifier(), ChatColor.WHITE, ChatColor.DARK_AQUA, d.getName(), ChatColor.WHITE));
            }

         } else {
            asker.sendMessage(String.format("The player (%s%s%s) cannot access the destination %s%s%s. Therefore they can't use mvtp at all for this.", ChatColor.AQUA, p.getDisplayName(), ChatColor.WHITE, ChatColor.DARK_AQUA, d.getName(), ChatColor.WHITE));
         }
      }
   }

   public boolean hasPermission(CommandSender sender, String node, boolean isOpRequired) {
      if (!(sender instanceof Player)) {
         return true;
      } else if (node == null) {
         return false;
      } else {
         return node.equals("") ? true : this.checkActualPermission(sender, node);
      }
   }

   private boolean checkActualPermission(CommandSender sender, String node) {
      Player player = (Player)sender;
      boolean hasPermission = sender.hasPermission(node);
      if (!sender.isPermissionSet(node)) {
         this.plugin.log(Level.FINER, String.format("The node [%s%s%s] was %sNOT%s set for [%s%s%s].", ChatColor.RED, node, ChatColor.WHITE, ChatColor.RED, ChatColor.WHITE, ChatColor.AQUA, player.getDisplayName(), ChatColor.WHITE));
      }

      if (hasPermission) {
         this.plugin.log(Level.FINER, "Checking to see if player [" + player.getName() + "] has permission [" + node + "]... YES");
      } else {
         this.plugin.log(Level.FINER, "Checking to see if player [" + player.getName() + "] has permission [" + node + "]... NO");
      }

      return hasPermission;
   }

   private boolean hasAnyParentPermission(CommandSender sender, String node) {
      String parentPerm = pullOneLevelOff(node);
      if (parentPerm == null) {
         return false;
      } else {
         return this.checkActualPermission(sender, parentPerm + ".*") ? true : this.hasAnyParentPermission(sender, parentPerm);
      }
   }

   private static String pullOneLevelOff(String node) {
      if (node == null) {
         return null;
      } else {
         int index = node.lastIndexOf(".");
         return index > 0 ? node.substring(0, index) : null;
      }
   }

   public String getType() {
      return "Bukkit Permissions (SuperPerms)";
   }

   public boolean hasAnyPermission(CommandSender sender, List nodes, boolean isOpRequired) {
      for(String node : nodes) {
         if (this.hasPermission(sender, node, isOpRequired)) {
            return true;
         }
      }

      return false;
   }

   public boolean hasAllPermission(CommandSender sender, List nodes, boolean isOpRequired) {
      for(String node : nodes) {
         if (!this.hasPermission(sender, node, isOpRequired)) {
            return false;
         }
      }

      return true;
   }

   public Permission addPermission(String string, PermissionDefault defaultValue) {
      if (this.plugin.getServer().getPluginManager().getPermission(string) == null) {
         Permission permission = new Permission(string, defaultValue);
         this.plugin.getServer().getPluginManager().addPermission(permission);
         this.addToParentPerms(string);
      }

      return this.plugin.getServer().getPluginManager().getPermission(string);
   }

   private void addToParentPerms(String permString) {
      String permStringChopped = permString.replace(".*", "");
      String[] seperated = permStringChopped.split("\\.");
      String parentPermString = getParentPerm(seperated);
      if (parentPermString == null) {
         this.addToRootPermission("*", permStringChopped);
         this.addToRootPermission("*.*", permStringChopped);
      } else {
         Permission parentPermission = this.plugin.getServer().getPluginManager().getPermission(parentPermString);
         if (parentPermission == null) {
            parentPermission = new Permission(parentPermString);
            this.plugin.getServer().getPluginManager().addPermission(parentPermission);
            this.addToParentPerms(parentPermString);
         }

         Permission actualPermission = this.plugin.getServer().getPluginManager().getPermission(permString);
         if (actualPermission == null) {
            actualPermission = new Permission(permString);
            this.plugin.getServer().getPluginManager().addPermission(actualPermission);
         }

         if (!parentPermission.getChildren().containsKey(permString)) {
            parentPermission.getChildren().put(actualPermission.getName(), true);
            this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(parentPermission);
         }

      }
   }

   private void addToRootPermission(String rootPerm, String permStringChopped) {
      Permission rootPermission = this.plugin.getServer().getPluginManager().getPermission(rootPerm);
      if (rootPermission == null) {
         rootPermission = new Permission(rootPerm);
         this.plugin.getServer().getPluginManager().addPermission(rootPermission);
      }

      rootPermission.getChildren().put(permStringChopped + ".*", true);
      this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(rootPermission);
   }

   private static String getParentPerm(String[] seperated) {
      if (seperated.length == 1) {
         return null;
      } else {
         String returnString = "";

         for(int i = 0; i < seperated.length - 1; ++i) {
            returnString = returnString + seperated[i] + ".";
         }

         return returnString + "*";
      }
   }
}
