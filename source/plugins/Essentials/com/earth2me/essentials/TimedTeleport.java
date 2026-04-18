package com.earth2me.essentials;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TimedTeleport implements Runnable {
   private static final double MOVE_CONSTANT = 0.3;
   private final net.ess3.api.IUser teleportOwner;
   private final net.ess3.api.IEssentials ess;
   private final Teleport teleport;
   private String timer_teleportee;
   private int timer_task = -1;
   private long timer_started;
   private long timer_delay;
   private double timer_health;
   private long timer_initX;
   private long timer_initY;
   private long timer_initZ;
   private ITarget timer_teleportTarget;
   private boolean timer_respawn;
   private boolean timer_canMove;
   private Trade timer_chargeFor;
   private PlayerTeleportEvent.TeleportCause timer_cause;

   public TimedTeleport(net.ess3.api.IUser user, net.ess3.api.IEssentials ess, Teleport teleport, long delay, net.ess3.api.IUser teleportUser, ITarget target, Trade chargeFor, PlayerTeleportEvent.TeleportCause cause, boolean respawn) {
      super();
      this.teleportOwner = user;
      this.ess = ess;
      this.teleport = teleport;
      this.timer_started = System.currentTimeMillis();
      this.timer_delay = delay;
      this.timer_health = teleportUser.getBase().getHealth();
      this.timer_initX = Math.round(teleportUser.getBase().getLocation().getX() * 0.3);
      this.timer_initY = Math.round(teleportUser.getBase().getLocation().getY() * 0.3);
      this.timer_initZ = Math.round(teleportUser.getBase().getLocation().getZ() * 0.3);
      this.timer_teleportee = teleportUser.getName();
      this.timer_teleportTarget = target;
      this.timer_chargeFor = chargeFor;
      this.timer_cause = cause;
      this.timer_respawn = respawn;
      this.timer_canMove = user.isAuthorized("essentials.teleport.timer.move");
      this.timer_task = ess.scheduleSyncRepeatingTask(this, 20L, 20L);
   }

   public void run() {
      if (this.teleportOwner != null && this.teleportOwner.getBase().isOnline() && this.teleportOwner.getBase().getLocation() != null) {
         net.ess3.api.IUser teleportUser = this.ess.getUser(this.timer_teleportee);
         if (teleportUser != null && teleportUser.getBase().isOnline()) {
            Location currLocation = teleportUser.getBase().getLocation();
            if (currLocation == null) {
               this.cancelTimer(false);
            } else if (this.timer_canMove || Math.round(currLocation.getX() * 0.3) == this.timer_initX && Math.round(currLocation.getY() * 0.3) == this.timer_initY && Math.round(currLocation.getZ() * 0.3) == this.timer_initZ && !(teleportUser.getBase().getHealth() < this.timer_health)) {
               this.timer_health = teleportUser.getBase().getHealth();
               long now = System.currentTimeMillis();
               if (now > this.timer_started + this.timer_delay) {
                  try {
                     this.teleport.cooldown(false);
                     teleportUser.sendMessage(I18n._("teleportationCommencing"));

                     try {
                        if (this.timer_respawn) {
                           this.teleport.respawnNow(teleportUser, this.timer_cause);
                        } else {
                           this.teleport.now(teleportUser, this.timer_teleportTarget, this.timer_cause);
                        }

                        this.cancelTimer(false);
                        if (this.timer_chargeFor != null) {
                           this.timer_chargeFor.charge(this.teleportOwner);
                        }
                     } catch (Throwable ex) {
                        this.ess.showError(this.teleportOwner.getBase(), ex, "teleport");
                     }
                  } catch (Exception ex) {
                     this.teleportOwner.sendMessage(I18n._("cooldownWithMessage", ex.getMessage()));
                     if (this.teleportOwner != teleportUser) {
                        teleportUser.sendMessage(I18n._("cooldownWithMessage", ex.getMessage()));
                     }
                  }
               }

            } else {
               this.cancelTimer(true);
            }
         } else {
            this.cancelTimer(false);
         }
      } else {
         this.cancelTimer(false);
      }
   }

   public void cancelTimer(boolean notifyUser) {
      if (this.timer_task != -1) {
         try {
            this.ess.getServer().getScheduler().cancelTask(this.timer_task);
            if (notifyUser) {
               this.teleportOwner.sendMessage(I18n._("pendingTeleportCancelled"));
               if (this.timer_teleportee != null && !this.timer_teleportee.equals(this.teleportOwner.getName())) {
                  this.ess.getUser(this.timer_teleportee).sendMessage(I18n._("pendingTeleportCancelled"));
               }
            }
         } finally {
            this.timer_task = -1;
         }

      }
   }
}
