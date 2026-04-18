package net.citizensnpcs;

import com.google.common.base.Preconditions;
import net.citizensnpcs.api.event.PlayerCreateNPCEvent;
import net.citizensnpcs.api.util.Messaging;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PaymentListener implements Listener {
   private final Economy provider;

   public PaymentListener(Economy provider) {
      super();
      Preconditions.checkNotNull(provider, "provider cannot be null");
      this.provider = provider;
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onPlayerCreateNPC(PlayerCreateNPCEvent event) {
      String name = event.getCreator().getName();
      boolean hasAccount = this.provider.hasAccount(name);
      if (hasAccount && !event.getCreator().hasPermission("citizens.npc.ignore-cost")) {
         double cost = Settings.Setting.NPC_COST.asDouble();
         EconomyResponse response = this.provider.withdrawPlayer(name, cost);
         if (!response.transactionSuccess()) {
            event.setCancelled(true);
            event.setCancelReason(response.errorMessage);
         } else {
            String formattedCost = this.provider.format(cost);
            Messaging.sendTr(event.getCreator(), "citizens.economy.money-withdrawn", formattedCost);
         }
      }
   }
}
