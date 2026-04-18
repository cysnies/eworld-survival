package me.main__.util.multiverse.SerializationConfig;

public interface Serializor {
   Object serialize(Object var1);

   Object deserialize(Object var1, Class var2) throws IllegalPropertyValueException;
}
