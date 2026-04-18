package net.citizensnpcs.api.trait;

import net.citizensnpcs.api.npc.NPC;

public interface TraitFactory {
   void addDefaultTraits(NPC var1);

   Trait getTrait(Class var1);

   Trait getTrait(String var1);

   Class getTraitClass(String var1);

   boolean isInternalTrait(Trait var1);

   void registerTrait(TraitInfo var1);
}
