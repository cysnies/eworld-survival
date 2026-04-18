package uk.org.whoami.authme.security.pbkdf2;

public interface PBKDF2Formatter {
   String toString(PBKDF2Parameters var1);

   boolean fromString(PBKDF2Parameters var1, String var2);
}
