package uk.org.whoami.authme.security.pbkdf2;

public interface PRF {
   void init(byte[] var1);

   byte[] doFinal(byte[] var1);

   int getHLen();
}
