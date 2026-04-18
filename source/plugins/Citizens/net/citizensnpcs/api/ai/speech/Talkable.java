package net.citizensnpcs.api.ai.speech;

import org.bukkit.entity.LivingEntity;

public interface Talkable extends Comparable {
   LivingEntity getEntity();

   String getName();

   void talkNear(SpeechContext var1, String var2, VocalChord var3);

   void talkTo(SpeechContext var1, String var2, VocalChord var3);
}
