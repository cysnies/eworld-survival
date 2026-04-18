package com.earth2me.essentials.utils;

import java.util.Collection;
import java.util.Locale;
import java.util.regex.Pattern;

public class StringUtil {
   private static final Pattern INVALIDFILECHARS = Pattern.compile("[^a-z0-9]");
   private static final Pattern INVALIDCHARS = Pattern.compile("[^\t\n\r -~\u0085 -\ud7ff\ue000-￼]");

   private StringUtil() {
      super();
   }

   public static String sanitizeFileName(String name) {
      return safeString(name);
   }

   public static String safeString(String string) {
      return INVALIDFILECHARS.matcher(string.toLowerCase(Locale.ENGLISH)).replaceAll("_");
   }

   public static String sanitizeString(String string) {
      return INVALIDCHARS.matcher(string).replaceAll("");
   }

   public static String joinList(Object... list) {
      return joinList(", ", list);
   }

   public static String joinList(String seperator, Object... list) {
      StringBuilder buf = new StringBuilder();

      for(Object each : list) {
         if (buf.length() > 0) {
            buf.append(seperator);
         }

         if (each instanceof Collection) {
            buf.append(joinList(seperator, ((Collection)each).toArray()));
         } else {
            try {
               buf.append(each.toString());
            } catch (Exception var8) {
               buf.append(each.toString());
            }
         }
      }

      return buf.toString();
   }
}
