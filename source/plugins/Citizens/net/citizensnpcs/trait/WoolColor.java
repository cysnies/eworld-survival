package net.citizensnpcs.trait;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.DyeColor;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.SheepDyeWoolEvent;

public class WoolColor extends Trait {
   private DyeColor color;
   boolean sheep;

   public WoolColor() {
      super("woolcolor");
      this.color = DyeColor.WHITE;
      this.sheep = false;
   }

   public void load(DataKey key) throws NPCLoadException {
      try {
         this.color = DyeColor.valueOf(key.getString(""));
      } catch (Exception var3) {
         this.color = DyeColor.WHITE;
      }

   }

   @EventHandler
   public void onSheepDyeWool(SheepDyeWoolEvent event) {
      if (this.npc.equals(CitizensAPI.getNPCRegistry().getNPC(event.getEntity()))) {
         event.setCancelled(true);
      }

   }

   public void onSpawn() {
      if (this.npc.getBukkitEntity() instanceof Sheep) {
         ((Sheep)this.npc.getBukkitEntity()).setColor(this.color);
         this.sheep = true;
      } else {
         this.sheep = false;
      }

   }

   public void save(DataKey key) {
      key.setString("", this.color.name());
   }

   public void setColor(DyeColor color) {
      this.color = color;
      if (this.sheep) {
         ((Sheep)this.npc.getBukkitEntity()).setColor(color);
      }

   }

   public String toString() {
      return "WoolColor{" + this.color.name() + "}";
   }
}
