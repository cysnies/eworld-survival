package net.citizensnpcs.api.persistence;

import net.citizensnpcs.api.util.DataKey;

public interface Persister {
   Object create(DataKey var1);

   void save(Object var1, DataKey var2);
}
