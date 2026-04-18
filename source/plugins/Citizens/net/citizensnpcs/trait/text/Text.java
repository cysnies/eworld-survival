package net.citizensnpcs.trait.text;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import net.citizensnpcs.Settings;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Paginator;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.trait.Toggleable;
import net.citizensnpcs.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class Text extends Trait implements Runnable, Toggleable, Listener, ConversationAbandonedListener {
   private final Map cooldowns = Maps.newHashMap();
   private int currentIndex;
   private String itemInHandPattern = "default";
   private final Plugin plugin;
   private boolean randomTalker;
   private double range;
   private boolean realisticLooker;
   private boolean talkClose;
   private final List text;
   private static Random RANDOM = Util.getFastRandom();

   public Text() {
      super("text");
      this.randomTalker = Settings.Setting.DEFAULT_RANDOM_TALKER.asBoolean();
      this.range = Settings.Setting.DEFAULT_TALK_CLOSE_RANGE.asDouble();
      this.realisticLooker = Settings.Setting.DEFAULT_REALISTIC_LOOKING.asBoolean();
      this.talkClose = Settings.Setting.DEFAULT_TALK_CLOSE.asBoolean();
      this.text = new ArrayList();
      this.plugin = CitizensAPI.getPlugin();
   }

   void add(String string) {
      this.text.add(string);
   }

   public void conversationAbandoned(ConversationAbandonedEvent event) {
      Bukkit.dispatchCommand((Player)event.getContext().getForWhom(), "npc text");
   }

   void edit(int index, String newText) {
      this.text.set(index, newText);
   }

   public Editor getEditor(final Player player) {
      final Conversation conversation = (new ConversationFactory(this.plugin)).addConversationAbandonedListener(this).withLocalEcho(false).withEscapeSequence("/npc text").withEscapeSequence("exit").withModality(false).withFirstPrompt(new TextStartPrompt(this)).buildConversation(player);
      return new Editor() {
         public void begin() {
            Messaging.sendTr(player, "citizens.editors.text.begin");
            conversation.begin();
         }

         public void end() {
            Messaging.sendTr(player, "citizens.editors.text.end");
            conversation.abandon();
         }
      };
   }

   boolean hasIndex(int index) {
      return index >= 0 && this.text.size() > index;
   }

   public void load(DataKey key) throws NPCLoadException {
      this.text.clear();

      for(DataKey sub : key.getIntegerSubKeys()) {
         this.text.add(sub.getString(""));
      }

      for(DataKey sub : key.getRelative("text").getIntegerSubKeys()) {
         this.text.add(sub.getString(""));
      }

      if (this.text.isEmpty()) {
         this.populateDefaultText();
      }

      this.talkClose = key.getBoolean("talk-close", this.talkClose);
      this.realisticLooker = key.getBoolean("realistic-looking", this.realisticLooker);
      this.randomTalker = key.getBoolean("random-talker", this.randomTalker);
      this.range = key.getDouble("range", this.range);
      this.itemInHandPattern = key.getString("talkitem", this.itemInHandPattern);
   }

   @EventHandler
   public void onRightClick(NPCRightClickEvent event) {
      if (event.getNPC().equals(this.npc)) {
         String localPattern = this.itemInHandPattern.equals("default") ? Settings.Setting.TALK_ITEM.asString() : this.itemInHandPattern;
         if (Util.matchesItemInHand(event.getClicker(), localPattern) && !this.shouldTalkClose()) {
            this.sendText(event.getClicker());
         }

      }
   }

   private void populateDefaultText() {
      this.text.addAll(Settings.Setting.DEFAULT_TEXT.asList());
   }

   void remove(int index) {
      this.text.remove(index);
   }

   public void run() {
      if (this.talkClose && this.npc.isSpawned()) {
         for(Entity search : this.npc.getBukkitEntity().getNearbyEntities(this.range, this.range, this.range)) {
            if (search instanceof Player) {
               Player player = (Player)search;
               Date cooldown = (Date)this.cooldowns.get(player.getName());
               if (cooldown != null) {
                  if (!(new Date()).after(cooldown)) {
                     return;
                  }

                  this.cooldowns.remove(player.getName());
               }

               if (!this.sendText(player)) {
                  return;
               }

               Date wait = new Date();
               int secondsDelta = RANDOM.nextInt(Settings.Setting.TALK_CLOSE_MAXIMUM_COOLDOWN.asInt()) + Settings.Setting.TALK_CLOSE_MINIMUM_COOLDOWN.asInt();
               if (secondsDelta <= 0) {
                  return;
               }

               long millisecondsDelta = TimeUnit.MILLISECONDS.convert((long)secondsDelta, TimeUnit.SECONDS);
               wait.setTime(wait.getTime() + millisecondsDelta);
               this.cooldowns.put(player.getName(), wait);
            }
         }

      }
   }

   public void save(DataKey key) {
      key.setBoolean("talk-close", this.talkClose);
      key.setBoolean("random-talker", this.randomTalker);
      key.setBoolean("realistic-looking", this.realisticLooker);
      key.setDouble("range", this.range);
      key.setString("talkitem", this.itemInHandPattern);

      for(int i = 0; i < 100; ++i) {
         key.removeKey(String.valueOf(i));
      }

      key.removeKey("text");

      for(int i = 0; i < this.text.size(); ++i) {
         key.setString("text." + String.valueOf(i), (String)this.text.get(i));
      }

   }

   boolean sendPage(Player player, int page) {
      Paginator paginator = (new Paginator()).header(this.npc.getName() + "'s Text Entries");

      for(int i = 0; i < this.text.size(); ++i) {
         paginator.addLine("<a>" + i + " <7>- <e>" + (String)this.text.get(i));
      }

      return paginator.sendPage(player, page);
   }

   private boolean sendText(Player player) {
      if (!player.hasPermission("citizens.admin") && !player.hasPermission("citizens.npc.talk")) {
         return false;
      } else if (this.text.size() == 0) {
         return false;
      } else {
         int index = 0;
         if (this.randomTalker) {
            index = RANDOM.nextInt(this.text.size());
         } else {
            if (this.currentIndex > this.text.size() - 1) {
               this.currentIndex = 0;
            }

            index = this.currentIndex++;
         }

         this.npc.getDefaultSpeechController().speak(new SpeechContext((String)this.text.get(index), player));
         return true;
      }
   }

   void setItemInHandPattern(String pattern) {
      this.itemInHandPattern = pattern;
   }

   void setRange(double range) {
      this.range = range;
   }

   boolean shouldTalkClose() {
      return this.talkClose;
   }

   public boolean toggle() {
      return this.talkClose = !this.talkClose;
   }

   boolean toggleRandomTalker() {
      return this.randomTalker = !this.randomTalker;
   }

   boolean toggleRealisticLooking() {
      return this.realisticLooker = !this.realisticLooker;
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("Text{talk-close=" + this.talkClose + ",text=");

      for(String line : this.text) {
         builder.append(line + ",");
      }

      builder.append("}");
      return builder.toString();
   }
}
