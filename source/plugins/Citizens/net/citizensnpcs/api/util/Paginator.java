package net.citizensnpcs.api.util;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;

public class Paginator {
   private String header;
   private final List lines = new ArrayList();
   private static final int LINES_PER_PAGE = 9;

   public Paginator() {
      super();
   }

   public void addLine(String line) {
      this.lines.add(line);
   }

   public Paginator header(String header) {
      this.header = header;
      return this;
   }

   public boolean sendPage(CommandSender sender, int page) {
      int pages = (int)(this.lines.size() / 9 == 0 ? (double)1.0F : Math.ceil((double)this.lines.size() / (double)9.0F));
      if (page >= 0 && page <= pages) {
         int startIndex = 9 * page - 9;
         int endIndex = page * 9;
         Messaging.send(sender, wrapHeader("<e>" + this.header + " <f>" + page + "/" + pages));
         if (this.lines.size() < endIndex) {
            endIndex = this.lines.size();
         }

         for(String line : this.lines.subList(startIndex, endIndex)) {
            Messaging.send(sender, line);
         }

         return true;
      } else {
         return false;
      }
   }

   public static String wrapHeader(Object string) {
      String highlight = "<e>";
      return highlight + "=====[ " + string.toString() + highlight + " ]=====";
   }
}
