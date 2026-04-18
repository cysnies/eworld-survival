package net.citizensnpcs.npc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.File;
import java.util.List;
import java.util.Map;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.MemoryDataKey;
import net.citizensnpcs.api.util.YamlStorage;

public class Template {
   private final String name;
   private final boolean override;
   private final Map replacements;
   private static YamlStorage templates = new YamlStorage(new File(CitizensAPI.getDataFolder(), "templates.yml"));

   private Template(String name, Map replacements, boolean override) {
      super();
      this.replacements = replacements;
      this.override = override;
      this.name = name;
   }

   public void apply(NPC npc) {
      MemoryDataKey memoryKey = new MemoryDataKey();
      ((CitizensNPC)npc).save(memoryKey);
      List<Node> queue = Lists.newArrayList(new Node[]{new Node("", this.replacements)});

      for(int i = 0; i < queue.size(); ++i) {
         Node node = (Node)queue.get(i);

         for(Map.Entry entry : node.map.entrySet()) {
            String fullKey = node.headKey + '.' + (String)entry.getKey();
            if (entry.getValue() instanceof Map) {
               queue.add(new Node(fullKey, (Map)entry.getValue()));
            } else {
               boolean overwrite = memoryKey.keyExists(fullKey) | this.override;
               if (overwrite) {
                  memoryKey.setRaw(fullKey, entry.getValue());
               }
            }
         }
      }

      ((CitizensNPC)npc).load(memoryKey);
   }

   public String getName() {
      return this.name;
   }

   public static Template byName(String name) {
      if (!templates.getKey("").keyExists(name)) {
         return null;
      } else {
         YamlStorage.YamlKey key = templates.getKey(name);
         boolean override = key.getBoolean("override", false);
         Map<String, Object> replacements = key.getRelative("replacements").getValuesDeep();
         return new Template(name, replacements, override);
      }
   }

   static {
      templates.load();
   }

   private static class Node {
      String headKey;
      Map map;

      private Node(String headKey, Map map) {
         super();
         this.headKey = headKey;
         this.map = map;
      }
   }

   public static class TemplateBuilder {
      private final String name;
      private boolean override;
      private final Map replacements = Maps.newHashMap();

      private TemplateBuilder(String name) {
         super();
         this.name = name;
      }

      public Template buildAndSave() {
         this.save();
         return new Template(this.name, this.replacements, this.override);
      }

      public TemplateBuilder from(NPC npc) {
         this.replacements.clear();
         MemoryDataKey key = new MemoryDataKey();
         ((CitizensNPC)npc).save(key);
         this.replacements.putAll(key.getRawTree());
         return this;
      }

      public TemplateBuilder override(boolean override) {
         this.override = override;
         return this;
      }

      public void save() {
         DataKey root = Template.templates.getKey(this.name);
         root.setBoolean("override", this.override);
         root.setRaw("replacements", this.replacements);
         Template.templates.save();
      }

      public static TemplateBuilder create(String name) {
         return new TemplateBuilder(name);
      }
   }
}
