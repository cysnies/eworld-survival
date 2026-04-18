package net.citizensnpcs.commands;

import java.util.List;
import net.citizensnpcs.Settings;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.NPCSelector;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

public class SelectionPrompt extends NumericPrompt {
   private final List choices;
   private final NPCSelector selector;

   public SelectionPrompt(NPCSelector selector, List possible) {
      super();
      this.choices = possible;
      this.selector = selector;
   }

   protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
      boolean found = false;

      for(NPC npc : this.choices) {
         if (input.intValue() == npc.getId()) {
            found = true;
            break;
         }
      }

      CommandSender sender = (CommandSender)context.getForWhom();
      if (!found) {
         Messaging.sendErrorTr(sender, "citizens.conversations.selection.invalid-choice", input);
         return this;
      } else {
         NPC toSelect = CitizensAPI.getNPCRegistry().getById(input.intValue());
         this.selector.select(sender, toSelect);
         Messaging.sendWithNPC(sender, Settings.Setting.SELECTION_MESSAGE.asString(), toSelect);
         return null;
      }
   }

   public String getPromptText(ConversationContext context) {
      String text = Messaging.tr("citizens.editors.selection.start-prompt");

      for(NPC npc : this.choices) {
         text = text + "\n    - " + npc.getId();
      }

      return text;
   }

   public static void start(NPCSelector selector, Player player, List possible) {
      Conversation conversation = (new ConversationFactory(CitizensAPI.getPlugin())).withLocalEcho(false).withEscapeSequence("exit").withModality(false).withFirstPrompt(new SelectionPrompt(selector, possible)).buildConversation(player);
      conversation.begin();
   }
}
