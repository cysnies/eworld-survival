package com.earth2me.essentials.signs;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.NumberUtil;
import net.ess3.api.IEssentials;

public class SignBalance extends EssentialsSign {
   public SignBalance() {
      super("Balance");
   }

   protected boolean onSignInteract(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException {
      player.sendMessage(I18n._("balance", NumberUtil.displayCurrency(player.getMoney(), ess)));
      return true;
   }
}
