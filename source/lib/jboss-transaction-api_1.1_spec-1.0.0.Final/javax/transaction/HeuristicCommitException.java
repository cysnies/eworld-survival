package javax.transaction;

public class HeuristicCommitException extends Exception {
   public HeuristicCommitException() {
      super();
   }

   public HeuristicCommitException(String msg) {
      super(msg);
   }
}
