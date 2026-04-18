package net.citizensnpcs.api.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.Map;

public abstract class DataKey {
   protected final String path;
   private static final Predicate SIMPLE_INTEGER_FILTER = new Predicate() {
      public boolean apply(DataKey key) {
         try {
            Integer.parseInt(key.name());
            return true;
         } catch (NumberFormatException var3) {
            return false;
         }
      }
   };

   protected DataKey(String path) {
      super();
      this.path = path;
   }

   protected String createRelativeKey(String from) {
      if (from.isEmpty()) {
         return this.path;
      } else if (from.charAt(0) == '.') {
         return this.path.isEmpty() ? from.substring(1, from.length()) : this.path + from;
      } else {
         return this.path.isEmpty() ? from : this.path + '.' + from;
      }
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         DataKey other = (DataKey)obj;
         if (this.path == null) {
            if (other.path != null) {
               return false;
            }
         } else if (!this.path.equals(other.path)) {
            return false;
         }

         return true;
      } else {
         return false;
      }
   }

   public abstract boolean getBoolean(String var1);

   public boolean getBoolean(String key, boolean value) {
      if (this.keyExists(key)) {
         return this.getBoolean(key);
      } else {
         this.setBoolean(key, value);
         return value;
      }
   }

   public abstract double getDouble(String var1);

   public double getDouble(String key, double value) {
      if (this.keyExists(key)) {
         return this.getDouble(key);
      } else {
         this.setDouble(key, value);
         return value;
      }
   }

   public abstract int getInt(String var1);

   public int getInt(String key, int value) {
      if (this.keyExists(key)) {
         return this.getInt(key);
      } else {
         this.setInt(key, value);
         return value;
      }
   }

   public Iterable getIntegerSubKeys() {
      return Iterables.filter(this.getSubKeys(), SIMPLE_INTEGER_FILTER);
   }

   public abstract long getLong(String var1);

   public long getLong(String key, long value) {
      if (this.keyExists(key)) {
         return this.getLong(key);
      } else {
         this.setLong(key, value);
         return value;
      }
   }

   public String getPath() {
      return this.path;
   }

   public abstract Object getRaw(String var1);

   public Object getRawUnchecked(String key) {
      return this.getRaw(key);
   }

   public DataKey getRelative(int key) {
      return this.getRelative(Integer.toString(key));
   }

   public abstract DataKey getRelative(String var1);

   public abstract String getString(String var1);

   public String getString(String key, String value) {
      if (this.keyExists(key)) {
         return this.getString(key);
      } else {
         this.setString(key, value);
         return value;
      }
   }

   public abstract Iterable getSubKeys();

   public abstract Map getValuesDeep();

   public int hashCode() {
      int prime = 31;
      return 31 + (this.path == null ? 0 : this.path.hashCode());
   }

   public boolean keyExists() {
      return this.keyExists("");
   }

   public abstract boolean keyExists(String var1);

   public abstract String name();

   public abstract void removeKey(String var1);

   public abstract void setBoolean(String var1, boolean var2);

   public abstract void setDouble(String var1, double var2);

   public abstract void setInt(String var1, int var2);

   public abstract void setLong(String var1, long var2);

   public abstract void setRaw(String var1, Object var2);

   public abstract void setString(String var1, String var2);
}
