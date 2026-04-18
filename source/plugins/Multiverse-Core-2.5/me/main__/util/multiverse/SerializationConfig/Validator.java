package me.main__.util.multiverse.SerializationConfig;

public interface Validator {
   Object validateChange(String var1, Object var2, Object var3) throws ChangeDeniedException;
}
