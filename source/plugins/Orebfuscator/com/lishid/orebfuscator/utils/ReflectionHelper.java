package com.lishid.orebfuscator.utils;

import com.lishid.orebfuscator.Orebfuscator;
import java.lang.reflect.Field;

public class ReflectionHelper {
   public ReflectionHelper() {
      super();
   }

   public static Object getPrivateField(Class c, Object object, String fieldName) {
      try {
         Field field = c.getDeclaredField(fieldName);
         field.setAccessible(true);
         return field.get(object);
      } catch (Exception e) {
         Orebfuscator.log((Throwable)e);
         return null;
      }
   }

   public static Object getPrivateField(Object object, String fieldName) {
      return getPrivateField(object.getClass(), object, fieldName);
   }

   public static void setPrivateField(Class c, Object object, String fieldName, Object value) {
      try {
         Field field = c.getDeclaredField(fieldName);
         field.setAccessible(true);
         field.set(object, value);
      } catch (Exception e) {
         Orebfuscator.log((Throwable)e);
      }

   }

   public static void setPrivateField(Object object, String fieldName, Object value) {
      setPrivateField(object.getClass(), object, fieldName, value);
   }

   public static void setPrivateFinal(Object object, String fieldName, Object value) {
      try {
         Field field = object.getClass().getDeclaredField(fieldName);
         field.setAccessible(true);
         Field modifiersField = Field.class.getDeclaredField("modifiers");
         modifiersField.setAccessible(true);
         modifiersField.setInt(field, field.getModifiers() & -17);
         field.set(object, value);
      } catch (Exception e) {
         Orebfuscator.log((Throwable)e);
      }

   }
}
