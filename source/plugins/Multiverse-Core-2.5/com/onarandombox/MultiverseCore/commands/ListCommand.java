package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class ListCommand extends PaginatedCoreCommand {
   public ListCommand(MultiverseCore plugin) {
      super(plugin);
      this.setName("World Listing");
      this.setCommandUsage("/mv list");
      this.setArgRange(0, 2);
      this.addKey("mvlist");
      this.addKey("mvl");
      this.addKey("mv list");
      this.setPermission("multiverse.core.list.worlds", "Displays a listing of all worlds that you can enter.", PermissionDefault.OP);
      this.setItemsPerPage(8);
   }

   private List getFancyWorldList(Player p) {
      List<String> worldList = new ArrayList();

      for(MultiverseWorld world : this.plugin.getMVWorldManager().getMVWorlds()) {
         if (p == null || this.plugin.getMVPerms().canEnterWorld(p, world)) {
            ChatColor color = ChatColor.GOLD;
            World.Environment env = world.getEnvironment();
            if (env == Environment.NETHER) {
               color = ChatColor.RED;
            } else if (env == Environment.NORMAL) {
               color = ChatColor.GREEN;
            } else if (env == Environment.THE_END) {
               color = ChatColor.AQUA;
            }

            StringBuilder builder = new StringBuilder();
            builder.append(world.getColoredWorldString()).append(ChatColor.WHITE);
            builder.append(" - ").append(color).append(world.getEnvironment());
            if (world.isHidden()) {
               if (p == null || this.plugin.getMVPerms().hasPermission(p, "multiverse.core.modify", true)) {
                  worldList.add(ChatColor.GRAY + "[H]" + builder.toString());
               }
            } else {
               worldList.add(builder.toString());
            }
         }
      }

      for(String name : this.plugin.getMVWorldManager().getUnloadedWorlds()) {
         if (p == null || this.plugin.getMVPerms().hasPermission(p, "multiverse.access." + name, true)) {
            worldList.add(ChatColor.GRAY + name + " - UNLOADED");
         }
      }

      return worldList;
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

   public void runCommand(CommandSender sender, List args) {
      sender.sendMessage(ChatColor.LIGHT_PURPLE + "====[ Multiverse World List ]====");
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      PaginatedCommand<String>.FilterObject filterObject = this.getPageAndFilter(args);
      List<String> availableWorlds = new ArrayList(this.getFancyWorldList(p));
      if (filterObject.getFilter().length() > 0) {
         availableWorlds = this.getFilteredItems(availableWorlds, filterObject.getFilter());
         if (availableWorlds.size() == 0) {
            sender.sendMessage(ChatColor.RED + "Sorry... " + ChatColor.WHITE + "No worlds matched your filter: " + ChatColor.AQUA + filterObject.getFilter());
            return;
         }
      }

      if (sender instanceof Player) {
         int totalPages = (int)Math.ceil((double)availableWorlds.size() / ((double)this.itemsPerPage + (double)0.0F));
         if (filterObject.getPage() > totalPages) {
            filterObject.setPage(totalPages);
         }

         sender.sendMessage(ChatColor.AQUA + " Page " + filterObject.getPage() + " of " + totalPages);
         this.showPage(filterObject.getPage(), sender, availableWorlds);
      } else {
         for(String c : availableWorlds) {
            sender.sendMessage(c);
         }

      }
   }
}
