package net.citizensnpcs.api.trait.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.ItemStorage;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class Equipment extends Trait {
   private final ItemStack[] equipment = new ItemStack[5];

   public Equipment() {
      super("equipment");
   }

   public ItemStack get(int slot) {
      if (this.npc.getBukkitEntity() instanceof Enderman && slot != 0) {
         throw new IllegalArgumentException("Slot must be 0 for enderman");
      } else if (slot >= 0 && slot <= 4) {
         return this.equipment[slot];
      } else {
         throw new IllegalArgumentException("Slot must be between 0 and 4");
      }
   }

   public ItemStack[] getEquipment() {
      return this.equipment;
   }

   private EntityEquipment getEquipmentFromEntity(LivingEntity entity) {
      return (EntityEquipment)(entity instanceof Player ? new PlayerEquipmentWrapper((Player)entity) : entity.getEquipment());
   }

   public void load(DataKey key) throws NPCLoadException {
      if (key.keyExists("hand")) {
         this.equipment[0] = ItemStorage.loadItemStack(key.getRelative("hand"));
      }

      if (key.keyExists("helmet")) {
         this.equipment[1] = ItemStorage.loadItemStack(key.getRelative("helmet"));
      }

      if (key.keyExists("chestplate")) {
         this.equipment[2] = ItemStorage.loadItemStack(key.getRelative("chestplate"));
      }

      if (key.keyExists("leggings")) {
         this.equipment[3] = ItemStorage.loadItemStack(key.getRelative("leggings"));
      }

      if (key.keyExists("boots")) {
         this.equipment[4] = ItemStorage.loadItemStack(key.getRelative("boots"));
      }

   }

   public void onSpawn() {
      if (this.npc.getBukkitEntity() instanceof LivingEntity) {
         if (this.npc.getBukkitEntity() instanceof Enderman) {
            Enderman enderman = (Enderman)this.npc.getBukkitEntity();
            if (this.equipment[0] != null) {
               enderman.setCarriedMaterial(this.equipment[0].getData());
            }
         } else {
            LivingEntity entity = this.npc.getBukkitEntity();
            EntityEquipment equip = this.getEquipmentFromEntity(entity);
            if (this.equipment[0] != null) {
               equip.setItemInHand(this.equipment[0]);
            }

            equip.setHelmet(this.equipment[1]);
            equip.setChestplate(this.equipment[2]);
            equip.setLeggings(this.equipment[3]);
            equip.setBoots(this.equipment[4]);
            if (entity instanceof Player) {
               ((Player)entity).updateInventory();
            }
         }

      }
   }

   public void save(DataKey key) {
      this.saveOrRemove(key.getRelative("hand"), this.equipment[0]);
      this.saveOrRemove(key.getRelative("helmet"), this.equipment[1]);
      this.saveOrRemove(key.getRelative("chestplate"), this.equipment[2]);
      this.saveOrRemove(key.getRelative("leggings"), this.equipment[3]);
      this.saveOrRemove(key.getRelative("boots"), this.equipment[4]);
   }

   private void saveOrRemove(DataKey key, ItemStack item) {
      if (item != null) {
         ItemStorage.saveItem(key, item);
      } else if (key.keyExists("")) {
         key.removeKey("");
      }

   }

   public void set(int slot, ItemStack item) {
      if (this.npc.getBukkitEntity() instanceof LivingEntity) {
         if (this.npc.getBukkitEntity() instanceof Enderman) {
            if (slot != 0) {
               throw new UnsupportedOperationException("Slot can only be 0 for enderman");
            }

            ((Enderman)this.npc.getBukkitEntity()).setCarriedMaterial(item.getData());
         } else {
            EntityEquipment equip = this.getEquipmentFromEntity(this.npc.getBukkitEntity());
            switch (slot) {
               case 0:
                  equip.setItemInHand(item);
                  break;
               case 1:
                  equip.setHelmet(item);
                  break;
               case 2:
                  equip.setChestplate(item);
                  break;
               case 3:
                  equip.setLeggings(item);
                  break;
               case 4:
                  equip.setBoots(item);
                  break;
               default:
                  throw new IllegalArgumentException("Slot must be between 0 and 4");
            }

            if (this.npc.getBukkitEntity() instanceof Player) {
               ((Player)this.npc.getBukkitEntity()).updateInventory();
            }
         }

         this.equipment[slot] = item;
      }
   }

   public String toString() {
      return "{hand=" + this.equipment[0] + ",helmet=" + this.equipment[1] + ",chestplate=" + this.equipment[2] + ",leggings=" + this.equipment[3] + ",boots=" + this.equipment[4] + "}";
   }

   private static class PlayerEquipmentWrapper implements EntityEquipment {
      private final Player player;

      private PlayerEquipmentWrapper(Player player) {
         super();
         this.player = player;
      }

      public void clear() {
         this.player.getInventory().clear();
      }

      public ItemStack[] getArmorContents() {
         return this.player.getInventory().getArmorContents();
      }

      public ItemStack getBoots() {
         return this.player.getInventory().getBoots();
      }

      public float getBootsDropChance() {
         throw new UnsupportedOperationException();
      }

      public ItemStack getChestplate() {
         return this.player.getInventory().getChestplate();
      }

      public float getChestplateDropChance() {
         throw new UnsupportedOperationException();
      }

      public ItemStack getHelmet() {
         return this.player.getInventory().getHelmet();
      }

      public float getHelmetDropChance() {
         throw new UnsupportedOperationException();
      }

      public Entity getHolder() {
         return this.player;
      }

      public ItemStack getItemInHand() {
         return this.player.getItemInHand();
      }

      public float getItemInHandDropChance() {
         throw new UnsupportedOperationException();
      }

      public ItemStack getLeggings() {
         return this.player.getInventory().getLeggings();
      }

      public float getLeggingsDropChance() {
         throw new UnsupportedOperationException();
      }

      public void setArmorContents(ItemStack[] items) {
         this.player.getInventory().setArmorContents(items);
      }

      public void setBoots(ItemStack boots) {
         this.player.getInventory().setBoots(boots);
      }

      public void setBootsDropChance(float chance) {
         throw new UnsupportedOperationException();
      }

      public void setChestplate(ItemStack chestplate) {
         this.player.getInventory().setChestplate(chestplate);
      }

      public void setChestplateDropChance(float chance) {
         throw new UnsupportedOperationException();
      }

      public void setHelmet(ItemStack helmet) {
         this.player.getInventory().setHelmet(helmet);
      }

      public void setHelmetDropChance(float chance) {
         throw new UnsupportedOperationException();
      }

      public void setItemInHand(ItemStack stack) {
         this.player.setItemInHand(stack);
      }

      public void setItemInHandDropChance(float chance) {
         throw new UnsupportedOperationException();
      }

      public void setLeggings(ItemStack leggings) {
         this.player.getInventory().setLeggings(leggings);
      }

      public void setLeggingsDropChance(float chance) {
         throw new UnsupportedOperationException();
      }
   }
}
