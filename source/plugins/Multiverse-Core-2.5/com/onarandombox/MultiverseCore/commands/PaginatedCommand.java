package com.onarandombox.MultiverseCore.commands;

import com.pneumaticraft.commandhandler.multiverse.Command;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class PaginatedCommand extends Command {
   private static final int DEFAULT_ITEMS_PER_PAGE = 9;
   protected int itemsPerPage = 9;

   public PaginatedCommand(JavaPlugin plugin) {
      super(plugin);
   }

   protected void setItemsPerPage(int items) {
      this.itemsPerPage = items;
   }

   protected abstract List getFilteredItems(List var1, String var2);

   protected String stitchThisString(List list) {
      StringBuilder builder = new StringBuilder();

      for(String s : list) {
         builder.append(s);
         builder.append(' ');
      }

      return builder.toString();
   }

   protected void showPage(int page, CommandSender sender, List cmds) {
      page = page <= 0 ? 1 : page;
      int start = (page - 1) * this.itemsPerPage;
      int end = start + this.itemsPerPage;

      for(int i = start; i < end; ++i) {
         if (i < cmds.size()) {
            sender.sendMessage(this.getItemText(cmds.get(i)));
         } else if (sender instanceof Player) {
            sender.sendMessage(" ");
         }
      }

   }

   protected abstract String getItemText(Object var1);

   protected FilterObject getPageAndFilter(List args) {
      int page = 1;
      String filter = "";
      if (args.size() == 0) {
         filter = "";
         page = 1;
      } else if (args.size() == 1) {
         try {
            page = Integer.parseInt((String)args.get(0));
         } catch (NumberFormatException var6) {
            filter = (String)args.get(0);
            page = 1;
         }
      } else if (args.size() == 2) {
         filter = (String)args.get(0);

         try {
            page = Integer.parseInt((String)args.get(1));
         } catch (NumberFormatException var5) {
            page = 1;
         }
      }

      return new FilterObject(page, filter);
   }

   protected class FilterObject {
      private Integer page;
      private String filter;

      public FilterObject(Integer page, String filter) {
         super();
         this.page = page;
         this.filter = filter;
      }

      public Integer getPage() {
         return this.page;
      }

      public void setPage(int page) {
         this.page = page;
      }

      public String getFilter() {
         return this.filter;
      }
   }
}
