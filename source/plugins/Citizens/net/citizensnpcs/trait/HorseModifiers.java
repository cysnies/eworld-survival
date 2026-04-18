package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.inventory.ItemStack;

public class HorseModifiers extends Trait {
   @Persist("armor")
   private ItemStack armor = null;
   @Persist("carryingChest")
   private boolean carryingChest;
   @Persist("color")
   private Horse.Color color;
   @Persist("saddle")
   private ItemStack saddle;
   @Persist("style")
   private Horse.Style style;
   @Persist("type")
   private Horse.Variant type;

   public HorseModifiers() {
      super("horsemodifiers");
      this.color = Color.CREAMY;
      this.saddle = null;
      this.style = Style.NONE;
      this.type = Variant.HORSE;
   }

   public Horse.Color getColor() {
      return this.color;
   }

   public Horse.Style getStyle() {
      return this.style;
   }

   public Horse.Variant getType() {
      return this.type;
   }

   public void onSpawn() {
      this.updateModifiers();
   }

   public void run() {
      if (this.npc.getBukkitEntity() instanceof Horse) {
         Horse horse = (Horse)this.npc.getBukkitEntity();
         this.saddle = horse.getInventory().getSaddle();
         this.armor = horse.getInventory().getArmor();
      }

   }

   public void setCarryingChest(boolean carryingChest) {
      this.carryingChest = carryingChest;
      this.updateModifiers();
   }

   public void setColor(Horse.Color color) {
      this.color = color;
      this.updateModifiers();
   }

   public void setStyle(Horse.Style style) {
      this.style = style;
      this.updateModifiers();
   }

   public void setType(Horse.Variant type) {
      this.type = type;
      this.updateModifiers();
   }

   private void updateModifiers() {
      if (this.npc.getBukkitEntity() instanceof Horse) {
         Horse horse = (Horse)this.npc.getBukkitEntity();
         horse.setCarryingChest(this.carryingChest);
         horse.setColor(this.color);
         horse.setStyle(this.style);
         horse.setVariant(this.type);
         horse.getInventory().setArmor(this.armor);
         horse.getInventory().setSaddle(this.saddle);
      }

   }
}
