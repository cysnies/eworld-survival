package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.customobjects.bo2.BO2Loader;
import com.khorn.terraincontrol.customobjects.bo3.BO3Loader;
import com.khorn.terraincontrol.generator.resourcegens.TreeType;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CustomObjectManager {
   public final Map loaders = new HashMap();
   public final Map globalObjects = new HashMap();

   public CustomObjectManager() {
      super();
      this.registerCustomObjectLoader("bo2", new BO2Loader());
      this.registerCustomObjectLoader("bo3", new BO3Loader());

      for(TreeType type : TreeType.values()) {
         this.registerGlobalObject(new TreeObject(type));
      }

      this.registerGlobalObject(new UseWorld());
      this.registerGlobalObject(new UseBiome());
      this.registerGlobalObject(new UseWorldAll());
      this.registerGlobalObject(new UseBiomeAll());
   }

   public void loadGlobalObjects() {
      TerrainControl.getEngine().getGlobalObjectsDirectory().mkdirs();
      Map<String, CustomObject> globalObjects = this.loadObjects(TerrainControl.getEngine().getGlobalObjectsDirectory());
      TerrainControl.log(globalObjects.size() + " global custom objects loaded.");
      this.globalObjects.putAll(globalObjects);
   }

   public void registerCustomObjectLoader(String extension, CustomObjectLoader loader) {
      this.loaders.put(extension.toLowerCase(), loader);
   }

   public void registerGlobalObject(CustomObject object) {
      this.globalObjects.put(object.getName().toLowerCase(), object);
   }

   public CustomObject getCustomObject(String name) {
      return (CustomObject)this.globalObjects.get(name.toLowerCase());
   }

   public CustomObject getCustomObject(String name, LocalWorld world) {
      return this.getCustomObject(name, world.getSettings());
   }

   public CustomObject getCustomObject(String name, WorldConfig config) {
      for(CustomObject object : config.customObjects) {
         if (object.getName().equalsIgnoreCase(name)) {
            return object;
         }
      }

      return this.getCustomObject(name);
   }

   public Map loadObjects(File directory) {
      if (!directory.isDirectory()) {
         throw new IllegalArgumentException("Given file is not a directory: " + directory.getAbsolutePath());
      } else {
         Map<String, CustomObject> objects = new HashMap();

         for(File file : directory.listFiles()) {
            String fileName = file.getName();
            int index = fileName.lastIndexOf(46);
            if (index != -1) {
               String objectType = fileName.substring(index + 1, fileName.length());
               String objectName = fileName.substring(0, index);
               CustomObjectLoader loader = (CustomObjectLoader)this.loaders.get(objectType.toLowerCase());
               if (loader != null) {
                  objects.put(objectName.toLowerCase(), loader.loadFromFile(objectName, file));
               }
            }
         }

         for(CustomObject object : objects.values()) {
            object.onEnable(objects);
         }

         return objects;
      }
   }

   public CustomObject getObjectFromString(String string, LocalWorld world) {
      return this.getObjectFromString(string, world.getSettings());
   }

   public CustomObject getObjectFromString(String string, WorldConfig config) {
      String[] parts = new String[]{string, ""};
      int start = string.indexOf("(");
      int end = string.lastIndexOf(")");
      if (start != -1 && end != -1) {
         parts[0] = string.substring(0, start);
         parts[1] = string.substring(start + 1, end);
      }

      CustomObject object = this.getCustomObject(parts[0], config);
      if (object != null && parts[1].length() != 0) {
         Map<String, String> settingsMap = new HashMap();
         String[] settings = parts[1].split(";");

         for(String setting : settings) {
            String[] settingParts = setting.split("=");
            if (settingParts.length == 1) {
               settingsMap.put(settingParts[0], "true");
            } else if (settingParts.length == 2) {
               settingsMap.put(settingParts[0].toLowerCase(), settingParts[1]);
            }
         }

         if (settingsMap.size() > 0) {
            object = object.applySettings(settingsMap);
         }
      }

      return object;
   }
}
