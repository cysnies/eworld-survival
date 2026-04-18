package com.onarandombox.MultiverseCore.utils;

import com.onarandombox.MultiverseCore.MultiverseCore;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class AnchorManager {
   private MultiverseCore plugin;
   private Map anchors;
   private FileConfiguration anchorConfig;

   public AnchorManager(MultiverseCore plugin) {
      super();
      this.plugin = plugin;
      this.anchors = new HashMap();
   }

   public void loadAnchors() {
      this.anchors = new HashMap();
      this.anchorConfig = YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "anchors.yml"));
      this.ensureConfigIsPrepared();
      ConfigurationSection anchorsSection = this.anchorConfig.getConfigurationSection("anchors");

      for(String key : anchorsSection.getKeys(false)) {
         Location anchorLocation = this.plugin.getLocationManipulation().stringToLocation(anchorsSection.getString(key, ""));
         if (anchorLocation != null) {
            CoreLogging.config("Loading anchor:  '%s'...", key);
            this.anchors.put(key, anchorLocation);
         } else {
            CoreLogging.warning("The location for anchor '%s' is INVALID.", key);
         }
      }

   }

   private void ensureConfigIsPrepared() {
      if (this.anchorConfig.getConfigurationSection("anchors") == null) {
         this.anchorConfig.createSection("anchors");
      }

   }

   public boolean saveAnchors() {
      try {
         this.anchorConfig.save(new File(this.plugin.getDataFolder(), "anchors.yml"));
         return true;
      } catch (IOException var2) {
         this.plugin.log(Level.SEVERE, "Failed to save anchors.yml. Please check your file permissions.");
         return false;
      }
   }

   public Location getAnchorLocation(String anchor) {
      return this.anchors.containsKey(anchor) ? (Location)this.anchors.get(anchor) : null;
   }

   public boolean saveAnchorLocation(String anchor, String location) {
      Location parsed = this.plugin.getLocationManipulation().stringToLocation(location);
      return parsed != null && this.saveAnchorLocation(anchor, parsed);
   }

   public boolean saveAnchorLocation(String anchor, Location l) {
      if (l == null) {
         return false;
      } else {
         this.anchorConfig.set("anchors." + anchor, this.plugin.getLocationManipulation().locationToString(l));
         this.anchors.put(anchor, l);
         return this.saveAnchors();
      }
   }

   public Set getAllAnchors() {
      return Collections.unmodifiableSet(this.anchors.keySet());
   }

   public Set getAnchors(Player p) {
      if (p == null) {
         return this.anchors.keySet();
      } else {
         Set<String> myAnchors = new HashSet();

         for(String anchor : this.anchors.keySet()) {
            Location ancLoc = (Location)this.anchors.get(anchor);
            if (ancLoc != null && p.hasPermission("multiverse.access." + ancLoc.getWorld().getName())) {
               myAnchors.add(anchor);
            }
         }

         return Collections.unmodifiableSet(myAnchors);
      }
   }

   public boolean deleteAnchor(String s) {
      if (this.anchors.containsKey(s)) {
         this.anchors.remove(s);
         this.anchorConfig.set("anchors." + s, (Object)null);
         return this.saveAnchors();
      } else {
         return false;
      }
   }
}
