package javax.persistence;

public class NonUniqueResultException extends PersistenceException {
   public NonUniqueResultException() {
      super();
   }

   public NonUniqueResultException(String message) {
      super(message);
   }
}
