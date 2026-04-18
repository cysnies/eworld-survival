package net.citizensnpcs.trait.waypoint.triggers;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.util.List;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.PlayerAnimation;
import net.citizensnpcs.util.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class AnimationTriggerPrompt extends StringPrompt implements WaypointTriggerPrompt {
   private final List animations = Lists.newArrayList();

   public AnimationTriggerPrompt() {
      super();
   }

   public Prompt acceptInput(ConversationContext context, String input) {
      if (input.equalsIgnoreCase("back")) {
         return (Prompt)context.getSessionData("previous");
      } else if (input.equalsIgnoreCase("finish")) {
         context.setSessionData("created-trigger", new AnimationTrigger(this.animations));
         return (Prompt)context.getSessionData("return-to");
      } else {
         PlayerAnimation animation = (PlayerAnimation)Util.matchEnum(PlayerAnimation.values(), input);
         if (animation == null) {
            Messaging.sendErrorTr((CommandSender)context.getForWhom(), "citizens.editors.waypoints.triggers.animation.invalid-animation", input, this.getValidAnimations());
         }

         return this;
      }
   }

   public String getPromptText(ConversationContext context) {
      Messaging.sendTr((CommandSender)context.getForWhom(), "citizens.editors.waypoints.triggers.animation.prompt", this.getValidAnimations());
      return "";
   }

   private String getValidAnimations() {
      return Joiner.on(", ").join(PlayerAnimation.values());
   }
}
