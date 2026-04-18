package uk.org.whoami.authme.settings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Spawn extends CustomConfiguration {
   private static Spawn spawn;
   private static List emptyList = new ArrayList();

   public Spawn() {
      super(new File("./plugins/AuthMe/spawn.yml"));
      spawn = this;
      this.load();
      this.save();
      this.saveDefault();
   }

   private void saveDefault() {
      if (!this.contains("spawn")) {
         this.set("spawn", emptyList);
         this.set("spawn.world", "");
         this.set("spawn.x", "");
         this.set("spawn.y", "");
         this.set("spawn.z", "");
         this.set("spawn.yaw", "");
         this.set("spawn.pitch", "");
         this.save();
      }

   }

   public static Spawn getInstance() {
      if (spawn == null) {
         spawn = new Spawn();
      }

      return spawn;
   }

   public boolean setSpawn(Location location) {
      try {
         this.set("spawn.world", location.getWorld().getName());
         this.set("spawn.x", location.getX());
         this.set("spawn.y", location.getY());
         this.set("spawn.z", location.getZ());
         this.set("spawn.yaw", location.getYaw());
         this.set("spawn.pitch", location.getPitch());
         this.save();
         return true;
      } catch (NullPointerException var3) {
         return false;
      }
   }

   public Location getLocation() {
      try {
         if (!this.getString("spawn.world").isEmpty() && this.getString("spawn.world") != "") {
            Location location = new Location(Bukkit.getWorld(this.getString("spawn.world")), this.getDouble("spawn.x"), this.getDouble("spawn.y"), this.getDouble("spawn.z"), Float.parseFloat(this.getString("spawn.yaw")), Float.parseFloat(this.getString("spawn.pitch")));
            return location;
         } else {
            return null;
         }
      } catch (NullPointerException var2) {
         return null;
      } catch (NumberFormatException var3) {
         return null;
      }
   }
}
