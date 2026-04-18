package net.citizensnpcs.api.util;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

public class MemoryDataKey extends DataKey {
   private String name;
   private final ConfigurationSection root;

   public MemoryDataKey() {
      super("");
      this.root = new MemoryConfiguration();
   }

   private MemoryDataKey(ConfigurationSection root, String path) {
      super(path);
      this.root = root;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         MemoryDataKey other = (MemoryDataKey)obj;
         if (this.path == null) {
            if (other.path != null) {
               return false;
            }
         } else if (!this.path.equals(other.path)) {
            return false;
         }

         return true;
      }
   }

   public boolean getBoolean(String key) {
      return this.root.getBoolean(this.getKeyFor(key), false);
   }

   public double getDouble(String key) {
      return this.root.getDouble(this.getKeyFor(key), (double)0.0F);
   }

   public int getInt(String key) {
      return this.root.getInt(this.getKeyFor(key), 0);
   }

   private String getKeyFor(String key) {
      if (key.isEmpty()) {
         return this.path;
      } else if (key.charAt(0) == '.') {
         return this.path.isEmpty() ? key.substring(1, key.length()) : this.path + key;
      } else {
         return this.path.isEmpty() ? key : this.path + "." + key;
      }
   }

   public long getLong(String key) {
      return this.root.getLong(this.getKeyFor(key), 0L);
   }

   public Object getRaw(String key) {
      return this.root.get(this.getKeyFor(key));
   }

   public Map getRawTree() {
      return this.root.getValues(true);
   }

   public MemoryDataKey getRelative(String relative) {
      String key = this.getKeyFor(relative);
      return new MemoryDataKey(this.root, key);
   }

   public String getString(String key) {
      return this.root.getString(this.getKeyFor(key), "");
   }

   public Iterable getSubKeys() {
      ConfigurationSection head = this.root.getConfigurationSection(this.path);
      if (head == null) {
         return Collections.emptyList();
      } else {
         Set<String> keys = head.getKeys(false);
         return Iterables.transform(keys, new Function() {
            public DataKey apply(@Nullable String input) {
               return new MemoryDataKey(MemoryDataKey.this.root, MemoryDataKey.this.getKeyFor(input));
            }
         });
      }
   }

   public Map getValuesDeep() {
      ConfigurationSection section = this.root.getConfigurationSection(this.path);
      return section == null ? Collections.emptyMap() : section.getValues(true);
   }

   public int hashCode() {
      int prime = 31;
      int result = 1;
      result = 31 * result + (this.path == null ? 0 : this.path.hashCode());
      return result;
   }

   public boolean keyExists(String key) {
      return this.root.isSet(this.getKeyFor(key));
   }

   public String name() {
      if (this.name == null) {
         int idx = this.path.lastIndexOf(46);
         this.name = idx == -1 ? this.path : this.path.substring(idx + 1, this.path.length());
      }

      return this.name;
   }

   public void removeKey(String key) {
      this.set(key, (Object)null);
   }

   private void set(String key, Object value) {
      this.root.set(this.getKeyFor(key), value);
   }

   public void setBoolean(String key, boolean value) {
      this.set(key, value);
   }

   public void setDouble(String key, double value) {
      this.set(key, value);
   }

   public void setInt(String key, int value) {
      this.set(key, value);
   }

   public void setLong(String key, long value) {
      this.set(key, value);
   }

   public void setRaw(String key, Object value) {
      this.set(key, value);
   }

   public void setString(String key, String value) {
      this.set(key, value);
   }
}
