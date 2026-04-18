package com.khorn.terraincontrol.bukkit.util;

import com.khorn.terraincontrol.configuration.Tag;
import java.lang.reflect.Field;
import java.util.Map;
import net.minecraft.server.v1_6_R2.NBTBase;
import net.minecraft.server.v1_6_R2.NBTTagByte;
import net.minecraft.server.v1_6_R2.NBTTagByteArray;
import net.minecraft.server.v1_6_R2.NBTTagCompound;
import net.minecraft.server.v1_6_R2.NBTTagDouble;
import net.minecraft.server.v1_6_R2.NBTTagFloat;
import net.minecraft.server.v1_6_R2.NBTTagInt;
import net.minecraft.server.v1_6_R2.NBTTagIntArray;
import net.minecraft.server.v1_6_R2.NBTTagList;
import net.minecraft.server.v1_6_R2.NBTTagLong;
import net.minecraft.server.v1_6_R2.NBTTagShort;
import net.minecraft.server.v1_6_R2.NBTTagString;

public class NBTHelper {
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$khorn$terraincontrol$configuration$Tag$Type;

   public NBTHelper() {
      super();
   }

   public static Tag getNBTFromNMSTagCompound(NBTTagCompound nmsTag) {
      Tag compoundTag = new Tag(Tag.Type.TAG_Compound, nmsTag.getName(), new Tag[]{new Tag(Tag.Type.TAG_End, (String)null, (Tag[])null)});
      Map nmsChildTags = null;

      try {
         Field mapField = NBTTagCompound.class.getDeclaredField("map");
         mapField.setAccessible(true);
         nmsChildTags = (Map)mapField.get(nmsTag);
      } catch (Exception e) {
         e.printStackTrace();
      }

      if (nmsChildTags == null) {
         return compoundTag;
      } else {
         for(Object nmsChildTagName : nmsChildTags.keySet()) {
            NBTBase nmsChildTag = (NBTBase)nmsChildTags.get(nmsChildTagName);
            Tag.Type type = Tag.Type.values()[nmsChildTag.getTypeId()];
            switch (type) {
               case TAG_End:
               default:
                  break;
               case TAG_Byte:
               case TAG_Short:
               case TAG_Int:
               case TAG_Long:
               case TAG_Float:
               case TAG_Double:
               case TAG_Byte_Array:
               case TAG_String:
               case TAG_Int_Array:
                  compoundTag.addTag(new Tag(type, nmsChildTag.getName(), getValueFromNms(nmsChildTag)));
                  break;
               case TAG_List:
                  Tag listChildTag = getNBTFromNMSTagList((NBTTagList)nmsChildTag);
                  if (listChildTag != null) {
                     compoundTag.addTag(listChildTag);
                  }
                  break;
               case TAG_Compound:
                  compoundTag.addTag(getNBTFromNMSTagCompound((NBTTagCompound)nmsChildTag));
            }
         }

         return compoundTag;
      }
   }

   private static Tag getNBTFromNMSTagList(NBTTagList nmsListTag) {
      if (nmsListTag.size() == 0) {
         return null;
      } else {
         Tag.Type listType = Tag.Type.values()[nmsListTag.get(0).getTypeId()];
         Tag listTag = new Tag(nmsListTag.getName(), listType);

         for(int i = 0; i < nmsListTag.size(); ++i) {
            NBTBase nmsChildTag = nmsListTag.get(i);
            switch (listType) {
               case TAG_End:
               default:
                  break;
               case TAG_Byte:
               case TAG_Short:
               case TAG_Int:
               case TAG_Long:
               case TAG_Float:
               case TAG_Double:
               case TAG_Byte_Array:
               case TAG_String:
               case TAG_Int_Array:
                  listTag.addTag(new Tag(listType, nmsChildTag.getName(), getValueFromNms(nmsChildTag)));
                  break;
               case TAG_List:
                  Tag listChildTag = getNBTFromNMSTagList((NBTTagList)nmsChildTag);
                  if (listChildTag != null) {
                     listTag.addTag(listChildTag);
                  }
                  break;
               case TAG_Compound:
                  listTag.addTag(getNBTFromNMSTagCompound((NBTTagCompound)nmsChildTag));
            }
         }

         return listTag;
      }
   }

   private static Object getValueFromNms(NBTBase nmsTag) {
      Tag.Type type = Tag.Type.values()[nmsTag.getTypeId()];
      switch (type) {
         case TAG_Byte:
            return ((NBTTagByte)nmsTag).data;
         case TAG_Short:
            return ((NBTTagShort)nmsTag).data;
         case TAG_Int:
            return ((NBTTagInt)nmsTag).data;
         case TAG_Long:
            return ((NBTTagLong)nmsTag).data;
         case TAG_Float:
            return ((NBTTagFloat)nmsTag).data;
         case TAG_Double:
            return ((NBTTagDouble)nmsTag).data;
         case TAG_Byte_Array:
            return ((NBTTagByteArray)nmsTag).data;
         case TAG_String:
            return ((NBTTagString)nmsTag).data;
         case TAG_List:
         case TAG_Compound:
         default:
            throw new IllegalArgumentException(type + "doesn't have a simple value!");
         case TAG_Int_Array:
            return ((NBTTagIntArray)nmsTag).data;
      }
   }

   public static NBTTagCompound getNMSFromNBTTagCompound(Tag compoundTag) {
      NBTTagCompound nmsTag = new NBTTagCompound(compoundTag.getName());
      Tag[] childTags = (Tag[])compoundTag.getValue();

      for(Tag tag : childTags) {
         switch (tag.getType()) {
            case TAG_End:
            default:
               break;
            case TAG_Byte:
            case TAG_Short:
            case TAG_Int:
            case TAG_Long:
            case TAG_Float:
            case TAG_Double:
            case TAG_Byte_Array:
            case TAG_String:
            case TAG_Int_Array:
               nmsTag.set(tag.getName(), createTagNms(tag.getType(), tag.getName(), tag.getValue()));
               break;
            case TAG_List:
               nmsTag.set(tag.getName(), getNMSFromNBTTagList(tag));
               break;
            case TAG_Compound:
               nmsTag.set(tag.getName(), getNMSFromNBTTagCompound(tag));
         }
      }

      return nmsTag;
   }

   private static NBTTagList getNMSFromNBTTagList(Tag listTag) {
      NBTTagList nmsTag = new NBTTagList(listTag.getName());
      Tag[] childTags = (Tag[])listTag.getValue();

      for(Tag tag : childTags) {
         switch (tag.getType()) {
            case TAG_End:
            default:
               break;
            case TAG_Byte:
            case TAG_Short:
            case TAG_Int:
            case TAG_Long:
            case TAG_Float:
            case TAG_Double:
            case TAG_Byte_Array:
            case TAG_String:
            case TAG_Int_Array:
               nmsTag.add(createTagNms(tag.getType(), tag.getName(), tag.getValue()));
               break;
            case TAG_List:
               nmsTag.add(getNMSFromNBTTagList(tag));
               break;
            case TAG_Compound:
               nmsTag.add(getNMSFromNBTTagCompound(tag));
         }
      }

      return nmsTag;
   }

   private static NBTBase createTagNms(Tag.Type type, String name, Object value) {
      switch (type) {
         case TAG_Byte:
            return new NBTTagByte(name, (Byte)value);
         case TAG_Short:
            return new NBTTagShort(name, (Short)value);
         case TAG_Int:
            return new NBTTagInt(name, (Integer)value);
         case TAG_Long:
            return new NBTTagLong(name, (Long)value);
         case TAG_Float:
            return new NBTTagFloat(name, (Float)value);
         case TAG_Double:
            return new NBTTagDouble(name, (Double)value);
         case TAG_Byte_Array:
            return new NBTTagByteArray(name, (byte[])value);
         case TAG_String:
            return new NBTTagString(name, (String)value);
         case TAG_List:
         case TAG_Compound:
         default:
            throw new IllegalArgumentException(type + "doesn't have a simple value!");
         case TAG_Int_Array:
            return new NBTTagIntArray(name, (int[])value);
      }
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$khorn$terraincontrol$configuration$Tag$Type() {
      int[] var10000 = $SWITCH_TABLE$com$khorn$terraincontrol$configuration$Tag$Type;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[Tag.Type.values().length];

         try {
            var0[Tag.Type.TAG_Byte.ordinal()] = 2;
         } catch (NoSuchFieldError var12) {
         }

         try {
            var0[Tag.Type.TAG_Byte_Array.ordinal()] = 8;
         } catch (NoSuchFieldError var11) {
         }

         try {
            var0[Tag.Type.TAG_Compound.ordinal()] = 11;
         } catch (NoSuchFieldError var10) {
         }

         try {
            var0[Tag.Type.TAG_Double.ordinal()] = 7;
         } catch (NoSuchFieldError var9) {
         }

         try {
            var0[Tag.Type.TAG_End.ordinal()] = 1;
         } catch (NoSuchFieldError var8) {
         }

         try {
            var0[Tag.Type.TAG_Float.ordinal()] = 6;
         } catch (NoSuchFieldError var7) {
         }

         try {
            var0[Tag.Type.TAG_Int.ordinal()] = 4;
         } catch (NoSuchFieldError var6) {
         }

         try {
            var0[Tag.Type.TAG_Int_Array.ordinal()] = 12;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[Tag.Type.TAG_List.ordinal()] = 10;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[Tag.Type.TAG_Long.ordinal()] = 5;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[Tag.Type.TAG_Short.ordinal()] = 3;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[Tag.Type.TAG_String.ordinal()] = 9;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$khorn$terraincontrol$configuration$Tag$Type = var0;
         return var0;
      }
   }
}
