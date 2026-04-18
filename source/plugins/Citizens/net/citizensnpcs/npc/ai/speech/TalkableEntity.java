package net.citizensnpcs.npc.ai.speech;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.ai.speech.Talkable;
import net.citizensnpcs.api.ai.speech.VocalChord;
import net.citizensnpcs.api.ai.speech.event.SpeechBystanderEvent;
import net.citizensnpcs.api.ai.speech.event.SpeechTargetedEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class TalkableEntity implements Talkable {
   LivingEntity entity;

   public TalkableEntity(LivingEntity entity) {
      super();
      this.entity = entity;
   }

   public TalkableEntity(NPC npc) {
      super();
      this.entity = npc.getBukkitEntity();
   }

   public TalkableEntity(Player player) {
      super();
      this.entity = player;
   }

   public int compareTo(Object o) {
      if (!(o instanceof LivingEntity)) {
         return -1;
      } else if (CitizensAPI.getNPCRegistry().isNPC((LivingEntity)o) && CitizensAPI.getNPCRegistry().isNPC(this.entity) && CitizensAPI.getNPCRegistry().getNPC((LivingEntity)o).getId() == CitizensAPI.getNPCRegistry().getNPC(this.entity).getId()) {
         return 0;
      } else {
         return (LivingEntity)o == this.entity ? 0 : 1;
      }
   }

   public LivingEntity getEntity() {
      return this.entity;
   }

   public String getName() {
      if (CitizensAPI.getNPCRegistry().isNPC(this.entity)) {
         return CitizensAPI.getNPCRegistry().getNPC(this.entity).getName();
      } else {
         return this.entity instanceof Player ? ((Player)this.entity).getName() : this.entity.getType().name().replace("_", " ");
      }
   }

   private void talk(String message) {
      if (this.entity instanceof Player && !CitizensAPI.getNPCRegistry().isNPC(this.entity)) {
         Messaging.send((Player)this.entity, message);
      }

   }

   public void talkNear(SpeechContext context, String text, VocalChord vocalChord) {
      SpeechBystanderEvent event = new SpeechBystanderEvent(this, context, text, vocalChord);
      Bukkit.getServer().getPluginManager().callEvent(event);
      if (!event.isCancelled()) {
         this.talk(event.getMessage());
      }
   }

   public void talkTo(SpeechContext context, String text, VocalChord vocalChord) {
      SpeechTargetedEvent event = new SpeechTargetedEvent(this, context, text, vocalChord);
      Bukkit.getServer().getPluginManager().callEvent(event);
      if (!event.isCancelled()) {
         this.talk(event.getMessage());
      }
   }
}
