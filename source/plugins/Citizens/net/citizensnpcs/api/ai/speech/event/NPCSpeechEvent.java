package net.citizensnpcs.api.ai.speech.event;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.event.NPCEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class NPCSpeechEvent extends NPCEvent implements Cancellable {
   private boolean cancelled = false;
   private SpeechContext context;
   private String vocalChordName;
   private static final HandlerList handlers = new HandlerList();

   public NPCSpeechEvent(SpeechContext context, String vocalChordName) {
      super(CitizensAPI.getNPCRegistry().getNPC(context.getTalker().getEntity()));
      this.vocalChordName = vocalChordName;
      this.context = context;
   }

   public SpeechContext getContext() {
      return this.context;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public String getVocalChordName() {
      return this.vocalChordName;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   public void setVocalChord(String name) {
      this.vocalChordName = name;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
