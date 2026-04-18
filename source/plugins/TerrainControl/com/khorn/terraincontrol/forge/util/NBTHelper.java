package com.khorn.terraincontrol.forge.util;

import com.khorn.terraincontrol.configuration.Tag;
import java.lang.reflect.Field;
import java.util.Map;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

public class NBTHelper {
   public NBTHelper() {
      super();
   }

   public static Tag getNBTFromNMSTagCompound(NBTTagCompound nmsTag) {
      Tag compoundTag = new Tag(Tag.Type.TAG_Compound, nmsTag.func_74740_e(), new Tag[]{new Tag(Tag.Type.TAG_End, (String)null, (Tag[])null)});
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
            Tag.Type type = Tag.Type.values()[nmsChildTag.func_74732_a()];
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
                  compoundTag.addTag(new Tag(type, nmsChildTag.func_74740_e(), getValueFromNms(nmsChildTag)));
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
      if (nmsListTag.func_74745_c() == 0) {
         return null;
      } else {
         Tag.Type listType = Tag.Type.values()[nmsListTag.func_74743_b(0).func_74732_a()];
         Tag listTag = new Tag(nmsListTag.func_74740_e(), listType);

         for(int i = 0; i < nmsListTag.func_74745_c(); ++i) {
            NBTBase nmsChildTag = nmsListTag.func_74743_b(i);
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
                  listTag.addTag(new Tag(listType, nmsChildTag.func_74740_e(), getValueFromNms(nmsChildTag)));
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
      Tag.Type type = Tag.Type.values()[nmsTag.func_74732_a()];
      switch (type) {
         case TAG_Byte:
            return ((NBTTagByte)nmsTag).field_74756_a;
         case TAG_Short:
            return ((NBTTagShort)nmsTag).field_74752_a;
         case TAG_Int:
            return ((NBTTagInt)nmsTag).field_74748_a;
         case TAG_Long:
            return ((NBTTagLong)nmsTag).field_74753_a;
         case TAG_Float:
            return ((NBTTagFloat)nmsTag).field_74750_a;
         case TAG_Double:
            return ((NBTTagDouble)nmsTag).field_74755_a;
         case TAG_Byte_Array:
            return ((NBTTagByteArray)nmsTag).field_74754_a;
         case TAG_String:
            return ((NBTTagString)nmsTag).field_74751_a;
         case TAG_Int_Array:
            return ((NBTTagIntArray)nmsTag).field_74749_a;
         default:
            throw new IllegalArgumentException(type + "doesn't have a simple value!");
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
               nmsTag.func_74782_a(tag.getName(), createTagNms(tag.getType(), tag.getName(), tag.getValue()));
               break;
            case TAG_List:
               nmsTag.func_74782_a(tag.getName(), getNMSFromNBTTagList(tag));
               break;
            case TAG_Compound:
               nmsTag.func_74782_a(tag.getName(), getNMSFromNBTTagCompound(tag));
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
               nmsTag.func_74742_a(createTagNms(tag.getType(), tag.getName(), tag.getValue()));
               break;
            case TAG_List:
               nmsTag.func_74742_a(getNMSFromNBTTagList(tag));
               break;
            case TAG_Compound:
               nmsTag.func_74742_a(getNMSFromNBTTagCompound(tag));
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
         case TAG_Int_Array:
            return new NBTTagIntArray(name, (int[])value);
         default:
            throw new IllegalArgumentException(type + "doesn't have a simple value!");
      }
   }
}
