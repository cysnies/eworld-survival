package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class AnchorCommand extends PaginatedCoreCommand {
   public AnchorCommand(MultiverseCore plugin) {
      super(plugin);
      this.setName("Create, Delete and Manage Anchor Destinations.");
      this.setCommandUsage("/mv anchor " + ChatColor.GREEN + "{name}" + ChatColor.GOLD + " [-d]");
      this.setArgRange(0, 2);
      this.addKey("mv anchor");
      this.addKey("mv anchors");
      this.addKey("mvanchor");
      this.addKey("mvanchors");
      this.addCommandExample("/mv anchor " + ChatColor.GREEN + "awesomething");
      this.addCommandExample("/mv anchor " + ChatColor.GREEN + "otherthing");
      this.addCommandExample("/mv anchor " + ChatColor.GREEN + "awesomething " + ChatColor.RED + "-d");
      this.addCommandExample("/mv anchors ");
      this.setPermission("multiverse.core.anchor.list", "Allows a player to list all anchors.", PermissionDefault.OP);
      this.addAdditonalPermission(new Permission("multiverse.core.anchor.create", "Allows a player to create anchors.", PermissionDefault.OP));
      this.addAdditonalPermission(new Permission("multiverse.core.anchor.delete", "Allows a player to delete anchors.", PermissionDefault.OP));
      this.setItemsPerPage(8);
   }

   private List getFancyAnchorList(Player p) {
      List<String> anchorList = new ArrayList();
      ChatColor color = ChatColor.GREEN;

      for(String anchor : this.plugin.getAnchorManager().getAnchors(p)) {
         anchorList.add(color + anchor);
         color = color == ChatColor.GREEN ? ChatColor.GOLD : ChatColor.GREEN;
      }

      return anchorList;
   }

   private void showList(CommandSender sender, List args) {
      if (!this.plugin.getMVPerms().hasPermission(sender, "multiverse.core.anchor.list", true)) {
         sender.sendMessage(ChatColor.RED + "You don't have the permission to list anchors!");
      } else {
         sender.sendMessage(ChatColor.LIGHT_PURPLE + "====[ Multiverse Anchor List ]====");
         Player p = null;
         if (sender instanceof Player) {
            p = (Player)sender;
         }

         PaginatedCommand<String>.FilterObject filterObject = this.getPageAndFilter(args);
         List<String> availableAnchors = new ArrayList(this.getFancyAnchorList(p));
         if (filterObject.getFilter().length() > 0) {
            availableAnchors = this.getFilteredItems(availableAnchors, filterObject.getFilter());
            if (availableAnchors.size() == 0) {
               sender.sendMessage(ChatColor.RED + "Sorry... " + ChatColor.WHITE + "No anchors matched your filter: " + ChatColor.AQUA + filterObject.getFilter());
               return;
            }
         } else if (availableAnchors.size() == 0) {
            sender.sendMessage(ChatColor.RED + "Sorry... " + ChatColor.WHITE + "No anchors were defined.");
            return;
         }

         if (sender instanceof Player) {
            int totalPages = (int)Math.ceil((double)availableAnchors.size() / ((double)this.itemsPerPage + (double)0.0F));
            if (filterObject.getPage() > totalPages) {
               filterObject.setPage(totalPages);
            } else if (filterObject.getPage() < 1) {
               filterObject.setPage(1);
            }

            sender.sendMessage(ChatColor.AQUA + " Page " + filterObject.getPage() + " of " + totalPages);
            this.showPage(filterObject.getPage(), sender, availableAnchors);
         } else {
            for(String c : availableAnchors) {
               sender.sendMessage(c);
            }

         }
      }
   }

   public void runCommand(CommandSender sender, List args) {
      if (args.size() == 0) {
         this.showList(sender, args);
      } else if (args.size() != 1 || this.getPageAndFilter(args).getPage() == 1 && !((String)args.get(0)).equals("1")) {
         if (args.size() == 2 && ((String)args.get(1)).equalsIgnoreCase("-d")) {
            if (!this.plugin.getMVPerms().hasPermission(sender, "multiverse.core.anchor.delete", true)) {
               sender.sendMessage(ChatColor.RED + "You don't have the permission to delete anchors!");
            } else if (this.plugin.getAnchorManager().deleteAnchor((String)args.get(0))) {
               sender.sendMessage("Anchor '" + (String)args.get(0) + "' was successfully " + ChatColor.RED + "deleted!");
            } else {
               sender.sendMessage("Anchor '" + (String)args.get(0) + "' was " + ChatColor.RED + " NOT " + ChatColor.WHITE + "deleted!");
            }

         } else if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to create Anchors.");
         } else {
            if (!this.plugin.getMVPerms().hasPermission(sender, "multiverse.core.anchor.create", true)) {
               sender.sendMessage(ChatColor.RED + "You don't have the permission to create anchors!");
            } else {
               Player player = (Player)sender;
               if (this.plugin.getAnchorManager().saveAnchorLocation((String)args.get(0), player.getLocation())) {
                  sender.sendMessage("Anchor '" + (String)args.get(0) + "' was successfully " + ChatColor.GREEN + "created!");
               } else {
                  sender.sendMessage("Anchor '" + (String)args.get(0) + "' was " + ChatColor.RED + " NOT " + ChatColor.WHITE + "created!");
               }
            }

         }
      } else {
         this.showList(sender, args);
      }
   }

   protected List getFilteredItems(List availableItems, String filter) {
      List<String> filtered = new ArrayList();

      for(String s : availableItems) {
         if (s.matches("(?i).*" + filter + ".*")) {
            filtered.add(s);
         }
      }

      return filtered;
   }

   protected String getItemText(String item) {
      return item;
   }
}
