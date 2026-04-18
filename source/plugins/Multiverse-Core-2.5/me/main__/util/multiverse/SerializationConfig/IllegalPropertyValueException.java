package me.main__.util.multiverse.SerializationConfig;

public class IllegalPropertyValueException extends Exception {
   private static final long serialVersionUID = 1L;

   public IllegalPropertyValueException() {
      super();
   }

   public IllegalPropertyValueException(String message) {
      super(message);
   }

   public IllegalPropertyValueException(Throwable cause) {
      super(cause);
   }

   public IllegalPropertyValueException(String message, Throwable cause) {
      super(message, cause);
   }
}
