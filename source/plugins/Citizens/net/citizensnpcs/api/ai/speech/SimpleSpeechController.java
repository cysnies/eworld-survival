package net.citizensnpcs.api.ai.speech;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.event.NPCSpeechEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Speech;
import org.bukkit.Bukkit;

public class SimpleSpeechController implements SpeechController {
   NPC npc;

   public SimpleSpeechController(NPC npc) {
      super();
      this.npc = npc;
   }

   public void speak(SpeechContext context) {
      this.speak(context, ((Speech)this.npc.getTrait(Speech.class)).getDefaultVocalChord());
   }

   public void speak(SpeechContext context, String vocalChordName) {
      context.setTalker(this.npc.getBukkitEntity());
      NPCSpeechEvent event = new NPCSpeechEvent(context, vocalChordName);
      Bukkit.getServer().getPluginManager().callEvent(event);
      if (!event.isCancelled()) {
         CitizensAPI.getSpeechFactory().getVocalChord(event.getVocalChordName()).talk(context);
      }
   }
}
