package com.earth2me.essentials;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.entity.Player;

public class EssentialsTimer implements Runnable {
   private final transient net.ess3.api.IEssentials ess;
   private final transient Set onlineUsers = new HashSet();
   private transient long lastPoll = System.nanoTime();
   private final LinkedList history = new LinkedList();
   private int skip1 = 0;
   private int skip2 = 0;
   private final long maxTime = 10000000L;
   private final long tickInterval = 50L;

   EssentialsTimer(net.ess3.api.IEssentials ess) {
      super();
      this.ess = ess;
      this.history.add((double)20.0F);
   }

   public void run() {
      long startTime = System.nanoTime();
      long currentTime = System.currentTimeMillis();
      long timeSpent = (startTime - this.lastPoll) / 1000L;
      if (timeSpent == 0L) {
         timeSpent = 1L;
      }

      if (this.history.size() > 10) {
         this.history.remove();
      }

      double tps = (double)5.0E7F / (double)timeSpent;
      if (tps <= (double)21.0F) {
         this.history.add(tps);
      }

      this.lastPoll = startTime;
      int count = 0;

      for(Player player : this.ess.getServer().getOnlinePlayers()) {
         ++count;
         if (this.skip1 > 0) {
            --this.skip1;
         } else {
            if (count % 10 == 0 && System.nanoTime() - startTime > 5000000L) {
               this.skip1 = count - 1;
               break;
            }

            try {
               User user = this.ess.getUser(player);
               this.onlineUsers.add(user.getName());
               user.setLastOnlineActivity(currentTime);
               user.checkActivity();
            } catch (Exception e) {
               this.ess.getLogger().log(Level.WARNING, "EssentialsTimer Error:", e);
            }
         }
      }

      count = 0;
      Iterator<String> iterator = this.onlineUsers.iterator();

      while(iterator.hasNext()) {
         ++count;
         if (this.skip2 > 0) {
            --this.skip2;
         } else {
            if (count % 10 == 0 && System.nanoTime() - startTime > 10000000L) {
               this.skip2 = count - 1;
               break;
            }

            User user = this.ess.getUser(iterator.next());
            if (user.getLastOnlineActivity() < currentTime && user.getLastOnlineActivity() > user.getLastLogout()) {
               if (!user.isHidden()) {
                  user.setLastLogout(user.getLastOnlineActivity());
               }

               iterator.remove();
            } else {
               user.checkMuteTimeout(currentTime);
               user.checkJailTimeout(currentTime);
               user.resetInvulnerabilityAfterTeleport();
            }
         }
      }

   }

   public double getAverageTPS() {
      double avg = (double)0.0F;

      for(Double f : this.history) {
         if (f != null) {
            avg += f;
         }
      }

      return avg / (double)this.history.size();
   }
}
