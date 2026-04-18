package fr.neatmonster.nocheatplus.updates;

import fr.neatmonster.nocheatplus.config.ConfigFile;

public class Updates {
   public Updates() {
      super();
   }

   public static String isConfigUpToDate(ConfigFile config) {
      Object created = config.get("configversion.created");
      if (created != null && created instanceof Integer) {
         int buildCreated = (Integer)created;
         if (buildCreated < 632) {
            return "Your configuration might be outdated.\nSome settings could have changed, you should regenerate it!";
         } else {
            return buildCreated > 632 ? "Your configuration seems to be created by a newer plugin version.\nSome settings could have changed, you should regenerate it!" : null;
         }
      } else {
         return null;
      }
   }

   public static boolean checkForUpdates(String versionString, int updateTimeout) {
      boolean updateAvailable = false;
      return updateAvailable;
   }
}
