package javax.transaction;

public class NotSupportedException extends Exception {
   public NotSupportedException() {
      super();
   }

   public NotSupportedException(String msg) {
      super(msg);
   }
}
