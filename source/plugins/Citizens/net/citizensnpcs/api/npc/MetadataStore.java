package net.citizensnpcs.api.npc;

import net.citizensnpcs.api.util.DataKey;

public interface MetadataStore {
   Object get(String var1);

   Object get(String var1, Object var2);

   boolean has(String var1);

   void loadFrom(DataKey var1);

   void remove(String var1);

   void saveTo(DataKey var1);

   void set(String var1, Object var2);

   void setPersistent(String var1, Object var2);
}
