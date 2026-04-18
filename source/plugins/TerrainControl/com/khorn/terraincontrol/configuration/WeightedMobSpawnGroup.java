package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.TerrainControl;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class WeightedMobSpawnGroup {
   protected String mob = null;
   protected int max = 0;
   protected int weight = 0;
   protected int min = 0;

   public WeightedMobSpawnGroup(String mobName, int weight, int min, int max) {
      super();
      this.mob = mobName;
      this.weight = weight;
      this.min = min;
      this.max = max;
   }

   public String getMobName() {
      return this.mob;
   }

   public int getWeight() {
      return this.weight;
   }

   public int getMin() {
      return this.min;
   }

   public int getMax() {
      return this.max;
   }

   public static List fromJson(String originalJson) {
      List<WeightedMobSpawnGroup> mobGroups = new ArrayList();
      String json = originalJson.trim();
      if (json.length() <= 2) {
         return mobGroups;
      } else {
         json = removeFirstAndLastChar(json);
         json = json.replace('{', '(');
         json = json.replace('}', ')');
         String[] groups = ConfigFile.readComplexString(json);

         try {
            for(String group : groups) {
               group = group.trim();
               group = removeFirstAndLastChar(group);
               String[] groupParts = group.split(",");
               String mobName = null;
               int weight = 0;
               int min = 0;
               int max = 0;

               for(String option : groupParts) {
                  option = option.trim();
                  String[] optionParts = option.split(":");
                  if (optionParts[0].trim().equalsIgnoreCase("\"mob\"")) {
                     mobName = removeFirstAndLastChar(optionParts[1].trim());
                  }

                  if (optionParts[0].trim().equalsIgnoreCase("\"weight\"")) {
                     weight = Integer.parseInt(optionParts[1].trim());
                  }

                  if (optionParts[0].trim().equalsIgnoreCase("\"min\"")) {
                     min = Integer.parseInt(optionParts[1].trim());
                  }

                  if (optionParts[0].trim().equalsIgnoreCase("\"max\"")) {
                     max = Integer.parseInt(optionParts[1].trim());
                  }
               }

               if (mobName != null && weight > 0 && min > 0 && max >= min) {
                  mobGroups.add(new WeightedMobSpawnGroup(mobName, weight, min, max));
               }
            }
         } catch (NumberFormatException e) {
            TerrainControl.log(Level.WARNING, "Incorrect number in JSON: " + originalJson);
            e.printStackTrace();
         } catch (ArrayIndexOutOfBoundsException var19) {
            TerrainControl.log(Level.WARNING, "Incorrect JSON: " + originalJson);
         }

         return mobGroups;
      }
   }

   public static String toJson(List list) {
      StringBuilder json = new StringBuilder("[");

      for(WeightedMobSpawnGroup group : list) {
         json.append("{\"mob\": \"");
         json.append(group.getMobName());
         json.append("\", \"weight\": ");
         json.append(group.getWeight());
         json.append(", \"min\": ");
         json.append(group.getMin());
         json.append(", \"max\": ");
         json.append(group.getMax());
         json.append("}, ");
      }

      if (json.length() != 1) {
         json.deleteCharAt(json.length() - 1);
         json.deleteCharAt(json.length() - 1);
      }

      json.append(']');
      return json.toString();
   }

   private static String removeFirstAndLastChar(String string) {
      return string.substring(1, string.length() - 1);
   }
}
