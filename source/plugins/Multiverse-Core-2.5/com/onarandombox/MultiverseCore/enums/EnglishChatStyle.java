package com.onarandombox.MultiverseCore.enums;

import org.bukkit.ChatColor;

public enum EnglishChatStyle {
   NORMAL((ChatColor)null),
   MAGIC(ChatColor.MAGIC),
   BOLD(ChatColor.BOLD),
   STRIKETHROUGH(ChatColor.STRIKETHROUGH),
   UNDERLINE(ChatColor.UNDERLINE),
   ITALIC(ChatColor.ITALIC);

   private final ChatColor color;

   private EnglishChatStyle(ChatColor color) {
      this.color = color;
   }

   public ChatColor getColor() {
      return this.color;
   }

   public static EnglishChatStyle fromString(String text) {
      if (text != null) {
         for(EnglishChatStyle c : values()) {
            if (text.equalsIgnoreCase(c.name())) {
               return c;
            }
         }
      }

      return null;
   }
}
