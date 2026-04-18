package uk.org.whoami.authme.security;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.security.pbkdf2.BinTools;
import uk.org.whoami.authme.security.pbkdf2.PBKDF2Engine;
import uk.org.whoami.authme.security.pbkdf2.PBKDF2Parameters;
import uk.org.whoami.authme.settings.Settings;

public class PasswordSecurity {
   private static SecureRandom rnd = new SecureRandom();
   public static HashMap userSalt = new HashMap();
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$uk$org$whoami$authme$security$PasswordSecurity$HashAlgorithm;

   public PasswordSecurity() {
      super();
   }

   private static String getMD5(String message) throws NoSuchAlgorithmException {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      md5.reset();
      md5.update(message.getBytes());
      byte[] digest = md5.digest();
      return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest));
   }

   private static String getSHA1(String message) throws NoSuchAlgorithmException {
      MessageDigest sha1 = MessageDigest.getInstance("SHA1");
      sha1.reset();
      sha1.update(message.getBytes());
      byte[] digest = sha1.digest();
      return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest));
   }

   private static String getSHA256(String message) throws NoSuchAlgorithmException {
      MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
      sha256.reset();
      sha256.update(message.getBytes());
      byte[] digest = sha256.digest();
      return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest));
   }

   private static String getSHA512(String message) throws NoSuchAlgorithmException {
      MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
      sha512.reset();
      sha512.update(message.getBytes());
      byte[] digest = sha512.digest();
      return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest));
   }

   public static String getWhirlpool(String message) {
      Whirlpool w = new Whirlpool();
      byte[] digest = new byte[64];
      w.NESSIEinit();
      w.NESSIEadd(message);
      w.NESSIEfinalize(digest);
      return Whirlpool.display(digest);
   }

   private static String getSaltedHash(String message, String salt) throws NoSuchAlgorithmException {
      return "$SHA$" + salt + "$" + getSHA256(getSHA256(message) + salt);
   }

   private static String getSaltedMd5(String message, String salt) throws NoSuchAlgorithmException {
      return "$MD5vb$" + salt + "$" + getMD5(getMD5(message) + salt);
   }

   private static String getSaltedMyBB(String message, String salt) throws NoSuchAlgorithmException {
      return getMD5(getMD5(salt) + getMD5(message));
   }

   private static String getXAuth(String message, String salt) {
      String hash = getWhirlpool(salt + message).toLowerCase();
      int saltPos = message.length() >= hash.length() ? hash.length() - 1 : message.length();
      return hash.substring(0, saltPos) + salt + hash.substring(saltPos);
   }

   private static String getSaltedIPB3(String message, String salt) throws NoSuchAlgorithmException {
      return getMD5(getMD5(salt) + getMD5(message));
   }

   private static String getBCrypt(String message, String salt) throws NoSuchAlgorithmException {
      return BCrypt.hashpw(message, salt);
   }

   private static String getWBB3(String message, String salt) throws NoSuchAlgorithmException {
      return getSHA1(salt.concat(getSHA1(salt.concat(getSHA1(message)))));
   }

   private static String getPBKDF2(String password, String salt) throws NoSuchAlgorithmException {
      String result = "pbkdf2_sha256$10000$" + salt + "$";
      PBKDF2Parameters params = new PBKDF2Parameters("HmacSHA256", "ASCII", BinTools.hex2bin(salt), 10000);
      PBKDF2Engine engine = new PBKDF2Engine(params);
      return result + BinTools.bin2hex(engine.deriveKey(password, 64));
   }

   private static String createSalt(int length) throws NoSuchAlgorithmException {
      byte[] msg = new byte[40];
      rnd.nextBytes(msg);
      MessageDigest sha1 = MessageDigest.getInstance("SHA1");
      sha1.reset();
      byte[] digest = sha1.digest(msg);
      return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest)).substring(0, length);
   }

   public static String getHash(HashAlgorithm alg, String password, String name) throws NoSuchAlgorithmException {
      switch (alg) {
         case MD5:
            return getMD5(password);
         case SHA1:
            return getSHA1(password);
         case SHA256:
            String salt = createSalt(16);
            return getSaltedHash(password, salt);
         case WHIRLPOOL:
            return getWhirlpool(password);
         case XAUTH:
            String xsalt = createSalt(12);
            return getXAuth(password, xsalt);
         case MD5VB:
            String salt2 = createSalt(16);
            return getSaltedMd5(password, salt2);
         case PHPBB:
            return getPhpBB(password);
         case PLAINTEXT:
            return getPlainText(password);
         case MYBB:
            String salt3 = "";

            try {
               salt3 = AuthMe.getInstance().database.getAuth(name).getSalt();
            } catch (NullPointerException var23) {
            }

            if (salt3.isEmpty() || salt3 == null) {
               salt3 = createSalt(8);
               userSalt.put(name, salt3);
            }

            return getSaltedMyBB(password, salt3);
         case IPB3:
            String salt4 = "";

            try {
               salt4 = AuthMe.getInstance().database.getAuth(name).getSalt();
            } catch (NullPointerException var22) {
            }

            if (salt4.isEmpty() || salt4 == null) {
               salt4 = createSalt(5);
               userSalt.put(name, salt4);
            }

            return getSaltedIPB3(password, salt4);
         case PHPFUSION:
            String salt5 = "";

            try {
               salt5 = AuthMe.getInstance().database.getAuth(name).getSalt();
            } catch (NullPointerException var21) {
            }

            if (salt5.isEmpty() || salt5 == null) {
               salt5 = createSalt(12);
               userSalt.put(name, getSHA1(salt5));
            }

            return getPhPFusion(password, getSHA1(salt5));
         case SMF:
            return getSHA1(name.toLowerCase() + password);
         case XFSHA1:
            return getSHA1(getSHA1(password) + Settings.getPredefinedSalt);
         case XFSHA256:
            return getSHA256(getSHA256(password) + Settings.getPredefinedSalt);
         case SALTED2MD5:
            String salt6 = "";

            try {
               salt6 = AuthMe.getInstance().database.getAuth(name).getSalt();
            } catch (NullPointerException var20) {
            }

            if (salt6.isEmpty() || salt6 == null) {
               salt6 = createSalt(Settings.saltLength);
               userSalt.put(name, salt6);
            }

            return getMD5(getMD5(password) + salt6);
         case JOOMLA:
            String saltj = "";

            try {
               saltj = AuthMe.getInstance().database.getAuth(name).getHash().split(":")[1];
            } catch (NullPointerException var18) {
            } catch (ArrayIndexOutOfBoundsException var19) {
            }

            if (saltj.isEmpty() || saltj == null) {
               saltj = createSalt(32);
               userSalt.put(name, saltj);
            }

            return getMD5(password + saltj) + ":" + saltj;
         case BCRYPT:
            String saltbcrypt = "";

            try {
               saltbcrypt = AuthMe.getInstance().database.getAuth(name).getSalt();
            } catch (NullPointerException var16) {
            } catch (ArrayIndexOutOfBoundsException var17) {
            }

            if (saltbcrypt.isEmpty() || saltbcrypt == null) {
               saltbcrypt = BCrypt.gensalt(Settings.bCryptLog2Rounds);
               userSalt.put(name, saltbcrypt);
            }

            return getBCrypt(password, saltbcrypt);
         case WBB3:
            String saltwbb = "";

            try {
               String saltbcrypt = AuthMe.getInstance().database.getAuth(name).getSalt();
            } catch (NullPointerException var14) {
            } catch (ArrayIndexOutOfBoundsException var15) {
            }

            if (saltwbb.isEmpty() || saltwbb == null) {
               saltwbb = createSalt(40);
               userSalt.put(name, saltwbb);
            }

            return getWBB3(password, saltwbb);
         case SHA512:
            return getSHA512(password);
         case DOUBLEMD5:
            return getMD5(getMD5(password));
         case PBKDF2:
            String saltpbkdf2 = createSalt(12);
            return getPBKDF2(password, saltpbkdf2);
         default:
            throw new NoSuchAlgorithmException("Unknown hash algorithm");
      }
   }

   public static boolean comparePasswordWithHash(String password, String hash, String playername) throws NoSuchAlgorithmException {
      if (hash.contains("$H$")) {
         PhpBB checkHash = new PhpBB();
         return checkHash.phpbb_check_hash(password, hash);
      } else if (!Settings.getMySQLColumnSalt.isEmpty() && Settings.getPasswordHash == PasswordSecurity.HashAlgorithm.WBB3) {
         String salt = AuthMe.getInstance().database.getAuth(playername).getSalt();
         return hash.equals(getWBB3(password, salt));
      } else if (!Settings.getMySQLColumnSalt.isEmpty() && Settings.getPasswordHash == PasswordSecurity.HashAlgorithm.IPB3) {
         String salt = AuthMe.getInstance().database.getAuth(playername).getSalt();
         return hash.equals(getSaltedIPB3(password, salt));
      } else if (!Settings.getMySQLColumnSalt.isEmpty() && Settings.getPasswordHash == PasswordSecurity.HashAlgorithm.BCRYPT) {
         String salt = AuthMe.getInstance().database.getAuth(playername).getSalt();
         return hash.equals(BCrypt.hashpw(password, salt));
      } else if (!Settings.getMySQLColumnSalt.isEmpty() && Settings.getPasswordHash == PasswordSecurity.HashAlgorithm.PHPFUSION) {
         String salt = AuthMe.getInstance().database.getAuth(playername).getSalt();
         return hash.equals(getPhPFusion(password, salt));
      } else if (!Settings.getMySQLColumnSalt.isEmpty() && Settings.getPasswordHash == PasswordSecurity.HashAlgorithm.MYBB) {
         String salt = AuthMe.getInstance().database.getAuth(playername).getSalt();
         return hash.equals(getSaltedMyBB(password, salt));
      } else if (Settings.getPasswordHash == PasswordSecurity.HashAlgorithm.SMF) {
         return hash.equals(getSHA1(playername.toLowerCase() + password));
      } else if (Settings.getPasswordHash == PasswordSecurity.HashAlgorithm.XFSHA1) {
         return hash.equals(getSHA1(getSHA1(password) + Settings.getPredefinedSalt));
      } else if (Settings.getPasswordHash == PasswordSecurity.HashAlgorithm.XFSHA256) {
         return hash.equals(getSHA256(getSHA256(password) + Settings.getPredefinedSalt));
      } else if (Settings.getPasswordHash == PasswordSecurity.HashAlgorithm.DOUBLEMD5) {
         return hash.equals(getMD5(getMD5(password)));
      } else if (!Settings.getMySQLColumnSalt.isEmpty() && Settings.getPasswordHash == PasswordSecurity.HashAlgorithm.SALTED2MD5) {
         String salt = AuthMe.getInstance().database.getAuth(playername).getSalt();
         return hash.equals(getMD5(getMD5(password) + salt));
      } else if (Settings.getPasswordHash == PasswordSecurity.HashAlgorithm.JOOMLA) {
         String salt = hash.split(":")[1];
         return hash.equals(getMD5(password + salt) + ":" + salt);
      } else if (Settings.getPasswordHash == PasswordSecurity.HashAlgorithm.SHA512) {
         return hash.equals(getSHA512(password));
      } else if (Settings.getPasswordHash == PasswordSecurity.HashAlgorithm.PBKDF2) {
         String[] line = hash.split("\\$");
         String salt = line[2];
         String derivedKey = line[3];
         PBKDF2Parameters params = new PBKDF2Parameters("HmacSHA256", "ASCII", BinTools.hex2bin(salt), 10000, BinTools.hex2bin(derivedKey));
         PBKDF2Engine engine = new PBKDF2Engine(params);
         return engine.verifyKey(password);
      } else if (hash.length() < 32) {
         return hash.equals(password);
      } else if (hash.length() == 32) {
         return hash.equals(getMD5(password));
      } else if (hash.length() == 40) {
         return hash.equals(getSHA1(password));
      } else if (hash.length() == 140) {
         int saltPos = password.length() >= hash.length() ? hash.length() - 1 : password.length();
         String salt = hash.substring(saltPos, saltPos + 12);
         return hash.equals(getXAuth(password, salt));
      } else {
         if (hash.contains("$")) {
            String[] line = hash.split("\\$");
            if (line.length > 3 && line[1].equals("SHA")) {
               return hash.equals(getSaltedHash(password, line[2]));
            }

            if (line[1].equals("MD5vb")) {
               return hash.equals(getSaltedMd5(password, line[2]));
            }
         }

         return false;
      }
   }

   private static String getPhpBB(String password) {
      PhpBB hash = new PhpBB();
      String phpBBhash = hash.phpbb_hash(password);
      return phpBBhash;
   }

   private static String getPlainText(String password) {
      return password;
   }

   public static String getPhPFusion(String msg, String keyString) {
      String digest = null;
      String algo = "HmacSHA256";

      try {
         SecretKeySpec key = new SecretKeySpec(keyString.getBytes("UTF-8"), algo);
         Mac mac = Mac.getInstance(algo);
         mac.init(key);
         byte[] bytes = mac.doFinal(msg.getBytes("ASCII"));
         StringBuffer hash = new StringBuffer();

         for(int i = 0; i < bytes.length; ++i) {
            String hex = Integer.toHexString(255 & bytes[i]);
            if (hex.length() == 1) {
               hash.append('0');
            }

            hash.append(hex);
         }

         digest = hash.toString();
      } catch (UnsupportedEncodingException var10) {
      } catch (InvalidKeyException var11) {
      } catch (NoSuchAlgorithmException var12) {
      }

      return digest;
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$uk$org$whoami$authme$security$PasswordSecurity$HashAlgorithm() {
      int[] var10000 = $SWITCH_TABLE$uk$org$whoami$authme$security$PasswordSecurity$HashAlgorithm;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[PasswordSecurity.HashAlgorithm.values().length];

         try {
            var0[PasswordSecurity.HashAlgorithm.BCRYPT.ordinal()] = 17;
         } catch (NoSuchFieldError var21) {
         }

         try {
            var0[PasswordSecurity.HashAlgorithm.DOUBLEMD5.ordinal()] = 20;
         } catch (NoSuchFieldError var20) {
         }

         try {
            var0[PasswordSecurity.HashAlgorithm.IPB3.ordinal()] = 10;
         } catch (NoSuchFieldError var19) {
         }

         try {
            var0[PasswordSecurity.HashAlgorithm.JOOMLA.ordinal()] = 16;
         } catch (NoSuchFieldError var18) {
         }

         try {
            var0[PasswordSecurity.HashAlgorithm.MD5.ordinal()] = 1;
         } catch (NoSuchFieldError var17) {
         }

         try {
            var0[PasswordSecurity.HashAlgorithm.MD5VB.ordinal()] = 6;
         } catch (NoSuchFieldError var16) {
         }

         try {
            var0[PasswordSecurity.HashAlgorithm.MYBB.ordinal()] = 9;
         } catch (NoSuchFieldError var15) {
         }

         try {
            var0[PasswordSecurity.HashAlgorithm.PBKDF2.ordinal()] = 21;
         } catch (NoSuchFieldError var14) {
         }

         try {
            var0[PasswordSecurity.HashAlgorithm.PHPBB.ordinal()] = 7;
         } catch (NoSuchFieldError var13) {
         }

         try {
            var0[PasswordSecurity.HashAlgorithm.PHPFUSION.ordinal()] = 11;
         } catch (NoSuchFieldError var12) {
         }

         try {
            var0[PasswordSecurity.HashAlgorithm.PLAINTEXT.ordinal()] = 8;
         } catch (NoSuchFieldError var11) {
         }

         try {
            var0[PasswordSecurity.HashAlgorithm.SALTED2MD5.ordinal()] = 15;
         } catch (NoSuchFieldError var10) {
         }

         try {
            var0[PasswordSecurity.HashAlgorithm.SHA1.ordinal()] = 2;
         } catch (NoSuchFieldError var9) {
         }

         try {
            var0[PasswordSecurity.HashAlgorithm.SHA256.ordinal()] = 3;
         } catch (NoSuchFieldError var8) {
         }

         try {
            var0[PasswordSecurity.HashAlgorithm.SHA512.ordinal()] = 19;
         } catch (NoSuchFieldError var7) {
         }

         try {
            var0[PasswordSecurity.HashAlgorithm.SMF.ordinal()] = 12;
         } catch (NoSuchFieldError var6) {
         }

         try {
            var0[PasswordSecurity.HashAlgorithm.WBB3.ordinal()] = 18;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[PasswordSecurity.HashAlgorithm.WHIRLPOOL.ordinal()] = 4;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[PasswordSecurity.HashAlgorithm.XAUTH.ordinal()] = 5;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[PasswordSecurity.HashAlgorithm.XFSHA1.ordinal()] = 13;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[PasswordSecurity.HashAlgorithm.XFSHA256.ordinal()] = 14;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$uk$org$whoami$authme$security$PasswordSecurity$HashAlgorithm = var0;
         return var0;
      }
   }

   public static enum HashAlgorithm {
      MD5,
      SHA1,
      SHA256,
      WHIRLPOOL,
      XAUTH,
      MD5VB,
      PHPBB,
      PLAINTEXT,
      MYBB,
      IPB3,
      PHPFUSION,
      SMF,
      XFSHA1,
      XFSHA256,
      SALTED2MD5,
      JOOMLA,
      BCRYPT,
      WBB3,
      SHA512,
      DOUBLEMD5,
      PBKDF2;

      private HashAlgorithm() {
      }
   }
}
