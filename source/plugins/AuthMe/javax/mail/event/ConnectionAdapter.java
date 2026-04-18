package javax.mail.event;

public abstract class ConnectionAdapter implements ConnectionListener {
   public ConnectionAdapter() {
      super();
   }

   public void opened(ConnectionEvent e) {
   }

   public void disconnected(ConnectionEvent e) {
   }

   public void closed(ConnectionEvent e) {
   }
}
