package com.onarandombox.MultiverseCore.enums;

import org.bukkit.ChatColor;

public enum EnglishChatColor {
   AQUA(ChatColor.AQUA),
   BLACK(ChatColor.BLACK),
   BLUE(ChatColor.BLUE),
   DARKAQUA(ChatColor.DARK_AQUA),
   DARKBLUE(ChatColor.DARK_BLUE),
   DARKGRAY(ChatColor.DARK_GRAY),
   DARKGREEN(ChatColor.DARK_GREEN),
   DARKPURPLE(ChatColor.DARK_PURPLE),
   DARKRED(ChatColor.DARK_RED),
   GOLD(ChatColor.GOLD),
   GRAY(ChatColor.GRAY),
   GREEN(ChatColor.GREEN),
   LIGHTPURPLE(ChatColor.LIGHT_PURPLE),
   RED(ChatColor.RED),
   YELLOW(ChatColor.YELLOW),
   WHITE(ChatColor.WHITE);

   private final ChatColor color;

   private EnglishChatColor(ChatColor color) {
      this.color = color;
   }

   public String getText() {
      return this.name();
   }

   public ChatColor getColor() {
      return this.color;
   }

   public static String getAllColors() {
      String buffer = "";

      for(EnglishChatColor c : values()) {
         buffer = buffer + c.getColor() + c.getText() + " ";
      }

      return buffer;
   }

   public static EnglishChatColor fromString(String text) {
      if (text != null) {
         for(EnglishChatColor c : values()) {
            if (text.equalsIgnoreCase(c.name())) {
               return c;
            }
         }
      }

      return null;
   }

   public static boolean isValidAliasColor(String aliasColor) {
      return fromString(aliasColor) != null;
   }
}
