package fr.neatmonster.nocheatplus.checks.inventory;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.components.DisableListener;
import fr.neatmonster.nocheatplus.utilities.InventoryUtil;
import org.bukkit.entity.Player;

public class Open extends Check implements DisableListener {
   private static Open instance = null;

   public static boolean checkClose(Player player) {
      return instance.check(player);
   }

   public Open() {
      super(CheckType.INVENTORY_OPEN);
      instance = this;
   }

   public void onDisable() {
      instance = null;
   }

   public boolean check(Player player) {
      if (this.isEnabled(player) && InventoryUtil.hasInventoryOpen(player)) {
         InventoryConfig cc = InventoryConfig.getConfig(player);
         if (cc.openClose) {
            player.closeInventory();
         }

         return cc.openCancelOther;
      } else {
         return false;
      }
   }
}
