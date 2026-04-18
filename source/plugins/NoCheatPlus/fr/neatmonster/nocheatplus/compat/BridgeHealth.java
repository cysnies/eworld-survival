package fr.neatmonster.nocheatplus.compat;

import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class BridgeHealth {
   private static Set failures = new HashSet();

   public BridgeHealth() {
      super();
   }

   public static final double getDoubleOrInt(Object obj, String methodName, Throwable reason) {
      if (reason != null) {
         String tag = obj.getClass().getName() + "." + methodName;
         if (failures.add(tag)) {
            checkLogEntry(tag);
         }
      }

      Object o1 = ReflectionUtil.invokeMethodNoArgs(obj, methodName, Double.TYPE, Integer.TYPE);
      if (o1 instanceof Number) {
         return ((Number)o1).doubleValue();
      } else {
         String message = "Expect method " + methodName + " in " + obj.getClass() + " with return type double or int, returned instead: " + (o1 == null ? "null" : o1.getClass().getName());
         if (reason == null) {
            throw new RuntimeException(message);
         } else {
            throw new RuntimeException(message, reason);
         }
      }
   }

   public static double getAmount(EntityRegainHealthEvent event) {
      try {
         return event.getAmount();
      } catch (IncompatibleClassChangeError e) {
         return getDoubleOrInt(event, "getAmount", e);
      }
   }

   public static double getDamage(EntityDamageEvent event) {
      try {
         return event.getDamage();
      } catch (IncompatibleClassChangeError e) {
         return getDoubleOrInt(event, "getDamage", e);
      }
   }

   public static void setDamage(EntityDamageEvent event, double damage) {
      try {
         event.setDamage(damage);
      } catch (IncompatibleClassChangeError e) {
         invokeVoid(event, "setDamage", (int)Math.round(damage), e);
      }

   }

   public static double getHealth(LivingEntity entity) {
      try {
         return entity.getHealth();
      } catch (IncompatibleClassChangeError e) {
         return getDoubleOrInt(entity, "getHealth", e);
      }
   }

   public static double getMaxHealth(LivingEntity entity) {
      try {
         return entity.getMaxHealth();
      } catch (IncompatibleClassChangeError e) {
         return getDoubleOrInt(entity, "getMaxHealth", e);
      }
   }

   public static double getLastDamage(LivingEntity entity) {
      try {
         return entity.getLastDamage();
      } catch (IncompatibleClassChangeError e) {
         return getDoubleOrInt(entity, "getLastDamage", e);
      }
   }

   public static void setHealth(LivingEntity entity, double health) {
      try {
         entity.setHealth(health);
      } catch (IncompatibleClassChangeError e) {
         invokeVoid(entity, "setHealth", (int)Math.round(health), e);
      }

   }

   public static void damage(LivingEntity entity, double damage) {
      try {
         entity.damage(damage);
      } catch (IncompatibleClassChangeError e) {
         invokeVoid(entity, "damage", (int)Math.round(damage), e);
      }

   }

   public static EntityDamageEvent getEntityDamageEvent(Entity entity, EntityDamageEvent.DamageCause damageCause, double damage) {
      try {
         return new EntityDamageEvent(entity, damageCause, damage);
      } catch (IncompatibleClassChangeError var5) {
         return new EntityDamageEvent(entity, damageCause, (int)Math.round(damage));
      }
   }

   public static void invokeVoid(Object obj, String methodName, int value, Throwable reason) {
      if (reason != null) {
         String tag = obj.getClass().getName() + "." + methodName;
         if (failures.add(tag)) {
            checkLogEntry(tag);
         }
      }

      try {
         obj.getClass().getMethod(methodName, Integer.TYPE).invoke(obj, value);
      } catch (Throwable var5) {
         throw new RuntimeException("Could not invoke " + methodName + " with one argument (int) on: " + obj.getClass().getName(), reason);
      }
   }

   private static void checkLogEntry(String tag) {
      if (ConfigManager.getConfigFile().getBoolean("logging.debug")) {
         LogUtil.logWarning("[NoCheatPlus] API incompatibility detected: " + tag);
      }

   }
}
