package net.citizensnpcs.trait.waypoint.triggers;

import net.citizensnpcs.api.util.Messaging;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;

public class DelayTriggerPrompt extends NumericPrompt implements WaypointTriggerPrompt {
   public DelayTriggerPrompt() {
      super();
   }

   protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
      int delay = Math.max(input.intValue(), 0);
      context.setSessionData("created-trigger", new DelayTrigger(delay));
      return (Prompt)context.getSessionData("return-to");
   }

   public String getPromptText(ConversationContext context) {
      return Messaging.tr("citizens.editors.waypoints.triggers.delay.prompt");
   }
}
