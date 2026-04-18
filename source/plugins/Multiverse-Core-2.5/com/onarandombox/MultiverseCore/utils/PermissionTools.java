package com.onarandombox.MultiverseCore.utils;

import com.fernferret.allpay.multiverse.GenericBank;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public class PermissionTools {
   private MultiverseCore plugin;

   public PermissionTools(MultiverseCore plugin) {
      super();
      this.plugin = plugin;
   }

   public void addToParentPerms(String permString) {
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

   private static String getParentPerm(String[] separatedPermissionString) {
      if (separatedPermissionString.length == 1) {
         return null;
      } else {
         String returnString = "";

         for(int i = 0; i < separatedPermissionString.length - 1; ++i) {
            returnString = returnString + separatedPermissionString[i] + ".";
         }

         return returnString + "*";
      }
   }

   public boolean playerHasMoneyToEnter(MultiverseWorld fromWorld, MultiverseWorld toWorld, CommandSender teleporter, Player teleportee, boolean pay) {
      Player teleporterPlayer;
      if (this.plugin.getMVConfig().getTeleportIntercept()) {
         if (teleporter instanceof ConsoleCommandSender) {
            return true;
         }

         if (teleporter == null) {
            teleporter = teleportee;
         }

         if (!(teleporter instanceof Player)) {
            return false;
         }

         teleporterPlayer = (Player)teleporter;
      } else {
         if (teleporter instanceof Player) {
            teleporterPlayer = (Player)teleporter;
         } else {
            teleporterPlayer = null;
         }

         if (teleporterPlayer == null) {
            return true;
         }
      }

      if (toWorld == null) {
         return true;
      } else {
         if (!toWorld.equals(fromWorld)) {
            if (toWorld.getPrice() == (double)0.0F) {
               return true;
            }

            if (this.plugin.getMVPerms().hasPermission(teleporter, toWorld.getExemptPermission().getName(), true)) {
               return true;
            }

            boolean usingVault;
            String formattedAmount;
            if (toWorld.getCurrency() <= 0 && this.plugin.getVaultHandler().getEconomy() != null) {
               usingVault = true;
               formattedAmount = this.plugin.getVaultHandler().getEconomy().format(toWorld.getPrice());
            } else {
               usingVault = false;
               formattedAmount = this.plugin.getBank().getFormattedAmount(teleporterPlayer, toWorld.getPrice(), toWorld.getCurrency());
            }

            String errString = "You need " + formattedAmount + " to send " + teleportee + " to " + toWorld.getColoredWorldString();
            if (teleportee.equals(teleporter)) {
               errString = "You need " + formattedAmount + " to enter " + toWorld.getColoredWorldString();
            }

            if (usingVault) {
               if (!this.plugin.getVaultHandler().getEconomy().has(teleporterPlayer.getName(), toWorld.getPrice())) {
                  return false;
               }

               if (pay) {
                  if (toWorld.getPrice() < (double)0.0F) {
                     this.plugin.getVaultHandler().getEconomy().depositPlayer(teleporterPlayer.getName(), toWorld.getPrice() * (double)-1.0F);
                  } else {
                     this.plugin.getVaultHandler().getEconomy().withdrawPlayer(teleporterPlayer.getName(), toWorld.getPrice());
                  }
               }
            } else {
               GenericBank bank = this.plugin.getBank();
               if (!bank.hasEnough(teleporterPlayer, toWorld.getPrice(), toWorld.getCurrency(), errString)) {
                  return false;
               }

               if (pay) {
                  if (toWorld.getPrice() < (double)0.0F) {
                     bank.give(teleporterPlayer, toWorld.getPrice() * (double)-1.0F, toWorld.getCurrency());
                  } else {
                     bank.take(teleporterPlayer, toWorld.getPrice(), toWorld.getCurrency());
                  }
               }
            }
         }

         return true;
      }
   }

   public boolean playerCanGoFromTo(MultiverseWorld fromWorld, MultiverseWorld toWorld, CommandSender teleporter, Player teleportee) {
      this.plugin.log(Level.FINEST, "Checking '" + teleporter + "' can send '" + teleportee + "' somewhere");
      Player teleporterPlayer;
      if (this.plugin.getMVConfig().getTeleportIntercept()) {
         if (teleporter instanceof ConsoleCommandSender) {
            return true;
         }

         if (teleporter == null) {
            teleporter = teleportee;
         }

         if (!(teleporter instanceof Player)) {
            return false;
         }

         teleporterPlayer = (Player)teleporter;
      } else {
         if (teleporter instanceof Player) {
            teleporterPlayer = (Player)teleporter;
         } else {
            teleporterPlayer = null;
         }

         if (teleporterPlayer == null) {
            return true;
         }
      }

      if (toWorld != null) {
         if (!this.plugin.getMVPerms().canEnterWorld(teleporterPlayer, toWorld)) {
            if (teleportee.equals(teleporter)) {
               teleporter.sendMessage("You don't have access to go here...");
            } else {
               teleporter.sendMessage("You can't send " + teleportee.getName() + " here...");
            }

            return false;
         } else if (fromWorld != null && fromWorld.getWorldBlacklist().contains(toWorld.getName())) {
            if (teleportee.equals(teleporter)) {
               teleporter.sendMessage("You don't have access to go to " + toWorld.getColoredWorldString() + " from " + fromWorld.getColoredWorldString());
            } else {
               teleporter.sendMessage("You don't have access to send " + teleportee.getName() + " from " + fromWorld.getColoredWorldString() + " to " + toWorld.getColoredWorldString());
            }

            return false;
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   public boolean playerCanBypassPlayerLimit(MultiverseWorld toWorld, CommandSender teleporter, Player teleportee) {
      if (teleporter == null) {
         teleporter = teleportee;
      }

      if (!(teleporter instanceof Player)) {
         return true;
      } else {
         MVPermissions perms = this.plugin.getMVPerms();
         if (perms.hasPermission(teleportee, "mv.bypass.playerlimit." + toWorld.getName(), false)) {
            return true;
         } else {
            teleporter.sendMessage("The world " + toWorld.getColoredWorldString() + " is full");
            return false;
         }
      }
   }

   public boolean playerCanIgnoreGameModeRestriction(MultiverseWorld toWorld, Player teleportee) {
      return toWorld != null ? this.plugin.getMVPerms().canIgnoreGameModeRestriction(teleportee, toWorld) : true;
   }
}
