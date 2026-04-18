package net.citizensnpcs.trait.text;

import net.citizensnpcs.api.util.Messaging;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class TextAddPrompt extends StringPrompt {
   private final Text text;

   public TextAddPrompt(Text text) {
      super();
      this.text = text;
   }

   public Prompt acceptInput(ConversationContext context, String input) {
      this.text.add(input);
      Messaging.sendTr((Player)context.getForWhom(), "citizens.editors.text.added-entry", input);
      return new TextStartPrompt(this.text);
   }

   public String getPromptText(ConversationContext context) {
      return ChatColor.GREEN + Messaging.tr("citizens.editors.text.add-prompt");
   }
}
