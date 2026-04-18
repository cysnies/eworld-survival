package buscript.multiverse.util;

public class TimeTools {
   public TimeTools() {
      super();
   }

   public static String toShortForm(long second) {
      long minute = second / 60L;
      second %= 60L;
      long hour = minute / 60L;
      minute %= 60L;
      long day = hour / 24L;
      hour %= 24L;
      StringBuilder time = new StringBuilder();
      if (day != 0L) {
         time.append(hour).append("d ");
      }

      if (hour != 0L) {
         time.append(hour).append("h ");
      }

      if (minute != 0L) {
         time.append(minute).append("m ");
      }

      if (second != 0L) {
         time.append(second).append("s ");
      }

      return time.toString().trim();
   }

   public static String toLongForm(long second) {
      if (second == 0L) {
         return "0 seconds";
      } else {
         long minute = second / 60L;
         second %= 60L;
         long hour = minute / 60L;
         minute %= 60L;
         long day = hour / 24L;
         hour %= 24L;
         StringBuilder time = new StringBuilder();
         if (day != 0L) {
            time.append(day);
         }

         if (day == 1L) {
            time.append(" day ");
         } else if (day > 1L) {
            time.append(" days ");
         }

         if (hour != 0L) {
            time.append(hour);
         }

         if (hour == 1L) {
            time.append(" hour ");
         } else if (hour > 1L) {
            time.append(" hours ");
         }

         if (minute != 0L) {
            time.append(minute);
         }

         if (minute == 1L) {
            time.append(" minute ");
         } else if (minute > 1L) {
            time.append(" minutes ");
         }

         if (second != 0L) {
            time.append(second);
         }

         if (second == 1L) {
            time.append(" second");
         } else if (second > 1L) {
            time.append(" seconds");
         }

         return time.toString().trim();
      }
   }

   public static long fromShortForm(String dhms) {
      long seconds = 0L;
      long minutes = 0L;
      long hours = 0L;
      long days = 0L;
      if (dhms.contains("d")) {
         try {
            days = (long)Integer.parseInt(dhms.split("d")[0].replaceAll(" ", ""));
         } catch (NumberFormatException var13) {
         }

         if (dhms.contains("h") || dhms.contains("m") || dhms.contains("s")) {
            dhms = dhms.split("d")[1];
         }
      }

      if (dhms.contains("h")) {
         try {
            hours = (long)Integer.parseInt(dhms.split("h")[0].replaceAll(" ", ""));
         } catch (NumberFormatException var12) {
         }

         if (dhms.contains("m") || dhms.contains("s")) {
            dhms = dhms.split("h")[1];
         }
      }

      if (dhms.contains("m")) {
         try {
            minutes = (long)Integer.parseInt(dhms.split("m")[0].replaceAll(" ", ""));
         } catch (NumberFormatException var11) {
         }

         if (dhms.contains("s")) {
            dhms = dhms.split("m")[1];
         }
      }

      if (dhms.contains("s")) {
         try {
            seconds = (long)Integer.parseInt(dhms.split("s")[0].replaceAll(" ", ""));
         } catch (NumberFormatException var10) {
         }
      }

      return days * 86400L + hours * 3600L + minutes * 60L + seconds;
   }

   public static long fromLongForm(String dhms) {
      long seconds = 0L;
      long minutes = 0L;
      long hours = 0L;
      long days = 0L;
      if (dhms.contains("days")) {
         try {
            days = (long)Integer.parseInt(dhms.split("days")[0].replaceAll(" ", ""));
         } catch (NumberFormatException var17) {
         }

         if (dhms.contains("hours") || dhms.contains("hour") || dhms.contains("minutes") || dhms.contains("seconds") || dhms.contains("minute") || dhms.contains("second")) {
            dhms = dhms.split("days")[1];
         }
      } else if (dhms.contains("day")) {
         try {
            days = (long)Integer.parseInt(dhms.split("day")[0].replaceAll(" ", ""));
         } catch (NumberFormatException var16) {
         }

         if (dhms.contains("hours") || dhms.contains("hour") || dhms.contains("minutes") || dhms.contains("seconds") || dhms.contains("minute") || dhms.contains("second")) {
            dhms = dhms.split("day")[1];
         }
      }

      if (dhms.contains("hours")) {
         try {
            hours = (long)Integer.parseInt(dhms.split("hours")[0].replaceAll(" ", ""));
         } catch (NumberFormatException var15) {
         }

         if (dhms.contains("minutes") || dhms.contains("seconds") || dhms.contains("minute") || dhms.contains("second")) {
            dhms = dhms.split("hours")[1];
         }
      } else if (dhms.contains("hour")) {
         try {
            hours = (long)Integer.parseInt(dhms.split("hour")[0].replaceAll(" ", ""));
         } catch (NumberFormatException var14) {
         }

         if (dhms.contains("minutes") || dhms.contains("seconds") || dhms.contains("minute") || dhms.contains("second")) {
            dhms = dhms.split("hour")[1];
         }
      }

      if (dhms.contains("minutes")) {
         try {
            minutes = (long)Integer.parseInt(dhms.split("minutes")[0].replaceAll(" ", ""));
         } catch (NumberFormatException var13) {
         }

         if (dhms.contains("seconds") || dhms.contains("second")) {
            dhms = dhms.split("minutes")[1];
         }
      } else if (dhms.contains("minute")) {
         try {
            minutes = (long)Integer.parseInt(dhms.split("minute")[0].replaceAll(" ", ""));
         } catch (NumberFormatException var12) {
         }

         if (dhms.contains("seconds") || dhms.contains("second")) {
            dhms = dhms.split("minute")[1];
         }
      }

      if (dhms.contains("seconds")) {
         try {
            seconds = (long)Integer.parseInt(dhms.split("seconds")[0].replaceAll(" ", ""));
         } catch (NumberFormatException var11) {
         }
      } else if (dhms.contains("second")) {
         try {
            seconds = (long)Integer.parseInt(dhms.split("second")[0].replaceAll(" ", ""));
         } catch (NumberFormatException var10) {
         }
      }

      return days * 86400L + hours * 3600L + minutes * 60L + seconds;
   }
}
