package net.citizensnpcs.trait;

import java.util.ArrayList;
import java.util.List;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Anchor;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Anchors extends Trait {
   private final List anchors = new ArrayList();

   public Anchors() {
      super("anchors");
   }

   public boolean addAnchor(String name, Location location) {
      Anchor newAnchor = new Anchor(name, location);
      if (this.anchors.contains(newAnchor)) {
         return false;
      } else {
         this.anchors.add(newAnchor);
         return true;
      }
   }

   public Anchor getAnchor(String name) {
      for(Anchor anchor : this.anchors) {
         if (anchor.getName().equalsIgnoreCase(name)) {
            return anchor;
         }
      }

      return null;
   }

   public List getAnchors() {
      return this.anchors;
   }

   public void load(DataKey key) throws NPCLoadException {
      for(DataKey sub : key.getRelative("list").getIntegerSubKeys()) {
         try {
            String[] parts = sub.getString("").split(";");
            this.anchors.add(new Anchor(parts[0], new Location(Bukkit.getServer().getWorld(parts[1]), Double.valueOf(parts[2]), Double.valueOf(parts[3]), Double.valueOf(parts[4]))));
         } catch (NumberFormatException e) {
            Messaging.logTr("citizens.notifications.skipping-invalid-anchor", sub.name(), e.getMessage());
         }
      }

   }

   public boolean removeAnchor(Anchor anchor) {
      if (this.anchors.contains(anchor)) {
         this.anchors.remove(anchor);
         return true;
      } else {
         return false;
      }
   }

   public void save(DataKey key) {
      key.removeKey("list");

      for(int i = 0; i < this.anchors.size(); ++i) {
         key.setString("list." + String.valueOf(i), ((Anchor)this.anchors.get(i)).stringValue());
      }

   }
}
