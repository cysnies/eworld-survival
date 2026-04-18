package javax.persistence;

public class QueryTimeoutException extends PersistenceException {
   Query query;

   public QueryTimeoutException() {
      super();
   }

   public QueryTimeoutException(String message) {
      super(message);
   }

   public QueryTimeoutException(String message, Throwable cause) {
      super(message, cause);
   }

   public QueryTimeoutException(Throwable cause) {
      super(cause);
   }

   public QueryTimeoutException(Query query) {
      super();
      this.query = query;
   }

   public QueryTimeoutException(String message, Throwable cause, Query query) {
      super(message, cause);
      this.query = query;
   }

   public Query getQuery() {
      return this.query;
   }
}
