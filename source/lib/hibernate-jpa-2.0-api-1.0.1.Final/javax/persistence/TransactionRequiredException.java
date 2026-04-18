package javax.persistence;

public class TransactionRequiredException extends PersistenceException {
   public TransactionRequiredException() {
      super();
   }

   public TransactionRequiredException(String message) {
      super(message);
   }
}
