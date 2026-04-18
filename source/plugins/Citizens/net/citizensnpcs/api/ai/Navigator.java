package net.citizensnpcs.api.ai;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public interface Navigator {
   void cancelNavigation();

   NavigatorParameters getDefaultParameters();

   EntityTarget getEntityTarget();

   NavigatorParameters getLocalParameters();

   NPC getNPC();

   Location getTargetAsLocation();

   TargetType getTargetType();

   boolean isNavigating();

   void setTarget(Entity var1, boolean var2);

   /** @deprecated */
   @Deprecated
   void setTarget(LivingEntity var1, boolean var2);

   void setTarget(Location var1);
}
