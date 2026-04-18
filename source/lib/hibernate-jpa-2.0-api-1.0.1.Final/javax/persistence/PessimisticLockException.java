package javax.persistence;

public class PessimisticLockException extends PersistenceException {
   Object entity;

   public PessimisticLockException() {
      super();
   }

   public PessimisticLockException(String message) {
      super(message);
   }

   public PessimisticLockException(String message, Throwable cause) {
      super(message, cause);
   }

   public PessimisticLockException(Throwable cause) {
      super(cause);
   }

   public PessimisticLockException(Object entity) {
      super();
      this.entity = entity;
   }

   public PessimisticLockException(String message, Throwable cause, Object entity) {
      super(message, cause);
      this.entity = entity;
   }

   public Object getEntity() {
      return this.entity;
   }
}
