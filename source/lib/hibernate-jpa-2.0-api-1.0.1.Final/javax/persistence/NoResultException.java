package javax.persistence;

public class NoResultException extends PersistenceException {
   public NoResultException() {
      super();
   }

   public NoResultException(String message) {
      super(message);
   }
}
