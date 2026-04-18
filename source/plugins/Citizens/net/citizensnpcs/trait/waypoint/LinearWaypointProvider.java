package net.citizensnpcs.trait.waypoint;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.ai.event.NavigatorCallback;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.trait.waypoint.triggers.TriggerEditPrompt;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.conversations.Conversation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class LinearWaypointProvider implements WaypointProvider {
   private LinearWaypointGoal currentGoal;
   private NPC npc;
   private final List waypoints = Lists.newArrayList();

   public LinearWaypointProvider() {
      super();
   }

   public WaypointEditor createEditor(Player player, CommandContext args) {
      if (args.hasFlag('h')) {
         this.waypoints.add(new Waypoint(player.getLocation()));
         return null;
      } else if (args.hasValueFlag("at")) {
         try {
            Location location = CommandContext.parseLocation(player.getLocation(), args.getFlag("at"));
            this.waypoints.add(new Waypoint(location));
         } catch (CommandException e) {
            Messaging.sendError(player, e.getMessage());
         }

         return null;
      } else if (args.hasFlag('c')) {
         this.waypoints.clear();
         return null;
      } else if (args.hasFlag('l')) {
         if (this.waypoints.size() > 0) {
            this.waypoints.remove(this.waypoints.size() - 1);
         }

         return null;
      } else if (args.hasFlag('p')) {
         this.setPaused(!this.isPaused());
         return null;
      } else {
         return new LinearWaypointEditor(player);
      }
   }

   public boolean isPaused() {
      return this.currentGoal.isPaused();
   }

   public void load(DataKey key) {
      for(DataKey root : key.getRelative("points").getIntegerSubKeys()) {
         Waypoint waypoint = (Waypoint)PersistenceLoader.load(Waypoint.class, root);
         if (waypoint != null) {
            this.waypoints.add(waypoint);
         }
      }

   }

   public void onSpawn(NPC npc) {
      this.npc = npc;
      if (this.currentGoal == null) {
         this.currentGoal = new LinearWaypointGoal();
         CitizensAPI.registerEvents(this.currentGoal);
         npc.getDefaultGoalController().addGoal(this.currentGoal, 1);
      }

   }

   public void save(DataKey key) {
      key.removeKey("points");
      key = key.getRelative("points");

      for(int i = 0; i < this.waypoints.size(); ++i) {
         PersistenceLoader.save(this.waypoints.get(i), key.getRelative(i));
      }

   }

   public void setPaused(boolean paused) {
      this.currentGoal.setPaused(paused);
   }

   private final class LinearWaypointEditor extends WaypointEditor {
      Conversation conversation;
      boolean editing;
      int editingSlot;
      private final Player player;
      private boolean showPath;
      private final Map waypointMarkers;
      private static final int LARGEST_SLOT = 8;

      private LinearWaypointEditor(Player player) {
         super();
         this.editing = true;
         this.editingSlot = LinearWaypointProvider.this.waypoints.size() - 1;
         this.waypointMarkers = Maps.newHashMap();
         this.player = player;
      }

      public void begin() {
         Messaging.sendTr(this.player, "citizens.editors.waypoints.linear.begin");
      }

      private void clearWaypoints() {
         this.editingSlot = 0;
         LinearWaypointProvider.this.waypoints.clear();
         this.onWaypointsModified();
         this.destroyWaypointMarkers();
         Messaging.sendTr(this.player, "citizens.editors.waypoints.linear.waypoints-cleared");
      }

      private void createWaypointMarker(int index, Waypoint waypoint) {
         Entity entity = this.spawnMarker(this.player.getWorld(), waypoint.getLocation().clone().add((double)0.0F, (double)1.0F, (double)0.0F));
         if (entity != null) {
            entity.setMetadata("waypointindex", new FixedMetadataValue(CitizensAPI.getPlugin(), index));
            this.waypointMarkers.put(waypoint, entity);
         }
      }

      private void createWaypointMarkers() {
         for(int i = 0; i < LinearWaypointProvider.this.waypoints.size(); ++i) {
            this.createWaypointMarker(i, (Waypoint)LinearWaypointProvider.this.waypoints.get(i));
         }

      }

      private void destroyWaypointMarkers() {
         for(Entity entity : this.waypointMarkers.values()) {
            entity.remove();
         }

         this.waypointMarkers.clear();
      }

      public void end() {
         if (this.editing) {
            if (this.conversation != null) {
               this.conversation.abandon();
            }

            Messaging.sendTr(this.player, "citizens.editors.waypoints.linear.end");
            this.editing = false;
            if (this.showPath) {
               this.destroyWaypointMarkers();
            }
         }
      }

      private String formatLoc(Location location) {
         return String.format("[[%d]], [[%d]], [[%d]]", location.getBlockX(), location.getBlockY(), location.getBlockZ());
      }

      public Waypoint getCurrentWaypoint() {
         if (LinearWaypointProvider.this.waypoints.size() != 0 && this.editing) {
            this.normaliseEditingSlot();
            return (Waypoint)LinearWaypointProvider.this.waypoints.get(this.editingSlot);
         } else {
            return null;
         }
      }

      private Location getPreviousWaypoint(int fromSlot) {
         if (LinearWaypointProvider.this.waypoints.size() <= 1) {
            return null;
         } else {
            --fromSlot;
            if (fromSlot < 0) {
               fromSlot = LinearWaypointProvider.this.waypoints.size() - 1;
            }

            return ((Waypoint)LinearWaypointProvider.this.waypoints.get(fromSlot)).getLocation();
         }
      }

      private void normaliseEditingSlot() {
         this.editingSlot = Math.max(0, Math.min(LinearWaypointProvider.this.waypoints.size() - 1, this.editingSlot));
      }

      @EventHandler
      public void onNPCDespawn(NPCDespawnEvent event) {
         if (event.getNPC().equals(LinearWaypointProvider.this.npc)) {
            Editor.leave(this.player);
         }

      }

      @EventHandler
      public void onNPCRemove(NPCRemoveEvent event) {
         if (event.getNPC().equals(LinearWaypointProvider.this.npc)) {
            Editor.leave(this.player);
         }

      }

      @EventHandler(
         ignoreCancelled = true
      )
      public void onPlayerChat(AsyncPlayerChatEvent event) {
         if (event.getPlayer().equals(this.player)) {
            String message = event.getMessage();
            if (message.equalsIgnoreCase("triggers")) {
               event.setCancelled(true);
               Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                  public void run() {
                     LinearWaypointEditor.this.conversation = TriggerEditPrompt.start(LinearWaypointEditor.this.player, LinearWaypointEditor.this);
                  }
               });
            } else if (message.equalsIgnoreCase("clear")) {
               event.setCancelled(true);
               Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                  public void run() {
                     LinearWaypointEditor.this.clearWaypoints();
                  }
               });
            } else if (message.equalsIgnoreCase("toggle path")) {
               event.setCancelled(true);
               Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                  public void run() {
                     LinearWaypointEditor.this.togglePath();
                  }
               });
            }

         }
      }

      @EventHandler(
         ignoreCancelled = true
      )
      public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.getPlayer().equals(this.player) && event.getAction() != Action.PHYSICAL) {
            if (event.getPlayer().getWorld() == LinearWaypointProvider.this.npc.getBukkitEntity().getWorld()) {
               if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_AIR) {
                  if (LinearWaypointProvider.this.waypoints.size() > 0) {
                     event.setCancelled(true);
                     this.normaliseEditingSlot();
                     Waypoint waypoint = (Waypoint)LinearWaypointProvider.this.waypoints.remove(this.editingSlot);
                     if (this.showPath) {
                        this.removeWaypointMarker(waypoint);
                     }

                     this.editingSlot = Math.max(0, this.editingSlot - 1);
                     Messaging.sendTr(this.player, "citizens.editors.waypoints.linear.removed-waypoint", LinearWaypointProvider.this.waypoints.size(), this.editingSlot + 1);
                  }
               } else {
                  if (event.getClickedBlock() == null) {
                     return;
                  }

                  event.setCancelled(true);
                  Location at = event.getClickedBlock().getLocation();
                  Location prev = this.getPreviousWaypoint(this.editingSlot);
                  if (prev != null) {
                     double distance = at.distanceSquared(prev);
                     double maxDistance = Math.pow((double)LinearWaypointProvider.this.npc.getNavigator().getDefaultParameters().range(), (double)2.0F);
                     if (distance > maxDistance) {
                        Messaging.sendErrorTr(this.player, "citizens.editors.waypoints.linear.range-exceeded", Math.sqrt(distance), Math.sqrt(maxDistance), ChatColor.RED);
                        return;
                     }
                  }

                  Waypoint element = new Waypoint(at);
                  this.normaliseEditingSlot();
                  LinearWaypointProvider.this.waypoints.add(this.editingSlot, element);
                  if (this.showPath) {
                     this.createWaypointMarker(this.editingSlot, element);
                  }

                  this.editingSlot = Math.min(this.editingSlot + 1, LinearWaypointProvider.this.waypoints.size());
                  Messaging.sendTr(this.player, "citizens.editors.waypoints.linear.added-waypoint", this.formatLoc(at), this.editingSlot + 1, LinearWaypointProvider.this.waypoints.size());
               }

               this.onWaypointsModified();
            }
         }
      }

      @EventHandler(
         ignoreCancelled = true
      )
      public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
         if (this.player.equals(event.getPlayer()) && this.showPath) {
            if (event.getRightClicked().hasMetadata("waypointindex")) {
               this.editingSlot = ((MetadataValue)event.getRightClicked().getMetadata("waypointindex").get(0)).asInt();
               Messaging.sendTr(this.player, "citizens.editors.waypoints.linear.edit-slot-set", this.editingSlot, this.formatLoc(((Waypoint)LinearWaypointProvider.this.waypoints.get(this.editingSlot)).getLocation()));
            }
         }
      }

      @EventHandler
      public void onPlayerItemHeldChange(PlayerItemHeldEvent event) {
         if (event.getPlayer().equals(this.player) && LinearWaypointProvider.this.waypoints.size() != 0) {
            int previousSlot = event.getPreviousSlot();
            int newSlot = event.getNewSlot();
            if (previousSlot == 0 && newSlot == 8) {
               --this.editingSlot;
            } else if (previousSlot == 8 && newSlot == 0) {
               ++this.editingSlot;
            } else {
               int diff = newSlot - previousSlot;
               if (Math.abs(diff) != 1) {
                  return;
               }

               this.editingSlot += diff > 0 ? 1 : -1;
            }

            this.normaliseEditingSlot();
            Messaging.sendTr(this.player, "citizens.editors.waypoints.linear.edit-slot-set", this.editingSlot, this.formatLoc(((Waypoint)LinearWaypointProvider.this.waypoints.get(this.editingSlot)).getLocation()));
         }
      }

      private void onWaypointsModified() {
         if (LinearWaypointProvider.this.currentGoal != null) {
            LinearWaypointProvider.this.currentGoal.onProviderChanged();
         }

      }

      private void removeWaypointMarker(Waypoint waypoint) {
         Entity entity = (Entity)this.waypointMarkers.remove(waypoint);
         if (entity != null) {
            entity.remove();
         }

      }

      private Entity spawnMarker(World world, Location at) {
         return NMS.spawnCustomEntity(world, at, EntityEnderCrystalMarker.class, EntityType.ENDER_CRYSTAL);
      }

      private void togglePath() {
         this.showPath = !this.showPath;
         if (this.showPath) {
            this.createWaypointMarkers();
            Messaging.sendTr(this.player, "citizens.editors.waypoints.linear.showing-markers");
         } else {
            this.destroyWaypointMarkers();
            Messaging.sendTr(this.player, "citizens.editors.waypoints.linear.not-showing-markers");
         }

      }
   }

   private class LinearWaypointGoal implements Goal {
      private final Location cachedLocation;
      private Waypoint currentDestination;
      private Iterator itr;
      private boolean paused;
      private GoalSelector selector;

      private LinearWaypointGoal() {
         super();
         this.cachedLocation = new Location((World)null, (double)0.0F, (double)0.0F, (double)0.0F);
      }

      private void ensureItr() {
         if (this.itr == null) {
            this.itr = LinearWaypointProvider.this.waypoints.iterator();
         } else if (!this.itr.hasNext()) {
            this.itr = this.getNewIterator();
         }

      }

      private Navigator getNavigator() {
         return LinearWaypointProvider.this.npc.getNavigator();
      }

      private Iterator getNewIterator() {
         LinearWaypointsCompleteEvent event = new LinearWaypointsCompleteEvent(LinearWaypointProvider.this, LinearWaypointProvider.this.waypoints.iterator());
         Bukkit.getPluginManager().callEvent(event);
         Iterator<Waypoint> next = event.getNextWaypoints();
         return next;
      }

      public boolean isPaused() {
         return this.paused;
      }

      public void onProviderChanged() {
         this.itr = LinearWaypointProvider.this.waypoints.iterator();
         if (this.currentDestination != null) {
            if (this.selector != null) {
               this.selector.finish();
            }

            if (LinearWaypointProvider.this.npc != null && LinearWaypointProvider.this.npc.getNavigator().isNavigating()) {
               LinearWaypointProvider.this.npc.getNavigator().cancelNavigation();
            }
         }

      }

      public void reset() {
         this.currentDestination = null;
         this.selector = null;
      }

      public void run(GoalSelector selector) {
         if (!this.getNavigator().isNavigating()) {
            selector.finish();
         }

      }

      public void setPaused(boolean pause) {
         if (pause && this.currentDestination != null) {
            this.selector.finish();
         }

         this.paused = pause;
      }

      public boolean shouldExecute(final GoalSelector selector) {
         if (!this.paused && this.currentDestination == null && LinearWaypointProvider.this.npc.isSpawned() && !this.getNavigator().isNavigating()) {
            this.ensureItr();
            boolean shouldExecute = this.itr.hasNext();
            if (!shouldExecute) {
               return false;
            } else {
               this.selector = selector;
               Waypoint next = (Waypoint)this.itr.next();
               Location npcLoc = LinearWaypointProvider.this.npc.getBukkitEntity().getLocation(this.cachedLocation);
               if (npcLoc.getWorld() == next.getLocation().getWorld() && !(npcLoc.distanceSquared(next.getLocation()) < LinearWaypointProvider.this.npc.getNavigator().getLocalParameters().distanceMargin())) {
                  this.currentDestination = next;
                  this.getNavigator().setTarget(this.currentDestination.getLocation());
                  this.getNavigator().getLocalParameters().addSingleUseCallback(new NavigatorCallback() {
                     public void onCompletion(@Nullable CancelReason cancelReason) {
                        if (LinearWaypointProvider.this.npc.isSpawned() && LinearWaypointGoal.this.currentDestination != null && Util.locationWithinRange(LinearWaypointProvider.this.npc.getBukkitEntity().getLocation(), LinearWaypointGoal.this.currentDestination.getLocation(), (double)4.0F)) {
                           LinearWaypointGoal.this.currentDestination.onReach(LinearWaypointProvider.this.npc);
                        }

                        selector.finish();
                     }
                  });
                  return true;
               } else {
                  return false;
               }
            }
         } else {
            return false;
         }
      }
   }
}
