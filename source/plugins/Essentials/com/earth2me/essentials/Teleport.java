package com.earth2me.essentials;

import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.LocationUtil;
import java.util.Calendar;
import java.util.GregorianCalendar;
import net.ess3.api.ITeleport;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Teleport implements ITeleport {
   private final net.ess3.api.IUser teleportOwner;
   private final net.ess3.api.IEssentials ess;
   private TimedTeleport timedTeleport;

   public Teleport(net.ess3.api.IUser user, net.ess3.api.IEssentials ess) {
      super();
      this.teleportOwner = user;
      this.ess = ess;
   }

   public void cooldown(boolean check) throws Exception {
      Calendar time = new GregorianCalendar();
      if (this.teleportOwner.getLastTeleportTimestamp() > 0L) {
         double cooldown = this.ess.getSettings().getTeleportCooldown();
         Calendar earliestTime = new GregorianCalendar();
         earliestTime.add(13, -((int)cooldown));
         earliestTime.add(14, -((int)(cooldown * (double)1000.0F % (double)1000.0F)));
         long earliestLong = earliestTime.getTimeInMillis();
         Long lastTime = this.teleportOwner.getLastTeleportTimestamp();
         if (lastTime > time.getTimeInMillis()) {
            this.teleportOwner.setLastTeleportTimestamp(time.getTimeInMillis());
            return;
         }

         if (lastTime > earliestLong && !this.teleportOwner.isAuthorized("essentials.teleport.cooldown.bypass")) {
            time.setTimeInMillis(lastTime);
            time.add(13, (int)cooldown);
            time.add(14, (int)(cooldown * (double)1000.0F % (double)1000.0F));
            throw new Exception(I18n._("timeBeforeTeleport", DateUtil.formatDateDiff(time.getTimeInMillis())));
         }
      }

      if (!check) {
         this.teleportOwner.setLastTeleportTimestamp(time.getTimeInMillis());
      }

   }

   private void warnUser(net.ess3.api.IUser user, double delay) {
      Calendar c = new GregorianCalendar();
      c.add(13, (int)delay);
      c.add(14, (int)(delay * (double)1000.0F % (double)1000.0F));
      user.sendMessage(I18n._("dontMoveMessage", DateUtil.formatDateDiff(c.getTimeInMillis())));
   }

   public void now(Location loc, boolean cooldown, PlayerTeleportEvent.TeleportCause cause) throws Exception {
      if (cooldown) {
         this.cooldown(false);
      }

      this.now(this.teleportOwner, new LocationTarget(loc), cause);
   }

   public void now(Player entity, boolean cooldown, PlayerTeleportEvent.TeleportCause cause) throws Exception {
      if (cooldown) {
         this.cooldown(false);
      }

      this.now(this.teleportOwner, new PlayerTarget(entity), cause);
   }

   protected void now(net.ess3.api.IUser teleportee, ITarget target, PlayerTeleportEvent.TeleportCause cause) throws Exception {
      this.cancel(false);
      teleportee.setLastLocation();
      teleportee.getBase().teleport(LocationUtil.getSafeDestination(teleportee, target.getLocation()), cause);
   }

   /** @deprecated */
   @Deprecated
   public void teleport(Location loc, Trade chargeFor) throws Exception {
      this.teleport(loc, chargeFor, TeleportCause.PLUGIN);
   }

   public void teleport(Location loc, Trade chargeFor, PlayerTeleportEvent.TeleportCause cause) throws Exception {
      this.teleport(this.teleportOwner, new LocationTarget(loc), chargeFor, cause);
   }

   public void teleport(Player entity, Trade chargeFor, PlayerTeleportEvent.TeleportCause cause) throws Exception {
      this.teleport(this.teleportOwner, new PlayerTarget(entity), chargeFor, cause);
   }

   public void teleportPlayer(net.ess3.api.IUser teleportee, Location loc, Trade chargeFor, PlayerTeleportEvent.TeleportCause cause) throws Exception {
      this.teleport(teleportee, new LocationTarget(loc), chargeFor, cause);
   }

   public void teleportPlayer(net.ess3.api.IUser teleportee, Player entity, Trade chargeFor, PlayerTeleportEvent.TeleportCause cause) throws Exception {
      this.teleport(teleportee, new PlayerTarget(entity), chargeFor, cause);
   }

   private void teleport(net.ess3.api.IUser teleportee, ITarget target, Trade chargeFor, PlayerTeleportEvent.TeleportCause cause) throws Exception {
      double delay = this.ess.getSettings().getTeleportDelay();
      if (chargeFor != null) {
         chargeFor.isAffordableFor(this.teleportOwner);
      }

      this.cooldown(true);
      if (!(delay <= (double)0.0F) && !this.teleportOwner.isAuthorized("essentials.teleport.timer.bypass") && !teleportee.isAuthorized("essentials.teleport.timer.bypass")) {
         this.cancel(false);
         this.warnUser(teleportee, delay);
         this.initTimer((long)(delay * (double)1000.0F), teleportee, target, chargeFor, cause, false);
      } else {
         this.cooldown(false);
         this.now(teleportee, target, cause);
         if (chargeFor != null) {
            chargeFor.charge(this.teleportOwner);
         }

      }
   }

   public void respawn(Trade chargeFor, PlayerTeleportEvent.TeleportCause cause) throws Exception {
      double delay = this.ess.getSettings().getTeleportDelay();
      if (chargeFor != null) {
         chargeFor.isAffordableFor(this.teleportOwner);
      }

      this.cooldown(true);
      if (!(delay <= (double)0.0F) && !this.teleportOwner.isAuthorized("essentials.teleport.timer.bypass")) {
         this.cancel(false);
         this.warnUser(this.teleportOwner, delay);
         this.initTimer((long)(delay * (double)1000.0F), this.teleportOwner, (ITarget)null, chargeFor, cause, true);
      } else {
         this.cooldown(false);
         this.respawnNow(this.teleportOwner, cause);
         if (chargeFor != null) {
            chargeFor.charge(this.teleportOwner);
         }

      }
   }

   protected void respawnNow(net.ess3.api.IUser teleportee, PlayerTeleportEvent.TeleportCause cause) throws Exception {
      Player player = teleportee.getBase();
      Location bed = player.getBedSpawnLocation();
      if (bed != null) {
         this.now(teleportee, new LocationTarget(bed), cause);
      } else {
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().info("Could not find bed spawn, forcing respawn event.");
         }

         PlayerRespawnEvent pre = new PlayerRespawnEvent(player, player.getWorld().getSpawnLocation(), false);
         this.ess.getServer().getPluginManager().callEvent(pre);
         this.now(teleportee, new LocationTarget(pre.getRespawnLocation()), cause);
      }

   }

   public void warp(net.ess3.api.IUser teleportee, String warp, Trade chargeFor, PlayerTeleportEvent.TeleportCause cause) throws Exception {
      Location loc = this.ess.getWarps().getWarp(warp);
      teleportee.sendMessage(I18n._("warpingTo", warp));
      this.teleport(teleportee, new LocationTarget(loc), chargeFor, cause);
   }

   public void back(Trade chargeFor) throws Exception {
      this.teleport(this.teleportOwner, new LocationTarget(this.teleportOwner.getLastLocation()), chargeFor, TeleportCause.COMMAND);
   }

   public void back() throws Exception {
      this.now(this.teleportOwner, new LocationTarget(this.teleportOwner.getLastLocation()), TeleportCause.COMMAND);
   }

   private void cancel(boolean notifyUser) {
      if (this.timedTeleport != null) {
         this.timedTeleport.cancelTimer(notifyUser);
         this.timedTeleport = null;
      }

   }

   private void initTimer(long delay, net.ess3.api.IUser teleportUser, ITarget target, Trade chargeFor, PlayerTeleportEvent.TeleportCause cause, boolean respawn) {
      this.timedTeleport = new TimedTeleport(this.teleportOwner, this.ess, this, delay, teleportUser, target, chargeFor, cause, respawn);
   }
}
