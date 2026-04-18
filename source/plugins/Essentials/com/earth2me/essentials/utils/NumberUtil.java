package com.earth2me.essentials.utils;

import com.earth2me.essentials.I18n;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import net.ess3.api.IEssentials;

public class NumberUtil {
   static DecimalFormat threeDPlaces = new DecimalFormat("#,###.###");
   static DecimalFormat currencyFormat;

   public NumberUtil() {
      super();
   }

   public static String shortCurrency(BigDecimal value, IEssentials ess) {
      return ess.getSettings().getCurrencySymbol() + formatAsCurrency(value);
   }

   public static String formatDouble(double value) {
      threeDPlaces.setRoundingMode(RoundingMode.HALF_UP);
      return threeDPlaces.format(value);
   }

   public static String formatAsCurrency(BigDecimal value) {
      currencyFormat.setRoundingMode(RoundingMode.FLOOR);
      String str = currencyFormat.format(value);
      if (str.endsWith(".00")) {
         str = str.substring(0, str.length() - 3);
      }

      return str;
   }

   public static String displayCurrency(BigDecimal value, IEssentials ess) {
      return I18n._("currency", ess.getSettings().getCurrencySymbol(), formatAsCurrency(value));
   }

   public static boolean isInt(String sInt) {
      try {
         Integer.parseInt(sInt);
         return true;
      } catch (NumberFormatException var2) {
         return false;
      }
   }

   static {
      currencyFormat = new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.US));
   }
}
