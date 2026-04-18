package chat;

import java.util.HashMap;
import java.util.List;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatLimit implements Listener {
   private String pn;
   private String per_chat_chatLimit;
   private String per_chat_badWords;
   private String per_chat_limit_vip;
   private int chatSameSpeed;
   private int chatSameSpeedVip;
   private int chatDifferSpeed;
   private int chatDifferSpeedVip;
   private int chatMaxLength;
   private int chatMinLength;
   private boolean badCancel;
   private String replaceWord;
   private List badWords;
   private HashMap chatHash;
   private HashMap lastChatHash;

   public ChatLimit(Chat main) {
      super();
      this.pn = main.getPn();
      this.chatHash = new HashMap();
      this.lastChatHash = new HashMap();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      main.getPm().registerEvents(this, main);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
      try {
         Player p = e.getPlayer();
         String msg = e.getMessage();
         long now = System.currentTimeMillis();
         Long last = (Long)this.lastChatHash.get(p);
         String lastString = (String)this.chatHash.get(p);
         if (!UtilPer.hasPer(p, this.per_chat_chatLimit)) {
            if (last != null) {
               long lastTime = now - last;
               String vip = "§m";
               if (UtilPer.hasPer(p, this.per_chat_limit_vip)) {
                  vip = "";
               }

               if (this.chatHash.containsKey(p) && lastString.equalsIgnoreCase(msg)) {
                  int limit = this.chatSameSpeed;
                  if (UtilPer.hasPer(p, this.per_chat_limit_vip)) {
                     limit = this.chatSameSpeedVip;
                  }

                  if (lastTime < (long)limit) {
                     e.setCancelled(true);
                     p.sendMessage(UtilFormat.format(this.pn, "spam", new Object[]{limit, vip}));
                     return;
                  }
               }

               int limit = this.chatDifferSpeed;
               if (UtilPer.hasPer(p, this.per_chat_limit_vip)) {
                  limit = this.chatDifferSpeedVip;
               }

               if (lastTime < (long)limit) {
                  e.setCancelled(true);
                  p.sendMessage(UtilFormat.format(this.pn, "spam2", new Object[]{limit, vip}));
                  return;
               }
            }

            int length = msg.length();
            if (length < this.chatMinLength || length > this.chatMaxLength) {
               e.setCancelled(true);
               p.sendMessage(UtilFormat.format(this.pn, "lengthErr", new Object[]{this.chatMinLength, this.chatMaxLength}));
               return;
            }
         }

         this.lastChatHash.put(p, now);
         this.chatHash.put(p, msg);
         if (!UtilPer.hasPer(p, this.per_chat_badWords)) {
            for(String check : this.badWords) {
               if (msg.indexOf(check) != -1) {
                  if (this.badCancel) {
                     e.setCancelled(true);
                  } else {
                     msg = msg.replace(check, this.replaceWord);
                     e.setMessage(msg);
                  }

                  p.sendMessage(UtilFormat.format(this.pn, "badWord", new Object[]{check}));
                  return;
               }
            }
         }
      } catch (Exception e1) {
         e1.printStackTrace();
         System.out.println(">>>>>>>>>>>>异常位置4");
      }

   }

   @EventHandler
   public void playerQuit(PlayerQuitEvent e) {
      this.leave(e.getPlayer());
   }

   private void loadConfig(YamlConfiguration config) {
      this.per_chat_chatLimit = config.getString("per_chat_chatLimit");
      this.per_chat_badWords = config.getString("per_chat_badWords");
      this.per_chat_limit_vip = config.getString("per_chat_limit_vip");
      this.chatSameSpeed = config.getInt("chatSameSpeed");
      this.chatSameSpeedVip = config.getInt("chatSameSpeedVip");
      this.chatDifferSpeed = config.getInt("chatDifferSpeed");
      this.chatDifferSpeedVip = config.getInt("chatDifferSpeedVip");
      this.chatMaxLength = config.getInt("chatMaxLength");
      this.chatMinLength = config.getInt("chatMinLength");
      this.badCancel = config.getBoolean("badCancel");
      this.replaceWord = config.getString("replaceWord");
      this.badWords = config.getStringList("badWords");
   }

   private void leave(Player p) {
      this.chatHash.remove(p);
      this.lastChatHash.remove(p);
   }
}
