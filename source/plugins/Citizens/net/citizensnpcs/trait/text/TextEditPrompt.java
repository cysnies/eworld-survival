package net.citizensnpcs.trait.text;

import net.citizensnpcs.api.util.Messaging;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class TextEditPrompt extends StringPrompt {
   private final Text text;

   public TextEditPrompt(Text text) {
      super();
      this.text = text;
   }

   public Prompt acceptInput(ConversationContext context, String input) {
      int index = (Integer)context.getSessionData("index");
      this.text.edit(index, input);
      Messaging.sendTr((CommandSender)context.getForWhom(), "citizens.editors.text.edited-text", index, input);
      return new TextStartPrompt(this.text);
   }

   public String getPromptText(ConversationContext context) {
      return ChatColor.GREEN + Messaging.tr("citizens.editors.text.edit-prompt");
   }
}
