package net.citizensnpcs.api.trait.trait;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.VocalChord;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;

public class Speech extends Trait {
   @Persist("")
   private String defaultVocalChord = "chat";
   public static final String DEFAULT_VOCAL_CHORD = "chat";

   public Speech() {
      super("speech");
   }

   public String getDefaultVocalChord() {
      return this.defaultVocalChord;
   }

   public void setDefaultVocalChord(Class clazz) {
      this.defaultVocalChord = CitizensAPI.getSpeechFactory().getVocalChordName(clazz);
   }

   public String toString() {
      return "DefaultVocalChord{" + this.defaultVocalChord + "}";
   }
}
