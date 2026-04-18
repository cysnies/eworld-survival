package me.main__.util.multiverse.SerializationConfig;

import java.util.HashMap;
import java.util.Map;

public class InstanceCache {
   private final Map instances = new HashMap();

   public InstanceCache() {
      super();
   }

   public void cacheInstance(Object instance) {
      this.instances.put(instance.getClass(), instance);
   }

   public Object getInstance(Class clazz) {
      return this.getInstance(clazz, (Object)null);
   }

   public Object getInstance(Class clazz, Object instantiator) {
      U u = (U)null;

      try {
         u = (U)this.instances.get(clazz);
      } catch (Exception var6) {
      }

      if (u == null) {
         try {
            u = (U)ReflectionUtils.safelyInstantiate(clazz, instantiator);
         } catch (Exception var5) {
         }
      }

      return u;
   }
}
