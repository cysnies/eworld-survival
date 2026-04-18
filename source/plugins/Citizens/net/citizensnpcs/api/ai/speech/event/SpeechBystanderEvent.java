package net.citizensnpcs.api.ai.speech.event;

import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.ai.speech.Talkable;
import net.citizensnpcs.api.ai.speech.VocalChord;

public class SpeechBystanderEvent extends SpeechEvent {
   public SpeechBystanderEvent(Talkable target, SpeechContext context, String message, VocalChord vocalChord) {
      super(target, context, message, vocalChord);
   }
}
