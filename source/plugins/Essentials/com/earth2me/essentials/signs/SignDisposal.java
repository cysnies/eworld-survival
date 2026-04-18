package com.earth2me.essentials.signs;

import com.earth2me.essentials.User;
import net.ess3.api.IEssentials;

public class SignDisposal extends EssentialsSign {
   public SignDisposal() {
      super("Disposal");
   }

   protected boolean onSignInteract(EssentialsSign.ISign sign, User player, String username, IEssentials ess) {
      player.getBase().openInventory(ess.getServer().createInventory(player.getBase(), 36));
      return true;
   }
}
