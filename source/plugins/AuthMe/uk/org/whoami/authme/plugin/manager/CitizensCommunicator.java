package uk.org.whoami.authme.plugin.manager;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.CitizensManager;
import org.bukkit.entity.Entity;
import uk.org.whoami.authme.AuthMe;

public class CitizensCommunicator {
   public AuthMe instance;

   public CitizensCommunicator(AuthMe instance) {
      super();
      this.instance = instance;
   }

   public boolean isNPC(Entity player, AuthMe instance) {
      try {
         if (instance.CitizensVersion == 1) {
            return CitizensManager.isNPC(player);
         } else {
            return instance.CitizensVersion == 2 ? CitizensAPI.getNPCRegistry().isNPC(player) : false;
         }
      } catch (NoClassDefFoundError var4) {
         return false;
      } catch (NullPointerException var5) {
         return false;
      }
   }
}
