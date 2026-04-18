package net.citizensnpcs.trait.waypoint.triggers;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.util.Messaging;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ChatTrigger implements WaypointTrigger {
   @Persist(
      required = true
   )
   private List lines;
   @Persist
   private double radius = (double)-1.0F;

   public ChatTrigger() {
      super();
   }

   public ChatTrigger(double radius, Collection chatLines) {
      super();
      this.radius = radius;
      this.lines = Lists.newArrayList(chatLines);
   }

   public String description() {
      return String.format("Chat Trigger [radius %d, %s]", this.radius, Joiner.on(", ").join(this.lines));
   }

   public void onWaypointReached(NPC npc, Location waypoint) {
      if (this.radius < (double)0.0F) {
         for(Player player : npc.getBukkitEntity().getWorld().getPlayers()) {
            for(String line : this.lines) {
               Messaging.send(player, line);
            }
         }
      } else {
         for(Entity entity : npc.getBukkitEntity().getNearbyEntities(this.radius, this.radius, this.radius)) {
            if (entity instanceof Player) {
               for(String line : this.lines) {
                  Messaging.send((Player)entity, line);
               }
            }
         }
      }

   }
}
