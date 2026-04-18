package net.citizensnpcs.api.ai;

import org.bukkit.entity.LivingEntity;

public interface AttackStrategy {
   boolean handle(LivingEntity var1, LivingEntity var2);
}
