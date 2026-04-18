package me.main__.util.multiverse.SerializationConfig;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class ReflectionUtils {
   protected ReflectionUtils() {
      super();
      throw new UnsupportedOperationException();
   }

   public static final Field getField(String fieldName, Class clazz, boolean ignoreCase) throws SecurityException, NoSuchFieldException {
      if (ignoreCase) {
         for(Field f : clazz.getDeclaredFields()) {
            if (f.getName().equalsIgnoreCase(fieldName)) {
               return f;
            }
         }

         throw new NoSuchFieldException(fieldName);
      } else {
         return clazz.getDeclaredField(fieldName);
      }
   }

   public static final Object safelyInstantiate(Class clazz) throws InstantiationException, IllegalAccessException, InvocationTargetException {
      return safelyInstantiate(clazz, (Object)null);
   }

   public static final Object safelyInstantiate(Class clazz, Object instantiator) throws InstantiationException, IllegalAccessException, InvocationTargetException {
      boolean needsInstance = false;

      Constructor<T> ctor;
      try {
         ctor = clazz.getDeclaredConstructor();
      } catch (NoSuchMethodException var11) {
         try {
            if (instantiator == null) {
               throw new NoSuchMethodException();
            }

            ctor = clazz.getDeclaredConstructor(instantiator.getClass());
            needsInstance = true;
         } catch (NoSuchMethodException var10) {
            throw new InstantiationException("Couldn't instantiate " + clazz + "!");
         }
      }

      Object e;
      try {
         ctor.setAccessible(true);
         if (needsInstance) {
            e = ctor.newInstance(instantiator);
            return e;
         }

         e = ctor.newInstance();
      } finally {
         ctor.setAccessible(false);
      }

      return e;
   }
}
