package com.earth2me.essentials.chat;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import net.ess3.api.IEssentials;
import net.ess3.api.events.LocalChatSpyEvent;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class EssentialsChatPlayerListenerNormal extends EssentialsChatPlayer {
   public EssentialsChatPlayerListenerNormal(Server server, IEssentials ess, Map chatStorage) {
      super(server, ess, chatStorage);
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerChat(AsyncPlayerChatEvent event) {
      if (!this.isAborted(event)) {
         long radius = (long)this.ess.getSettings().getChatRadius();
         if (radius >= 1L) {
            radius *= radius;
            ChatStore chatStore = this.getChatStore(event);
            User user = chatStore.getUser();
            chatStore.setRadius(radius);
            if (event.getMessage().length() > 1 && chatStore.getType().length() > 0) {
               StringBuilder permission = new StringBuilder();
               permission.append("essentials.chat.").append(chatStore.getType());
               if (user.isAuthorized(permission.toString())) {
                  StringBuilder format = new StringBuilder();
                  format.append(chatStore.getType()).append("Format");
                  event.setMessage(event.getMessage().substring(1));
                  event.setFormat(I18n._(format.toString(), new Object[]{event.getFormat()}));
               } else {
                  StringBuilder errorMsg = new StringBuilder();
                  errorMsg.append("notAllowedTo").append(chatStore.getType().substring(0, 1).toUpperCase(Locale.ENGLISH)).append(chatStore.getType().substring(1));
                  user.sendMessage(I18n._(errorMsg.toString(), new Object[0]));
                  event.setCancelled(true);
               }
            } else {
               Location loc = user.getLocation();
               World world = loc.getWorld();
               if (this.charge(event, chatStore)) {
                  Set<Player> outList = event.getRecipients();
                  Set<Player> spyList = new HashSet();

                  try {
                     outList.add(event.getPlayer());
                  } catch (UnsupportedOperationException ex) {
                     if (this.ess.getSettings().isDebug()) {
                        this.ess.getLogger().log(Level.INFO, "Plugin triggered custom chat event, local chat handling aborted.", ex);
                     }

                     return;
                  }

                  String format = event.getFormat();
                  event.setFormat(I18n._("chatTypeLocal", new Object[0]).concat(event.getFormat()));
                  logger.info(I18n._("localFormat", new Object[]{user.getName(), event.getMessage()}));
                  Iterator<Player> it = outList.iterator();

                  while(it.hasNext()) {
                     Player onlinePlayer = (Player)it.next();
                     User onlineUser = this.ess.getUser(onlinePlayer);
                     if (!onlineUser.equals(user)) {
                        boolean abort = false;
                        Location playerLoc = onlineUser.getLocation();
                        if (playerLoc.getWorld() != world) {
                           abort = true;
                        } else {
                           double delta = playerLoc.distanceSquared(loc);
                           if (delta > (double)chatStore.getRadius()) {
                              abort = true;
                           }
                        }

                        if (abort) {
                           if (onlineUser.isAuthorized("essentials.chat.spy")) {
                              spyList.add(onlinePlayer);
                           }

                           it.remove();
                        }
                     }
                  }

                  if (outList.size() < 2) {
                     user.sendMessage(I18n._("localNoOne", new Object[0]));
                  }

                  LocalChatSpyEvent spyEvent = new LocalChatSpyEvent(event.isAsynchronous(), event.getPlayer(), format, event.getMessage(), spyList);
                  this.server.getPluginManager().callEvent(spyEvent);
                  if (!spyEvent.isCancelled()) {
                     for(Player onlinePlayer : spyEvent.getRecipients()) {
                        onlinePlayer.sendMessage(String.format(spyEvent.getFormat(), user.getDisplayName(), spyEvent.getMessage()));
                     }
                  }

               }
            }
         }
      }
   }
}
