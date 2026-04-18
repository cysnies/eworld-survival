package uk.org.whoami.authme.security.pbkdf2;

public class PBKDF2HexFormatter implements PBKDF2Formatter {
   public PBKDF2HexFormatter() {
      super();
   }

   public boolean fromString(PBKDF2Parameters p, String s) {
      if (p != null && s != null) {
         String[] p123 = s.split(":");
         if (p123 != null && p123.length == 3) {
            byte[] salt = BinTools.hex2bin(p123[0]);
            int iterationCount = Integer.parseInt(p123[1]);
            byte[] bDK = BinTools.hex2bin(p123[2]);
            p.setSalt(salt);
            p.setIterationCount(iterationCount);
            p.setDerivedKey(bDK);
            return false;
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   public String toString(PBKDF2Parameters p) {
      String s = BinTools.bin2hex(p.getSalt()) + ":" + p.getIterationCount() + ":" + BinTools.bin2hex(p.getDerivedKey());
      return s;
   }
}
