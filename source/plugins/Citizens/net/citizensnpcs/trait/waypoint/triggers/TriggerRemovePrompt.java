package net.citizensnpcs.trait.waypoint.triggers;

import java.util.List;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.trait.waypoint.WaypointEditor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class TriggerRemovePrompt extends StringPrompt {
   private final WaypointEditor editor;

   public TriggerRemovePrompt(WaypointEditor editor) {
      super();
      this.editor = editor;
   }

   public Prompt acceptInput(ConversationContext context, String input) {
      if (input.equalsIgnoreCase("back")) {
         context.setSessionData("said", false);
         return (Prompt)context.getSessionData("previous");
      } else if (this.editor.getCurrentWaypoint() == null) {
         Messaging.sendErrorTr((CommandSender)context.getForWhom(), "citizens.editors.waypoints.triggers.main.missing-waypoint");
         return this;
      } else {
         int index = 0;

         try {
            index = Math.max(0, Integer.parseInt(input) - 1);
         } catch (NumberFormatException var5) {
            Messaging.sendErrorTr((CommandSender)context.getForWhom(), "citizens.editors.waypoints.triggers.remove.not-a-number");
            return this;
         }

         List<WaypointTrigger> triggers = this.editor.getCurrentWaypoint().getTriggers();
         if (index >= triggers.size() && index < triggers.size()) {
            triggers.remove(index);
            Messaging.sendTr((CommandSender)context.getForWhom(), "citizens.editors.waypoints.triggers.remove.removed", index + 1);
         } else {
            Messaging.sendErrorTr((CommandSender)context.getForWhom(), "citizens.editors.waypoints.triggers.remove.index-out-of-range", triggers.size());
         }

         return this;
      }
   }

   public String getPromptText(ConversationContext context) {
      if (this.editor.getCurrentWaypoint() == null) {
         Messaging.sendErrorTr((CommandSender)context.getForWhom(), "citizens.editors.waypoints.triggers.main.missing-waypoint");
         return "";
      } else if (context.getSessionData("said") == Boolean.TRUE) {
         return "";
      } else {
         context.setSessionData("said", true);
         String root = Messaging.tr("citizens.editors.waypoints.triggers.remove.prompt");
         int i = 1;

         for(WaypointTrigger trigger : this.editor.getCurrentWaypoint().getTriggers()) {
            root = root + String.format("<br>     %d. " + trigger.description(), i++);
         }

         Messaging.sendTr((CommandSender)context.getForWhom(), "citizens.editors.waypoints.triggers.remove.prompt" + root);
         return "";
      }
   }
}
