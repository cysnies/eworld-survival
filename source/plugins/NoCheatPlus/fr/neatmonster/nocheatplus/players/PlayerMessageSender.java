package fr.neatmonster.nocheatplus.players;

import fr.neatmonster.nocheatplus.utilities.OnDemandTickListener;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.entity.Player;

public class PlayerMessageSender extends OnDemandTickListener {
   private final List messageEntries = new LinkedList();

   public PlayerMessageSender() {
      super();
   }

   public boolean delegateTick(int tick, long timeLast) {
      MessageEntry[] entries;
      synchronized(this.messageEntries) {
         entries = new MessageEntry[this.messageEntries.size()];
         this.messageEntries.toArray(entries);
         this.messageEntries.clear();
      }

      for(int i = 0; i < entries.length; ++i) {
         MessageEntry entry = entries[i];
         Player player = DataManager.getPlayerExact(entry.playerName);
         if (player != null && player.isOnline()) {
            player.sendMessage(entry.message);
         }
      }

      synchronized(this.messageEntries) {
         if (this.messageEntries.isEmpty()) {
            this.unRegister(true);
         }

         return true;
      }
   }

   public void sendMessageThreadSafe(String playerName, String message) {
      MessageEntry entry = new MessageEntry(playerName.toLowerCase(), message);
      synchronized(this.messageEntries) {
         this.messageEntries.add(entry);
         this.register();
      }
   }

   private final class MessageEntry {
      public final String playerName;
      public final String message;

      public MessageEntry(String playerName, String message) {
         super();
         this.playerName = playerName;
         this.message = message;
      }
   }
}
