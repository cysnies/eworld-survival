package com.onarandombox.MultiverseCore.destination;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.commands.TeleportCommand;
import com.onarandombox.MultiverseCore.utils.PermissionTools;
import com.pneumaticraft.commandhandler.multiverse.Command;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class DestinationFactory {
   private MultiverseCore plugin;
   private Map destList;
   private Command teleportCommand;

   public DestinationFactory(MultiverseCore plugin) {
      super();
      this.plugin = plugin;
      this.destList = new HashMap();

      for(Command c : this.plugin.getCommandHandler().getAllCommands()) {
         if (c instanceof TeleportCommand) {
            this.teleportCommand = c;
         }
      }

   }

   public MVDestination getDestination(String destination) {
      String idenChar = "";
      if (destination.split(":").length > 1) {
         idenChar = destination.split(":")[0];
      }

      if (this.destList.containsKey(idenChar)) {
         Class<? extends MVDestination> myClass = (Class)this.destList.get(idenChar);

         try {
            MVDestination mydest = (MVDestination)myClass.newInstance();
            if (!mydest.isThisType(this.plugin, destination)) {
               return new InvalidDestination();
            }

            mydest.setDestination(this.plugin, destination);
            return mydest;
         } catch (InstantiationException var5) {
         } catch (IllegalAccessException var6) {
         }
      }

      return new InvalidDestination();
   }

   public boolean registerDestinationType(Class c, String identifier) {
      if (this.destList.containsKey(identifier)) {
         return false;
      } else {
         this.destList.put(identifier, c);
         if (identifier.equals("")) {
            identifier = "w";
         }

         Permission self = this.plugin.getServer().getPluginManager().getPermission("multiverse.teleport.self." + identifier);
         Permission other = this.plugin.getServer().getPluginManager().getPermission("multiverse.teleport.other." + identifier);
         PermissionTools pt = new PermissionTools(this.plugin);
         if (self == null) {
            self = new Permission("multiverse.teleport.self." + identifier, "Permission to teleport yourself for the " + identifier + " destination.", PermissionDefault.OP);
            this.plugin.getServer().getPluginManager().addPermission(self);
            pt.addToParentPerms("multiverse.teleport.self." + identifier);
         }

         if (other == null) {
            other = new Permission("multiverse.teleport.other." + identifier, "Permission to teleport others for the " + identifier + " destination.", PermissionDefault.OP);
            this.plugin.getServer().getPluginManager().addPermission(other);
            pt.addToParentPerms("multiverse.teleport.other." + identifier);
         }

         this.teleportCommand.addAdditonalPermission(self);
         this.teleportCommand.addAdditonalPermission(other);
         return true;
      }
   }
}
