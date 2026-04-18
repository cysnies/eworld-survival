package com.earth2me.essentials.perm;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.plugin.Plugin;
import org.tyrannyofheaven.bukkit.zPermissions.ZPermissionsService;

public class ZPermissionsHandler extends SuperpermsHandler implements Listener {
   private ZPermissionsService service = null;
   private boolean hasGetPlayerPrimaryGroup = false;

   public ZPermissionsHandler(Plugin plugin) {
      super();
      this.acquireZPermissionsService();
      if (!this.isReady()) {
         Bukkit.getPluginManager().registerEvents(this, plugin);
      }

   }

   @EventHandler
   public void onServiceRegister(ServiceRegisterEvent event) {
      if (ZPermissionsService.class.equals(event.getProvider().getService())) {
         this.acquireZPermissionsService();
      }

   }

   public String getGroup(Player base) {
      return !this.isReady() ? super.getGroup(base) : this.getPrimaryGroup(base.getName());
   }

   public List getGroups(Player base) {
      return (List)(!this.isReady() ? super.getGroups(base) : new ArrayList(this.service.getPlayerGroups(base.getName())));
   }

   public boolean inGroup(Player base, String group) {
      if (!this.isReady()) {
         return super.inGroup(base, group);
      } else {
         for(String test : this.service.getPlayerGroups(base.getName())) {
            if (test.equalsIgnoreCase(group)) {
               return true;
            }
         }

         return false;
      }
   }

   public String getPrefix(Player base) {
      return !this.isReady() ? super.getPrefix(base) : this.getPrefixSuffix(base, "prefix");
   }

   public String getSuffix(Player base) {
      return !this.isReady() ? super.getSuffix(base) : this.getPrefixSuffix(base, "suffix");
   }

   public boolean canBuild(Player base, String group) {
      return this.hasPermission(base, "essentials.build");
   }

   private String getPrefixSuffix(Player base, String metadataName) {
      String playerPrefixSuffix;
      try {
         playerPrefixSuffix = (String)this.service.getPlayerMetadata(base.getName(), metadataName, String.class);
      } catch (IllegalStateException var6) {
         playerPrefixSuffix = null;
      }

      if (playerPrefixSuffix == null) {
         try {
            return (String)this.service.getGroupMetadata(this.getPrimaryGroup(base.getName()), metadataName, String.class);
         } catch (IllegalStateException var5) {
            return null;
         }
      } else {
         return playerPrefixSuffix;
      }
   }

   private void acquireZPermissionsService() {
      this.service = (ZPermissionsService)Bukkit.getServicesManager().load(ZPermissionsService.class);
      if (this.isReady()) {
         try {
            this.service.getClass().getMethod("getPlayerPrimaryGroup", String.class);
            this.hasGetPlayerPrimaryGroup = true;
         } catch (NoSuchMethodException var2) {
            this.hasGetPlayerPrimaryGroup = false;
         } catch (SecurityException var3) {
            this.hasGetPlayerPrimaryGroup = false;
         }
      }

   }

   private boolean isReady() {
      return this.service != null;
   }

   private String getPrimaryGroup(String playerName) {
      if (this.hasGetPlayerPrimaryGroup) {
         return this.service.getPlayerPrimaryGroup(playerName);
      } else {
         List<String> groups = this.service.getPlayerAssignedGroups(playerName);
         return (String)groups.get(0);
      }
   }
}
