package net.citizensnpcs.api.npc;

import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Storage;
import org.bukkit.entity.EntityType;

public class SimpleNPCDataStore implements NPCDataStore {
   private final Storage root;
   private static final String LOAD_NAME_NOT_FOUND = "citizens.notifications.npc-name-not-found";
   private static final String LOAD_UNKNOWN_NPC_TYPE = "citizens.notifications.unknown-npc-type";

   private SimpleNPCDataStore(Storage saves) {
      super();
      this.root = saves;
   }

   public void clearData(NPC npc) {
      this.root.getKey("npc").removeKey(Integer.toString(npc.getId()));
   }

   public int createUniqueNPCId(NPCRegistry registry) {
      DataKey key = this.root.getKey("");
      int newId = key.getInt("last-created-npc-id", -1);
      if (newId != -1 && registry.getById(newId + 1) == null) {
         ++newId;
      } else {
         int maxId = Integer.MIN_VALUE;

         for(NPC npc : registry) {
            if (npc.getId() > maxId) {
               maxId = npc.getId();
            }
         }

         newId = maxId == Integer.MIN_VALUE ? 0 : maxId + 1;
      }

      key.setInt("last-created-npc-id", newId);
      return newId;
   }

   public void loadInto(NPCRegistry registry) {
      for(DataKey key : this.root.getKey("npc").getIntegerSubKeys()) {
         int id = Integer.parseInt(key.name());
         if (!key.keyExists("name")) {
            Messaging.logTr("citizens.notifications.npc-name-not-found", id);
         } else {
            String unparsedEntityType = key.getString("traits.type", "PLAYER");
            EntityType type = matchEntityType(unparsedEntityType);
            if (type == null) {
               Messaging.logTr("citizens.notifications.unknown-npc-type", unparsedEntityType);
            } else {
               NPC npc = registry.createNPC(type, id, key.getString("name"));
               npc.load(key);
            }
         }
      }

   }

   public void saveToDisk() {
      (new Thread() {
         public void run() {
            SimpleNPCDataStore.this.root.save();
         }
      }).start();
   }

   public void saveToDiskImmediate() {
      this.root.save();
   }

   public void store(NPC npc) {
      npc.save(this.root.getKey("npc." + npc.getId()));
   }

   public void storeAll(NPCRegistry registry) {
      for(NPC npc : registry) {
         this.store(npc);
      }

   }

   public static NPCDataStore create(Storage storage) {
      return new SimpleNPCDataStore(storage);
   }

   private static EntityType matchEntityType(String toMatch) {
      EntityType type = EntityType.fromName(toMatch);
      return type != null ? type : (EntityType)matchEnum(EntityType.values(), toMatch);
   }

   private static Enum matchEnum(Enum[] values, String toMatch) {
      T type = (T)null;

      for(Enum check : values) {
         String name = check.name();
         if (name.matches(toMatch) || name.equalsIgnoreCase(toMatch) || name.replace("_", "").equalsIgnoreCase(toMatch) || name.replace('_', '-').equalsIgnoreCase(toMatch) || name.replace('_', ' ').equalsIgnoreCase(toMatch) || name.startsWith(toMatch)) {
            type = check;
            break;
         }
      }

      return type;
   }
}
