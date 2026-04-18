package net.citizensnpcs.npc.ai.speech;

import java.util.Collections;
import java.util.List;
import net.citizensnpcs.Settings;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.ai.speech.Talkable;
import net.citizensnpcs.api.ai.speech.VocalChord;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class Chat implements VocalChord {
   public final String VOCAL_CHORD_NAME = "chat";

   public Chat() {
      super();
   }

   public String getName() {
      return "chat";
   }

   public void talk(SpeechContext context) {
      if (context.getTalker() != null) {
         NPC npc = CitizensAPI.getNPCRegistry().getNPC(context.getTalker().getEntity());
         if (npc != null) {
            if (!context.hasRecipients()) {
               String text = Settings.Setting.CHAT_FORMAT.asString().replace("<npc>", npc.getName()).replace("<text>", context.getMessage());
               this.talkToBystanders(npc, text, context);
            } else if (context.size() <= 1) {
               String text = Settings.Setting.CHAT_FORMAT_TO_TARGET.asString().replace("<npc>", npc.getName()).replace("<text>", context.getMessage());
               String targetName = "";

               for(Talkable entity : context) {
                  entity.talkTo(context, text, this);
                  targetName = entity.getName();
               }

               if (Settings.Setting.CHAT_BYSTANDERS_HEAR_TARGETED_CHAT.asBoolean()) {
                  String bystanderText = Settings.Setting.CHAT_FORMAT_TO_BYSTANDERS.asString().replace("<npc>", npc.getName()).replace("<target>", targetName).replace("<text>", context.getMessage());
                  this.talkToBystanders(npc, bystanderText, context);
               }
            } else {
               String text = Settings.Setting.CHAT_FORMAT_TO_TARGET.asString().replace("<npc>", npc.getName()).replace("<text>", context.getMessage());
               List<String> targetNames = Collections.emptyList();

               for(Talkable entity : context) {
                  entity.talkTo(context, text, this);
                  targetNames.add(entity.getName());
               }

               if (Settings.Setting.CHAT_BYSTANDERS_HEAR_TARGETED_CHAT.asBoolean()) {
                  String targets = "";
                  int max = Settings.Setting.CHAT_MAX_NUMBER_OF_TARGETS.asInt();
                  String[] format = Settings.Setting.CHAT_FORMAT_WITH_TARGETS_TO_BYSTANDERS.asString().split("\\|");
                  if (format.length != 4) {
                     Messaging.severe("npc.chat.format.with-target-to-bystanders invalid!");
                  }

                  if (max == 1) {
                     targets = format[0].replace("<npc>", (CharSequence)targetNames.get(0)) + format[3];
                  } else if (max != 2 && targetNames.size() != 2) {
                     if (max >= 3) {
                        targets = format[0].replace("<npc>", (CharSequence)targetNames.get(0));
                        int x = 1;

                        for(x = 1; x < max - 1 && targetNames.size() - 1 != x; ++x) {
                           targets = targets + format[1].replace("<npc>", (CharSequence)targetNames.get(x));
                        }

                        if (targetNames.size() == max) {
                           targets = targets + format[2].replace("<npc>", (CharSequence)targetNames.get(x));
                        } else {
                           targets = targets + format[3];
                        }
                     }
                  } else if (targetNames.size() == 2) {
                     targets = format[0].replace("<npc>", (CharSequence)targetNames.get(0)) + format[2].replace("<npc>", (CharSequence)targetNames.get(1));
                  } else {
                     targets = format[0].replace("<npc>", (CharSequence)targetNames.get(0)) + format[1].replace("<npc>", (CharSequence)targetNames.get(1)) + format[3];
                  }

                  String bystanderText = Settings.Setting.CHAT_FORMAT_WITH_TARGETS_TO_BYSTANDERS.asString().replace("<npc>", npc.getName()).replace("<targets>", targets).replace("<text>", context.getMessage());
                  this.talkToBystanders(npc, bystanderText, context);
               }
            }
         }
      }
   }

   private void talkToBystanders(NPC npc, String text, SpeechContext context) {
      for(Entity bystander : npc.getBukkitEntity().getNearbyEntities(Settings.Setting.CHAT_RANGE.asDouble(), Settings.Setting.CHAT_RANGE.asDouble(), Settings.Setting.CHAT_RANGE.asDouble())) {
         if (bystander instanceof LivingEntity) {
            if (context.hasRecipients()) {
               for(Talkable target : context) {
                  if (target.getEntity() != bystander) {
                     (new TalkableEntity((LivingEntity)bystander)).talkNear(context, text, this);
                  }
               }
            } else {
               (new TalkableEntity((LivingEntity)bystander)).talkNear(context, text, this);
            }
         }
      }

   }
}
