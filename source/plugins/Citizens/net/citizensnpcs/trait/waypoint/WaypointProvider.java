package net.citizensnpcs.trait.waypoint;

import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.entity.Player;

public interface WaypointProvider {
   WaypointEditor createEditor(Player var1, CommandContext var2);

   boolean isPaused();

   void load(DataKey var1);

   void onSpawn(NPC var1);

   void save(DataKey var1);

   void setPaused(boolean var1);
}
