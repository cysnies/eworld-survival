package javax.transaction;

public class RollbackException extends Exception {
   public RollbackException() {
      super();
   }

   public RollbackException(String msg) {
      super(msg);
   }
}
