package javax.transaction;

import java.rmi.RemoteException;

public class TransactionRolledbackException extends RemoteException {
   public TransactionRolledbackException() {
      super();
   }

   public TransactionRolledbackException(String msg) {
      super(msg);
   }
}
