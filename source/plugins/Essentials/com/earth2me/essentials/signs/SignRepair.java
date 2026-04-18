package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.commands.Commandrepair;
import com.earth2me.essentials.commands.NotEnoughArgumentsException;
import net.ess3.api.IEssentials;

public class SignRepair extends EssentialsSign {
   public SignRepair() {
      super("Repair");
   }

   protected boolean onSignCreate(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException {
      String repairTarget = sign.getLine(1);
      if (repairTarget.isEmpty()) {
         sign.setLine(1, "Hand");
      } else if (!repairTarget.equalsIgnoreCase("all") && !repairTarget.equalsIgnoreCase("hand")) {
         sign.setLine(1, "§c<hand|all>");
         throw new SignException(I18n._("invalidSignLine", 2));
      }

      this.validateTrade(sign, 2, ess);
      return true;
   }

   protected boolean onSignInteract(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException, ChargeException {
      Trade charge = this.getTrade(sign, 2, ess);
      charge.isAffordableFor(player);
      Commandrepair command = new Commandrepair();
      command.setEssentials(ess);

      try {
         if (sign.getLine(1).equalsIgnoreCase("hand")) {
            command.repairHand(player);
         } else {
            if (!sign.getLine(1).equalsIgnoreCase("all")) {
               throw new NotEnoughArgumentsException();
            }

            command.repairAll(player);
         }
      } catch (Exception ex) {
         throw new SignException(ex.getMessage(), ex);
      }

      charge.charge(player);
      Trade.log("Sign", "Repair", "Interact", username, (Trade)null, username, charge, sign.getBlock().getLocation(), ess);
      return true;
   }
}
