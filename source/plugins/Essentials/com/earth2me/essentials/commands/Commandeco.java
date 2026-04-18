package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.NumberUtil;
import java.math.BigDecimal;
import java.util.Locale;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandeco extends EssentialsCommand {
   public Commandeco() {
      super("eco");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         BigDecimal startingBalance = this.ess.getSettings().getStartingBalance();
         BigDecimal broadcast = null;
         BigDecimal broadcastAll = null;

         EcoCommands cmd;
         BigDecimal amount;
         try {
            cmd = Commandeco.EcoCommands.valueOf(args[0].toUpperCase(Locale.ENGLISH));
            amount = cmd == Commandeco.EcoCommands.RESET ? startingBalance : new BigDecimal(args[2].replaceAll("[^0-9\\.]", ""));
         } catch (Exception ex) {
            throw new NotEnoughArgumentsException(ex);
         }

         if (args[1].contentEquals("**")) {
            for(String sUser : this.ess.getUserMap().getAllUniqueUsers()) {
               User player = this.ess.getUser(sUser);
               switch (cmd) {
                  case GIVE:
                     player.giveMoney(amount);
                     break;
                  case TAKE:
                     this.take(amount, player, (CommandSender)null);
                     break;
                  case RESET:
                  case SET:
                     this.set(amount, player, (CommandSender)null);
                     broadcastAll = amount;
               }
            }
         } else if (args[1].contentEquals("*")) {
            for(Player onlinePlayer : server.getOnlinePlayers()) {
               User player = this.ess.getUser(onlinePlayer);
               switch (cmd) {
                  case GIVE:
                     player.giveMoney(amount);
                     break;
                  case TAKE:
                     this.take(amount, player, (CommandSender)null);
                     break;
                  case RESET:
                  case SET:
                     this.set(amount, player, (CommandSender)null);
                     broadcast = amount;
               }
            }
         } else {
            User player = this.getPlayer(server, args, 1, true, true);
            switch (cmd) {
               case GIVE:
                  player.giveMoney(amount, sender);
                  break;
               case TAKE:
                  this.take(amount, player, sender);
                  break;
               case RESET:
               case SET:
                  this.set(amount, player, sender);
            }
         }

         if (broadcast != null) {
            server.broadcastMessage(I18n._("resetBal", NumberUtil.displayCurrency(broadcast, this.ess)));
         }

         if (broadcastAll != null) {
            server.broadcastMessage(I18n._("resetBalAll", NumberUtil.displayCurrency(broadcastAll, this.ess)));
         }

      }
   }

   private void take(BigDecimal amount, User player, CommandSender sender) throws Exception {
      BigDecimal money = player.getMoney();
      BigDecimal minBalance = this.ess.getSettings().getMinMoney();
      if (money.subtract(amount).compareTo(minBalance) > 0) {
         player.takeMoney(amount, sender);
      } else {
         if (sender != null) {
            throw new Exception(I18n._("insufficientFunds"));
         }

         player.setMoney(minBalance);
         player.sendMessage(I18n._("takenFromAccount", NumberUtil.displayCurrency(player.getMoney(), this.ess)));
      }

   }

   private void set(BigDecimal amount, User player, CommandSender sender) {
      BigDecimal minBalance = this.ess.getSettings().getMinMoney();
      boolean underMinimum = amount.compareTo(minBalance) < 0;
      player.setMoney(underMinimum ? minBalance : amount);
      player.sendMessage(I18n._("setBal", NumberUtil.displayCurrency(player.getMoney(), this.ess)));
      if (sender != null) {
         sender.sendMessage(I18n._("setBalOthers", player.getDisplayName(), NumberUtil.displayCurrency(player.getMoney(), this.ess)));
      }

   }

   private static enum EcoCommands {
      GIVE,
      TAKE,
      SET,
      RESET;

      private EcoCommands() {
      }
   }
}
