package me.main__.util.multiverse.SerializationConfig;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

final class DefaultSerializor implements Serializor {
   private static final Map primitiveToWrapperMap = new HashMap();

   DefaultSerializor() {
      super();
   }

   public Object serialize(Object object) {
      return !(object instanceof ConfigurationSerializable) && !(object instanceof Iterable) ? String.valueOf(object) : object;
   }

   public Object deserialize(Object serialized, Class anothertype) throws IllegalPropertyValueException {
      try {
         if (String.class.isAssignableFrom(anothertype)) {
            return serialized;
         } else if (ConfigurationSerializable.class.isAssignableFrom(anothertype)) {
            return serialized instanceof ConfigurationSerializable ? serialized : ConfigurationSerialization.deserializeObject((Map)serialized);
         } else if (Iterable.class.isAssignableFrom(anothertype) && serialized instanceof Iterable) {
            return serialized;
         } else {
            Class<?> type;
            if (primitiveToWrapperMap.containsKey(anothertype)) {
               type = (Class)primitiveToWrapperMap.get(anothertype);
            } else {
               type = anothertype;
            }

            Method valueOf = type.getMethod("valueOf", String.class);
            return valueOf.invoke((Object)null, serialized);
         }
      } catch (Exception e) {
         throw new IllegalPropertyValueException(e);
      }
   }

   public static final Map getPrimitiveToWrapperMap() {
      return Collections.unmodifiableMap(primitiveToWrapperMap);
   }

   static {
      primitiveToWrapperMap.put(Integer.TYPE, Integer.class);
      primitiveToWrapperMap.put(Boolean.TYPE, Boolean.class);
      primitiveToWrapperMap.put(Long.TYPE, Long.class);
      primitiveToWrapperMap.put(Double.TYPE, Double.class);
      primitiveToWrapperMap.put(Float.TYPE, Float.class);
      primitiveToWrapperMap.put(Byte.TYPE, Byte.class);
      primitiveToWrapperMap.put(Short.TYPE, Short.class);
   }
}
