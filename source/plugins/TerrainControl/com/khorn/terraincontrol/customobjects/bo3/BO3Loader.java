package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.Tag;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.CustomObjectLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class BO3Loader implements CustomObjectLoader {
   private static Map loadedTags = new HashMap();

   public BO3Loader() {
      super();
      TerrainControl.getConfigFunctionsManager().registerConfigFunction("Block", BlockFunction.class);
      TerrainControl.getConfigFunctionsManager().registerConfigFunction("Branch", BranchFunction.class);
      TerrainControl.getConfigFunctionsManager().registerConfigFunction("RandomBlock", RandomBlockFunction.class);
      TerrainControl.getConfigFunctionsManager().registerConfigFunction("BlockCheck", BlockCheck.class);
      TerrainControl.getConfigFunctionsManager().registerConfigFunction("LightCheck", LightCheck.class);
   }

   public CustomObject loadFromFile(String objectName, File file) {
      return new BO3(objectName, file);
   }

   public static Tag loadMetadata(String name, File bo3File) {
      String path = bo3File.getParent() + File.separator + name;
      if (loadedTags.containsKey(path)) {
         return (Tag)loadedTags.get(path);
      } else {
         FileInputStream stream = null;

         Tag metadata;
         try {
            stream = new FileInputStream(path);
            metadata = Tag.readFrom(stream, true);
            stream.close();
         } catch (FileNotFoundException var12) {
            TerrainControl.log(Level.WARNING, "NBT file " + path + " not found");
            tryToClose(stream);
            return null;
         } catch (IOException e) {
            tryToClose(stream);

            try {
               stream = new FileInputStream(path);
               metadata = Tag.readFrom(stream, false);
               stream.close();
            } catch (IOException var11) {
               TerrainControl.log(Level.SEVERE, "Failed to read NBT meta file: " + e.getMessage());
               e.printStackTrace();
               tryToClose(stream);
               return null;
            }
         }

         Tag[] values = (Tag[])metadata.getValue();

         for(Tag subTag : values) {
            if (subTag.getName() != null && subTag.getName().equals("id") && subTag.getType().equals(Tag.Type.TAG_String)) {
               return metadata;
            }
         }

         try {
            return registerMetadata(path, ((Tag[])((Tag[])metadata.getValue()))[0]);
         } catch (Exception e) {
            TerrainControl.log(Level.WARNING, "Structure of NBT file is incorrect: " + e.getMessage());
            return null;
         }
      }
   }

   public static Tag registerMetadata(String pathOnDisk, Tag metadata) {
      loadedTags.put(pathOnDisk, metadata);
      return metadata;
   }

   private static void tryToClose(InputStream stream) {
      if (stream != null) {
         try {
            stream.close();
         } catch (IOException var2) {
         }
      }

   }

   public void onShutdown() {
      loadedTags.clear();
   }
}
