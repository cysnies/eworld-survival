package com.onarandombox.MultiverseCore.utils;

import com.onarandombox.MultiverseCore.api.MultiverseMessaging;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MVMessaging implements MultiverseMessaging {
   private Map sentList = new HashMap();
   private int cooldown = 5000;

   public MVMessaging() {
      super();
   }

   public void setCooldown(int milliseconds) {
      this.cooldown = milliseconds;
   }

   public boolean sendMessage(CommandSender sender, String message, boolean ignoreCooldown) {
      return this.sendMessages(sender, new String[]{message}, ignoreCooldown);
   }

   public boolean sendMessages(CommandSender sender, String[] messages, boolean ignoreCooldown) {
      if (sender instanceof Player && !ignoreCooldown) {
         if (!this.sentList.containsKey(sender.getName())) {
            sendMessages(sender, messages);
            this.sentList.put(sender.getName(), System.currentTimeMillis());
            return true;
         } else {
            long time = System.currentTimeMillis();
            if (time >= (Long)this.sentList.get(sender.getName()) + (long)this.cooldown) {
               sendMessages(sender, messages);
               this.sentList.put(sender.getName(), System.currentTimeMillis());
               return true;
            } else {
               return false;
            }
         }
      } else {
         sendMessages(sender, messages);
         return true;
      }
   }

   public boolean sendMessages(CommandSender sender, Collection messages, boolean ignoreCooldown) {
      return this.sendMessages(sender, (String[])messages.toArray(new String[0]), ignoreCooldown);
   }

   private static void sendMessages(CommandSender sender, String[] messages) {
      for(String s : messages) {
         sender.sendMessage(s);
      }

   }

   public int getCooldown() {
      return this.cooldown;
   }
}
