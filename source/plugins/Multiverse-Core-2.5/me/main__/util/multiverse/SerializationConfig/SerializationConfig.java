package me.main__.util.multiverse.SerializationConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public abstract class SerializationConfig implements ConfigurationSerializable {
   private static final InstanceCache serializorCache = new InstanceCache();
   private static final InstanceCache validatorCache = new InstanceCache();
   private static final Map aliasMap = new WeakHashMap();
   private static Logger logger = null;
   private final Map pendingVPropChanges = new HashMap();
   private Object objectUsing = this;
   private Validator globalValidator = null;
   private Map validatorMap = new HashMap();

   public static void initLogging(Logger log) {
      logger = log;
   }

   public static void registerAlias(Class clazz, String alias, String property) {
      Map<String, String> myAliasMap = getAliasMap(clazz);
      myAliasMap.put(alias, property);
   }

   public static Map getAliasMap(Class clazz) {
      if (!aliasMap.containsKey(clazz)) {
         Method defaultsMethod = null;

         Map<String, String> defaultMap;
         try {
            defaultsMethod = clazz.getDeclaredMethod("getAliases");
            defaultsMethod.setAccessible(true);
            defaultMap = (Map)defaultsMethod.invoke((Object)null);
         } catch (Exception var7) {
            defaultMap = null;
         } finally {
            if (defaultsMethod != null) {
               defaultsMethod.setAccessible(false);
            }

         }

         if (defaultMap == null) {
            defaultMap = new HashMap();
         }

         aliasMap.put(clazz, defaultMap);
      }

      return (Map)aliasMap.get(clazz);
   }

   protected final void registerAlias(String alias, String property) {
      registerAlias(this.getClass(), alias, property);
   }

   protected void registerObjectUsing(Object object) {
      this.objectUsing = object;
   }

   protected void registerGlobalValidator(Validator validator) {
      this.globalValidator = validator;
   }

   protected final Map getAliasMap() {
      return getAliasMap(this.getClass());
   }

   public static void registerAll(Class clazz) {
      ConfigurationSerialization.registerClass(clazz);
      Field[] fields = clazz.getDeclaredFields();

      for(Field f : fields) {
         f.setAccessible(true);
         if (f.isAnnotationPresent(Property.class)) {
            Class<?> fieldclazz = f.getType();
            if (SerializationConfig.class.isAssignableFrom(fieldclazz)) {
               Class<? extends SerializationConfig> subclass = fieldclazz.asSubclass(SerializationConfig.class);
               registerAll(subclass);
            } else if (ConfigurationSerializable.class.isAssignableFrom(fieldclazz)) {
               Class<? extends ConfigurationSerializable> subclass = fieldclazz.asSubclass(ConfigurationSerializable.class);
               ConfigurationSerialization.registerClass(subclass);
            }
         }

         f.setAccessible(false);
      }

   }

   public static void unregisterAll(Class clazz) {
      ConfigurationSerialization.unregisterClass(clazz);
      Field[] fields = clazz.getDeclaredFields();

      for(Field f : fields) {
         f.setAccessible(true);
         if (f.isAnnotationPresent(Property.class)) {
            Class<?> fieldclazz = f.getType();
            if (ConfigurationSerializable.class.isAssignableFrom(fieldclazz)) {
               Class<? extends ConfigurationSerializable> subclass = fieldclazz.asSubclass(ConfigurationSerializable.class);
               ConfigurationSerialization.unregisterClass(subclass);
            }
         }

         f.setAccessible(false);
      }

   }

   public SerializationConfig() {
      super();
      this.setDefaults();
   }

   public SerializationConfig(Map values) {
      super();
      this.loadValues(values);
   }

   private void log(Level level, String message) {
      this.log(level, message, (Exception)null);
   }

   private void log(Level level, String message, Exception e) {
      if (logger != null) {
         logger.log(level, message);
      } else if (e != null) {
         e.printStackTrace();
      }

   }

   protected void loadValues(Map values) {
      this.setDefaults();
      Field[] fields = this.getClass().getDeclaredFields();

      for(Field f : fields) {
         f.setAccessible(true);
         Property propertyInfo = (Property)f.getAnnotation(Property.class);
         Object serializedValue = values.get(f.getName());
         if (serializedValue != null && propertyInfo != null && (!VirtualProperty.class.isAssignableFrom(f.getType()) || propertyInfo.persistVirtual())) {
            try {
               Class<? extends Serializor<?, ?>> serializorClass = propertyInfo.serializor();
               Serializor serializor = (Serializor)serializorCache.getInstance(serializorClass, this);
               Object value = serializor.deserialize(serializedValue, getFieldType(f));
               if (value != null) {
                  if (!VirtualProperty.class.isAssignableFrom(f.getType())) {
                     f.set(this, value);
                  } else {
                     synchronized(this.pendingVPropChanges) {
                        this.pendingVPropChanges.put(f, value);
                     }
                  }
               }
            } catch (IllegalAccessException e) {
               this.log(Level.WARNING, "Access exception while loading value for " + f.getName(), e);
            } catch (IllegalPropertyValueException e) {
               this.log(Level.WARNING, "Exception while loading value for " + f.getName(), e);
            }
         }

         f.setAccessible(false);
      }

   }

   protected void flushPendingVPropChanges() {
      synchronized(this.pendingVPropChanges) {
         for(Map.Entry entry : this.pendingVPropChanges.entrySet()) {
            try {
               ((Field)entry.getKey()).setAccessible(true);
               ((VirtualProperty)((Field)entry.getKey()).get(this)).set(entry.getValue());
               ((Field)entry.getKey()).setAccessible(false);
            } catch (IllegalAccessException e) {
               throw new RuntimeException(e);
            }
         }

         this.pendingVPropChanges.clear();
      }
   }

   protected void buildVPropChanges() {
      synchronized(this.pendingVPropChanges) {
         if (!this.pendingVPropChanges.isEmpty()) {
            throw new IllegalStateException("pendingVPropChanges has to be empty!");
         } else {
            try {
               for(Field f : this.getClass().getDeclaredFields()) {
                  f.setAccessible(true);
                  if (VirtualProperty.class.isAssignableFrom(f.getType()) && f.isAnnotationPresent(Property.class) && ((Property)f.getAnnotation(Property.class)).persistVirtual()) {
                     this.pendingVPropChanges.put(f, ((VirtualProperty)f.get(this)).get());
                  }

                  f.setAccessible(false);
               }
            } catch (IllegalAccessException e) {
               throw new RuntimeException(e);
            }

         }
      }
   }

   protected void copyValues(SerializationConfig other) {
      this.loadValues(other.serialize());
   }

   public final Map serialize() {
      Field[] fields = this.getClass().getDeclaredFields();
      Map<String, Object> ret = new LinkedHashMap();

      for(Field f : fields) {
         if (this.pendingVPropChanges.containsKey(f)) {
            ret.put(f.getName(), ((Serializor)serializorCache.getInstance(((Property)f.getAnnotation(Property.class)).serializor())).serialize(this.pendingVPropChanges.get(f)));
         } else {
            f.setAccessible(true);
            Property propertyInfo = (Property)f.getAnnotation(Property.class);
            if (propertyInfo != null && (!VirtualProperty.class.isAssignableFrom(f.getType()) || propertyInfo.persistVirtual())) {
               try {
                  Class<Serializor<?, ?>> serializorClass = propertyInfo.serializor();
                  Serializor serializor = (Serializor)serializorCache.getInstance(serializorClass, this);
                  Object raw = f.get(this);
                  if (raw instanceof VirtualProperty) {
                     raw = ((VirtualProperty)raw).get();
                  }

                  ret.put(f.getName(), serializor.serialize(raw));
               } catch (Exception var11) {
               }
            }

            f.setAccessible(false);
         }
      }

      return ret;
   }

   private String fixupName(String name, boolean ignoreCase) {
      if (this.getAliasMap().containsKey(name)) {
         return (String)this.getAliasMap().get(name);
      } else {
         if (ignoreCase) {
            for(Map.Entry entry : this.getAliasMap().entrySet()) {
               if (((String)entry.getKey()).equalsIgnoreCase(name)) {
                  return (String)entry.getValue();
               }
            }
         }

         return name;
      }
   }

   public final boolean setPropertyValue(String property, Object value) throws ClassCastException, NoSuchPropertyException {
      return this.setPropertyValue(property, value, false);
   }

   public final boolean setPropertyValue(String property, Object value, boolean ignoreCase) throws ClassCastException, NoSuchPropertyException {
      return this.setPropertyValue(property, value, ignoreCase, false);
   }

   private final boolean setPropertyValue(String property, Object value, boolean ignoreCase, boolean recursive) throws ClassCastException, NoSuchPropertyException {
      if (!recursive) {
         property = this.fixupName(property, ignoreCase);
      }

      try {
         String[] nodes = property.split("\\.");
         if (nodes.length == 1) {
            Field field = null;

            try {
               field = ReflectionUtils.getField(nodes[0], this.getClass(), ignoreCase);
               field.setAccessible(true);
               if (!field.isAnnotationPresent(Property.class)) {
                  throw new MissingAnnotationException("Property");
               } else if (!field.getType().isAssignableFrom(value.getClass()) && !field.getType().isPrimitive() && !VirtualProperty.class.isAssignableFrom(field.getType())) {
                  throw new ClassCastException(value.getClass().toString() + " cannot be cast to " + field.getType().toString());
               } else {
                  Property propertyInfo = (Property)field.getAnnotation(Property.class);
                  if (!VirtualProperty.class.isAssignableFrom(field.getType())) {
                     boolean var40 = this.validateAndDoChange(field, value);
                     return var40;
                  } else {
                     try {
                        value = this.validate(field, propertyInfo, value);
                     } catch (ChangeDeniedException var26) {
                        boolean var41 = false;
                        return var41;
                     }

                     VirtualProperty<Object> vProp = (VirtualProperty)field.get(this);
                     vProp.set(value);
                     boolean var42 = true;
                     return var42;
                  }
               }
            } catch (MissingAnnotationException e) {
               throw new NoSuchPropertyException(e);
            } catch (NoSuchFieldException e) {
               throw new NoSuchPropertyException(e);
            } catch (ClassCastException e) {
               throw e;
            } catch (Exception var30) {
               boolean vProp = false;
               return vProp;
            } finally {
               if (field != null) {
                  field.setAccessible(false);
               }

            }
         } else {
            String nextNode = nodes[0];
            Field nodeField = ReflectionUtils.getField(nextNode, this.getClass(), ignoreCase);
            nodeField.setAccessible(true);
            if (!nodeField.isAnnotationPresent(Property.class)) {
               throw new Exception();
            } else {
               SerializationConfig child = (SerializationConfig)nodeField.get(this);
               StringBuilder sb = new StringBuilder();

               for(int i = 1; i < nodes.length; ++i) {
                  sb.append(nodes[i]).append('.');
               }

               sb.deleteCharAt(sb.length() - 1);
               Exception ex = null;

               boolean ret;
               try {
                  ret = child.setPropertyValue(sb.toString(), value, ignoreCase, true);
               } catch (Exception e) {
                  ex = e;
                  ret = false;
               }

               try {
                  this.validate(nodeField, (Property)nodeField.getAnnotation(Property.class), child);
               } catch (Exception var24) {
               }

               if (ex != null) {
                  throw ex;
               } else {
                  return ret;
               }
            }
         }
      } catch (ClassCastException e) {
         throw e;
      } catch (NoSuchPropertyException e) {
         throw e;
      } catch (Exception var34) {
         return false;
      }
   }

   public final boolean setProperty(String property, String value) throws NoSuchPropertyException {
      return this.setProperty(property, value, false);
   }

   private static final Class getFieldType(Field field) {
      return VirtualProperty.class.isAssignableFrom(field.getType()) ? ((Property)field.getAnnotation(Property.class)).virtualType() : field.getType();
   }

   public final boolean setProperty(String property, String value, boolean ignoreCase) throws NoSuchPropertyException {
      return this.setProperty(property, value, ignoreCase, false);
   }

   private final boolean setProperty(String property, String value, boolean ignoreCase, boolean recursive) throws NoSuchPropertyException {
      if (!recursive) {
         property = this.fixupName(property, ignoreCase);
      }

      try {
         String[] nodes = property.split("\\.");
         if (nodes.length == 1) {
            Field field = null;

            try {
               field = ReflectionUtils.getField(nodes[0], this.getClass(), ignoreCase);
               field.setAccessible(true);
               if (!field.isAnnotationPresent(Property.class)) {
                  throw new MissingAnnotationException("Property");
               } else {
                  Property propertyInfo = (Property)field.getAnnotation(Property.class);
                  Class<? extends Serializor<?, ?>> serializorClass = propertyInfo.serializor();
                  Serializor serializor = (Serializor)serializorCache.getInstance(serializorClass, this);

                  Object oVal;
                  try {
                     oVal = serializor.deserialize(value, getFieldType(field));
                  } catch (IllegalPropertyValueException var28) {
                     boolean var46 = false;
                     return var46;
                  } catch (RuntimeException var29) {
                     boolean var12 = false;
                     return var12;
                  }

                  if (VirtualProperty.class.isAssignableFrom(field.getType())) {
                     try {
                        oVal = this.validate(field, propertyInfo, oVal);
                     } catch (ChangeDeniedException var27) {
                        boolean var47 = false;
                        return var47;
                     }

                     VirtualProperty<Object> vProp = (VirtualProperty)field.get(this);
                     vProp.set(oVal);
                     boolean var48 = true;
                     return var48;
                  } else {
                     boolean vProp = this.validateAndDoChange(field, oVal);
                     return vProp;
                  }
               }
            } catch (MissingAnnotationException e) {
               throw new NoSuchPropertyException(e);
            } catch (NoSuchFieldException e) {
               throw new NoSuchPropertyException(e);
            } catch (Exception var32) {
               boolean child = false;
               return child;
            } finally {
               if (field != null) {
                  field.setAccessible(false);
               }

            }
         } else {
            String nextNode = nodes[0];
            Field nodeField = ReflectionUtils.getField(nextNode, this.getClass(), ignoreCase);
            nodeField.setAccessible(true);
            if (!nodeField.isAnnotationPresent(Property.class)) {
               throw new Exception();
            } else {
               SerializationConfig child = (SerializationConfig)nodeField.get(this);
               StringBuilder sb = new StringBuilder();

               for(int i = 1; i < nodes.length; ++i) {
                  sb.append(nodes[i]).append('.');
               }

               sb.deleteCharAt(sb.length() - 1);
               Exception ex = null;

               boolean ret;
               try {
                  ret = child.setProperty(sb.toString(), value, ignoreCase, true);
               } catch (Exception e) {
                  ex = e;
                  ret = false;
               }

               try {
                  this.validate(nodeField, (Property)nodeField.getAnnotation(Property.class), child);
               } catch (Exception var25) {
               }

               if (ex != null) {
                  throw ex;
               } else {
                  return ret;
               }
            }
         }
      } catch (NoSuchPropertyException e) {
         throw e;
      } catch (Exception var35) {
         return false;
      }
   }

   public final String getProperty(String property) throws NoSuchPropertyException {
      return this.getProperty(property, false);
   }

   public final String getProperty(String property, boolean ignoreCase) throws NoSuchPropertyException {
      try {
         String[] nodes = property.split("\\.");
         if (nodes.length == 1) {
            Field field = null;

            String var10;
            try {
               field = ReflectionUtils.getField(this.fixupName(nodes[0], ignoreCase), this.getClass(), ignoreCase);
               field.setAccessible(true);
               if (!field.isAnnotationPresent(Property.class)) {
                  throw new MissingAnnotationException("Property");
               }

               Property propertyInfo = (Property)field.getAnnotation(Property.class);
               Class<? extends Serializor<?, ?>> serializorClass = propertyInfo.serializor();
               Serializor serializor = (Serializor)serializorCache.getInstance(serializorClass, this);
               Object rawValue;
               if (VirtualProperty.class.isAssignableFrom(field.getType())) {
                  VirtualProperty<Object> vProp = (VirtualProperty)field.get(this);
                  rawValue = vProp.get();
               } else {
                  rawValue = field.get(this);
               }

               Object serialized = serializor.serialize(rawValue);
               var10 = serialized.toString();
            } catch (MissingAnnotationException e) {
               throw new NoSuchPropertyException(e);
            } catch (NoSuchFieldException e) {
               throw new NoSuchPropertyException(e);
            } catch (Exception e) {
               throw e;
            } finally {
               if (field != null) {
                  field.setAccessible(false);
               }

            }

            return var10;
         } else {
            String nextNode = nodes[0];
            Field nodeField = ReflectionUtils.getField(this.fixupName(nextNode, ignoreCase), this.getClass(), ignoreCase);
            nodeField.setAccessible(true);
            if (!nodeField.isAnnotationPresent(Property.class)) {
               throw new Exception();
            } else {
               SerializationConfig child = (SerializationConfig)nodeField.get(this);
               StringBuilder sb = new StringBuilder();

               for(int i = 1; i < nodes.length; ++i) {
                  sb.append(nodes[i]).append('.');
               }

               sb.deleteCharAt(sb.length() - 1);
               return child.getProperty(sb.toString(), ignoreCase);
            }
         }
      } catch (NoSuchPropertyException e) {
         throw e;
      } catch (Exception e) {
         throw new RuntimeException("Unexpected exception in getProperty(String, boolean)!", e);
      }
   }

   public final String getPropertyDescription(String property) throws NoSuchPropertyException {
      return this.getPropertyDescription(property, false);
   }

   public final String getPropertyDescription(String property, boolean ignoreCase) throws NoSuchPropertyException {
      try {
         String[] nodes = property.split("\\.");
         if (nodes.length == 1) {
            Field field = null;

            String var23;
            try {
               field = ReflectionUtils.getField(this.fixupName(nodes[0], ignoreCase), this.getClass(), ignoreCase);
               field.setAccessible(true);
               if (!field.isAnnotationPresent(Property.class)) {
                  throw new MissingAnnotationException("Property");
               }

               Property propertyInfo = (Property)field.getAnnotation(Property.class);
               var23 = propertyInfo.description();
            } catch (MissingAnnotationException e) {
               throw new NoSuchPropertyException(e);
            } catch (NoSuchFieldException e) {
               throw new NoSuchPropertyException(e);
            } catch (Exception e) {
               throw e;
            } finally {
               if (field != null) {
                  field.setAccessible(false);
               }

            }

            return var23;
         } else {
            String nextNode = nodes[0];
            Field nodeField = ReflectionUtils.getField(this.fixupName(nextNode, ignoreCase), this.getClass(), ignoreCase);
            nodeField.setAccessible(true);
            if (!nodeField.isAnnotationPresent(Property.class)) {
               throw new Exception();
            } else {
               SerializationConfig child = (SerializationConfig)nodeField.get(this);
               StringBuilder sb = new StringBuilder();

               for(int i = 1; i < nodes.length; ++i) {
                  sb.append(nodes[i]).append('.');
               }

               sb.deleteCharAt(sb.length() - 1);
               return child.getProperty(sb.toString(), ignoreCase);
            }
         }
      } catch (NoSuchPropertyException e) {
         throw e;
      } catch (Exception e) {
         throw new RuntimeException("Unexpected exception in getPropertyDescription(String, boolean)!", e);
      }
   }

   protected final boolean setPropertyValueUnchecked(String property, Object value) {
      try {
         return this.setPropertyValue(property, value);
      } catch (NoSuchPropertyException e) {
         throw new RuntimeException(e);
      }
   }

   protected final boolean setPropertyValueUnchecked(String property, Object value, boolean ignoreCase) {
      try {
         return this.setPropertyValue(property, value, ignoreCase);
      } catch (NoSuchPropertyException e) {
         throw new RuntimeException(e);
      }
   }

   protected final boolean setPropertyUnchecked(String property, String value) {
      try {
         return this.setProperty(property, value);
      } catch (NoSuchPropertyException e) {
         throw new RuntimeException(e);
      }
   }

   protected final boolean setPropertyUnchecked(String property, String value, boolean ignoreCase) {
      try {
         return this.setProperty(property, value, ignoreCase);
      } catch (NoSuchPropertyException e) {
         throw new RuntimeException(e);
      }
   }

   protected final String getPropertyUnchecked(String property) {
      try {
         return this.getProperty(property);
      } catch (NoSuchPropertyException e) {
         throw new RuntimeException(e);
      }
   }

   protected final String getPropertyUnchecked(String property, boolean ignoreCase) {
      try {
         return this.getProperty(property, ignoreCase);
      } catch (NoSuchPropertyException e) {
         throw new RuntimeException(e);
      }
   }

   protected final String getPropertyDescriptionUnchecked(String property) {
      try {
         return this.getPropertyDescription(property);
      } catch (NoSuchPropertyException e) {
         throw new RuntimeException(e);
      }
   }

   protected final String getPropertyDescriptionUnchecked(String property, boolean ignoreCase) {
      try {
         return this.getPropertyDescription(property, ignoreCase);
      } catch (NoSuchPropertyException e) {
         throw new RuntimeException(e);
      }
   }

   protected void registerValidator(String fieldName, Validator validator) {
      try {
         Field field = ReflectionUtils.getField(fieldName, this.getClass(), true);
         if (field != null) {
            this.validatorMap.put(field, validator);
         }
      } catch (NoSuchFieldException var4) {
      }

   }

   private Object validate(Field field, Property propertyInfo, Object newVal) throws IllegalAccessException, ChangeDeniedException {
      Validator<Object> validator = null;
      if (this.validatorMap.containsKey(field)) {
         validator = (Validator)this.validatorMap.get(field);
      } else if (propertyInfo.validator() != Validator.class) {
         validator = (Validator)validatorCache.getInstance(propertyInfo.validator(), this);
      } else if (this.globalValidator != null) {
         validator = this.globalValidator;
      } else if (this.getClass().isAnnotationPresent(ValidateAllWith.class)) {
         ValidateAllWith validAll = (ValidateAllWith)this.getClass().getAnnotation(ValidateAllWith.class);
         validator = (Validator)validatorCache.getInstance(validAll.value(), this);
      }

      if (validator != null) {
         try {
            if (validator instanceof ObjectUsingValidator) {
               newVal = ((ObjectUsingValidator)validator).validateChange(field.getName(), newVal, this.getValue(field), this.objectUsing);
            } else {
               newVal = validator.validateChange(field.getName(), newVal, this.getValue(field));
            }
         } catch (ClassCastException e) {
            throw new IllegalArgumentException("Illegal validator!", e);
         }
      }

      return newVal;
   }

   private Object getValue(Field field) throws IllegalArgumentException, IllegalAccessException {
      return VirtualProperty.class.isAssignableFrom(field.getType()) ? ((VirtualProperty)field.get(this)).get() : field.get(this);
   }

   private boolean validateAndDoChange(Field field, Object newVal) throws Exception {
      if (!field.getType().isAssignableFrom(newVal.getClass()) && !field.getType().isPrimitive()) {
         return false;
      } else {
         Property propInfo = (Property)field.getAnnotation(Property.class);

         try {
            newVal = (T)this.validate(field, propInfo, newVal);
         } catch (ChangeDeniedException var5) {
            return false;
         }

         field.set(this, newVal);
         return true;
      }
   }

   protected abstract void setDefaults();
}
