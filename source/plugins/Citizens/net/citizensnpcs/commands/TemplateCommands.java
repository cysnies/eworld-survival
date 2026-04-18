package net.citizensnpcs.commands;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.Template;
import org.bukkit.command.CommandSender;

@Requirements(
   selected = true,
   ownership = true
)
public class TemplateCommands {
   public TemplateCommands(Citizens plugin) {
      super();
   }

   @Command(
      aliases = {"template", "tpl"},
      usage = "apply [template name] (id id2...)",
      desc = "Applies a template to the selected NPC",
      modifiers = {"apply"},
      min = 2,
      permission = "citizens.templates.apply"
   )
   @Requirements
   public void apply(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      Template template = Template.byName(args.getString(1));
      if (template == null) {
         throw new CommandException("citizens.commands.template.missing");
      } else {
         int appliedCount = 0;
         if (args.argsLength() == 2) {
            if (npc == null) {
               throw new CommandException(Messaging.tr("citizens.commands.requirements.must-have-selected"));
            }

            template.apply(npc);
            ++appliedCount;
         } else {
            String joined = args.getJoinedStrings(2, ',');
            List<Integer> ids = Lists.newArrayList();

            for(String id : Splitter.on(',').trimResults().split(joined)) {
               int parsed = Integer.parseInt(id);
               ids.add(parsed);
            }

            for(NPC toApply : Iterables.transform(ids, new Function() {
               public NPC apply(@Nullable Integer arg0) {
                  return arg0 == null ? null : CitizensAPI.getNPCRegistry().getById(arg0);
               }
            })) {
               template.apply(toApply);
               ++appliedCount;
            }
         }

         Messaging.sendTr(sender, "citizens.commands.template.applied", appliedCount);
      }
   }

   @Command(
      aliases = {"template", "tpl"},
      usage = "create [template name] (-o)",
      desc = "Creates a template from the selected NPC",
      modifiers = {"create"},
      min = 2,
      max = 2,
      flags = "o",
      permission = "citizens.templates.create"
   )
   public void create(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      String name = args.getString(1);
      if (Template.byName(name) != null) {
         throw new CommandException("citizens.commands.template.conflict");
      } else {
         Template.TemplateBuilder.create(name).from(npc).override(args.hasFlag('o')).buildAndSave();
         Messaging.sendTr(sender, "citizens.commands.template.created");
      }
   }
}
