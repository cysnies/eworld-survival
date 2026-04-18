package net.citizensnpcs.npc.ai.speech;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import net.citizensnpcs.api.ai.speech.SpeechFactory;
import net.citizensnpcs.api.ai.speech.Talkable;
import net.citizensnpcs.api.ai.speech.VocalChord;
import org.bukkit.entity.LivingEntity;

public class CitizensSpeechFactory implements SpeechFactory {
   Map registered = new HashMap();

   public CitizensSpeechFactory() {
      super();
   }

   public VocalChord getVocalChord(Class clazz) {
      try {
         return (VocalChord)clazz.newInstance();
      } catch (InstantiationException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      }

      return null;
   }

   public VocalChord getVocalChord(String name) {
      if (this.isRegistered(name)) {
         try {
            return (VocalChord)((Class)this.registered.get(name.toLowerCase())).newInstance();
         } catch (InstantiationException e) {
            e.printStackTrace();
         } catch (IllegalAccessException e) {
            e.printStackTrace();
         }
      }

      return null;
   }

   public String getVocalChordName(Class clazz) {
      for(Map.Entry vocalChord : this.registered.entrySet()) {
         if (vocalChord.getValue() == clazz) {
            return (String)vocalChord.getKey();
         }
      }

      return null;
   }

   public boolean isRegistered(String name) {
      return this.registered.containsKey(name.toLowerCase());
   }

   public Talkable newTalkableEntity(LivingEntity entity) {
      return entity == null ? null : new TalkableEntity(entity);
   }

   public void register(Class clazz, String name) {
      Preconditions.checkNotNull(name, "info cannot be null");
      Preconditions.checkNotNull(clazz, "vocalchord cannot be null");
      if (this.registered.containsKey(name.toLowerCase())) {
         throw new IllegalArgumentException("vocalchord name already registered");
      } else {
         this.registered.put(name.toLowerCase(), clazz);
      }
   }
}
