package com.onarandombox.MultiverseCore.api;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public interface MultiverseCoreConfig extends ConfigurationSerializable {
   boolean setConfigProperty(String var1, String var2);

   void setTeleportCooldown(int var1);

   int getTeleportCooldown();

   void setFirstSpawnWorld(String var1);

   String getFirstSpawnWorld();

   void setVersion(int var1);

   double getVersion();

   void setMessageCooldown(int var1);

   int getMessageCooldown();

   void setGlobalDebug(int var1);

   int getGlobalDebug();

   void setDisplayPermErrors(boolean var1);

   boolean getDisplayPermErrors();

   void setFirstSpawnOverride(boolean var1);

   boolean getFirstSpawnOverride();

   void setTeleportIntercept(boolean var1);

   boolean getTeleportIntercept();

   void setPrefixChat(boolean var1);

   boolean getPrefixChat();

   void setEnforceAccess(boolean var1);

   boolean getEnforceAccess();

   void setUseAsyncChat(boolean var1);

   boolean getUseAsyncChat();

   void setSilentStart(boolean var1);

   boolean getSilentStart();

   void setUseDefaultPortalSearch(boolean var1);

   boolean isUsingDefaultPortalSearch();

   void setPortalSearchRadius(int var1);

   int getPortalSearchRadius();
}
