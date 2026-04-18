package net.citizensnpcs.api.command;

import com.google.common.collect.Sets;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.command.exception.RequirementMissingException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

public class RequirementsProcessor implements CommandAnnotationProcessor {
   public RequirementsProcessor() {
      super();
   }

   public Class getAnnotationClass() {
      return Requirements.class;
   }

   public void process(CommandSender sender, CommandContext context, Annotation instance, Object[] methodArgs) throws CommandException {
      Requirements requirements = (Requirements)instance;
      NPC npc = methodArgs.length >= 3 && methodArgs[2] instanceof NPC ? (NPC)methodArgs[2] : null;
      boolean canRedefineSelected = context.hasValueFlag("id") && sender.hasPermission("npc.select");
      String error = Messaging.tr("citizens.commands.requirements.must-have-selected");
      if (canRedefineSelected) {
         npc = CitizensAPI.getNPCRegistry().getById(context.getFlagInteger("id"));
         if (methodArgs.length >= 3) {
            methodArgs[2] = npc;
         }

         if (npc == null) {
            error = error + ' ' + Messaging.tr("citizens.commands.id-not-found", context.getFlagInteger("id"));
         }
      }

      if (requirements.selected() && npc == null) {
         throw new RequirementMissingException(error);
      } else if (requirements.ownership() && npc != null && !sender.hasPermission("citizens.admin") && !((Owner)npc.getTrait(Owner.class)).isOwnedBy(sender)) {
         throw new RequirementMissingException(Messaging.tr("citizens.commands.requirements.must-be-owner"));
      } else if (npc != null) {
         for(Class clazz : requirements.traits()) {
            if (!npc.hasTrait(clazz)) {
               throw new RequirementMissingException(Messaging.tr("citizens.commands.requirements.missing-required-trait", clazz.getSimpleName()));
            }
         }

         Set<EntityType> types = Sets.newEnumSet(Arrays.asList(requirements.types()), EntityType.class);
         if (types.contains(EntityType.UNKNOWN)) {
            types = EnumSet.allOf(EntityType.class);
         }

         types.removeAll(Sets.newHashSet(requirements.excludedTypes()));
         EntityType type = ((MobType)npc.getTrait(MobType.class)).getType();
         if (!types.contains(type)) {
            throw new RequirementMissingException(Messaging.tr("citizens.commands.requirements.disallowed-mobtype", type.getName()));
         }
      }
   }
}
