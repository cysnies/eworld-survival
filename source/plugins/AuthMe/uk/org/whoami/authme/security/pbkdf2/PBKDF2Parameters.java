package uk.org.whoami.authme.security.pbkdf2;

public class PBKDF2Parameters {
   protected byte[] salt;
   protected int iterationCount;
   protected String hashAlgorithm;
   protected String hashCharset;
   protected byte[] derivedKey;

   public PBKDF2Parameters() {
      super();
      this.hashAlgorithm = null;
      this.hashCharset = "UTF-8";
      this.salt = null;
      this.iterationCount = 1000;
      this.derivedKey = null;
   }

   public PBKDF2Parameters(String hashAlgorithm, String hashCharset, byte[] salt, int iterationCount) {
      super();
      this.hashAlgorithm = hashAlgorithm;
      this.hashCharset = hashCharset;
      this.salt = salt;
      this.iterationCount = iterationCount;
      this.derivedKey = null;
   }

   public PBKDF2Parameters(String hashAlgorithm, String hashCharset, byte[] salt, int iterationCount, byte[] derivedKey) {
      super();
      this.hashAlgorithm = hashAlgorithm;
      this.hashCharset = hashCharset;
      this.salt = salt;
      this.iterationCount = iterationCount;
      this.derivedKey = derivedKey;
   }

   public int getIterationCount() {
      return this.iterationCount;
   }

   public void setIterationCount(int iterationCount) {
      this.iterationCount = iterationCount;
   }

   public byte[] getSalt() {
      return this.salt;
   }

   public void setSalt(byte[] salt) {
      this.salt = salt;
   }

   public byte[] getDerivedKey() {
      return this.derivedKey;
   }

   public void setDerivedKey(byte[] derivedKey) {
      this.derivedKey = derivedKey;
   }

   public String getHashAlgorithm() {
      return this.hashAlgorithm;
   }

   public void setHashAlgorithm(String hashAlgorithm) {
      this.hashAlgorithm = hashAlgorithm;
   }

   public String getHashCharset() {
      return this.hashCharset;
   }

   public void setHashCharset(String hashCharset) {
      this.hashCharset = hashCharset;
   }
}
