package javax.mail.internet;

import com.sun.mail.util.PropUtil;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import javax.mail.Address;
import javax.mail.Session;

public class InternetAddress extends Address implements Cloneable {
   protected String address;
   protected String personal;
   protected String encodedPersonal;
   private static final long serialVersionUID = -7507595530758302903L;
   private static final boolean ignoreBogusGroupName = PropUtil.getBooleanSystemProperty("mail.mime.address.ignorebogusgroupname", true);
   private static final String rfc822phrase = "()<>@,;:\\\"\t .[]".replace(' ', '\u0000').replace('\t', '\u0000');
   private static final String specialsNoDotNoAt = "()<>,;:\\\"[]";
   private static final String specialsNoDot = "()<>,;:\\\"[]@";

   public InternetAddress() {
      super();
   }

   public InternetAddress(String address) throws AddressException {
      super();
      InternetAddress[] a = parse(address, true);
      if (a.length != 1) {
         throw new AddressException("Illegal address", address);
      } else {
         this.address = a[0].address;
         this.personal = a[0].personal;
         this.encodedPersonal = a[0].encodedPersonal;
      }
   }

   public InternetAddress(String address, boolean strict) throws AddressException {
      this(address);
      if (strict) {
         if (this.isGroup()) {
            this.getGroup(true);
         } else {
            checkAddress(this.address, true, true);
         }
      }

   }

   public InternetAddress(String address, String personal) throws UnsupportedEncodingException {
      this(address, personal, (String)null);
   }

   public InternetAddress(String address, String personal, String charset) throws UnsupportedEncodingException {
      super();
      this.address = address;
      this.setPersonal(personal, charset);
   }

   public Object clone() {
      InternetAddress a = null;

      try {
         a = (InternetAddress)super.clone();
      } catch (CloneNotSupportedException var3) {
      }

      return a;
   }

   public String getType() {
      return "rfc822";
   }

   public void setAddress(String address) {
      this.address = address;
   }

   public void setPersonal(String name, String charset) throws UnsupportedEncodingException {
      this.personal = name;
      if (name != null) {
         this.encodedPersonal = MimeUtility.encodeWord(name, charset, (String)null);
      } else {
         this.encodedPersonal = null;
      }

   }

   public void setPersonal(String name) throws UnsupportedEncodingException {
      this.personal = name;
      if (name != null) {
         this.encodedPersonal = MimeUtility.encodeWord(name);
      } else {
         this.encodedPersonal = null;
      }

   }

   public String getAddress() {
      return this.address;
   }

   public String getPersonal() {
      if (this.personal != null) {
         return this.personal;
      } else if (this.encodedPersonal != null) {
         try {
            this.personal = MimeUtility.decodeText(this.encodedPersonal);
            return this.personal;
         } catch (Exception var2) {
            return this.encodedPersonal;
         }
      } else {
         return null;
      }
   }

   public String toString() {
      if (this.encodedPersonal == null && this.personal != null) {
         try {
            this.encodedPersonal = MimeUtility.encodeWord(this.personal);
         } catch (UnsupportedEncodingException var2) {
         }
      }

      if (this.encodedPersonal != null) {
         return quotePhrase(this.encodedPersonal) + " <" + this.address + ">";
      } else {
         return !this.isGroup() && !this.isSimple() ? "<" + this.address + ">" : this.address;
      }
   }

   public String toUnicodeString() {
      String p = this.getPersonal();
      if (p != null) {
         return quotePhrase(p) + " <" + this.address + ">";
      } else {
         return !this.isGroup() && !this.isSimple() ? "<" + this.address + ">" : this.address;
      }
   }

   private static String quotePhrase(String phrase) {
      int len = phrase.length();
      boolean needQuoting = false;

      for(int i = 0; i < len; ++i) {
         char c = phrase.charAt(i);
         if (c == '"' || c == '\\') {
            StringBuffer sb = new StringBuffer(len + 3);
            sb.append('"');

            for(int j = 0; j < len; ++j) {
               char cc = phrase.charAt(j);
               if (cc == '"' || cc == '\\') {
                  sb.append('\\');
               }

               sb.append(cc);
            }

            sb.append('"');
            return sb.toString();
         }

         if (c < ' ' && c != '\r' && c != '\n' && c != '\t' || c >= 127 || rfc822phrase.indexOf(c) >= 0) {
            needQuoting = true;
         }
      }

      if (needQuoting) {
         StringBuffer sb = new StringBuffer(len + 2);
         sb.append('"').append(phrase).append('"');
         return sb.toString();
      } else {
         return phrase;
      }
   }

   private static String unquote(String s) {
      if (s.startsWith("\"") && s.endsWith("\"") && s.length() > 1) {
         s = s.substring(1, s.length() - 1);
         if (s.indexOf(92) >= 0) {
            StringBuffer sb = new StringBuffer(s.length());

            for(int i = 0; i < s.length(); ++i) {
               char c = s.charAt(i);
               if (c == '\\' && i < s.length() - 1) {
                  ++i;
                  c = s.charAt(i);
               }

               sb.append(c);
            }

            s = sb.toString();
         }
      }

      return s;
   }

   public boolean equals(Object a) {
      if (!(a instanceof InternetAddress)) {
         return false;
      } else {
         String s = ((InternetAddress)a).getAddress();
         if (s == this.address) {
            return true;
         } else {
            return this.address != null && this.address.equalsIgnoreCase(s);
         }
      }
   }

   public int hashCode() {
      return this.address == null ? 0 : this.address.toLowerCase(Locale.ENGLISH).hashCode();
   }

   public static String toString(Address[] addresses) {
      return toString(addresses, 0);
   }

   public static String toString(Address[] addresses, int used) {
      if (addresses != null && addresses.length != 0) {
         StringBuffer sb = new StringBuffer();

         for(int i = 0; i < addresses.length; ++i) {
            if (i != 0) {
               sb.append(", ");
               used += 2;
            }

            String s = addresses[i].toString();
            int len = lengthOfFirstSegment(s);
            if (used + len > 76) {
               sb.append("\r\n\t");
               used = 8;
            }

            sb.append(s);
            used = lengthOfLastSegment(s, used);
         }

         return sb.toString();
      } else {
         return null;
      }
   }

   private static int lengthOfFirstSegment(String s) {
      int pos;
      return (pos = s.indexOf("\r\n")) != -1 ? pos : s.length();
   }

   private static int lengthOfLastSegment(String s, int used) {
      int pos;
      return (pos = s.lastIndexOf("\r\n")) != -1 ? s.length() - pos - 2 : s.length() + used;
   }

   public static InternetAddress getLocalAddress(Session session) {
      try {
         return _getLocalAddress(session);
      } catch (SecurityException var2) {
      } catch (AddressException var3) {
      } catch (UnknownHostException var4) {
      }

      return null;
   }

   static InternetAddress _getLocalAddress(Session session) throws SecurityException, AddressException, UnknownHostException {
      String user = null;
      String host = null;
      String address = null;
      if (session == null) {
         user = System.getProperty("user.name");
         host = getLocalHostName();
      } else {
         address = session.getProperty("mail.from");
         if (address == null) {
            user = session.getProperty("mail.user");
            if (user == null || user.length() == 0) {
               user = session.getProperty("user.name");
            }

            if (user == null || user.length() == 0) {
               user = System.getProperty("user.name");
            }

            host = session.getProperty("mail.host");
            if (host == null || host.length() == 0) {
               host = getLocalHostName();
            }
         }
      }

      if (address == null && user != null && user.length() != 0 && host != null && host.length() != 0) {
         address = MimeUtility.quote(user.trim(), "()<>,;:\\\"[]@\t ") + "@" + host;
      }

      return address == null ? null : new InternetAddress(address);
   }

   private static String getLocalHostName() throws UnknownHostException {
      String host = null;
      InetAddress me = InetAddress.getLocalHost();
      if (me != null) {
         host = me.getHostName();
         if (host != null && host.length() > 0 && isInetAddressLiteral(host)) {
            host = '[' + host + ']';
         }
      }

      return host;
   }

   private static boolean isInetAddressLiteral(String addr) {
      boolean sawHex = false;
      boolean sawColon = false;

      for(int i = 0; i < addr.length(); ++i) {
         char c = addr.charAt(i);
         if ((c < '0' || c > '9') && c != '.') {
            if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z')) {
               if (c != ':') {
                  return false;
               }

               sawColon = true;
            } else {
               sawHex = true;
            }
         }
      }

      if (sawHex && !sawColon) {
         return false;
      } else {
         return true;
      }
   }

   public static InternetAddress[] parse(String addresslist) throws AddressException {
      return parse(addresslist, true);
   }

   public static InternetAddress[] parse(String addresslist, boolean strict) throws AddressException {
      return parse(addresslist, strict, false);
   }

   public static InternetAddress[] parseHeader(String addresslist, boolean strict) throws AddressException {
      return parse(addresslist, strict, true);
   }

   private static InternetAddress[] parse(String param0, boolean param1, boolean param2) throws AddressException {
      // $FF: Couldn't be decompiled
   }

   public void validate() throws AddressException {
      if (this.isGroup()) {
         this.getGroup(true);
      } else {
         checkAddress(this.getAddress(), true, true);
      }

   }

   private static void checkAddress(String addr, boolean routeAddr, boolean validate) throws AddressException {
      int start = 0;
      int len = addr.length();
      if (len == 0) {
         throw new AddressException("Empty address", addr);
      } else {
         int i;
         if (routeAddr && addr.charAt(0) == '@') {
            for(start = 0; (i = indexOfAny(addr, ",:", start)) >= 0; start = i + 1) {
               if (addr.charAt(start) != '@') {
                  throw new AddressException("Illegal route-addr", addr);
               }

               if (addr.charAt(i) == ':') {
                  start = i + 1;
                  break;
               }
            }
         }

         char c = '\uffff';
         char lastc = '\uffff';
         boolean inquote = false;
         i = start;

         while(true) {
            label156: {
               if (i < len) {
                  lastc = c;
                  c = addr.charAt(i);
                  if (c == '\\' || lastc == '\\') {
                     break label156;
                  }

                  if (c == '"') {
                     if (inquote) {
                        if (validate && i + 1 < len && addr.charAt(i + 1) != '@') {
                           throw new AddressException("Quote not at end of local address", addr);
                        }

                        inquote = false;
                     } else {
                        if (validate && i != 0) {
                           throw new AddressException("Quote not at start of local address", addr);
                        }

                        inquote = true;
                     }
                     break label156;
                  }

                  if (inquote) {
                     break label156;
                  }

                  if (c != '@') {
                     if (c <= ' ' || c >= 127) {
                        throw new AddressException("Local address contains control or whitespace", addr);
                     }

                     if ("()<>,;:\\\"[]@".indexOf(c) >= 0) {
                        throw new AddressException("Local address contains illegal character", addr);
                     }
                     break label156;
                  }

                  if (i == 0) {
                     throw new AddressException("Missing local name", addr);
                  }
               }

               if (inquote) {
                  throw new AddressException("Unterminated quote", addr);
               }

               if (c != '@') {
                  if (validate) {
                     throw new AddressException("Missing final '@domain'", addr);
                  }

                  return;
               }

               start = i + 1;
               if (start >= len) {
                  throw new AddressException("Missing domain", addr);
               }

               if (addr.charAt(start) == '.') {
                  throw new AddressException("Domain starts with dot", addr);
               }

               for(int i = start; i < len; ++i) {
                  c = addr.charAt(i);
                  if (c == '[') {
                     return;
                  }

                  if (c <= ' ' || c >= 127) {
                     throw new AddressException("Domain contains control or whitespace", addr);
                  }

                  if (!Character.isLetterOrDigit(c) && c != '-' && c != '.') {
                     throw new AddressException("Domain contains illegal character", addr);
                  }

                  if (c == '.' && lastc == '.') {
                     throw new AddressException("Domain contains dot-dot", addr);
                  }

                  lastc = c;
               }

               if (lastc == '.') {
                  throw new AddressException("Domain ends with dot", addr);
               }

               return;
            }

            ++i;
         }
      }
   }

   private boolean isSimple() {
      return this.address == null || indexOfAny(this.address, "()<>,;:\\\"[]") < 0;
   }

   public boolean isGroup() {
      return this.address != null && this.address.endsWith(";") && this.address.indexOf(58) > 0;
   }

   public InternetAddress[] getGroup(boolean strict) throws AddressException {
      String addr = this.getAddress();
      if (!addr.endsWith(";")) {
         return null;
      } else {
         int ix = addr.indexOf(58);
         if (ix < 0) {
            return null;
         } else {
            String list = addr.substring(ix + 1, addr.length() - 1);
            return parseHeader(list, strict);
         }
      }
   }

   private static int indexOfAny(String s, String any) {
      return indexOfAny(s, any, 0);
   }

   private static int indexOfAny(String s, String any, int start) {
      try {
         int len = s.length();

         for(int i = start; i < len; ++i) {
            if (any.indexOf(s.charAt(i)) >= 0) {
               return i;
            }
         }

         return -1;
      } catch (StringIndexOutOfBoundsException var5) {
         return -1;
      }
   }
}
