package net.citizensnpcs.api.npc;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public interface NPCRegistry extends Iterable {
   NPC createNPC(EntityType var1, int var2, String var3);

   NPC createNPC(EntityType var1, String var2);

   void deregister(NPC var1);

   void deregisterAll();

   NPC getById(int var1);

   NPC getNPC(Entity var1);

   boolean isNPC(Entity var1);
}
