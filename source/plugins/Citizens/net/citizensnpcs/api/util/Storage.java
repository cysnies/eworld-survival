package net.citizensnpcs.api.util;

public interface Storage {
   DataKey getKey(String var1);

   boolean load();

   void save();
}
