package uk.org.whoami.authme;

import java.io.File;
import java.io.FileWriter;
import java.util.Random;
import java.util.Scanner;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import uk.org.whoami.authme.events.AuthMeTeleportEvent;
import uk.org.whoami.authme.settings.Settings;

public class Utils {
   private String currentGroup;
   private static Utils singleton;
   private String unLoggedGroup;
   BukkitTask id;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$uk$org$whoami$authme$Utils$groupType;

   public Utils() {
      super();
      this.unLoggedGroup = Settings.getUnloggedinGroup;
   }

   public void setGroup(Player player, groupType group) {
      if (player.isOnline()) {
         if (Settings.isPermissionCheckEnabled) {
            switch (group) {
               case UNREGISTERED:
                  this.currentGroup = AuthMe.permission.getPrimaryGroup(player);
                  AuthMe.permission.playerRemoveGroup(player, this.currentGroup);
                  AuthMe.permission.playerAddGroup(player, Settings.unRegisteredGroup);
                  break;
               case REGISTERED:
                  this.currentGroup = AuthMe.permission.getPrimaryGroup(player);
                  AuthMe.permission.playerRemoveGroup(player, this.currentGroup);
                  AuthMe.permission.playerAddGroup(player, Settings.getRegisteredGroup);
            }

         }
      }
   }

   public String removeAll(Player player) {
      if (!getInstance().useGroupSystem()) {
         return null;
      } else {
         if (!Settings.getJoinPermissions.isEmpty()) {
            this.hasPermOnJoin(player);
         }

         this.currentGroup = AuthMe.permission.getPrimaryGroup(player.getWorld(), player.getName().toString());
         return AuthMe.permission.playerRemoveGroup(player.getWorld(), player.getName().toString(), this.currentGroup) && AuthMe.permission.playerAddGroup(player.getWorld(), player.getName().toString(), this.unLoggedGroup) ? this.currentGroup : null;
      }
   }

   public boolean addNormal(Player player, String group) {
      if (!getInstance().useGroupSystem()) {
         return false;
      } else {
         return AuthMe.permission.playerRemoveGroup(player.getWorld(), player.getName().toString(), this.unLoggedGroup) && AuthMe.permission.playerAddGroup(player.getWorld(), player.getName().toString(), group);
      }
   }

   private String hasPermOnJoin(Player player) {
      for(String permission : Settings.getJoinPermissions) {
         if (AuthMe.permission.playerHas(player, permission)) {
            AuthMe.permission.playerAddTransient(player, permission);
         }
      }

      return null;
   }

   public boolean isUnrestricted(Player player) {
      if (!Settings.getUnrestrictedName.isEmpty() && Settings.getUnrestrictedName != null) {
         return Settings.getUnrestrictedName.contains(player.getName());
      } else {
         return false;
      }
   }

   public static Utils getInstance() {
      singleton = new Utils();
      return singleton;
   }

   private boolean useGroupSystem() {
      return Settings.isPermissionCheckEnabled && !Settings.getUnloggedinGroup.isEmpty();
   }

   public void packCoords(int x, final int y, int z, String w, final Player pl) {
      final World theWorld;
      if (w.equals("unavailableworld")) {
         theWorld = pl.getWorld();
      } else {
         theWorld = Bukkit.getWorld(w);
      }

      if (theWorld == null) {
         theWorld = pl.getWorld();
      }

      Location locat = new Location(theWorld, (double)x, (double)y + 0.6, (double)z);
      final Location loc = locat.getBlock().getLocation();
      Bukkit.getScheduler().scheduleSyncDelayedTask(AuthMe.getInstance(), new Runnable() {
         public void run() {
            AuthMeTeleportEvent tpEvent = new AuthMeTeleportEvent(pl, loc);
            AuthMe.getInstance().getServer().getPluginManager().callEvent(tpEvent);
            if (!tpEvent.isCancelled()) {
               if (!tpEvent.getTo().getChunk().isLoaded()) {
                  tpEvent.getTo().getChunk().load();
               }

               pl.teleport(tpEvent.getTo());
            }

         }
      });
      this.id = Bukkit.getScheduler().runTaskTimer(AuthMe.authme, new Runnable() {
         public void run() {
            int current = (int)pl.getLocation().getY();
            World currentWorld = pl.getWorld();
            if (current != y && theWorld.getName() == currentWorld.getName()) {
               pl.teleport(loc);
            }

         }
      }, 1L, 20L);
      Bukkit.getScheduler().scheduleSyncDelayedTask(AuthMe.authme, new Runnable() {
         public void run() {
            Utils.this.id.cancel();
         }
      }, 60L);
   }

   public boolean obtainToken() {
      File file = new File("plugins/AuthMe/passpartu.token");
      if (file.exists()) {
         file.delete();
      }

      FileWriter writer = null;

      try {
         file.createNewFile();
         writer = new FileWriter(file);
         String token = this.generateToken();
         writer.write(token + ":" + System.currentTimeMillis() / 1000L + "\r\n");
         writer.flush();
         ConsoleLogger.info("[AuthMe] Security passpartu token: " + token);
         writer.close();
         return true;
      } catch (Exception e) {
         e.printStackTrace();
         return false;
      }
   }

   public boolean readToken(String inputToken) {
      File file = new File("plugins/AuthMe/passpartu.token");
      if (!file.exists()) {
         return false;
      } else if (inputToken.isEmpty()) {
         return false;
      } else {
         Scanner reader = null;

         try {
            reader = new Scanner(file);

            while(reader.hasNextLine()) {
               String line = reader.nextLine();
               if (line.contains(":")) {
                  String[] tokenInfo = line.split(":");
                  if (tokenInfo[0].equals(inputToken) && System.currentTimeMillis() / 1000L - 30L <= (long)Integer.parseInt(tokenInfo[1])) {
                     file.delete();
                     reader.close();
                     return true;
                  }
               }
            }
         } catch (Exception e) {
            e.printStackTrace();
         }

         reader.close();
         return false;
      }
   }

   private String generateToken() {
      Random rnd = new Random();
      char[] arr = new char[5];

      for(int i = 0; i < 5; ++i) {
         int n = rnd.nextInt(36);
         arr[i] = (char)(n < 10 ? 48 + n : 97 + n - 10);
      }

      return new String(arr);
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$uk$org$whoami$authme$Utils$groupType() {
      int[] var10000 = $SWITCH_TABLE$uk$org$whoami$authme$Utils$groupType;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[Utils.groupType.values().length];

         try {
            var0[Utils.groupType.LOGGEDIN.ordinal()] = 4;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[Utils.groupType.NOTLOGGEDIN.ordinal()] = 3;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[Utils.groupType.REGISTERED.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[Utils.groupType.UNREGISTERED.ordinal()] = 1;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$uk$org$whoami$authme$Utils$groupType = var0;
         return var0;
      }
   }

   public static enum groupType {
      UNREGISTERED,
      REGISTERED,
      NOTLOGGEDIN,
      LOGGEDIN;

      private groupType() {
      }
   }
}
