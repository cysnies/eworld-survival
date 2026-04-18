package javax.transaction;

public class SystemException extends Exception {
   public int errorCode;

   public SystemException() {
      super();
   }

   public SystemException(String msg) {
      super(msg);
   }

   public SystemException(int errcode) {
      super();
      this.errorCode = errcode;
   }
}
