package net.citizensnpcs.trait.text;

import net.citizensnpcs.api.util.Messaging;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

public class PageChangePrompt extends NumericPrompt {
   private final Text text;

   public PageChangePrompt(Text text) {
      super();
      this.text = text;
   }

   public Prompt acceptValidatedInput(ConversationContext context, Number input) {
      Player player = (Player)context.getForWhom();
      if (!this.text.sendPage(player, input.intValue())) {
         Messaging.sendErrorTr(player, "citizens.editors.text.invalid-page");
         return new TextStartPrompt(this.text);
      } else {
         return (Prompt)context.getSessionData("previous");
      }
   }

   public String getFailedValidationText(ConversationContext context, String input) {
      return ChatColor.RED + Messaging.tr("citizens.editors.text.invalid-page");
   }

   public String getPromptText(ConversationContext context) {
      return Messaging.tr("citizens.editors.text.change-page-prompt");
   }
}
