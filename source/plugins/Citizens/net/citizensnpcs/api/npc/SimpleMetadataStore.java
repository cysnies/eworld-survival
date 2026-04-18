package net.citizensnpcs.api.npc;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.util.Map;
import net.citizensnpcs.api.util.DataKey;

public class SimpleMetadataStore implements MetadataStore {
   private final Map metadata = Maps.newHashMap();

   public SimpleMetadataStore() {
      super();
   }

   private void checkPrimitive(Object data) {
      Preconditions.checkNotNull(data, "data cannot be null");
      boolean isPrimitive = data instanceof String || data instanceof Boolean || data instanceof Number;
      if (!isPrimitive) {
         throw new IllegalArgumentException("data is not primitive");
      }
   }

   public Object get(String key) {
      Preconditions.checkNotNull(key, "key cannot be null");
      MetadataObject normal = (MetadataObject)this.metadata.get(key);
      return normal == null ? null : normal.value;
   }

   public Object get(String key, Object def) {
      T t = (T)this.get(key);
      if (t == null) {
         this.set(key, def);
         return def;
      } else {
         return t;
      }
   }

   public boolean has(String key) {
      Preconditions.checkNotNull(key, "key cannot be null");
      return this.metadata.containsKey(key);
   }

   public void loadFrom(DataKey key) {
      for(Map.Entry entry : this.metadata.entrySet()) {
         if (((MetadataObject)entry.getValue()).persistent) {
            this.remove((String)entry.getKey());
         }
      }

      for(DataKey subKey : key.getSubKeys()) {
         this.setPersistent(subKey.name(), subKey.getRaw(""));
      }

   }

   public void remove(String key) {
      this.metadata.remove(key);
   }

   public void saveTo(DataKey key) {
      Preconditions.checkNotNull(key, "key cannot be null");

      for(Map.Entry entry : this.metadata.entrySet()) {
         if (((MetadataObject)entry.getValue()).persistent) {
            key.setRaw((String)entry.getKey(), ((MetadataObject)entry.getValue()).value);
         }
      }

   }

   public void set(String key, Object data) {
      Preconditions.checkNotNull(data, "data cannot be null");
      Preconditions.checkNotNull(key, "key cannot be null");
      this.metadata.put(key, new MetadataObject(data, false));
   }

   public void setPersistent(String key, Object data) {
      Preconditions.checkNotNull(key, "key cannot be null");
      this.checkPrimitive(data);
      this.metadata.put(key, new MetadataObject(data, true));
   }

   private static class MetadataObject {
      final boolean persistent;
      final Object value;

      public MetadataObject(Object raw, boolean persistent) {
         super();
         this.value = raw;
         this.persistent = persistent;
      }
   }
}
