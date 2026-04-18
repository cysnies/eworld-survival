package net.citizensnpcs.api.util;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class YamlStorage implements FileStorage {
   private final FileConfiguration config;
   private final File file;

   public YamlStorage(File file) {
      this(file, (String)null);
   }

   public YamlStorage(File file, String header) {
      super();
      this.config = new YamlConfiguration();
      this.file = file;
      if (!file.exists()) {
         this.create();
         if (header != null) {
            this.config.options().header(header);
         }

         this.save();
      }

   }

   private void create() {
      try {
         Bukkit.getLogger().log(Level.INFO, "Creating file: " + this.file.getName());
         this.file.getParentFile().mkdirs();
         this.file.createNewFile();
      } catch (IOException var2) {
         Bukkit.getLogger().log(Level.SEVERE, "Could not create file: " + this.file.getName());
      }

   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         YamlStorage other = (YamlStorage)obj;
         if (this.file == null) {
            if (other.file != null) {
               return false;
            }
         } else if (!this.file.equals(other.file)) {
            return false;
         }

         return true;
      } else {
         return false;
      }
   }

   public File getFile() {
      return this.file;
   }

   public YamlKey getKey(String root) {
      return new YamlKey(root);
   }

   public int hashCode() {
      int prime = 31;
      int result = 1;
      result = 31 * result + (this.file == null ? 0 : this.file.hashCode());
      return result;
   }

   public boolean load() {
      try {
         this.config.load(this.file);
         return true;
      } catch (Exception ex) {
         ex.printStackTrace();
         return false;
      }
   }

   private boolean pathExists(String key) {
      return this.config.get(key) != null;
   }

   public void save() {
      try {
         Files.createParentDirs(this.file);
         File temporaryFile = File.createTempFile(this.file.getName(), (String)null, this.file.getParentFile());
         temporaryFile.deleteOnExit();
         this.config.save(temporaryFile);
         this.file.delete();
         temporaryFile.renameTo(this.file);
         temporaryFile.delete();
      } catch (Exception ex) {
         ex.printStackTrace();
      }

   }

   public String toString() {
      return "YamlStorage {file=" + this.file + "}";
   }

   public class YamlKey extends DataKey {
      public YamlKey(String root) {
         super(root);
      }

      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (super.equals(obj) && this.getClass() == obj.getClass()) {
            YamlKey other = (YamlKey)obj;
            return this.getOuterType().equals(other.getOuterType());
         } else {
            return false;
         }
      }

      public boolean getBoolean(String key) {
         String path = this.createRelativeKey(key);
         if (YamlStorage.this.pathExists(path)) {
            return YamlStorage.this.config.getString(path) == null ? YamlStorage.this.config.getBoolean(path) : Boolean.parseBoolean(YamlStorage.this.config.getString(path));
         } else {
            return false;
         }
      }

      public boolean getBoolean(String key, boolean def) {
         return YamlStorage.this.config.getBoolean(this.createRelativeKey(key), def);
      }

      public double getDouble(String key) {
         return this.getDouble(key, (double)0.0F);
      }

      public double getDouble(String key, double def) {
         String path = this.createRelativeKey(key);
         if (YamlStorage.this.pathExists(path)) {
            Object value = YamlStorage.this.config.get(path);
            if (value instanceof Number) {
               return ((Number)value).doubleValue();
            } else {
               String raw = value.toString();
               return raw.isEmpty() ? def : Double.parseDouble(raw);
            }
         } else {
            return def;
         }
      }

      public int getInt(String key) {
         return this.getInt(key, 0);
      }

      public int getInt(String key, int def) {
         String path = this.createRelativeKey(key);
         if (YamlStorage.this.pathExists(path)) {
            Object value = YamlStorage.this.config.get(path);
            if (value instanceof Number) {
               return ((Number)value).intValue();
            } else {
               String raw = value.toString();
               return raw.isEmpty() ? def : Integer.parseInt(raw);
            }
         } else {
            return def;
         }
      }

      public long getLong(String key) {
         return this.getLong(key, 0L);
      }

      public long getLong(String key, long def) {
         String path = this.createRelativeKey(key);
         if (YamlStorage.this.pathExists(path)) {
            Object value = YamlStorage.this.config.get(path);
            if (value instanceof Number) {
               return ((Number)value).longValue();
            } else {
               String raw = value.toString();
               return raw.isEmpty() ? def : Long.parseLong(raw);
            }
         } else {
            return def;
         }
      }

      private YamlStorage getOuterType() {
         return YamlStorage.this;
      }

      public Object getRaw(String key) {
         return YamlStorage.this.config.get(this.createRelativeKey(key));
      }

      public YamlKey getRelative(String relative) {
         return relative != null && !relative.isEmpty() ? YamlStorage.this.new YamlKey(this.createRelativeKey(relative)) : this;
      }

      public String getString(String key) {
         String path = this.createRelativeKey(key);
         return YamlStorage.this.pathExists(path) ? YamlStorage.this.config.get(path).toString() : "";
      }

      public Iterable getSubKeys() {
         ConfigurationSection section = YamlStorage.this.config.getConfigurationSection(this.path);
         if (section == null) {
            return Collections.emptyList();
         } else {
            List<DataKey> res = new ArrayList();

            for(String key : section.getKeys(false)) {
               res.add(this.getRelative(key));
            }

            return res;
         }
      }

      public Map getValuesDeep() {
         ConfigurationSection subSection = YamlStorage.this.config.getConfigurationSection(this.path);
         return subSection == null ? Collections.emptyMap() : subSection.getValues(true);
      }

      public int hashCode() {
         int prime = 31;
         int result = 31 * super.hashCode() + this.getOuterType().hashCode();
         return result;
      }

      public boolean keyExists(String key) {
         return YamlStorage.this.config.get(this.createRelativeKey(key)) != null;
      }

      public String name() {
         int last = this.path.lastIndexOf(46);
         return this.path.substring(last == 0 ? 0 : last + 1);
      }

      public void removeKey(String key) {
         YamlStorage.this.config.set(this.createRelativeKey(key), (Object)null);
      }

      public void setBoolean(String key, boolean value) {
         YamlStorage.this.config.set(this.createRelativeKey(key), value);
      }

      public void setDouble(String key, double value) {
         YamlStorage.this.config.set(this.createRelativeKey(key), String.valueOf(value));
      }

      public void setInt(String key, int value) {
         YamlStorage.this.config.set(this.createRelativeKey(key), value);
      }

      public void setLong(String key, long value) {
         YamlStorage.this.config.set(this.createRelativeKey(key), value);
      }

      public void setRaw(String key, Object value) {
         YamlStorage.this.config.set(this.createRelativeKey(key), value);
      }

      public void setString(String key, String value) {
         YamlStorage.this.config.set(this.createRelativeKey(key), value);
      }

      public String toString() {
         return "YamlKey [path=" + this.path + "]";
      }
   }
}
