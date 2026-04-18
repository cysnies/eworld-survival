package net.citizensnpcs.trait.waypoint.triggers;

import com.google.common.collect.Lists;
import java.util.List;
import net.citizensnpcs.api.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class ChatTriggerPrompt extends StringPrompt implements WaypointTriggerPrompt {
   private final List lines = Lists.newArrayList();
   private double radius = (double)-1.0F;

   public ChatTriggerPrompt() {
      super();
   }

   public Prompt acceptInput(ConversationContext context, String input) {
      if (input.equalsIgnoreCase("back")) {
         return (Prompt)context.getSessionData("previous");
      } else if (input.startsWith("radius")) {
         try {
            this.radius = Double.parseDouble(input.split(" ")[1]);
         } catch (NumberFormatException var4) {
            Messaging.sendErrorTr((CommandSender)context.getForWhom(), "citizens.editors.waypoints.triggers.chat.invalid-radius");
         } catch (IndexOutOfBoundsException var5) {
            Messaging.sendErrorTr((CommandSender)context.getForWhom(), "citizens.editors.waypoints.triggers.chat.missing-radius");
         }

         return this;
      } else if (input.equalsIgnoreCase("finish")) {
         context.setSessionData("created-trigger", new ChatTrigger(this.radius, this.lines));
         return (Prompt)context.getSessionData("return-to");
      } else {
         this.lines.add(input);
         return this;
      }
   }

   public String getPromptText(ConversationContext context) {
      Messaging.sendTr((CommandSender)context.getForWhom(), "citizens.editors.waypoints.triggers.chat.prompt");
      return "";
   }
}
