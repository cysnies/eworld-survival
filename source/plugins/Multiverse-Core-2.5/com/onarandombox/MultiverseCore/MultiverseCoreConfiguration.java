package com.onarandombox.MultiverseCore;

import com.onarandombox.MultiverseCore.api.MultiverseCoreConfig;
import com.onarandombox.MultiverseCore.utils.CoreLogging;
import java.util.Map;
import me.main__.util.multiverse.SerializationConfig.NoSuchPropertyException;
import me.main__.util.multiverse.SerializationConfig.Property;
import me.main__.util.multiverse.SerializationConfig.SerializationConfig;

public class MultiverseCoreConfiguration extends SerializationConfig implements MultiverseCoreConfig {
   private static MultiverseCoreConfiguration instance;
   @Property
   private volatile boolean enforceaccess;
   @Property
   private volatile boolean prefixchat;
   @Property
   private volatile boolean useasyncchat;
   @Property
   private volatile boolean teleportintercept;
   @Property
   private volatile boolean firstspawnoverride;
   @Property
   private volatile boolean displaypermerrors;
   @Property
   private volatile int globaldebug;
   @Property
   private volatile boolean silentstart;
   @Property
   private volatile int messagecooldown;
   @Property
   private volatile double version;
   @Property
   private volatile String firstspawnworld;
   @Property
   private volatile int teleportcooldown;
   @Property
   private volatile boolean defaultportalsearch;
   @Property
   private volatile int portalsearchradius;

   public static void setInstance(MultiverseCoreConfiguration instance) {
      MultiverseCoreConfiguration.instance = instance;
   }

   public static boolean isSet() {
      return instance != null;
   }

   public static MultiverseCoreConfiguration getInstance() {
      if (instance == null) {
         throw new IllegalStateException("The instance wasn't set!");
      } else {
         return instance;
      }
   }

   public MultiverseCoreConfiguration() {
      super();
      setInstance(this);
   }

   public MultiverseCoreConfiguration(Map values) {
      super(values);
      setInstance(this);
   }

   protected void setDefaults() {
      this.enforceaccess = false;
      this.useasyncchat = true;
      this.prefixchat = true;
      this.teleportintercept = true;
      this.firstspawnoverride = true;
      this.displaypermerrors = true;
      this.globaldebug = 0;
      this.messagecooldown = 5000;
      this.teleportcooldown = 1000;
      this.version = 2.9;
      this.silentstart = false;
      this.defaultportalsearch = false;
      this.portalsearchradius = 16;
   }

   public boolean setConfigProperty(String property, String value) {
      try {
         return this.setProperty(property, value, true);
      } catch (NoSuchPropertyException var4) {
         return false;
      }
   }

   public boolean getEnforceAccess() {
      return this.enforceaccess;
   }

   public void setEnforceAccess(boolean enforceAccess) {
      this.enforceaccess = enforceAccess;
   }

   public boolean getPrefixChat() {
      return this.prefixchat;
   }

   public void setPrefixChat(boolean prefixChat) {
      this.prefixchat = prefixChat;
   }

   public boolean getTeleportIntercept() {
      return this.teleportintercept;
   }

   public void setTeleportIntercept(boolean teleportIntercept) {
      this.teleportintercept = teleportIntercept;
   }

   public boolean getFirstSpawnOverride() {
      return this.firstspawnoverride;
   }

   public void setFirstSpawnOverride(boolean firstSpawnOverride) {
      this.firstspawnoverride = firstSpawnOverride;
   }

   public boolean getDisplayPermErrors() {
      return this.displaypermerrors;
   }

   public void setDisplayPermErrors(boolean displayPermErrors) {
      this.displaypermerrors = displayPermErrors;
   }

   public int getGlobalDebug() {
      return this.globaldebug;
   }

   public void setGlobalDebug(int globalDebug) {
      this.globaldebug = globalDebug;
      CoreLogging.setDebugLevel(globalDebug);
   }

   public int getMessageCooldown() {
      return this.messagecooldown;
   }

   public void setMessageCooldown(int messageCooldown) {
      this.messagecooldown = messageCooldown;
   }

   public double getVersion() {
      return this.version;
   }

   public void setVersion(int version) {
      this.version = (double)version;
   }

   public String getFirstSpawnWorld() {
      return this.firstspawnworld;
   }

   public void setFirstSpawnWorld(String firstSpawnWorld) {
      this.firstspawnworld = firstSpawnWorld;
   }

   public int getTeleportCooldown() {
      return this.teleportcooldown;
   }

   public void setTeleportCooldown(int teleportCooldown) {
      this.teleportcooldown = teleportCooldown;
   }

   public void setUseAsyncChat(boolean useAsyncChat) {
      this.useasyncchat = useAsyncChat;
   }

   public boolean getUseAsyncChat() {
      return this.useasyncchat;
   }

   public void setSilentStart(boolean silentStart) {
      CoreLogging.setShowingConfig(!silentStart);
      this.silentstart = silentStart;
   }

   public boolean getSilentStart() {
      return this.silentstart;
   }

   public void setUseDefaultPortalSearch(boolean useDefaultPortalSearch) {
      this.defaultportalsearch = useDefaultPortalSearch;
   }

   public boolean isUsingDefaultPortalSearch() {
      return this.defaultportalsearch;
   }

   public void setPortalSearchRadius(int searchRadius) {
      this.portalsearchradius = searchRadius;
   }

   public int getPortalSearchRadius() {
      return this.portalsearchradius;
   }
}
