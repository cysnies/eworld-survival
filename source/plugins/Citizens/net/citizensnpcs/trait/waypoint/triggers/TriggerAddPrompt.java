package net.citizensnpcs.trait.waypoint.triggers;

import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.trait.waypoint.WaypointEditor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class TriggerAddPrompt extends StringPrompt {
   private final WaypointEditor editor;

   public TriggerAddPrompt(WaypointEditor editor) {
      super();
      this.editor = editor;
   }

   public Prompt acceptInput(ConversationContext context, String input) {
      input = input.toLowerCase().trim();
      if (input.equalsIgnoreCase("back")) {
         context.setSessionData("said", false);
         return (Prompt)context.getSessionData("previous");
      } else {
         Prompt prompt = WaypointTriggerRegistry.getTriggerPromptFrom(input);
         if (prompt == null) {
            Messaging.sendErrorTr((CommandSender)context.getForWhom(), "citizens.editors.waypoints.triggers.add.invalid-trigger", input);
            context.setSessionData("said", false);
            return this;
         } else {
            return prompt;
         }
      }
   }

   public String getPromptText(ConversationContext context) {
      WaypointTrigger returned = (WaypointTrigger)context.getSessionData("created-trigger");
      if (returned != null) {
         if (this.editor.getCurrentWaypoint() != null) {
            this.editor.getCurrentWaypoint().addTrigger(returned);
            context.setSessionData("created-trigger", (Object)null);
            Messaging.sendTr((CommandSender)context.getForWhom(), "citizens.editors.waypoints.triggers.add.added", returned.description());
         } else {
            Messaging.sendErrorTr((CommandSender)context.getForWhom(), "citizens.editors.waypoints.triggers.main.missing-waypoint");
         }
      }

      if (context.getSessionData("said") == Boolean.TRUE) {
         return "";
      } else {
         context.setSessionData("said", true);
         context.setSessionData("return-to", this);
         return Messaging.tr("citizens.editors.waypoints.triggers.add.prompt", WaypointTriggerRegistry.describeValidTriggerNames());
      }
   }
}
