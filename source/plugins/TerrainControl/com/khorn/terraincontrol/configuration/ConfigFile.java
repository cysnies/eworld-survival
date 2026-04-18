package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.TerrainControl;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ConfigFile {
   private BufferedWriter settingsWriter;
   protected Map settingsCache = new HashMap();
   private boolean writeComments;

   public ConfigFile() {
      super();
   }

   protected void readSettingsFile(File f) {
      BufferedReader settingsReader = null;
      if (f.exists()) {
         try {
            settingsReader = new BufferedReader(new FileReader(f));
            int lineNumber = 0;

            String thisLine;
            while((thisLine = settingsReader.readLine()) != null) {
               ++lineNumber;
               if (!thisLine.trim().equals("") && !thisLine.startsWith("#") && !thisLine.startsWith("<")) {
                  if (!thisLine.contains(":") && !thisLine.toLowerCase().contains("(")) {
                     if (thisLine.contains("=")) {
                        String[] splitSettings = thisLine.split("=", 2);
                        this.settingsCache.put(splitSettings[0].trim().toLowerCase(), splitSettings[1].trim());
                     } else {
                        this.settingsCache.put(thisLine.trim(), Integer.toString(lineNumber));
                     }
                  } else if (!thisLine.contains("(") || thisLine.contains(":") && thisLine.indexOf(40) >= thisLine.indexOf(":")) {
                     String[] splitSettings = thisLine.split(":", 2);
                     this.settingsCache.put(splitSettings[0].trim().toLowerCase(), splitSettings[1].trim());
                  } else {
                     this.settingsCache.put(thisLine.trim(), Integer.toString(lineNumber));
                  }
               }
            }
         } catch (IOException e) {
            e.printStackTrace();
            if (settingsReader != null) {
               try {
                  settingsReader.close();
               } catch (IOException localIOException1) {
                  localIOException1.printStackTrace();
               }
            }
         } finally {
            if (settingsReader != null) {
               try {
                  settingsReader.close();
               } catch (IOException localIOException2) {
                  localIOException2.printStackTrace();
               }
            }

         }
      } else {
         this.sayFileNotFound(f);
      }

   }

   protected void sayNotFound(String settingsName) {
   }

   protected void sayHadWrongValue(String settingsName) {
      TerrainControl.log(settingsName + " had wrong value");
   }

   protected void sayFileNotFound(File file) {
      TerrainControl.log("File not found: " + file.getName());
   }

   protected List readModSettings(String settingsName, List defaultValue) {
      settingsName = settingsName.toLowerCase();
      if (this.settingsCache.containsKey(settingsName)) {
         String json = (String)this.settingsCache.get(settingsName);
         return json == null ? defaultValue : WeightedMobSpawnGroup.fromJson(json);
      } else {
         this.sayNotFound(settingsName);
         return defaultValue;
      }
   }

   protected ArrayList readModSettings(String settingsName, ArrayList defaultValue) {
      settingsName = settingsName.toLowerCase();
      if (this.settingsCache.containsKey(settingsName)) {
         ArrayList<String> out = new ArrayList();
         if (!((String)this.settingsCache.get(settingsName)).trim().equals("") && !((String)this.settingsCache.get(settingsName)).equals("None")) {
            Collections.addAll(out, ((String)this.settingsCache.get(settingsName)).split(","));
            return out;
         } else {
            return out;
         }
      } else {
         this.sayNotFound(settingsName);
         return defaultValue;
      }
   }

   protected int readModSettings(String settingsName, int defaultValue) {
      settingsName = settingsName.toLowerCase();
      if (this.settingsCache.containsKey(settingsName)) {
         try {
            return Integer.valueOf((String)this.settingsCache.get(settingsName));
         } catch (NumberFormatException var4) {
            this.sayHadWrongValue(settingsName);
         }
      }

      this.sayNotFound(settingsName);
      return defaultValue;
   }

   protected long readModSettings(String settingsName, long defaultValue) {
      settingsName = settingsName.toLowerCase();
      if (this.settingsCache.containsKey(settingsName)) {
         String value = (String)this.settingsCache.get(settingsName);
         if (value.isEmpty()) {
            return 0L;
         }

         try {
            return Long.parseLong(value);
         } catch (NumberFormatException var6) {
            this.sayHadWrongValue(settingsName);
         }
      }

      this.sayNotFound(settingsName);
      return defaultValue;
   }

   protected byte readModSettings(String settingsName, byte defaultValue) {
      settingsName = settingsName.toLowerCase();
      if (this.settingsCache.containsKey(settingsName)) {
         try {
            short number = Short.valueOf((String)this.settingsCache.get(settingsName));
            if (number >= 0 && number <= 255) {
               return (byte)number;
            }

            throw new NumberFormatException();
         } catch (NumberFormatException var4) {
            this.sayHadWrongValue(settingsName);
         }
      }

      this.sayNotFound(settingsName);
      return defaultValue;
   }

   protected String readModSettings(String settingsName, String defaultValue) {
      settingsName = settingsName.toLowerCase();
      if (this.settingsCache.containsKey(settingsName)) {
         return (String)this.settingsCache.get(settingsName);
      } else {
         this.sayNotFound(settingsName);
         return defaultValue;
      }
   }

   protected double readModSettings(String settingsName, double defaultValue) {
      settingsName = settingsName.toLowerCase();
      if (this.settingsCache.containsKey(settingsName)) {
         try {
            return Double.valueOf((String)this.settingsCache.get(settingsName));
         } catch (NumberFormatException var5) {
            this.sayHadWrongValue(settingsName);
         }
      }

      this.sayNotFound(settingsName);
      return defaultValue;
   }

   protected int readModSettingsColor(String settingsName, String defaultValue) {
      settingsName = settingsName.toLowerCase();
      Color color = Color.decode(defaultValue);
      if (this.settingsCache.containsKey(settingsName)) {
         try {
            color = Color.decode((String)this.settingsCache.get(settingsName));
         } catch (NumberFormatException var5) {
            this.sayHadWrongValue(settingsName);
         }
      } else {
         this.sayNotFound(settingsName);
      }

      return color.getRGB() & 16777215;
   }

   protected float readModSettings(String settingsName, float defaultValue) {
      settingsName = settingsName.toLowerCase();
      if (this.settingsCache.containsKey(settingsName)) {
         try {
            return Float.valueOf((String)this.settingsCache.get(settingsName));
         } catch (NumberFormatException var4) {
            this.sayHadWrongValue(settingsName);
         }
      }

      this.sayNotFound(settingsName);
      return defaultValue;
   }

   protected boolean readModSettings(String settingsName, boolean defaultValue) {
      settingsName = settingsName.toLowerCase();
      if (this.settingsCache.containsKey(settingsName)) {
         return Boolean.valueOf((String)this.settingsCache.get(settingsName));
      } else {
         this.sayNotFound(settingsName);
         return defaultValue;
      }
   }

   protected Enum readModSettings(String settingsName, Enum defaultValue) {
      settingsName = settingsName.toLowerCase();
      if (this.settingsCache.containsKey(settingsName)) {
         Class<?> enumClass = defaultValue.getDeclaringClass();
         String value = (String)this.settingsCache.get(settingsName);
         if (enumClass.isEnum()) {
            Object[] enumValues = enumClass.getEnumConstants();

            for(Object enumValue : enumValues) {
               String enumName = ((Enum)enumValue).name();
               if (enumName.toLowerCase().equals(value) || enumName.equals(value)) {
                  return (Enum)enumValue;
               }
            }

            this.sayHadWrongValue(settingsName);
         }
      }

      this.sayNotFound(settingsName);
      return defaultValue;
   }

   protected Object readSettings(TCSetting value) {
      Object obj = null;
      switch (value.getReturnType()) {
         case String:
            obj = this.readModSettings(value.name(), value.stringValue());
            break;
         case Boolean:
            obj = this.readModSettings(value.name(), value.booleanValue());
            break;
         case Int:
            obj = this.readModSettings(value.name(), value.intValue());
            break;
         case Long:
            obj = this.readModSettings(value.name(), value.longValue());
            break;
         case Enum:
            obj = this.readModSettings(value.name(), value.enumValue());
            break;
         case Double:
            obj = this.readModSettings(value.name(), value.doubleValue());
            break;
         case Float:
            obj = this.readModSettings(value.name(), value.floatValue());
            break;
         case StringArray:
            obj = this.readModSettings(value.name(), value.stringArrayListValue());
            break;
         case Color:
            obj = this.readModSettingsColor(value.name(), value.stringValue());
      }

      return obj;
   }

   public void writeSettingsFile(File settingsFile, boolean comments) {
      this.writeComments = comments;

      try {
         this.settingsWriter = new BufferedWriter(new FileWriter(settingsFile, false));
         this.writeConfigSettings();
      } catch (IOException e) {
         e.printStackTrace();
         if (this.settingsWriter != null) {
            try {
               this.settingsWriter.close();
            } catch (IOException localIOException1) {
               localIOException1.printStackTrace();
            }
         }
      } finally {
         if (this.settingsWriter != null) {
            try {
               this.settingsWriter.close();
            } catch (IOException localIOException2) {
               localIOException2.printStackTrace();
            }
         }

      }

   }

   protected void writeValue(String settingsName, ArrayList settingsValue) throws IOException {
      String out = "";

      for(String key : settingsValue) {
         if (out.equals("")) {
            out = out + key;
         } else {
            out = out + "," + key;
         }
      }

      this.settingsWriter.write(settingsName + ":" + out);
      this.settingsWriter.newLine();
   }

   protected void writeValue(String settingsName, List settingsValue) throws IOException {
      this.settingsWriter.write(settingsName + ": " + WeightedMobSpawnGroup.toJson(settingsValue));
      this.settingsWriter.newLine();
   }

   protected void writeValue(String settingsName, byte settingsValue) throws IOException {
      this.settingsWriter.write(settingsName + ":" + (settingsValue & 255));
      this.settingsWriter.newLine();
   }

   protected void writeValue(String settingsName, int settingsValue) throws IOException {
      this.settingsWriter.write(settingsName + ":" + Integer.toString(settingsValue));
      this.settingsWriter.newLine();
   }

   protected void writeValue(String settingsName, double settingsValue) throws IOException {
      this.settingsWriter.write(settingsName + ":" + Double.toString(settingsValue));
      this.settingsWriter.newLine();
   }

   protected void writeValue(String settingsName, float settingsValue) throws IOException {
      this.settingsWriter.write(settingsName + ":" + Float.toString(settingsValue));
      this.settingsWriter.newLine();
   }

   protected void writeValue(String settingsName, boolean settingsValue) throws IOException {
      this.settingsWriter.write(settingsName + ":" + Boolean.toString(settingsValue));
      this.settingsWriter.newLine();
   }

   protected void writeValue(String settingsName, String settingsValue) throws IOException {
      this.settingsWriter.write(settingsName + ":" + settingsValue);
      this.settingsWriter.newLine();
   }

   protected void writeValue(String settingsName) throws IOException {
      this.settingsWriter.write(settingsName);
      this.settingsWriter.newLine();
   }

   protected void writeColorValue(String settingsName, int RGB) throws IOException {
      this.settingsWriter.write(settingsName + ":0x" + Integer.toHexString(16777215 & RGB | 16777216).substring(1));
      this.settingsWriter.newLine();
   }

   protected void writeBigTitle(String title) throws IOException {
      this.settingsWriter.newLine();
      this.settingsWriter.write("#######################################################################");
      this.settingsWriter.newLine();
      this.settingsWriter.write("# +-----------------------------------------------------------------+ #");
      this.settingsWriter.newLine();
      StringBuilder builder = new StringBuilder(title);
      builder.insert(0, ' ');
      builder.append(' ');

      for(boolean flag = true; builder.length() < 65; flag = !flag) {
         if (flag) {
            builder.insert(0, ' ');
         } else {
            builder.append(' ');
         }
      }

      this.settingsWriter.write("# |" + builder.toString() + "| #");
      this.settingsWriter.newLine();
      this.settingsWriter.write("# +-----------------------------------------------------------------+ #");
      this.settingsWriter.newLine();
      this.settingsWriter.write("#######################################################################");
      this.settingsWriter.newLine();
      this.settingsWriter.newLine();
   }

   protected void writeSmallTitle(String title) throws IOException {
      int titleLength = title.length();
      StringBuilder rowBuilder = new StringBuilder(titleLength + 4);

      for(int i = 0; i < titleLength + 4; ++i) {
         rowBuilder.append('#');
      }

      this.settingsWriter.write(rowBuilder.toString());
      this.settingsWriter.newLine();
      this.settingsWriter.write("# " + title + " #");
      this.settingsWriter.newLine();
      this.settingsWriter.write(rowBuilder.toString());
      this.settingsWriter.newLine();
      this.settingsWriter.newLine();
   }

   protected void writeComment(String comment) throws IOException {
      if (this.writeComments) {
         if (comment.length() > 0) {
            this.settingsWriter.write("# " + comment);
         }

         this.settingsWriter.newLine();
      }
   }

   protected void writeNewLine() throws IOException {
      this.settingsWriter.newLine();
   }

   protected abstract void writeConfigSettings() throws IOException;

   protected abstract void readConfigSettings();

   protected abstract void correctSettings();

   protected abstract void renameOldSettings();

   protected void renameOldSetting(String oldValue, TCDefaultValues newValue) {
      if (this.settingsCache.containsKey(oldValue.toLowerCase())) {
         this.settingsCache.put(newValue.name().toLowerCase(), this.settingsCache.get(oldValue.toLowerCase()));
      }

   }

   protected int applyBounds(int value, int min, int max) {
      if (value > max) {
         return max;
      } else {
         return value < min ? min : value;
      }
   }

   protected double applyBounds(double value, double min, double max) {
      if (value > max) {
         return max;
      } else {
         return value < min ? min : value;
      }
   }

   protected float applyBounds(float value, float min, float max) {
      if (value > max) {
         return max;
      } else {
         return value < min ? min : value;
      }
   }

   protected float applyBounds(float value, float min, float max, float minValue) {
      value = this.applyBounds(value, min, max);
      return value < minValue ? minValue + 1.0F : value;
   }

   protected int applyBounds(int value, int min, int max, int minValue) {
      value = this.applyBounds(value, min, max);
      return value < minValue ? minValue + 1 : value;
   }

   protected ArrayList filterBiomes(ArrayList biomes, ArrayList customBiomes) {
      ArrayList<String> output = new ArrayList();

      for(String key : biomes) {
         key = key.trim();
         if (customBiomes.contains(key)) {
            output.add(key);
         } else if (DefaultBiome.Contain(key)) {
            output.add(key);
         }
      }

      return output;
   }

   protected static void writeStringToStream(DataOutputStream stream, String value) throws IOException {
      byte[] bytes = value.getBytes();
      stream.writeShort(bytes.length);
      stream.write(bytes);
   }

   protected static String readStringFromStream(DataInputStream stream) throws IOException {
      byte[] chars = new byte[stream.readShort()];
      if (stream.read(chars, 0, chars.length) != chars.length) {
         throw new EOFException();
      } else {
         return new String(chars);
      }
   }

   public static String[] readComplexString(String line) {
      ArrayList<String> buffer = new ArrayList();
      int index = 0;
      int lastFound = 0;
      int inBracer = 0;

      for(char c : line.toCharArray()) {
         if (c == ',' && inBracer == 0) {
            buffer.add(line.substring(lastFound, index));
            lastFound = index + 1;
         }

         if (c == '(') {
            ++inBracer;
         }

         if (c == ')') {
            --inBracer;
         }

         ++index;
      }

      buffer.add(line.substring(lastFound, index));
      String[] output = new String[0];
      if (inBracer == 0) {
         output = (String[])buffer.toArray(output);
      }

      return output;
   }
}
