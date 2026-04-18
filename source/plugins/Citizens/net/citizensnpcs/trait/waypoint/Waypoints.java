package net.citizensnpcs.trait.waypoint;

import com.google.common.collect.Maps;
import java.util.Map;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.util.StringHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Waypoints extends Trait {
   private WaypointProvider provider = new LinearWaypointProvider();
   private String providerName = "linear";
   private static final Map providers = Maps.newHashMap();

   public Waypoints() {
      super("waypoints");
   }

   private WaypointProvider create(Class clazz) {
      try {
         return (WaypointProvider)clazz.newInstance();
      } catch (Exception ex) {
         ex.printStackTrace();
         return null;
      }
   }

   public void describeProviders(CommandSender sender) {
      Messaging.sendTr(sender, "citizens.waypoints.available-providers-header");

      for(String name : providers.keySet()) {
         Messaging.send(sender, "    - " + StringHelper.wrap(name));
      }

   }

   public WaypointProvider getCurrentProvider() {
      return this.provider;
   }

   public String getCurrentProviderName() {
      return this.providerName;
   }

   public Editor getEditor(Player player, CommandContext args) {
      return this.provider.createEditor(player, args);
   }

   public void load(DataKey key) throws NPCLoadException {
      this.provider = null;
      this.providerName = key.getString("provider", "linear");

      for(Map.Entry entry : providers.entrySet()) {
         if (((String)entry.getKey()).equals(this.providerName)) {
            this.provider = this.create((Class)entry.getValue());
            break;
         }
      }

      if (this.provider != null) {
         this.provider.load(key.getRelative(this.providerName));
      }
   }

   public void onSpawn() {
      if (this.provider != null) {
         this.provider.onSpawn(this.getNPC());
      }

   }

   public void save(DataKey key) {
      if (this.provider != null) {
         this.provider.save(key.getRelative(this.providerName));
         key.setString("provider", this.providerName);
      }
   }

   public boolean setWaypointProvider(String name) {
      name = name.toLowerCase();
      Class<? extends WaypointProvider> clazz = (Class)providers.get(name);
      if (clazz == null) {
         return false;
      } else {
         this.provider = this.create(clazz);
         if (this.provider == null) {
            return false;
         } else {
            this.providerName = name;
            if (this.npc != null && this.npc.isSpawned()) {
               this.provider.onSpawn(this.npc);
            }

            return true;
         }
      }
   }

   public static void registerWaypointProvider(Class clazz, String name) {
      providers.put(name, clazz);
   }

   static {
      providers.put("linear", LinearWaypointProvider.class);
      providers.put("wander", WanderWaypointProvider.class);
   }
}
