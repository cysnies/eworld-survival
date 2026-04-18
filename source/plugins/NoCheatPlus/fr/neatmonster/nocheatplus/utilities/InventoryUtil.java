package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil {
   public InventoryUtil() {
      super();
   }

   public static int getFreeSlots(Inventory inventory) {
      ItemStack[] contents = inventory.getContents();
      int count = 0;

      for(int i = 0; i < contents.length; ++i) {
         ItemStack stack = contents[i];
         if (stack == null || stack.getTypeId() == 0) {
            ++count;
         }
      }

      return count;
   }

   public static int getStackCount(Inventory inventory, ItemStack reference) {
      if (inventory == null) {
         return 0;
      } else if (reference == null) {
         return getFreeSlots(inventory);
      } else {
         int id = reference.getTypeId();
         int durability = reference.getDurability();
         ItemStack[] contents = inventory.getContents();
         int count = 0;

         for(int i = 0; i < contents.length; ++i) {
            ItemStack stack = contents[i];
            if (stack != null && stack.getTypeId() == id && stack.getDurability() == durability) {
               ++count;
            }
         }

         return count;
      }
   }

   public static int getStackCount(InventoryView view, ItemStack reference) {
      return getStackCount(view.getBottomInventory(), reference) + getStackCount(view.getTopInventory(), reference);
   }

   public static boolean closePlayerInventoryRecursively(Entity entity) {
      Player player = getPlayerPassengerRecursively(entity);
      return player != null && closeOpenInventory((Player)entity);
   }

   public static Player getPlayerPassengerRecursively(Entity entity) {
      while(true) {
         if (entity != null) {
            if (entity instanceof Player) {
               return (Player)entity;
            }

            Entity passenger = entity.getPassenger();
            if (!entity.equals(passenger)) {
               entity = passenger;
               continue;
            }
         }

         return null;
      }
   }

   public static boolean closeOpenInventory(Player player) {
      if (hasInventoryOpen(player)) {
         player.closeInventory();
         return true;
      } else {
         return true;
      }
   }

   public static boolean hasInventoryOpen(Player player) {
      InventoryView view = player.getOpenInventory();
      return view != null && view.getType() != InventoryType.CRAFTING;
   }
}
