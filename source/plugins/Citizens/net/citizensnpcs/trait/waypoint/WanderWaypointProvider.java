package net.citizensnpcs.trait.waypoint;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.goals.WanderGoal;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.entity.Player;

public class WanderWaypointProvider implements WaypointProvider {
   private Goal currentGoal;
   private volatile boolean paused;
   @Persist
   private final int xrange = 3;
   @Persist
   private final int yrange = 25;
   private static final int DEFAULT_XRANGE = 3;
   private static final int DEFAULT_YRANGE = 25;

   public WanderWaypointProvider() {
      super();
   }

   public WaypointEditor createEditor(Player player, CommandContext args) {
      return new WaypointEditor() {
         public void begin() {
         }

         public void end() {
         }
      };
   }

   public boolean isPaused() {
      return this.paused;
   }

   public void load(DataKey key) {
   }

   public void onSpawn(NPC npc) {
      if (this.currentGoal == null) {
         this.currentGoal = WanderGoal.createWithNPCAndRange(npc, 3, 25);
         CitizensAPI.registerEvents(this.currentGoal);
      }

      npc.getDefaultGoalController().addGoal(this.currentGoal, 1);
   }

   public void save(DataKey key) {
   }

   public void setPaused(boolean paused) {
      this.paused = paused;
   }
}
