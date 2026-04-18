package javax.mail;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;

public abstract class Transport extends Service {
   private volatile Vector transportListeners = null;

   public Transport(Session session, URLName urlname) {
      super(session, urlname);
   }

   public static void send(Message msg) throws MessagingException {
      msg.saveChanges();
      send0(msg, msg.getAllRecipients(), (String)null, (String)null);
   }

   public static void send(Message msg, Address[] addresses) throws MessagingException {
      msg.saveChanges();
      send0(msg, addresses, (String)null, (String)null);
   }

   public static void send(Message msg, String user, String password) throws MessagingException {
      msg.saveChanges();
      send0(msg, msg.getAllRecipients(), user, password);
   }

   public static void send(Message msg, Address[] addresses, String user, String password) throws MessagingException {
      msg.saveChanges();
      send0(msg, addresses, user, password);
   }

   private static void send0(Message msg, Address[] addresses, String user, String password) throws MessagingException {
      if (addresses != null && addresses.length != 0) {
         Hashtable protocols = new Hashtable();
         Vector invalid = new Vector();
         Vector validSent = new Vector();
         Vector validUnsent = new Vector();

         for(int i = 0; i < addresses.length; ++i) {
            if (protocols.containsKey(addresses[i].getType())) {
               Vector v = (Vector)protocols.get(addresses[i].getType());
               v.addElement(addresses[i]);
            } else {
               Vector w = new Vector();
               w.addElement(addresses[i]);
               protocols.put(addresses[i].getType(), w);
            }
         }

         int dsize = protocols.size();
         if (dsize == 0) {
            throw new SendFailedException("No recipient addresses");
         } else {
            Session s = msg.session != null ? msg.session : Session.getDefaultInstance(System.getProperties(), (Authenticator)null);
            if (dsize == 1) {
               Transport transport = s.getTransport(addresses[0]);

               try {
                  if (user != null) {
                     transport.connect(user, password);
                  } else {
                     transport.connect();
                  }

                  transport.sendMessage(msg, addresses);
               } finally {
                  transport.close();
               }

            } else {
               MessagingException chainedEx = null;
               boolean sendFailed = false;
               Enumeration e = protocols.elements();

               while(e.hasMoreElements()) {
                  Vector v = (Vector)e.nextElement();
                  Address[] protaddresses = new Address[v.size()];
                  v.copyInto(protaddresses);
                  Transport transport;
                  if ((transport = s.getTransport(protaddresses[0])) == null) {
                     for(int j = 0; j < protaddresses.length; ++j) {
                        invalid.addElement(protaddresses[j]);
                     }
                  } else {
                     try {
                        transport.connect();
                        transport.sendMessage(msg, protaddresses);
                     } catch (SendFailedException var30) {
                        sendFailed = true;
                        if (chainedEx == null) {
                           chainedEx = var30;
                        } else {
                           chainedEx.setNextException(var30);
                        }

                        Address[] a = var30.getInvalidAddresses();
                        if (a != null) {
                           for(int j = 0; j < a.length; ++j) {
                              invalid.addElement(a[j]);
                           }
                        }

                        a = var30.getValidSentAddresses();
                        if (a != null) {
                           for(int k = 0; k < a.length; ++k) {
                              validSent.addElement(a[k]);
                           }
                        }

                        Address[] c = var30.getValidUnsentAddresses();
                        if (c != null) {
                           for(int l = 0; l < c.length; ++l) {
                              validUnsent.addElement(c[l]);
                           }
                        }
                     } catch (MessagingException mex) {
                        sendFailed = true;
                        if (chainedEx == null) {
                           chainedEx = mex;
                        } else {
                           chainedEx.setNextException(mex);
                        }
                     } finally {
                        transport.close();
                     }
                  }
               }

               if (sendFailed || invalid.size() != 0 || validUnsent.size() != 0) {
                  Address[] a = (Address[])null;
                  Address[] b = (Address[])null;
                  Address[] c = (Address[])null;
                  if (validSent.size() > 0) {
                     a = new Address[validSent.size()];
                     validSent.copyInto(a);
                  }

                  if (validUnsent.size() > 0) {
                     b = new Address[validUnsent.size()];
                     validUnsent.copyInto(b);
                  }

                  if (invalid.size() > 0) {
                     c = new Address[invalid.size()];
                     invalid.copyInto(c);
                  }

                  throw new SendFailedException("Sending failed", chainedEx, a, b, c);
               }
            }
         }
      } else {
         throw new SendFailedException("No recipient addresses");
      }
   }

   public abstract void sendMessage(Message var1, Address[] var2) throws MessagingException;

   public synchronized void addTransportListener(TransportListener l) {
      if (this.transportListeners == null) {
         this.transportListeners = new Vector();
      }

      this.transportListeners.addElement(l);
   }

   public synchronized void removeTransportListener(TransportListener l) {
      if (this.transportListeners != null) {
         this.transportListeners.removeElement(l);
      }

   }

   protected void notifyTransportListeners(int type, Address[] validSent, Address[] validUnsent, Address[] invalid, Message msg) {
      if (this.transportListeners != null) {
         TransportEvent e = new TransportEvent(this, type, validSent, validUnsent, invalid, msg);
         this.queueEvent(e, this.transportListeners);
      }
   }
}
