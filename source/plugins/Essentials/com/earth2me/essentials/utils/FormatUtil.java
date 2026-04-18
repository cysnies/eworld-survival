package com.earth2me.essentials.utils;

import java.util.regex.Pattern;
import net.ess3.api.IUser;

public class FormatUtil {
   static final transient Pattern VANILLA_PATTERN = Pattern.compile("§+[0-9A-FK-ORa-fk-or]?");
   static final transient Pattern VANILLA_COLOR_PATTERN = Pattern.compile("§+[0-9A-Fa-f]");
   static final transient Pattern VANILLA_MAGIC_PATTERN = Pattern.compile("§+[Kk]");
   static final transient Pattern VANILLA_FORMAT_PATTERN = Pattern.compile("§+[L-ORl-or]");
   static final transient Pattern REPLACE_ALL_PATTERN = Pattern.compile("(?<!&)&([0-9a-fk-orA-FK-OR])");
   static final transient Pattern REPLACE_COLOR_PATTERN = Pattern.compile("(?<!&)&([0-9a-fA-F])");
   static final transient Pattern REPLACE_MAGIC_PATTERN = Pattern.compile("(?<!&)&([Kk])");
   static final transient Pattern REPLACE_FORMAT_PATTERN = Pattern.compile("(?<!&)&([l-orL-OR])");
   static final transient Pattern REPLACE_PATTERN = Pattern.compile("&&(?=[0-9a-fk-orA-FK-OR])");
   static final transient Pattern LOGCOLOR_PATTERN = Pattern.compile("\\x1B\\[([0-9]{1,2}(;[0-9]{1,2})?)?[m|K]");
   static final transient Pattern URL_PATTERN = Pattern.compile("((?:(?:https?)://)?[\\w-_\\.]{2,})\\.([a-z]{2,3}(?:/\\S+)?)");
   public static final Pattern IPPATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

   public FormatUtil() {
      super();
   }

   public static String stripFormat(String input) {
      return input == null ? null : stripColor(input, VANILLA_PATTERN);
   }

   public static String stripEssentialsFormat(String input) {
      return input == null ? null : stripColor(input, REPLACE_ALL_PATTERN);
   }

   public static String formatMessage(IUser user, String permBase, String input) {
      if (input == null) {
         return null;
      } else {
         String message = formatString(user, permBase, input);
         if (!user.isAuthorized(permBase + ".url")) {
            message = blockURL(message);
         }

         return message;
      }
   }

   public static String replaceFormat(String input) {
      return input == null ? null : replaceColor(input, REPLACE_ALL_PATTERN);
   }

   static String replaceColor(String input, Pattern pattern) {
      return REPLACE_PATTERN.matcher(pattern.matcher(input).replaceAll("§$1")).replaceAll("&");
   }

   public static String formatString(IUser user, String permBase, String input) {
      if (input == null) {
         return null;
      } else {
         String message;
         if (!user.isAuthorized(permBase + ".color") && !user.isAuthorized(permBase + ".colour")) {
            message = stripColor(input, VANILLA_COLOR_PATTERN);
         } else {
            message = replaceColor(input, REPLACE_COLOR_PATTERN);
         }

         if (user.isAuthorized(permBase + ".magic")) {
            message = replaceColor(message, REPLACE_MAGIC_PATTERN);
         } else {
            message = stripColor(message, VANILLA_MAGIC_PATTERN);
         }

         if (user.isAuthorized(permBase + ".format")) {
            message = replaceColor(message, REPLACE_FORMAT_PATTERN);
         } else {
            message = stripColor(message, VANILLA_FORMAT_PATTERN);
         }

         return message;
      }
   }

   public static String stripLogColorFormat(String input) {
      return input == null ? null : stripColor(input, LOGCOLOR_PATTERN);
   }

   static String stripColor(String input, Pattern pattern) {
      return pattern.matcher(input).replaceAll("");
   }

   public static String lastCode(String input) {
      int pos = input.lastIndexOf("§");
      return pos != -1 && pos + 1 != input.length() ? input.substring(pos, pos + 2) : "";
   }

   static String blockURL(String input) {
      if (input == null) {
         return null;
      } else {
         String text;
         for(text = URL_PATTERN.matcher(input).replaceAll("$1 $2"); URL_PATTERN.matcher(text).find(); text = URL_PATTERN.matcher(text).replaceAll("$1 $2")) {
         }

         return text;
      }
   }

   public static boolean validIP(String ipAddress) {
      return IPPATTERN.matcher(ipAddress).matches();
   }
}
