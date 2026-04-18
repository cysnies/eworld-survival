package javax.mail.event;

public abstract class MessageCountAdapter implements MessageCountListener {
   public MessageCountAdapter() {
      super();
   }

   public void messagesAdded(MessageCountEvent e) {
   }

   public void messagesRemoved(MessageCountEvent e) {
   }
}
