package net.citizensnpcs.api.npc;

import net.citizensnpcs.api.ai.GoalController;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.speech.SpeechController;
import net.citizensnpcs.api.astar.Agent;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerTeleportEvent;

public interface NPC extends Agent, Cloneable {
   String DEFAULT_PROTECTED_METADATA = "protected";
   String LEASH_PROTECTED_METADATA = "protected-leash";
   String RESPAWN_DELAY_METADATA = "respawn-delay";
   String TARGETABLE_METADATA = "protected-target";

   void addTrait(Class var1);

   void addTrait(Trait var1);

   NPC clone();

   MetadataStore data();

   boolean despawn();

   boolean despawn(DespawnReason var1);

   void destroy();

   void faceLocation(Location var1);

   LivingEntity getBukkitEntity();

   GoalController getDefaultGoalController();

   SpeechController getDefaultSpeechController();

   String getFullName();

   int getId();

   String getName();

   Navigator getNavigator();

   Location getStoredLocation();

   Trait getTrait(Class var1);

   Iterable getTraits();

   boolean hasTrait(Class var1);

   boolean isProtected();

   boolean isSpawned();

   void load(DataKey var1);

   void removeTrait(Class var1);

   void save(DataKey var1);

   void setBukkitEntityType(EntityType var1);

   void setName(String var1);

   void setProtected(boolean var1);

   boolean spawn(Location var1);

   void teleport(Location var1, PlayerTeleportEvent.TeleportCause var2);
}
