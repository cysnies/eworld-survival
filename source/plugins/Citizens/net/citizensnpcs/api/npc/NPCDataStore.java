package net.citizensnpcs.api.npc;

public interface NPCDataStore {
   void clearData(NPC var1);

   int createUniqueNPCId(NPCRegistry var1);

   void loadInto(NPCRegistry var1);

   void saveToDisk();

   void saveToDiskImmediate();

   void store(NPC var1);

   void storeAll(NPCRegistry var1);
}
