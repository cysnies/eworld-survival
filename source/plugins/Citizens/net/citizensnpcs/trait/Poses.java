package net.citizensnpcs.trait;

import com.google.common.collect.Maps;
import java.util.Map;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Paginator;
import net.citizensnpcs.util.Pose;
import net.citizensnpcs.util.Util;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class Poses extends Trait {
   private final Map poses = Maps.newHashMap();

   public Poses() {
      super("poses");
   }

   public boolean addPose(String name, Location location) {
      name = name.toLowerCase();
      Pose newPose = new Pose(name, location.getPitch(), location.getYaw());
      if (!this.poses.containsValue(newPose) && !this.poses.containsKey(name)) {
         this.poses.put(name, newPose);
         return true;
      } else {
         return false;
      }
   }

   private void assumePose(float yaw, float pitch) {
      if (!this.npc.isSpawned()) {
         this.npc.spawn(((CurrentLocation)this.npc.getTrait(CurrentLocation.class)).getLocation());
      }

      Util.assumePose(this.npc.getBukkitEntity(), yaw, pitch);
   }

   public void assumePose(Location location) {
      this.assumePose(location.getYaw(), location.getPitch());
   }

   public void assumePose(String flag) {
      Pose pose = (Pose)this.poses.get(flag.toLowerCase());
      this.assumePose(pose.getYaw(), pose.getPitch());
   }

   public void describe(CommandSender sender, int page) throws CommandException {
      Paginator paginator = (new Paginator()).header("Pose");
      paginator.addLine("<e>Key: <a>ID  <b>Name  <c>Pitch/Yaw");
      int i = 0;

      for(Pose pose : this.poses.values()) {
         String line = "<a>" + i + "<b>  " + pose.getName() + "<c>  " + pose.getPitch() + "/" + pose.getYaw();
         paginator.addLine(line);
         ++i;
      }

      if (!paginator.sendPage(sender, page)) {
         throw new CommandException("citizens.commands.page-missing");
      }
   }

   public Pose getPose(String name) {
      for(Pose pose : this.poses.values()) {
         if (pose.getName().equalsIgnoreCase(name)) {
            return pose;
         }
      }

      return null;
   }

   public boolean hasPose(String pose) {
      return this.poses.containsKey(pose.toLowerCase());
   }

   public void load(DataKey key) throws NPCLoadException {
      this.poses.clear();

      for(DataKey sub : key.getRelative("list").getIntegerSubKeys()) {
         try {
            String[] parts = sub.getString("").split(";");
            this.poses.put(parts[0], new Pose(parts[0], Float.valueOf(parts[1]), Float.valueOf(parts[2])));
         } catch (NumberFormatException e) {
            Messaging.logTr("citizens.notifications.skipping-invalid-pose", sub.name(), e.getMessage());
         }
      }

   }

   public boolean removePose(String pose) {
      return this.poses.remove(pose.toLowerCase()) != null;
   }

   public void save(DataKey key) {
      key.removeKey("list");
      int i = 0;

      for(Pose pose : this.poses.values()) {
         key.setString("list." + i, pose.stringValue());
         ++i;
      }

   }
}
