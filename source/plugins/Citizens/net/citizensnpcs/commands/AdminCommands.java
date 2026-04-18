package net.citizensnpcs.commands;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.StringHelper;
import org.bukkit.command.CommandSender;

@Requirements
public class AdminCommands {
   private final Citizens plugin;

   public AdminCommands(Citizens plugin) {
      super();
      this.plugin = plugin;
   }

   @Command(
      aliases = {"citizens"},
      desc = "Show basic plugin information",
      max = 0,
      permission = "citizens.admin"
   )
   public void citizens(CommandContext args, CommandSender sender, NPC npc) {
      Messaging.send(sender, "          " + StringHelper.wrapHeader("<e>Citizens v" + this.plugin.getDescription().getVersion()));
      Messaging.send(sender, "     <7>-- <c>Written by fullwall and aPunch");
      Messaging.send(sender, "     <7>-- <c>Source Code: http://github.com/CitizensDev");
      Messaging.send(sender, "     <7>-- <c>Website: " + this.plugin.getDescription().getWebsite());
   }

   @Command(
      aliases = {"citizens"},
      usage = "reload",
      desc = "Reload Citizens",
      modifiers = {"reload"},
      min = 1,
      max = 1,
      permission = "citizens.admin"
   )
   public void reload(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      Messaging.sendTr(sender, "citizens.notifications.reloading");

      try {
         this.plugin.reload();
         Messaging.sendTr(sender, "citizens.notifications.reloaded");
      } catch (NPCLoadException ex) {
         ex.printStackTrace();
         throw new CommandException("citizens.notifications.error-reloading");
      }
   }

   @Command(
      aliases = {"citizens"},
      usage = "save (-a)",
      desc = "Save NPCs",
      help = "citizens.commands.citizens.save.help",
      modifiers = {"save"},
      min = 1,
      max = 1,
      flags = "a",
      permission = "citizens.admin"
   )
   public void save(CommandContext args, CommandSender sender, NPC npc) {
      Messaging.sendTr(sender, "citizens.notifications.saving");
      this.plugin.storeNPCs(args);
      Messaging.sendTr(sender, "citizens.notifications.saved");
   }
}
