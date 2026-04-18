package net.citizensnpcs.api.jnbt;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;

public final class NBTUtils {
   private NBTUtils() {
      super();
   }

   public static Tag createTag(String name, Object value) {
      Class<?> clazz = value.getClass();
      if (clazz != Byte.TYPE && clazz != Byte.class) {
         if (clazz != Short.TYPE && clazz != Short.class) {
            if (clazz != Integer.TYPE && clazz != Integer.class) {
               if (clazz != Long.TYPE && clazz != Long.class) {
                  if (clazz != Float.TYPE && clazz != Float.class) {
                     if (clazz != Double.TYPE && clazz != Double.class) {
                        if (clazz == byte[].class) {
                           return new ByteArrayTag(name, (byte[])value);
                        } else if (clazz == int[].class) {
                           return new IntArrayTag(name, (int[])value);
                        } else if (clazz == String.class) {
                           return new StringTag(name, (String)value);
                        } else if (List.class.isAssignableFrom(clazz)) {
                           List<?> list = (List)value;
                           if (list.isEmpty()) {
                              throw new IllegalArgumentException("cannot set empty list");
                           } else {
                              List<Tag> newList = Lists.newArrayList();
                              Class<? extends Tag> tagClass = null;

                              for(Object v : list) {
                                 Tag tag = createTag("", v);
                                 if (tag == null) {
                                    throw new IllegalArgumentException("cannot convert list value to tag");
                                 }

                                 if (tagClass == null) {
                                    tagClass = tag.getClass();
                                 } else if (tagClass != tag.getClass()) {
                                    throw new IllegalArgumentException("list values must be of homogeneous type");
                                 }

                                 newList.add(tag);
                              }

                              return new ListTag(name, tagClass, newList);
                           }
                        } else if (Map.class.isAssignableFrom(clazz)) {
                           Map<String, Object> map = (Map)value;
                           if (map.isEmpty()) {
                              throw new IllegalArgumentException("cannot set empty list");
                           } else {
                              Map<String, Tag> newMap = Maps.newHashMap();

                              for(Map.Entry entry : map.entrySet()) {
                                 Tag tag = createTag("", entry.getValue());
                                 if (tag == null) {
                                    throw new IllegalArgumentException("cannot convert map value with key " + (String)entry.getKey() + " to tag");
                                 }

                                 newMap.put(entry.getKey(), tag);
                              }

                              return new CompoundTag(name, newMap);
                           }
                        } else {
                           return null;
                        }
                     } else {
                        return new DoubleTag(name, (Double)value);
                     }
                  } else {
                     return new FloatTag(name, (Float)value);
                  }
               } else {
                  return new LongTag(name, (Long)value);
               }
            } else {
               return new IntTag(name, (Integer)value);
            }
         } else {
            return new ShortTag(name, (Short)value);
         }
      } else {
         return new ByteTag(name, (Byte)value);
      }
   }

   public static Class getTypeClass(int type) {
      switch (type) {
         case 0:
            return EndTag.class;
         case 1:
            return ByteTag.class;
         case 2:
            return ShortTag.class;
         case 3:
            return IntTag.class;
         case 4:
            return LongTag.class;
         case 5:
            return FloatTag.class;
         case 6:
            return DoubleTag.class;
         case 7:
            return ByteArrayTag.class;
         case 8:
            return StringTag.class;
         case 9:
            return ListTag.class;
         case 10:
            return CompoundTag.class;
         case 11:
            return IntArrayTag.class;
         default:
            throw new IllegalArgumentException("Invalid tag type : " + type + ".");
      }
   }

   public static int getTypeCode(Class clazz) {
      if (clazz.equals(ByteArrayTag.class)) {
         return 7;
      } else if (clazz.equals(ByteTag.class)) {
         return 1;
      } else if (clazz.equals(CompoundTag.class)) {
         return 10;
      } else if (clazz.equals(DoubleTag.class)) {
         return 6;
      } else if (clazz.equals(EndTag.class)) {
         return 0;
      } else if (clazz.equals(FloatTag.class)) {
         return 5;
      } else if (clazz.equals(IntTag.class)) {
         return 3;
      } else if (clazz.equals(ListTag.class)) {
         return 9;
      } else if (clazz.equals(LongTag.class)) {
         return 4;
      } else if (clazz.equals(ShortTag.class)) {
         return 2;
      } else if (clazz.equals(StringTag.class)) {
         return 8;
      } else if (clazz.equals(IntArrayTag.class)) {
         return 11;
      } else {
         throw new IllegalArgumentException("Invalid tag classs (" + clazz.getName() + ").");
      }
   }

   public static String getTypeName(Class clazz) {
      if (clazz.equals(ByteArrayTag.class)) {
         return "TAG_Byte_Array";
      } else if (clazz.equals(ByteTag.class)) {
         return "TAG_Byte";
      } else if (clazz.equals(CompoundTag.class)) {
         return "TAG_Compound";
      } else if (clazz.equals(DoubleTag.class)) {
         return "TAG_Double";
      } else if (clazz.equals(EndTag.class)) {
         return "TAG_End";
      } else if (clazz.equals(FloatTag.class)) {
         return "TAG_Float";
      } else if (clazz.equals(IntTag.class)) {
         return "TAG_Int";
      } else if (clazz.equals(ListTag.class)) {
         return "TAG_List";
      } else if (clazz.equals(LongTag.class)) {
         return "TAG_Long";
      } else if (clazz.equals(ShortTag.class)) {
         return "TAG_Short";
      } else if (clazz.equals(StringTag.class)) {
         return "TAG_String";
      } else if (clazz.equals(IntArrayTag.class)) {
         return "TAG_Int_Array";
      } else {
         throw new IllegalArgumentException("Invalid tag classs (" + clazz.getName() + ").");
      }
   }
}
