package net.citizensnpcs.trait.waypoint.triggers;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.util.regex.Pattern;
import net.citizensnpcs.api.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.RegexPrompt;
import org.bukkit.entity.Player;

public class TeleportTriggerPrompt extends RegexPrompt implements WaypointTriggerPrompt {
   private static final Pattern PATTERN = Pattern.compile("here|back|[\\p{L}]+?:[0-9]+?:[0-9]+?:[0-9]+?", 66);

   public TeleportTriggerPrompt() {
      super(PATTERN);
   }

   protected Prompt acceptValidatedInput(ConversationContext context, String input) {
      input = input.trim();
      if (input.equalsIgnoreCase("back")) {
         return (Prompt)context.getSessionData("previous");
      } else if (input.equalsIgnoreCase("here")) {
         Player player = (Player)context.getForWhom();
         context.setSessionData("created-trigger", new TeleportTrigger(player.getLocation()));
         return (Prompt)context.getSessionData("return-to");
      } else {
         String[] parts = (String[])Iterables.toArray(Splitter.on(':').split(input), String.class);
         String worldName = parts[0];
         World world = Bukkit.getWorld(worldName);
         if (world == null) {
            Messaging.sendErrorTr((CommandSender)context.getForWhom(), "citizens.commands.errors.missing-world");
            return this;
         } else {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            context.setSessionData("created-trigger", new Location(world, (double)x, (double)y, (double)z));
            return (Prompt)context.getSessionData("return-to");
         }
      }
   }

   public String getPromptText(ConversationContext context) {
      return Messaging.tr("citizens.editors.waypoints.triggers.teleport.prompt");
   }
}
