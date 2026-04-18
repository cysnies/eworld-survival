package javax.transaction;

import java.rmi.RemoteException;

public class TransactionRequiredException extends RemoteException {
   public TransactionRequiredException() {
      super();
   }

   public TransactionRequiredException(String msg) {
      super(msg);
   }
}
