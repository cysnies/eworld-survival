package javax.transaction;

public class HeuristicRollbackException extends Exception {
   public HeuristicRollbackException() {
      super();
   }

   public HeuristicRollbackException(String msg) {
      super(msg);
   }
}
