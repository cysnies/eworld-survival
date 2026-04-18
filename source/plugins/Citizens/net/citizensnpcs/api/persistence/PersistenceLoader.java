package net.citizensnpcs.api.persistence;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Primitives;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.Location;

public class PersistenceLoader {
   private static final Map fieldCache = new WeakHashMap();
   private static final Map loadedDelegates = new WeakHashMap();
   private static final Exception loadException = new Exception() {
      private static final long serialVersionUID = -4245839150826112365L;

      public void fillInStackTrace(StackTraceElement[] elements) {
      }
   };
   private static final Map persistRedirects = new WeakHashMap();

   public PersistenceLoader() {
      super();
   }

   private static String createRelativeKey(String key, int ext) {
      return createRelativeKey(key, Integer.toString(ext));
   }

   private static String createRelativeKey(String parent, String ext) {
      if (ext.isEmpty()) {
         return parent;
      } else if (ext.charAt(0) == '.') {
         return parent.isEmpty() ? ext.substring(1, ext.length()) : parent + ext;
      } else {
         return parent.isEmpty() ? ext : parent + '.' + ext;
      }
   }

   private static void deserialise(PersistField field, DataKey root) throws Exception {
      Class<?> type = field.getType();
      Class<?> collectionType = field.getCollectionType();
      if (!Collection.class.isAssignableFrom(collectionType) && !Map.class.isAssignableFrom(collectionType)) {
         throw loadException;
      } else {
         Object value;
         if (List.class.isAssignableFrom(type)) {
            List<Object> list = (List)(!List.class.isAssignableFrom(collectionType) ? Lists.newArrayList() : collectionType.newInstance());
            Object raw = root.getRaw(field.key);
            if (raw instanceof List && collectionType.isAssignableFrom(raw.getClass())) {
               list = (List)raw;
            } else {
               deserialiseCollection(list, root, field);
            }

            value = list;
         } else if (Set.class.isAssignableFrom(type)) {
            Set<Object> set;
            if (Set.class.isAssignableFrom(collectionType)) {
               set = (Set)collectionType.newInstance();
            } else if (field.getType().isEnum()) {
               set = EnumSet.noneOf(field.getType());
            } else {
               set = (Set)(field.get() != null && Set.class.isAssignableFrom(field.get().getClass()) ? field.get().getClass().newInstance() : Sets.newHashSet());
            }

            Object raw = root.getRaw(field.key);
            if (raw instanceof Set && collectionType.isAssignableFrom(raw.getClass())) {
               set = (Set)raw;
            } else {
               deserialiseCollection(set, root, field);
            }

            value = set;
         } else if (Map.class.isAssignableFrom(type)) {
            Map<String, Object> map;
            if (Map.class.isAssignableFrom(collectionType)) {
               map = (Map)collectionType.newInstance();
            } else {
               map = (Map)(field.get() != null && Map.class.isAssignableFrom(field.get().getClass()) && !field.get().getClass().isInterface() ? field.get() : Maps.newHashMap());
            }

            deserialiseMap(map, root, field);
            value = map;
         } else {
            value = deserialiseValue(field, root.getRelative(field.key));
         }

         if (value == null && field.isRequired()) {
            throw loadException;
         } else {
            if (type.isPrimitive()) {
               if (value == null) {
                  return;
               }

               type = Primitives.wrap(type);
            }

            if (value == null || type.isAssignableFrom(value.getClass())) {
               field.set(value);
            }
         }
      }
   }

   private static void deserialiseCollection(Collection collection, DataKey root, PersistField field) {
      for(DataKey subKey : root.getRelative(field.key).getSubKeys()) {
         Object loaded = deserialiseValue(field, subKey);
         if (loaded != null) {
            collection.add(loaded);
         }
      }

   }

   private static void deserialiseMap(Map map, DataKey root, PersistField field) {
      for(DataKey subKey : root.getRelative(field.key).getSubKeys()) {
         Object loaded = deserialiseValue(field, subKey);
         if (loaded != null) {
            map.put(subKey.name(), loaded);
         }
      }

   }

   private static Object deserialiseValue(PersistField field, DataKey root) {
      Class<?> type = field.field.getType().isEnum() ? field.field.getType() : getGenericType(field.field);
      if (field.delegate == null && type.isEnum()) {
         Class<? extends Enum> clazz = type;
         Object obj = root.getRaw("");
         if (obj instanceof String) {
            try {
               return Enum.valueOf(clazz, obj.toString());
            } catch (IllegalArgumentException var6) {
            }
         }
      }

      return field.delegate == null ? root.getRaw("") : field.delegate.create(root);
   }

   private static void ensureDelegateLoaded(Class delegateClass) {
      if (!loadedDelegates.containsKey(delegateClass)) {
         try {
            loadedDelegates.put(delegateClass, delegateClass.newInstance());
         } catch (Exception e) {
            e.printStackTrace();
            loadedDelegates.put(delegateClass, (Object)null);
         }

      }
   }

   private static Persister getDelegate(Field field, Class fallback) {
      DelegatePersistence delegate = (DelegatePersistence)field.getAnnotation(DelegatePersistence.class);
      Persister<?> persister;
      if (delegate == null) {
         persister = (Persister)loadedDelegates.get(persistRedirects.get(fallback));
         if (persister == null) {
            return null;
         }
      } else {
         persister = (Persister)loadedDelegates.get(delegate.value());
      }

      if (persister == null) {
         persister = (Persister)loadedDelegates.get(persistRedirects.get(fallback));
      }

      return persister;
   }

   private static Field[] getFields(Class clazz) {
      Field[] fields = (Field[])fieldCache.get(clazz);
      if (fields == null) {
         fieldCache.put(clazz, fields = getFieldsFromClass(clazz));
      }

      return fields;
   }

   private static Field[] getFieldsFromClass(Class clazz) {
      List<Field> toFilter = Lists.newArrayList(clazz.getDeclaredFields());
      Iterator<Field> itr = toFilter.iterator();

      while(itr.hasNext()) {
         Field field = (Field)itr.next();
         field.setAccessible(true);
         Persist persistAnnotation = (Persist)field.getAnnotation(Persist.class);
         if (persistAnnotation == null) {
            itr.remove();
         } else {
            DelegatePersistence delegate = (DelegatePersistence)field.getAnnotation(DelegatePersistence.class);
            if (delegate != null) {
               Class<? extends Persister<?>> delegateClass = delegate.value();
               ensureDelegateLoaded(delegateClass);
               Persister<?> in = (Persister)loadedDelegates.get(delegateClass);
               if (in == null) {
                  itr.remove();
               }
            }
         }
      }

      return (Field[])toFilter.toArray(new Field[toFilter.size()]);
   }

   private static Class getGenericType(Field field) {
      if (field.getGenericType() != null && field.getGenericType() instanceof ParameterizedType) {
         Type[] args = ((ParameterizedType)field.getGenericType()).getActualTypeArguments();
         return args.length > 0 && args[0] instanceof Class ? (Class)args[0] : field.getType();
      } else {
         return field.getType();
      }
   }

   public static Object load(Class clazz, DataKey root) {
      T instance;
      try {
         instance = (T)clazz.newInstance();
      } catch (InstantiationException e) {
         e.printStackTrace();
         return null;
      } catch (IllegalAccessException e) {
         e.printStackTrace();
         return null;
      }

      return instance == null ? null : load(instance, root);
   }

   public static Object load(Object instance, DataKey root) {
      Class<?> clazz = instance.getClass();
      Field[] fields = getFields(clazz);

      for(Field field : fields) {
         try {
            deserialise(new PersistField(field, instance), root);
         } catch (Exception e) {
            if (e != loadException) {
               e.printStackTrace();
            }

            return null;
         }
      }

      return instance;
   }

   public static void registerPersistDelegate(Class clazz, Class delegateClass) {
      persistRedirects.put(clazz, delegateClass);
      ensureDelegateLoaded(delegateClass);
   }

   public static void save(Object instance, DataKey root) {
      Class<?> clazz = instance.getClass();
      Field[] fields = getFields(clazz);

      for(Field field : fields) {
         serialise(new PersistField(field, instance), root);
      }

   }

   private static void serialise(PersistField field, DataKey root) {
      if (field.get() != null) {
         if (Collection.class.isAssignableFrom(field.getType())) {
            Collection<?> collection = (Collection)field.get();
            root.removeKey(field.key);
            int i = 0;

            for(Object object : collection) {
               String key = createRelativeKey(field.key, i);
               serialiseValue(field, root.getRelative(key), object);
               ++i;
            }
         } else if (Map.class.isAssignableFrom(field.getType())) {
            Map<String, Object> map = (Map)field.get();
            root.removeKey(field.key);

            for(Map.Entry entry : map.entrySet()) {
               String key = createRelativeKey(field.key, (String)entry.getKey());
               serialiseValue(field, root.getRelative(key), entry.getValue());
            }
         } else {
            serialiseValue(field, root.getRelative(field.key), field.get());
         }

      }
   }

   private static void serialiseValue(PersistField field, DataKey root, Object value) {
      if (field.delegate != null) {
         field.delegate.save(value, root);
      } else if (value instanceof Enum) {
         root.setRaw("", ((Enum)value).name());
      } else {
         root.setRaw("", value);
      }

   }

   static {
      registerPersistDelegate(Location.class, LocationPersister.class);
   }

   private static class PersistField {
      private final Persister delegate;
      private final Field field;
      private final Object instance;
      private final String key;
      private final Persist persistAnnotation;
      private Object value;
      private static final Object NULL = new Object();

      private PersistField(Field field, Object instance) {
         super();
         this.field = field;
         this.persistAnnotation = (Persist)field.getAnnotation(Persist.class);
         this.key = this.persistAnnotation.value().equals("UNINITIALISED") ? field.getName() : this.persistAnnotation.value();
         Class<?> fallback = field.getType();
         if (field.getGenericType() instanceof ParameterizedType) {
            fallback = (Class)((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
         }

         this.delegate = PersistenceLoader.getDelegate(field, fallback);
         this.instance = instance;
      }

      public Object get() {
         if (this.value == null) {
            try {
               this.value = this.field.get(this.instance);
            } catch (Exception e) {
               e.printStackTrace();
               this.value = NULL;
            }
         }

         return this.value == NULL ? null : this.value;
      }

      public Class getCollectionType() {
         return this.persistAnnotation.collectionType();
      }

      public Class getType() {
         return this.field.getType();
      }

      public boolean isRequired() {
         return this.persistAnnotation.required();
      }

      public void set(Object value) {
         try {
            this.field.set(this.instance, value);
         } catch (Exception e) {
            e.printStackTrace();
         }

      }
   }
}
