package com.khorn.terraincontrol.configuration;

import java.util.ArrayList;

public interface TCSetting {
   String name();

   int intValue();

   long longValue();

   float floatValue();

   double doubleValue();

   Enum enumValue();

   SettingsType getReturnType();

   String stringValue();

   ArrayList stringArrayListValue();

   boolean booleanValue();

   public static enum SettingsType {
      String,
      Boolean,
      Int,
      Long,
      Enum,
      Double,
      Float,
      StringArray,
      Color;

      private SettingsType() {
      }
   }
}
