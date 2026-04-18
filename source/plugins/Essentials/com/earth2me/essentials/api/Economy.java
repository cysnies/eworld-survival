package com.earth2me.essentials.api;

import com.earth2me.essentials.EssentialsConf;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.NumberUtil;
import com.earth2me.essentials.utils.StringUtil;
import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.ess3.api.IEssentials;

public class Economy {
   private static final Logger logger = Logger.getLogger("Minecraft");
   private static IEssentials ess;
   private static final String noCallBeforeLoad = "Essentials API is called before Essentials is loaded.";
   public static final MathContext MATH_CONTEXT;

   public Economy() {
      super();
   }

   public static void setEss(IEssentials aEss) {
      ess = aEss;
   }

   private static void createNPCFile(String name) {
      File folder = new File(ess.getDataFolder(), "userdata");
      if (!folder.exists()) {
         folder.mkdirs();
      }

      EssentialsConf npcConfig = new EssentialsConf(new File(folder, StringUtil.sanitizeFileName(name) + ".yml"));
      npcConfig.load();
      npcConfig.setProperty("npc", (Object)true);
      npcConfig.setProperty("money", ess.getSettings().getStartingBalance());
      npcConfig.forceSave();
   }

   private static void deleteNPC(String name) {
      File folder = new File(ess.getDataFolder(), "userdata");
      if (!folder.exists()) {
         folder.mkdirs();
      }

      File config = new File(folder, StringUtil.sanitizeFileName(name) + ".yml");
      EssentialsConf npcConfig = new EssentialsConf(config);
      npcConfig.load();
      if (npcConfig.hasProperty("npc") && npcConfig.getBoolean("npc", false)) {
         if (!config.delete()) {
            logger.log(Level.WARNING, I18n._("deleteFileError", config));
         }

         ess.getUserMap().removeUser(name);
      }

   }

   private static User getUserByName(String name) {
      if (ess == null) {
         throw new RuntimeException("Essentials API is called before Essentials is loaded.");
      } else {
         return ess.getUser(name);
      }
   }

   /** @deprecated */
   @Deprecated
   public static double getMoney(String name) throws UserDoesNotExistException {
      return getMoneyExact(name).doubleValue();
   }

   public static BigDecimal getMoneyExact(String name) throws UserDoesNotExistException {
      User user = getUserByName(name);
      if (user == null) {
         throw new UserDoesNotExistException(name);
      } else {
         return user.getMoney();
      }
   }

   /** @deprecated */
   @Deprecated
   public static void setMoney(String name, double balance) throws UserDoesNotExistException, NoLoanPermittedException {
      try {
         setMoney(name, BigDecimal.valueOf(balance));
      } catch (ArithmeticException e) {
         logger.log(Level.WARNING, "Failed to set balance of " + name + " to " + balance + ": " + e.getMessage(), e);
      }

   }

   public static void setMoney(String name, BigDecimal balance) throws UserDoesNotExistException, NoLoanPermittedException {
      User user = getUserByName(name);
      if (user == null) {
         throw new UserDoesNotExistException(name);
      } else if (balance.compareTo(ess.getSettings().getMinMoney()) < 0) {
         throw new NoLoanPermittedException();
      } else if (balance.signum() < 0 && !user.isAuthorized("essentials.eco.loan")) {
         throw new NoLoanPermittedException();
      } else {
         user.setMoney(balance);
      }
   }

   /** @deprecated */
   @Deprecated
   public static void add(String name, double amount) throws UserDoesNotExistException, NoLoanPermittedException {
      try {
         add(name, BigDecimal.valueOf(amount));
      } catch (ArithmeticException e) {
         logger.log(Level.WARNING, "Failed to add " + amount + " to balance of " + name + ": " + e.getMessage(), e);
      }

   }

   public static void add(String name, BigDecimal amount) throws UserDoesNotExistException, NoLoanPermittedException, ArithmeticException {
      BigDecimal result = getMoneyExact(name).add(amount, MATH_CONTEXT);
      setMoney(name, result);
   }

   /** @deprecated */
   @Deprecated
   public static void subtract(String name, double amount) throws UserDoesNotExistException, NoLoanPermittedException {
      try {
         substract(name, BigDecimal.valueOf(amount));
      } catch (ArithmeticException e) {
         logger.log(Level.WARNING, "Failed to substract " + amount + " of balance of " + name + ": " + e.getMessage(), e);
      }

   }

   public static void substract(String name, BigDecimal amount) throws UserDoesNotExistException, NoLoanPermittedException, ArithmeticException {
      BigDecimal result = getMoneyExact(name).subtract(amount, MATH_CONTEXT);
      setMoney(name, result);
   }

   /** @deprecated */
   @Deprecated
   public static void divide(String name, double amount) throws UserDoesNotExistException, NoLoanPermittedException {
      try {
         divide(name, BigDecimal.valueOf(amount));
      } catch (ArithmeticException e) {
         logger.log(Level.WARNING, "Failed to divide balance of " + name + " by " + amount + ": " + e.getMessage(), e);
      }

   }

   public static void divide(String name, BigDecimal amount) throws UserDoesNotExistException, NoLoanPermittedException, ArithmeticException {
      BigDecimal result = getMoneyExact(name).divide(amount, MATH_CONTEXT);
      setMoney(name, result);
   }

   /** @deprecated */
   @Deprecated
   public static void multiply(String name, double amount) throws UserDoesNotExistException, NoLoanPermittedException {
      try {
         multiply(name, BigDecimal.valueOf(amount));
      } catch (ArithmeticException e) {
         logger.log(Level.WARNING, "Failed to multiply balance of " + name + " by " + amount + ": " + e.getMessage(), e);
      }

   }

   public static void multiply(String name, BigDecimal amount) throws UserDoesNotExistException, NoLoanPermittedException, ArithmeticException {
      BigDecimal result = getMoneyExact(name).multiply(amount, MATH_CONTEXT);
      setMoney(name, result);
   }

   public static void resetBalance(String name) throws UserDoesNotExistException, NoLoanPermittedException {
      if (ess == null) {
         throw new RuntimeException("Essentials API is called before Essentials is loaded.");
      } else {
         setMoney(name, ess.getSettings().getStartingBalance());
      }
   }

   /** @deprecated */
   @Deprecated
   public static boolean hasEnough(String name, double amount) throws UserDoesNotExistException {
      try {
         return hasEnough(name, BigDecimal.valueOf(amount));
      } catch (ArithmeticException e) {
         logger.log(Level.WARNING, "Failed to compare balance of " + name + " with " + amount + ": " + e.getMessage(), e);
         return false;
      }
   }

   public static boolean hasEnough(String name, BigDecimal amount) throws UserDoesNotExistException, ArithmeticException {
      return amount.compareTo(getMoneyExact(name)) <= 0;
   }

   /** @deprecated */
   @Deprecated
   public static boolean hasMore(String name, double amount) throws UserDoesNotExistException {
      try {
         return hasMore(name, BigDecimal.valueOf(amount));
      } catch (ArithmeticException e) {
         logger.log(Level.WARNING, "Failed to compare balance of " + name + " with " + amount + ": " + e.getMessage(), e);
         return false;
      }
   }

   public static boolean hasMore(String name, BigDecimal amount) throws UserDoesNotExistException, ArithmeticException {
      return amount.compareTo(getMoneyExact(name)) < 0;
   }

   /** @deprecated */
   @Deprecated
   public static boolean hasLess(String name, double amount) throws UserDoesNotExistException {
      try {
         return hasLess(name, BigDecimal.valueOf(amount));
      } catch (ArithmeticException e) {
         logger.log(Level.WARNING, "Failed to compare balance of " + name + " with " + amount + ": " + e.getMessage(), e);
         return false;
      }
   }

   public static boolean hasLess(String name, BigDecimal amount) throws UserDoesNotExistException, ArithmeticException {
      return amount.compareTo(getMoneyExact(name)) > 0;
   }

   public static boolean isNegative(String name) throws UserDoesNotExistException {
      return getMoneyExact(name).signum() < 0;
   }

   /** @deprecated */
   @Deprecated
   public static String format(double amount) {
      try {
         return format(BigDecimal.valueOf(amount));
      } catch (NumberFormatException e) {
         logger.log(Level.WARNING, "Failed to display " + amount + ": " + e.getMessage(), e);
         return "NaN";
      }
   }

   public static String format(BigDecimal amount) {
      if (ess == null) {
         throw new RuntimeException("Essentials API is called before Essentials is loaded.");
      } else {
         return NumberUtil.displayCurrency(amount, ess);
      }
   }

   public static boolean playerExists(String name) {
      return getUserByName(name) != null;
   }

   public static boolean isNPC(String name) throws UserDoesNotExistException {
      User user = getUserByName(name);
      if (user == null) {
         throw new UserDoesNotExistException(name);
      } else {
         return user.isNPC();
      }
   }

   public static boolean createNPC(String name) {
      User user = getUserByName(name);
      if (user == null) {
         createNPCFile(name);
         return true;
      } else {
         return false;
      }
   }

   public static void removeNPC(String name) throws UserDoesNotExistException {
      User user = getUserByName(name);
      if (user == null) {
         throw new UserDoesNotExistException(name);
      } else {
         deleteNPC(name);
      }
   }

   static {
      MATH_CONTEXT = MathContext.DECIMAL128;
   }
}
