package net.citizensnpcs.api.persistence;

import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.ItemStorage;
import org.bukkit.inventory.ItemStack;

public class ItemStackPersister implements Persister {
   public ItemStackPersister() {
      super();
   }

   public ItemStack create(DataKey root) {
      return ItemStorage.loadItemStack(root);
   }

   public void save(ItemStack instance, DataKey root) {
      ItemStorage.saveItem(root, instance);
   }
}
