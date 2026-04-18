package net.citizensnpcs.api.ai.speech.event;

import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.ai.speech.Talkable;
import net.citizensnpcs.api.ai.speech.VocalChord;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpeechEvent extends Event implements Cancellable {
   private boolean cancelled = false;
   SpeechContext context;
   String message;
   Talkable target;
   VocalChord vocalChord;
   private static final HandlerList handlers = new HandlerList();

   public SpeechEvent(Talkable target, SpeechContext context, String message, VocalChord vocalChord) {
      super();
      this.target = target;
      this.context = context;
      this.vocalChord = vocalChord;
      this.message = message;
   }

   public SpeechContext getContext() {
      return this.context;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public String getMessage() {
      return this.message;
   }

   public String getVocalChordName() {
      return this.vocalChord.getName();
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   public void setMessage(String formattedMessage) {
      this.message = formattedMessage;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
