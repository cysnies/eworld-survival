package net.citizensnpcs.api.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;

public class Colorizer {
   private static Pattern COLOR_MATCHER;
   private static String GROUP = "§$1";

   public Colorizer() {
      super();
   }

   public static String parseColors(String parsed) {
      Matcher matcher = COLOR_MATCHER.matcher(ChatColor.translateAlternateColorCodes('&', parsed));
      return matcher.replaceAll(GROUP);
   }

   static {
      String colors = "";

      for(ChatColor color : ChatColor.values()) {
         colors = colors + color.getChar();
      }

      COLOR_MATCHER = Pattern.compile("\\<([COLORS])\\>".replace("COLORS", colors), 2);
   }
}
