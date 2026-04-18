package net.citizensnpcs.trait.text;

import net.citizensnpcs.api.util.Messaging;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class TextRemovePrompt extends StringPrompt {
   private final Text text;

   public TextRemovePrompt(Text text) {
      super();
      this.text = text;
   }

   public Prompt acceptInput(ConversationContext context, String input) {
      Player player = (Player)context.getForWhom();

      try {
         int index = Integer.parseInt(input);
         if (!this.text.hasIndex(index)) {
            Messaging.sendErrorTr(player, "citizens.editors.text.invalid-index", index);
            return new TextStartPrompt(this.text);
         } else {
            this.text.remove(index);
            Messaging.sendTr(player, "citizens.editors.text.removed-entry", index);
            return new TextStartPrompt(this.text);
         }
      } catch (NumberFormatException var5) {
         if (input.equalsIgnoreCase("page")) {
            context.setSessionData("previous", this);
            return new PageChangePrompt(this.text);
         } else {
            Messaging.sendErrorTr(player, "citizens.editors.text.invalid-input");
            return new TextStartPrompt(this.text);
         }
      }
   }

   public String getPromptText(ConversationContext context) {
      this.text.sendPage((Player)context.getForWhom(), 1);
      return Messaging.tr("citizens.editors.text.remove-prompt");
   }
}
