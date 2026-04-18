package net.citizensnpcs.npc;

import com.google.common.base.Preconditions;
import java.util.Iterator;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCCreateEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.ByIdArray;
import net.citizensnpcs.util.NMS;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class CitizensNPCRegistry implements NPCRegistry {
   private final ByIdArray npcs = new ByIdArray();
   private final NPCDataStore saves;

   public CitizensNPCRegistry(NPCDataStore store) {
      super();
      this.saves = store;
   }

   public NPC createNPC(EntityType type, int id, String name) {
      Preconditions.checkNotNull(name, "name cannot be null");
      Preconditions.checkNotNull(type, "type cannot be null");
      CitizensNPC npc = this.getByType(type, id, name);
      if (npc == null) {
         throw new IllegalStateException("Could not create NPC.");
      } else {
         this.npcs.put(npc.getId(), npc);
         Bukkit.getPluginManager().callEvent(new NPCCreateEvent(npc));
         return npc;
      }
   }

   public NPC createNPC(EntityType type, String name) {
      return this.createNPC(type, this.generateUniqueId(), name);
   }

   public void deregister(NPC npc) {
      this.npcs.remove(npc.getId());
      if (this.saves != null) {
         this.saves.clearData(npc);
      }

      npc.despawn(DespawnReason.REMOVAL);
   }

   public void deregisterAll() {
      Iterator<NPC> itr = this.iterator();

      while(itr.hasNext()) {
         NPC npc = (NPC)itr.next();
         itr.remove();
         npc.despawn(DespawnReason.REMOVAL);

         for(Trait t : npc.getTraits()) {
            t.onRemove();
         }

         if (this.saves != null) {
            this.saves.clearData(npc);
         }
      }

   }

   private int generateUniqueId() {
      return this.saves.createUniqueNPCId(this);
   }

   public NPC getById(int id) {
      if (id < 0) {
         throw new IllegalArgumentException("invalid id");
      } else {
         return (NPC)this.npcs.get(id);
      }
   }

   private CitizensNPC getByType(EntityType type, int id, String name) {
      return new CitizensNPC(id, name, EntityControllers.createForType(type));
   }

   public NPC getNPC(Entity entity) {
      if (entity == null) {
         return null;
      } else if (entity instanceof NPCHolder) {
         return ((NPCHolder)entity).getNPC();
      } else if (!(entity instanceof LivingEntity)) {
         return null;
      } else {
         Object handle = NMS.getHandle((LivingEntity)entity);
         return handle instanceof NPCHolder ? ((NPCHolder)handle).getNPC() : null;
      }
   }

   public boolean isNPC(Entity entity) {
      return this.getNPC(entity) != null;
   }

   public Iterator iterator() {
      return this.npcs.iterator();
   }
}
