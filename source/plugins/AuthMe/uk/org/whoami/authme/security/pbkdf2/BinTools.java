package uk.org.whoami.authme.security.pbkdf2;

public class BinTools {
   public static final String hex = "0123456789ABCDEF";

   public BinTools() {
      super();
   }

   public static String bin2hex(byte[] b) {
      if (b == null) {
         return "";
      } else {
         StringBuffer sb = new StringBuffer(2 * b.length);

         for(int i = 0; i < b.length; ++i) {
            int v = (256 + b[i]) % 256;
            sb.append("0123456789ABCDEF".charAt(v / 16 & 15));
            sb.append("0123456789ABCDEF".charAt(v % 16 & 15));
         }

         return sb.toString();
      }
   }

   public static byte[] hex2bin(String s) {
      String m = s;
      if (s == null) {
         m = "";
      } else if (s.length() % 2 != 0) {
         m = "0" + s;
      }

      byte[] r = new byte[m.length() / 2];
      int i = 0;

      for(int n = 0; i < m.length(); ++n) {
         char h = m.charAt(i++);
         char l = m.charAt(i++);
         r[n] = (byte)(hex2bin(h) * 16 + hex2bin(l));
      }

      return r;
   }

   public static int hex2bin(char c) {
      if (c >= '0' && c <= '9') {
         return c - 48;
      } else if (c >= 'A' && c <= 'F') {
         return c - 65 + 10;
      } else if (c >= 'a' && c <= 'f') {
         return c - 97 + 10;
      } else {
         throw new IllegalArgumentException("Input string may only contain hex digits, but found '" + c + "'");
      }
   }

   public static void main(String[] args) {
      byte[] b = new byte[256];
      byte bb = 0;

      for(int i = 0; i < 256; ++i) {
         b[i] = bb++;
      }

      String s = bin2hex(b);
      byte[] c = hex2bin(s);
      String t = bin2hex(c);
      if (!s.equals(t)) {
         throw new AssertionError("Mismatch");
      }
   }
}
