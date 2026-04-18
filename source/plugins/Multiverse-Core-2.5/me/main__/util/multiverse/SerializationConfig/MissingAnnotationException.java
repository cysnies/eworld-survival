package me.main__.util.multiverse.SerializationConfig;

public class MissingAnnotationException extends Exception {
   private static final long serialVersionUID = 1L;

   public MissingAnnotationException() {
      super();
   }

   public MissingAnnotationException(String message) {
      super(message);
   }

   public MissingAnnotationException(Throwable cause) {
      super(cause);
   }

   public MissingAnnotationException(String message, Throwable cause) {
      super(message, cause);
   }
}
