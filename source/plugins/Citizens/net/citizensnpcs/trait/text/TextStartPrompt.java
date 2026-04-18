package net.citizensnpcs.trait.text;

import net.citizensnpcs.Settings;
import net.citizensnpcs.api.util.Messaging;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class TextStartPrompt extends StringPrompt {
   private final Text text;

   public TextStartPrompt(Text text) {
      super();
      this.text = text;
   }

   public Prompt acceptInput(ConversationContext context, String original) {
      String[] parts = ChatColor.stripColor(original.trim()).split(" ");
      String input = parts[0];
      CommandSender sender = (CommandSender)context.getForWhom();
      if (input.equalsIgnoreCase("add")) {
         return new TextAddPrompt(this.text);
      } else if (input.equalsIgnoreCase("edit")) {
         return new TextEditStartPrompt(this.text);
      } else if (input.equalsIgnoreCase("remove")) {
         return new TextRemovePrompt(this.text);
      } else {
         if (input.equalsIgnoreCase("random")) {
            Messaging.sendTr(sender, "citizens.editors.text.random-talker-set", this.text.toggleRandomTalker());
         } else if (input.equalsIgnoreCase("realistic looking")) {
            Messaging.sendTr(sender, "citizens.editors.text.realistic-looking-set", this.text.toggleRealisticLooking());
         } else if (!input.equalsIgnoreCase("close") && !input.equalsIgnoreCase("talk-close")) {
            if (input.equalsIgnoreCase("range")) {
               try {
                  double range = Math.min(Math.max((double)0.0F, Double.parseDouble(parts[1])), Settings.Setting.MAX_TEXT_RANGE.asDouble());
                  this.text.setRange(range);
                  Messaging.sendTr(sender, "citizens.editors.text.range-set", range);
               } catch (NumberFormatException var8) {
                  Messaging.sendErrorTr(sender, "citizens.editors.text.invalid-range");
               } catch (ArrayIndexOutOfBoundsException var9) {
                  Messaging.sendErrorTr(sender, "citizens.editors.text.invalid-range");
               }
            } else if (input.equalsIgnoreCase("item")) {
               if (parts.length > 1) {
                  this.text.setItemInHandPattern(parts[1]);
                  Messaging.sendTr(sender, "citizens.editors.text.talk-item-set", parts[1]);
               }
            } else if (input.equalsIgnoreCase("help")) {
               context.setSessionData("said-text", false);
               Messaging.send(sender, this.getPromptText(context));
            } else {
               Messaging.sendErrorTr(sender, "citizens.editors.text.invalid-edit-type");
            }
         } else {
            Messaging.sendTr(sender, "citizens.editors.text.close-talker-set", this.text.toggle());
         }

         return new TextStartPrompt(this.text);
      }
   }

   public String getPromptText(ConversationContext context) {
      if (context.getSessionData("said-text") == Boolean.TRUE) {
         return "";
      } else {
         String text = Messaging.tr("citizens.editors.text.start-prompt");
         context.setSessionData("said-text", Boolean.TRUE);
         return text;
      }
   }
}
