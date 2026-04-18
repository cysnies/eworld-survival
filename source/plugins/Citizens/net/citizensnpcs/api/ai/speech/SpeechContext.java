package net.citizensnpcs.api.ai.speech;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.LivingEntity;

public class SpeechContext implements Iterable {
   private String message;
   private List recipients;
   private Talkable talker;

   public SpeechContext() {
      super();
      this.recipients = Collections.emptyList();
      this.talker = null;
   }

   public SpeechContext(NPC talker, String message) {
      super();
      this.recipients = Collections.emptyList();
      this.talker = null;
      if (talker != null) {
         this.setTalker(talker.getBukkitEntity());
      }

      this.message = message;
   }

   public SpeechContext(NPC talker, String message, LivingEntity recipient) {
      this(talker, message);
      if (recipient != null) {
         this.addRecipient(recipient);
      }

   }

   public SpeechContext(String message) {
      super();
      this.recipients = Collections.emptyList();
      this.talker = null;
      this.message = message;
   }

   public SpeechContext(String message, LivingEntity recipient) {
      super();
      this.recipients = Collections.emptyList();
      this.talker = null;
      this.message = message;
      if (recipient != null) {
         this.addRecipient(recipient);
      }

   }

   public SpeechContext addRecipient(LivingEntity entity) {
      if (this.recipients.isEmpty()) {
         this.recipients = new ArrayList();
      }

      this.recipients.add(CitizensAPI.getSpeechFactory().newTalkableEntity(entity));
      return this;
   }

   public SpeechContext addRecipients(List talkables) {
      if (this.recipients.isEmpty()) {
         this.recipients = new ArrayList();
      }

      this.recipients.addAll(talkables);
      return this;
   }

   public String getMessage() {
      return this.message;
   }

   public Talkable getTalker() {
      return this.talker;
   }

   public boolean hasRecipients() {
      return !this.recipients.isEmpty();
   }

   public Iterator iterator() {
      Iterator<Talkable> itr = this.recipients.iterator();
      return itr;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   public void setTalker(LivingEntity talker) {
      this.talker = CitizensAPI.getSpeechFactory().newTalkableEntity(talker);
   }

   public int size() {
      return this.recipients.size();
   }
}
