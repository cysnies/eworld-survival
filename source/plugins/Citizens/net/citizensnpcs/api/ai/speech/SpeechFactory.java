package net.citizensnpcs.api.ai.speech;

import org.bukkit.entity.LivingEntity;

public interface SpeechFactory {
   VocalChord getVocalChord(Class var1);

   VocalChord getVocalChord(String var1);

   String getVocalChordName(Class var1);

   boolean isRegistered(String var1);

   Talkable newTalkableEntity(LivingEntity var1);

   void register(Class var1, String var2);
}
