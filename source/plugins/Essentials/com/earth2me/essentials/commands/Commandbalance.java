package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.NumberUtil;
import java.math.BigDecimal;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public class Commandbalance extends EssentialsCommand {
   public Commandbalance() {
      super("balance");
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User target = this.getPlayer(server, args, 0, true, true);
         sender.sendMessage(I18n._("balanceOther", target.getDisplayName(), NumberUtil.displayCurrency(target.getMoney(), this.ess)));
      }
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length == 1 && user.isAuthorized("essentials.balance.others")) {
         User target = this.getPlayer(server, args, 0, true, true);
         BigDecimal bal = target.getMoney();
         user.sendMessage(I18n._("balanceOther", target.getDisplayName(), NumberUtil.displayCurrency(bal, this.ess)));
      } else {
         if (args.length >= 2) {
            throw new NotEnoughArgumentsException();
         }

         BigDecimal bal = user.getMoney();
         user.sendMessage(I18n._("balance", NumberUtil.displayCurrency(bal, this.ess)));
      }

   }
}
