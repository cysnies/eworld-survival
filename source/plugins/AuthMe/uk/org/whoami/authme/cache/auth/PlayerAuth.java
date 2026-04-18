package uk.org.whoami.authme.cache.auth;

import uk.org.whoami.authme.security.PasswordSecurity;
import uk.org.whoami.authme.settings.Settings;

public class PlayerAuth {
   private String nickname;
   private String hash;
   private String ip = "198.18.0.1";
   private long lastLogin;
   private int x = 0;
   private int y = 0;
   private int z = 0;
   private String world = "world";
   private String salt = "";
   private String vBhash = null;
   private int groupId;
   private String email = "your@email.com";

   public PlayerAuth(String nickname, String hash, String ip, long lastLogin) {
      super();
      this.nickname = nickname;
      this.hash = hash;
      this.ip = ip;
      this.lastLogin = lastLogin;
   }

   public PlayerAuth(String nickname, String hash, String ip, long lastLogin, String email) {
      super();
      this.nickname = nickname;
      this.hash = hash;
      this.ip = ip;
      this.lastLogin = lastLogin;
      this.email = email;
   }

   public PlayerAuth(String nickname, int x, int y, int z, String world) {
      super();
      this.nickname = nickname;
      this.x = x;
      this.y = y;
      this.z = z;
      this.world = world;
   }

   public PlayerAuth(String nickname, String hash, String ip, long lastLogin, int x, int y, int z, String world, String email) {
      super();
      this.nickname = nickname;
      this.hash = hash;
      this.ip = ip;
      this.lastLogin = lastLogin;
      this.x = x;
      this.y = y;
      this.z = z;
      this.world = world;
      this.email = email;
   }

   public PlayerAuth(String nickname, String hash, String salt, int groupId, String ip, long lastLogin, int x, int y, int z, String world, String email) {
      super();
      this.nickname = nickname;
      this.hash = hash;
      this.ip = ip;
      this.lastLogin = lastLogin;
      this.x = x;
      this.y = y;
      this.z = z;
      this.world = world;
      this.salt = salt;
      this.groupId = groupId;
      this.email = email;
   }

   public PlayerAuth(String nickname, String hash, String salt, int groupId, String ip, long lastLogin) {
      super();
      this.nickname = nickname;
      this.hash = hash;
      this.ip = ip;
      this.lastLogin = lastLogin;
      this.salt = salt;
      this.groupId = groupId;
   }

   public PlayerAuth(String nickname, String hash, String salt, String ip, long lastLogin) {
      super();
      this.nickname = nickname;
      this.hash = hash;
      this.ip = ip;
      this.lastLogin = lastLogin;
      this.salt = salt;
   }

   public PlayerAuth(String nickname, String hash, String salt, String ip, long lastLogin, int x, int y, int z, String world, String email) {
      super();
      this.nickname = nickname;
      this.hash = hash;
      this.ip = ip;
      this.lastLogin = lastLogin;
      this.x = x;
      this.y = y;
      this.z = z;
      this.world = world;
      this.salt = salt;
      this.email = email;
   }

   public PlayerAuth(String nickname, String hash, String ip, long lastLogin, int x, int y, int z, String world) {
      super();
      this.nickname = nickname;
      this.hash = hash;
      this.ip = ip;
      this.lastLogin = lastLogin;
      this.x = x;
      this.y = y;
      this.z = z;
      this.world = world;
      this.email = "your@email.com";
   }

   public String getIp() {
      return this.ip;
   }

   public String getNickname() {
      return this.nickname;
   }

   public String getHash() {
      if (!this.salt.isEmpty() && Settings.getPasswordHash == PasswordSecurity.HashAlgorithm.MD5VB) {
         this.vBhash = "$MD5vb$" + this.salt + "$" + this.hash;
         return this.vBhash;
      } else {
         return this.hash;
      }
   }

   public String getSalt() {
      return this.salt;
   }

   public int getGroupId() {
      return this.groupId;
   }

   public int getQuitLocX() {
      return this.x;
   }

   public int getQuitLocY() {
      return this.y;
   }

   public int getQuitLocZ() {
      return this.z;
   }

   public String getEmail() {
      return this.email;
   }

   public void setQuitLocX(int x) {
      this.x = x;
   }

   public void setQuitLocY(int y) {
      this.y = y;
   }

   public void setQuitLocZ(int z) {
      this.z = z;
   }

   public long getLastLogin() {
      return this.lastLogin;
   }

   public void setHash(String hash) {
      this.hash = hash;
   }

   public void setIp(String ip) {
      this.ip = ip;
   }

   public void setLastLogin(long lastLogin) {
      this.lastLogin = lastLogin;
   }

   public void setEmail(String email) {
      this.email = email;
   }

   public void setSalt(String salt) {
      this.salt = salt;
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof PlayerAuth)) {
         return false;
      } else {
         PlayerAuth other = (PlayerAuth)obj;
         return other.getIp().equals(this.ip) && other.getNickname().equals(this.nickname);
      }
   }

   public int hashCode() {
      int hashCode = 7;
      hashCode = 71 * hashCode + (this.nickname != null ? this.nickname.hashCode() : 0);
      hashCode = 71 * hashCode + (this.ip != null ? this.ip.hashCode() : 0);
      return hashCode;
   }

   public void setWorld(String world) {
      this.world = world;
   }

   public String getWorld() {
      return this.world;
   }

   public String toString() {
      String s = "Player : " + this.nickname + " ! IP : " + this.ip + " ! LastLogin : " + this.lastLogin + " ! LastPosition : " + this.x + "," + this.y + "," + this.z + "," + this.world + " ! Email : " + this.email + " ! Hash : " + this.hash + " ! Salt : " + this.salt;
      return s;
   }
}
