package javax.transaction;

import java.rmi.RemoteException;

public class InvalidTransactionException extends RemoteException {
   public InvalidTransactionException() {
      super();
   }

   public InvalidTransactionException(String msg) {
      super(msg);
   }
}
