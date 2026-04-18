package me.main__.util.multiverse.SerializationConfig;

public abstract class ObjectUsingValidator implements Validator {
   public ObjectUsingValidator() {
      super();
   }

   /** @deprecated */
   @Deprecated
   public final Object validateChange(String property, Object newValue, Object oldValue) throws ChangeDeniedException, UnsupportedOperationException {
      throw new UnsupportedOperationException();
   }

   public abstract Object validateChange(String var1, Object var2, Object var3, Object var4) throws ChangeDeniedException;
}
