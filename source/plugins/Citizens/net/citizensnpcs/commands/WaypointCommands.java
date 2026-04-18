package net.citizensnpcs.commands;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.ai.StuckAction;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.trait.waypoint.Waypoints;
import org.bukkit.command.CommandSender;

@Requirements(
   ownership = true,
   selected = true
)
public class WaypointCommands {
   public WaypointCommands(Citizens plugin) {
      super();
   }

   @Command(
      aliases = {"waypoints", "waypoint", "wp"},
      usage = "disableteleporting",
      desc = "Disables teleportation when stuck (temporary command)",
      modifiers = {"disableteleport"},
      min = 1,
      max = 1,
      permission = "citizens.waypoints.disableteleport"
   )
   public void disableTeleporting(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      npc.getNavigator().getDefaultParameters().stuckAction((StuckAction)null);
      Messaging.sendTr(sender, "citizens.commands.waypoints.disableteleporting.disabled");
   }

   @Command(
      aliases = {"waypoints", "waypoint", "wp"},
      usage = "provider [provider name] (-d)",
      desc = "Sets the current waypoint provider",
      modifiers = {"provider"},
      min = 1,
      max = 2,
      flags = "d",
      permission = "citizens.waypoints.provider"
   )
   public void provider(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      Waypoints waypoints = (Waypoints)npc.getTrait(Waypoints.class);
      if (args.argsLength() == 1) {
         if (args.hasFlag('d')) {
            waypoints.describeProviders(sender);
         } else {
            Messaging.sendTr(sender, "citizens.waypoints.current-provider", waypoints.getCurrentProviderName());
         }

      } else {
         boolean success = waypoints.setWaypointProvider(args.getString(1));
         if (!success) {
            throw new CommandException("Provider not found.");
         } else {
            Messaging.sendTr(sender, "citizens.waypoints.set-provider", args.getString(1));
         }
      }
   }
}
