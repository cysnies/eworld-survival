package net.citizensnpcs.api.util;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.zip.GZIPInputStream;
import net.citizensnpcs.api.jnbt.ByteTag;
import net.citizensnpcs.api.jnbt.CompoundTag;
import net.citizensnpcs.api.jnbt.DoubleTag;
import net.citizensnpcs.api.jnbt.FloatTag;
import net.citizensnpcs.api.jnbt.IntTag;
import net.citizensnpcs.api.jnbt.LongTag;
import net.citizensnpcs.api.jnbt.NBTInputStream;
import net.citizensnpcs.api.jnbt.NBTOutputStream;
import net.citizensnpcs.api.jnbt.NBTUtils;
import net.citizensnpcs.api.jnbt.ShortTag;
import net.citizensnpcs.api.jnbt.StringTag;
import net.citizensnpcs.api.jnbt.Tag;

public class NBTStorage implements FileStorage {
   private final File file;
   private final String name;
   private final Map root;

   public NBTStorage(File file) {
      this(file, "root");
   }

   public NBTStorage(File file, String name) {
      super();
      this.root = Maps.newHashMap();
      this.file = file;
      if (!this.file.exists()) {
         this.create();
      }

      this.name = name;
   }

   public NBTStorage(String file) {
      this(new File(file), "root");
   }

   private void create() {
      try {
         Messaging.log("Creating file: " + this.file.getName());
         Files.createParentDirs(this.file);
         this.file.createNewFile();
      } catch (IOException ex) {
         Messaging.severe("Could not create file: " + this.file.getName());
         ex.printStackTrace();
      }

   }

   public File getFile() {
      return this.file;
   }

   public DataKey getKey(String root) {
      return new NBTKey(root);
   }

   public boolean load() {
      NBTInputStream stream = null;

      boolean var9;
      try {
         stream = new NBTInputStream(new GZIPInputStream(new FileInputStream(this.file)));
         Tag tag = stream.readTag();
         if (tag != null && tag instanceof CompoundTag) {
            this.root.clear();
            this.root.putAll(((CompoundTag)tag).getValue());
            return true;
         }

         var9 = false;
      } catch (IOException ex) {
         ex.printStackTrace();
         var9 = false;
         return var9;
      } finally {
         Closeables.closeQuietly(stream);
      }

      return var9;
   }

   public void save() {
      NBTOutputStream stream = null;

      try {
         Files.createParentDirs(this.file);
         File temporaryFile = File.createTempFile(this.file.getName(), (String)null, this.file.getParentFile());
         temporaryFile.deleteOnExit();
         stream = new NBTOutputStream(new FileOutputStream(temporaryFile));
         stream.writeTag(new CompoundTag(this.name, this.root));
         stream.close();
         this.file.delete();
         temporaryFile.renameTo(this.file);
         temporaryFile.delete();
      } catch (IOException ex) {
         ex.printStackTrace();
      } finally {
         Closeables.closeQuietly(stream);
      }

   }

   public String toString() {
      return "NBTStorage {file=" + this.file + "}";
   }

   public class NBTKey extends DataKey {
      public NBTKey(String root) {
         super(root);
      }

      private String createRelativeKey(String parent, String sub) {
         if (sub.isEmpty()) {
            return parent;
         } else if (sub.charAt(0) == '.') {
            return parent.isEmpty() ? sub.substring(1, sub.length()) : parent + sub;
         } else {
            return parent.isEmpty() ? sub : parent + "." + sub;
         }
      }

      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NBTKey other = (NBTKey)obj;
            if (!this.getOuterType().equals(other.getOuterType())) {
               return false;
            } else {
               if (this.path == null) {
                  if (other.path != null) {
                     return false;
                  }
               } else if (!this.path.equals(other.path)) {
                  return false;
               }

               return true;
            }
         } else {
            return false;
         }
      }

      private Number extractNumber(Tag tag) {
         if (tag == null) {
            return null;
         } else if (tag instanceof DoubleTag) {
            return ((DoubleTag)tag).getValue();
         } else if (tag instanceof IntTag) {
            return ((IntTag)tag).getValue();
         } else if (tag instanceof ShortTag) {
            return ((ShortTag)tag).getValue();
         } else if (tag instanceof ByteTag) {
            return ((ByteTag)tag).getValue();
         } else if (tag instanceof FloatTag) {
            return ((FloatTag)tag).getValue();
         } else {
            return tag instanceof LongTag ? ((LongTag)tag).getValue() : null;
         }
      }

      private Map findLastParent(String[] parts) {
         Map<String, Tag> map = NBTStorage.this.root;

         for(int i = 0; i < parts.length - 1; ++i) {
            if (!map.containsKey(parts[i]) || !(map.get(parts[i]) instanceof CompoundTag)) {
               return null;
            }

            map = ((CompoundTag)map.get(parts[i])).getValue();
         }

         return map;
      }

      private Tag findLastTag(String key) {
         return this.findLastTag(key, true);
      }

      private Tag findLastTag(String key, boolean relative) {
         String[] parts = (String[])Iterables.toArray(Splitter.on('.').omitEmptyStrings().split(relative ? this.createRelativeKey(key) : key), String.class);
         if (parts.length == 0) {
            return new CompoundTag(NBTStorage.this.name, NBTStorage.this.root);
         } else {
            Map<String, Tag> map = this.findLastParent(parts);
            return !map.containsKey(parts[parts.length - 1]) ? null : (Tag)map.get(parts[parts.length - 1]);
         }
      }

      public boolean getBoolean(String key) {
         Number number = this.extractNumber(this.findLastTag(key));
         if (number == null) {
            return false;
         } else {
            return number.byteValue() >= 1;
         }
      }

      public double getDouble(String key) {
         Number number = this.extractNumber(this.findLastTag(key));
         return number == null ? (double)0.0F : number.doubleValue();
      }

      public int getInt(String key) {
         Number number = this.extractNumber(this.findLastTag(key));
         return number == null ? 0 : number.intValue();
      }

      public long getLong(String key) {
         Number number = this.extractNumber(this.findLastTag(key));
         return number == null ? 0L : number.longValue();
      }

      private String getNameFor(String key) {
         String[] parts = (String[])Iterables.toArray(Splitter.on('.').split(this.createRelativeKey(key)), String.class);
         return parts[parts.length - 1];
      }

      private NBTStorage getOuterType() {
         return NBTStorage.this;
      }

      public Object getRaw(String key) {
         Tag tag = this.findLastTag(key);
         return tag == null ? null : tag.getValue();
      }

      public DataKey getRelative(String relative) {
         return NBTStorage.this.new NBTKey(this.createRelativeKey(relative));
      }

      public String getString(String key) {
         Tag tag = this.findLastTag(key);
         return tag != null && tag instanceof StringTag ? ((StringTag)tag).getValue() : "";
      }

      public Iterable getSubKeys() {
         Tag tag = this.findLastTag(this.path, false);
         if (!(tag instanceof CompoundTag)) {
            return Collections.emptyList();
         } else {
            List<DataKey> subKeys = Lists.newArrayList();

            for(String name : ((CompoundTag)tag).getValue().keySet()) {
               subKeys.add(NBTStorage.this.new NBTKey(this.createRelativeKey(name)));
            }

            return subKeys;
         }
      }

      public Map getValuesDeep() {
         Tag tag = this.findLastTag(this.path, false);
         if (!(tag instanceof CompoundTag)) {
            return Collections.emptyMap();
         } else {
            Queue<Node> node = new ArrayDeque(ImmutableList.of(new Node(tag)));
            Map<String, Object> values = Maps.newHashMap();

            while(!node.isEmpty()) {
               Node root = (Node)node.poll();

               for(Map.Entry entry : root.values.entrySet()) {
                  String key = this.createRelativeKey(root.parent, (String)entry.getKey());
                  if (entry.getValue() instanceof CompoundTag) {
                     node.add(new Node(key, (Tag)entry.getValue()));
                  } else {
                     values.put(key, ((Tag)entry.getValue()).getValue());
                  }
               }
            }

            return values;
         }
      }

      public int hashCode() {
         int prime = 31;
         int result = 1;
         result = 31 * result + this.getOuterType().hashCode();
         result = 31 * result + (this.path == null ? 0 : this.path.hashCode());
         return result;
      }

      public boolean keyExists(String key) {
         return this.findLastTag(this.createRelativeKey(key)) != null;
      }

      public String name() {
         int last = this.path.lastIndexOf(46);
         return this.path.substring(last == 0 ? 0 : last + 1);
      }

      private void putTag(String key, Tag tag) {
         String[] parts = (String[])Iterables.toArray(Splitter.on('.').split(this.createRelativeKey(key)), String.class);
         Map<String, Tag> parent = NBTStorage.this.root;

         for(int i = 0; i < parts.length - 1; ++i) {
            if (!parent.containsKey(parts[i]) || !(parent.get(parts[i]) instanceof CompoundTag)) {
               parent.put(parts[i], new CompoundTag(parts[i]));
            }

            parent = ((CompoundTag)parent.get(parts[i])).getValue();
         }

         parent.put(tag.getName(), tag);
      }

      public void removeKey(String key) {
         String[] parts = (String[])Iterables.toArray(Splitter.on('.').split(this.createRelativeKey(key)), String.class);
         Map<String, Tag> parent = this.findLastParent(parts);
         parent.remove(parts[parts.length - 1]);
      }

      public void setBoolean(String key, boolean value) {
         this.putTag(key, new ByteTag(this.getNameFor(key), (byte)(value ? 1 : 0)));
      }

      public void setDouble(String key, double value) {
         this.putTag(key, new DoubleTag(this.getNameFor(key), value));
      }

      public void setInt(String key, int value) {
         this.putTag(key, new IntTag(this.getNameFor(key), value));
      }

      public void setLong(String key, long value) {
         this.putTag(key, new LongTag(this.getNameFor(key), value));
      }

      public void setRaw(String key, Object value) {
         Tag tag = NBTUtils.createTag(this.getNameFor(key), value);
         if (tag == null) {
            throw new IllegalArgumentException("could not convert value to tag");
         } else {
            this.putTag(key, tag);
         }
      }

      public void setString(String key, String value) {
         this.putTag(key, new StringTag(this.getNameFor(key), value));
      }
   }

   private static class Node {
      final String parent;
      final Map values;

      public Node(String parent, Tag tag) {
         super();
         this.parent = parent;
         this.values = ((CompoundTag)tag).getValue();
      }

      public Node(Tag tag) {
         this("", tag);
      }
   }
}
