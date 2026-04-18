package net.citizensnpcs.api.trait.trait;

import java.util.Arrays;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.ItemStorage;
import org.bukkit.inventory.ItemStack;

public class Inventory extends Trait {
   private ItemStack[] contents = new ItemStack[36];

   public Inventory() {
      super("inventory");
   }

   public ItemStack[] getContents() {
      return this.contents;
   }

   public void load(DataKey key) throws NPCLoadException {
      this.contents = this.parseContents(key);
   }

   private ItemStack[] parseContents(DataKey key) throws NPCLoadException {
      ItemStack[] contents = new ItemStack[36];

      for(DataKey slotKey : key.getIntegerSubKeys()) {
         contents[Integer.parseInt(slotKey.name())] = ItemStorage.loadItemStack(slotKey);
      }

      return contents;
   }

   public void save(DataKey key) {
      int slot = 0;

      for(ItemStack item : this.contents) {
         key.removeKey(String.valueOf(slot));
         if (item != null) {
            ItemStorage.saveItem(key.getRelative(String.valueOf(slot)), item);
         }

         ++slot;
      }

   }

   public void setContents(ItemStack[] contents) {
      this.contents = contents;
   }

   public String toString() {
      return "Inventory{" + Arrays.toString(this.contents) + "}";
   }
}
