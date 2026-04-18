package net.citizensnpcs.trait.waypoint.triggers;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.util.PlayerAnimation;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class AnimationTrigger implements WaypointTrigger {
   @Persist(
      required = true
   )
   private List animations;

   public AnimationTrigger() {
      super();
   }

   public AnimationTrigger(Collection collection) {
      super();
      this.animations = Lists.newArrayList(this.animations);
   }

   public String description() {
      return String.format("Animation Trigger [animating %s]", Joiner.on(", ").join(this.animations));
   }

   public void onWaypointReached(NPC npc, Location waypoint) {
      if (npc.getBukkitEntity().getType() == EntityType.PLAYER) {
         Player player = (Player)npc.getBukkitEntity();

         for(PlayerAnimation animation : this.animations) {
            animation.play(player);
         }

      }
   }
}
