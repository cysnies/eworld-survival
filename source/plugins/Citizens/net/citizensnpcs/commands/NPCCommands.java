package net.citizensnpcs.commands;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.Settings;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.command.exception.NoPermissionsException;
import net.citizensnpcs.api.command.exception.ServerCommandException;
import net.citizensnpcs.api.event.CommandSenderCreateNPCEvent;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.PlayerCreateNPCEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.api.trait.trait.Speech;
import net.citizensnpcs.api.util.Colorizer;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Paginator;
import net.citizensnpcs.npc.NPCSelector;
import net.citizensnpcs.npc.Template;
import net.citizensnpcs.trait.Age;
import net.citizensnpcs.trait.Anchors;
import net.citizensnpcs.trait.Controllable;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.trait.Gravity;
import net.citizensnpcs.trait.HorseModifiers;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.NPCSkeletonType;
import net.citizensnpcs.trait.OcelotModifiers;
import net.citizensnpcs.trait.Poses;
import net.citizensnpcs.trait.Powered;
import net.citizensnpcs.trait.SlimeSize;
import net.citizensnpcs.trait.VillagerProfession;
import net.citizensnpcs.trait.WolfModifiers;
import net.citizensnpcs.trait.ZombieModifier;
import net.citizensnpcs.util.Anchor;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.StringHelper;
import net.citizensnpcs.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Ocelot.Type;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

@Requirements(
   selected = true,
   ownership = true
)
public class NPCCommands {
   private final NPCRegistry npcRegistry = CitizensAPI.getNPCRegistry();
   private final NPCSelector selector;

   public NPCCommands(Citizens plugin) {
      super();
      this.selector = plugin.getNPCSelector();
   }

   @Command(
      aliases = {"npc"},
      usage = "age [age] (-l)",
      desc = "Set the age of a NPC",
      help = "citizens.commands.npc.age.help",
      flags = "l",
      modifiers = {"age"},
      min = 1,
      max = 2,
      permission = "citizens.npc.age"
   )
   public void age(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      if (npc.isSpawned() && npc.getBukkitEntity() instanceof Ageable) {
         Age trait = (Age)npc.getTrait(Age.class);
         boolean toggleLock = args.hasFlag('l');
         if (toggleLock) {
            Messaging.sendTr(sender, trait.toggle() ? "citizens.commands.npc.age.locked" : "citizens.commands.npc.age.unlocked");
         }

         if (args.argsLength() <= 1) {
            if (!toggleLock) {
               trait.describe(sender);
            }

         } else {
            int age = 0;

            try {
               age = args.getInteger(1);
               if (age < -24000 || age > 0) {
                  throw new CommandException("citizens.commands.npc.age.invalid-age");
               }

               Messaging.sendTr(sender, "citizens.commands.npc.age.set-normal", npc.getName(), age);
            } catch (NumberFormatException var8) {
               if (args.getString(1).equalsIgnoreCase("baby")) {
                  age = -24000;
                  Messaging.sendTr(sender, "citizens.commands.npc.age.set-baby", npc.getName());
               } else {
                  if (!args.getString(1).equalsIgnoreCase("adult")) {
                     throw new CommandException("citizens.commands.npc.age.invalid-age");
                  }

                  age = 0;
                  Messaging.sendTr(sender, "citizens.commands.npc.age.set-adult", npc.getName());
               }
            }

            trait.setAge(age);
         }
      } else {
         throw new CommandException("citizens.commands.npc.age.cannot-be-aged");
      }
   }

   @Command(
      aliases = {"npc"},
      usage = "anchor (--save [name]|--assume [name]|--remove [name]) (-a)(-c)",
      desc = "Changes/Saves/Lists NPC's location anchor(s)",
      flags = "ac",
      modifiers = {"anchor"},
      min = 1,
      max = 3,
      permission = "citizens.npc.anchor"
   )
   public void anchor(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      Anchors trait = (Anchors)npc.getTrait(Anchors.class);
      if (args.hasValueFlag("save")) {
         if (args.getFlag("save").isEmpty()) {
            throw new CommandException("citizens.commands.npc.anchor.invalid-name");
         }

         if (args.getSenderLocation() == null) {
            throw new ServerCommandException();
         }

         if (args.hasFlag('c')) {
            if (!trait.addAnchor(args.getFlag("save"), args.getSenderTargetBlockLocation())) {
               throw new CommandException("citizens.commands.npc.anchor.already-exists", new Object[]{args.getFlag("save")});
            }

            Messaging.sendTr(sender, "citizens.commands.npc.anchor.added");
         } else {
            if (!trait.addAnchor(args.getFlag("save"), args.getSenderLocation())) {
               throw new CommandException("citizens.commands.npc.anchor.already-exists", new Object[]{args.getFlag("save")});
            }

            Messaging.sendTr(sender, "citizens.commands.npc.anchor.added");
         }
      } else if (args.hasValueFlag("assume")) {
         if (args.getFlag("assume").isEmpty()) {
            throw new CommandException("citizens.commands.npc.anchor.invalid-name");
         }

         Anchor anchor = trait.getAnchor(args.getFlag("assume"));
         if (anchor == null) {
            throw new CommandException("citizens.commands.npc.anchor.missing", new Object[]{args.getFlag("assume")});
         }

         npc.teleport(anchor.getLocation(), TeleportCause.COMMAND);
      } else if (args.hasValueFlag("remove")) {
         if (args.getFlag("remove").isEmpty()) {
            throw new CommandException("citizens.commands.npc.anchor.invalid-name");
         }

         if (!trait.removeAnchor(trait.getAnchor(args.getFlag("remove")))) {
            throw new CommandException("citizens.commands.npc.anchor.missing", new Object[]{args.getFlag("remove")});
         }

         Messaging.sendTr(sender, "citizens.commands.npc.anchor.removed");
      } else if (!args.hasFlag('a')) {
         Paginator paginator = (new Paginator()).header("Anchors");
         paginator.addLine("<e>Key: <a>ID  <b>Name  <c>World  <d>Location (X,Y,Z)");

         for(int i = 0; i < trait.getAnchors().size(); ++i) {
            String line = "<a>" + i + "<b>  " + ((Anchor)trait.getAnchors().get(i)).getName() + "<c>  " + ((Anchor)trait.getAnchors().get(i)).getLocation().getWorld().getName() + "<d>  " + ((Anchor)trait.getAnchors().get(i)).getLocation().getBlockX() + ", " + ((Anchor)trait.getAnchors().get(i)).getLocation().getBlockY() + ", " + ((Anchor)trait.getAnchors().get(i)).getLocation().getBlockZ();
            paginator.addLine(line);
         }

         int page = args.getInteger(1, 1);
         if (!paginator.sendPage(sender, page)) {
            throw new CommandException("citizens.commands.page-missing");
         }
      }

      if (args.hasFlag('a')) {
         if (sender instanceof ConsoleCommandSender) {
            throw new ServerCommandException();
         } else {
            npc.teleport(args.getSenderLocation(), TeleportCause.COMMAND);
         }
      }
   }

   @Command(
      aliases = {"npc"},
      usage = "controllable|control (-m,-y,-n)",
      desc = "Toggles whether the NPC can be ridden and controlled",
      modifiers = {"controllable", "control"},
      min = 1,
      max = 1,
      flags = "myn"
   )
   public void controllable(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      if ((!npc.isSpawned() || sender.hasPermission("citizens.npc.controllable." + npc.getBukkitEntity().getType().toString().toLowerCase())) && sender.hasPermission("citizens.npc.controllable")) {
         if (!npc.hasTrait(Controllable.class)) {
            npc.addTrait((Trait)(new Controllable(false)));
         }

         Controllable trait = (Controllable)npc.getTrait(Controllable.class);
         boolean enabled = trait.toggle();
         if (args.hasFlag('y')) {
            enabled = trait.setEnabled(true);
         } else if (args.hasFlag('n')) {
            enabled = trait.setEnabled(false);
         }

         String key = enabled ? "citizens.commands.npc.controllable.set" : "citizens.commands.npc.controllable.removed";
         Messaging.sendTr(sender, key, npc.getName());
         if (enabled && args.hasFlag('m') && sender instanceof Player) {
            trait.mount((Player)sender);
         }

      } else {
         throw new NoPermissionsException();
      }
   }

   @Command(
      aliases = {"npc"},
      usage = "copy (--name newname)",
      desc = "Copies an NPC",
      modifiers = {"copy"},
      min = 1,
      max = 1,
      permission = "citizens.npc.copy"
   )
   public void copy(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      String name = args.getFlag("name", npc.getFullName());
      NPC copy = npc.clone();
      if (!copy.getFullName().equals(name)) {
         copy.setName(name);
      }

      if (copy.isSpawned() && args.getSenderLocation() != null) {
         Location location = args.getSenderLocation();
         location.getChunk().load();
         copy.teleport(location, TeleportCause.COMMAND);
         ((CurrentLocation)copy.getTrait(CurrentLocation.class)).setLocation(location);
      }

      CommandSenderCreateNPCEvent event = (CommandSenderCreateNPCEvent)(sender instanceof Player ? new PlayerCreateNPCEvent((Player)sender, copy) : new CommandSenderCreateNPCEvent(sender, copy));
      Bukkit.getPluginManager().callEvent(event);
      if (event.isCancelled()) {
         event.getNPC().destroy();
         String reason = "Couldn't create NPC.";
         if (!event.getCancelReason().isEmpty()) {
            reason = reason + " Reason: " + event.getCancelReason();
         }

         throw new CommandException(reason);
      } else {
         Messaging.sendTr(sender, "citizens.commands.npc.copy.copied", npc.getName());
         this.selector.select(sender, copy);
      }
   }

   @Command(
      aliases = {"npc"},
      usage = "create [name] ((-b,u) --at (x:y:z:world) --type (type) --trait ('trait1, trait2...') --b (behaviours))",
      desc = "Create a new NPC",
      flags = "bu",
      modifiers = {"create"},
      min = 2,
      permission = "citizens.npc.create"
   )
   @Requirements
   public void create(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      String name = Colorizer.parseColors(args.getJoinedStrings(1));
      if (name.length() > 16) {
         Messaging.sendErrorTr(sender, "citizens.commands.npc.create.npc-name-too-long");
         name = name.substring(0, 16);
      }

      if (name.length() <= 0) {
         throw new CommandException();
      } else {
         EntityType type = EntityType.PLAYER;
         if (args.hasValueFlag("type")) {
            String inputType = args.getFlag("type");
            type = Util.matchEntityType(inputType);
            if (type == null) {
               Messaging.sendErrorTr(sender, "citizens.commands.npc.create.invalid-mobtype", inputType);
               type = EntityType.PLAYER;
            } else if (!LivingEntity.class.isAssignableFrom(type.getEntityClass())) {
               Messaging.sendErrorTr(sender, "citizens.commands.npc.create.not-living-mobtype", type);
               type = EntityType.PLAYER;
            }
         }

         if (!sender.hasPermission("citizens.npc.create.*") && !sender.hasPermission("citizens.npc.createall") && !sender.hasPermission("citizens.npc.create." + type.name().toLowerCase().replace("_", ""))) {
            throw new NoPermissionsException();
         } else {
            npc = this.npcRegistry.createNPC(type, name);
            String msg = "You created [[" + npc.getName() + "]]";
            int age = 0;
            if (args.hasFlag('b')) {
               if (!Ageable.class.isAssignableFrom(type.getEntityClass())) {
                  Messaging.sendErrorTr(sender, "citizens.commands.npc.age.cannot-be-aged", type.name().toLowerCase().replace("_", "-"));
               } else {
                  age = -24000;
                  msg = msg + " as a baby";
               }
            }

            if (!Settings.Setting.SERVER_OWNS_NPCS.asBoolean()) {
               ((Owner)npc.getTrait(Owner.class)).setOwner(sender.getName());
            }

            ((MobType)npc.getTrait(MobType.class)).setType(type);
            Location spawnLoc = null;
            if (sender instanceof Player) {
               spawnLoc = args.getSenderLocation();
            } else if (sender instanceof BlockCommandSender) {
               spawnLoc = args.getSenderLocation();
            }

            CommandSenderCreateNPCEvent event = (CommandSenderCreateNPCEvent)(sender instanceof Player ? new PlayerCreateNPCEvent((Player)sender, npc) : new CommandSenderCreateNPCEvent(sender, npc));
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
               npc.destroy();
               String reason = "Couldn't create NPC.";
               if (!event.getCancelReason().isEmpty()) {
                  reason = reason + " Reason: " + event.getCancelReason();
               }

               throw new CommandException(reason);
            } else {
               if (args.hasValueFlag("at")) {
                  spawnLoc = CommandContext.parseLocation(args.getSenderLocation(), args.getFlag("at"));
               }

               if (spawnLoc == null) {
                  npc.destroy();
                  throw new CommandException("citizens.commands.npc.create.invalid-location");
               } else {
                  if (!args.hasFlag('u')) {
                     npc.spawn(spawnLoc);
                  }

                  if (args.hasValueFlag("trait")) {
                     Iterable<String> parts = Splitter.on(',').trimResults().split(args.getFlag("trait"));
                     StringBuilder builder = new StringBuilder();

                     for(String tr : parts) {
                        Trait trait = CitizensAPI.getTraitFactory().getTrait(tr);
                        if (trait != null) {
                           npc.addTrait(trait);
                           builder.append(StringHelper.wrap(tr) + ", ");
                        }
                     }

                     if (builder.length() > 0) {
                        builder.delete(builder.length() - 2, builder.length());
                     }

                     msg = msg + " with traits " + builder.toString();
                  }

                  if (args.hasValueFlag("template")) {
                     Iterable<String> parts = Splitter.on(',').trimResults().split(args.getFlag("template"));
                     StringBuilder builder = new StringBuilder();

                     for(String part : parts) {
                        Template template = Template.byName(part);
                        if (template != null) {
                           template.apply(npc);
                           builder.append(StringHelper.wrap(part) + ", ");
                        }
                     }

                     if (builder.length() > 0) {
                        builder.delete(builder.length() - 2, builder.length());
                     }

                     msg = msg + " with templates " + builder.toString();
                  }

                  if (npc.getBukkitEntity() instanceof Ageable) {
                     ((Age)npc.getTrait(Age.class)).setAge(age);
                  }

                  this.selector.select(sender, npc);
                  Messaging.send(sender, msg + '.');
               }
            }
         }
      }
   }

   @Command(
      aliases = {"npc"},
      usage = "despawn (id)",
      desc = "Despawn a NPC",
      modifiers = {"despawn"},
      min = 1,
      max = 2,
      permission = "citizens.npc.despawn"
   )
   @Requirements
   public void despawn(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      if (npc == null || args.argsLength() == 2) {
         if (args.argsLength() < 2) {
            throw new CommandException("citizens.commands.requirements.must-have-selected");
         }

         int id = args.getInteger(1);
         npc = CitizensAPI.getNPCRegistry().getById(id);
         if (npc == null) {
            throw new CommandException("citizens.commands.npc.spawn.missing-npc-id", new Object[]{id});
         }
      }

      ((Spawned)npc.getTrait(Spawned.class)).setSpawned(false);
      npc.despawn(DespawnReason.REMOVAL);
      Messaging.sendTr(sender, "citizens.commands.npc.despawn.despawned", npc.getName());
   }

   @Command(
      aliases = {"npc"},
      usage = "gamemode [gamemode]",
      desc = "Changes the gamemode",
      modifiers = {"gamemode"},
      min = 1,
      max = 2,
      permission = "citizens.npc.gravity"
   )
   @Requirements(
      selected = true,
      ownership = true,
      types = {EntityType.PLAYER}
   )
   public void gamemode(CommandContext args, CommandSender sender, NPC npc) {
      Player player = (Player)npc.getBukkitEntity();
      if (args.argsLength() == 1) {
         Messaging.sendTr(sender, "citizens.commands.npc.gamemode.describe", npc.getName(), player.getGameMode().name().toLowerCase());
      } else {
         GameMode mode = null;

         try {
            int value = args.getInteger(1);
            mode = GameMode.getByValue(value);
         } catch (NumberFormatException var9) {
            try {
               mode = GameMode.valueOf(args.getString(1));
            } catch (IllegalArgumentException var8) {
            }
         }

         if (mode == null) {
            Messaging.sendErrorTr(sender, "citizens.commands.npc.gamemode.invalid", args.getString(1));
         } else {
            player.setGameMode(mode);
            Messaging.sendTr(sender, "citizens.commands.npc.gamemode.set", mode.name().toLowerCase());
         }
      }
   }

   @Command(
      aliases = {"npc"},
      usage = "gravity",
      desc = "Toggles gravity",
      modifiers = {"gravity"},
      min = 1,
      max = 1,
      permission = "citizens.npc.gravity"
   )
   public void gravity(CommandContext args, CommandSender sender, NPC npc) {
      boolean enabled = ((Gravity)npc.getTrait(Gravity.class)).toggle();
      String key = enabled ? "citizens.commands.npc.gravity.enabled" : "citizens.commands.npc.gravity.disabled";
      Messaging.sendTr(sender, key, npc.getName());
   }

   @Command(
      aliases = {"npc"},
      usage = "horse (--color color) (--type type) (--style style) (-cb)",
      desc = "Sets horse modifiers",
      help = "Use the -c flag to make the horse have a chest, or the -b flag to stop them from having a chest.",
      modifiers = {"horse"},
      min = 1,
      max = 1,
      flags = "cb",
      permission = "citizens.npc.horse"
   )
   @Requirements(
      selected = true,
      ownership = true,
      types = {EntityType.HORSE}
   )
   public void horse(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      HorseModifiers horse = (HorseModifiers)npc.getTrait(HorseModifiers.class);
      String output = "";
      if (args.hasFlag('c')) {
         horse.setCarryingChest(true);
         output = output + Messaging.tr("citizens.commands.npc.horse.chest-set") + " ";
      } else if (args.hasFlag('b')) {
         horse.setCarryingChest(false);
         output = output + Messaging.tr("citizens.commands.npc.horse.chest-unset") + " ";
      }

      if (args.hasValueFlag("color") || args.hasValueFlag("colour")) {
         String colorRaw = args.getFlag("color", args.getFlag("colour"));
         Horse.Color color = (Horse.Color)Util.matchEnum(Color.values(), colorRaw);
         if (color == null) {
            String valid = Util.listValuesPretty(Color.values());
            throw new CommandException("citizens.commands.npc.horse.invalid-color", new Object[]{valid});
         }

         horse.setColor(color);
         output = output + Messaging.tr("citizens.commands.npc.horse.color-set", Util.prettyEnum(color));
      }

      if (args.hasValueFlag("type")) {
         Horse.Variant variant = (Horse.Variant)Util.matchEnum(Variant.values(), args.getFlag("type"));
         if (variant == null) {
            String valid = Util.listValuesPretty(Variant.values());
            throw new CommandException("citizens.commands.npc.horse.invalid-variant", new Object[]{valid});
         }

         horse.setType(variant);
         output = output + Messaging.tr("citizens.commands.npc.horse.type-set", Util.prettyEnum(variant));
      }

      if (args.hasValueFlag("style")) {
         Horse.Style style = (Horse.Style)Util.matchEnum(Style.values(), args.getFlag("style"));
         if (style == null) {
            String valid = Util.listValuesPretty(Style.values());
            throw new CommandException("citizens.commands.npc.horse.invalid-style", new Object[]{valid});
         }

         horse.setStyle(style);
         output = output + Messaging.tr("citizens.commands.npc.horse.style-set", Util.prettyEnum(style));
      }

      if (output.isEmpty()) {
         Messaging.sendTr(sender, "citizens.commands.npc.horse.describe", Util.prettyEnum(horse.getColor()), Util.prettyEnum(horse.getType()), Util.prettyEnum(horse.getStyle()));
      } else {
         sender.sendMessage(output);
      }

   }

   @Command(
      aliases = {"npc"},
      usage = "id",
      desc = "Sends the selected NPC's ID to the sender",
      modifiers = {"id"},
      min = 1,
      max = 1,
      permission = "citizens.npc.id"
   )
   public void id(CommandContext args, CommandSender sender, NPC npc) {
      Messaging.send(sender, npc.getId());
   }

   @Command(
      aliases = {"npc"},
      usage = "leashable",
      desc = "Toggles leashability",
      modifiers = {"leashable"},
      min = 1,
      max = 1,
      flags = "t",
      permission = "citizens.npc.leashable"
   )
   public void leashable(CommandContext args, CommandSender sender, NPC npc) {
      boolean vulnerable = !(Boolean)npc.data().get("protected-leash", true);
      if (args.hasFlag('t')) {
         npc.data().set("protected-leash", vulnerable);
      } else {
         npc.data().setPersistent("protected-leash", vulnerable);
      }

      String key = vulnerable ? "citizens.commands.npc.leashable.stopped" : "citizens.commands.npc.leashable.set";
      Messaging.sendTr(sender, key, npc.getName());
   }

   @Command(
      aliases = {"npc"},
      usage = "list (page) ((-a) --owner (owner) --type (type) --char (char))",
      desc = "List NPCs",
      flags = "a",
      modifiers = {"list"},
      min = 1,
      max = 2,
      permission = "citizens.npc.list"
   )
   @Requirements
   public void list(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      List<NPC> npcs = new ArrayList();
      if (args.hasFlag('a')) {
         for(NPC add : this.npcRegistry) {
            npcs.add(add);
         }
      } else if (args.getValueFlags().size() == 0 && sender instanceof Player) {
         for(NPC add : this.npcRegistry) {
            if (!npcs.contains(add) && ((Owner)add.getTrait(Owner.class)).isOwnedBy(sender)) {
               npcs.add(add);
            }
         }
      } else {
         if (args.hasValueFlag("owner")) {
            String name = args.getFlag("owner");

            for(NPC add : this.npcRegistry) {
               if (!npcs.contains(add) && ((Owner)add.getTrait(Owner.class)).isOwnedBy(name)) {
                  npcs.add(add);
               }
            }
         }

         if (args.hasValueFlag("type")) {
            EntityType type = Util.matchEntityType(args.getFlag("type"));
            if (type == null) {
               throw new CommandException("citizens.commands.invalid-mobtype", new Object[]{type});
            }

            for(NPC add : this.npcRegistry) {
               if (!npcs.contains(add) && ((MobType)add.getTrait(MobType.class)).getType() == type) {
                  npcs.add(add);
               }
            }
         }
      }

      Paginator paginator = (new Paginator()).header("NPCs");
      paginator.addLine("<e>Key: <a>ID  <b>Name");

      for(int i = 0; i < npcs.size(); i += 2) {
         String line = "<a>" + ((NPC)npcs.get(i)).getId() + "<b>  " + ((NPC)npcs.get(i)).getName();
         if (npcs.size() >= i + 2) {
            line = line + "      <a>" + ((NPC)npcs.get(i + 1)).getId() + "<b>  " + ((NPC)npcs.get(i + 1)).getName();
         }

         paginator.addLine(line);
      }

      int page = args.getInteger(1, 1);
      if (!paginator.sendPage(sender, page)) {
         throw new CommandException("citizens.commands.page-missing");
      }
   }

   @Command(
      aliases = {"npc"},
      usage = "lookclose",
      desc = "Toggle whether a NPC will look when a player is near",
      modifiers = {"lookclose", "look", "rotate"},
      min = 1,
      max = 1,
      permission = "citizens.npc.lookclose"
   )
   public void lookClose(CommandContext args, CommandSender sender, NPC npc) {
      Messaging.sendTr(sender, ((LookClose)npc.getTrait(LookClose.class)).toggle() ? "citizens.commands.npc.lookclose.set" : "citizens.commands.npc.lookclose.stopped", npc.getName());
   }

   @Command(
      aliases = {"npc"},
      usage = "mount",
      desc = "Mounts a controllable NPC",
      modifiers = {"mount"},
      min = 1,
      max = 1,
      permission = "citizens.npc.controllable"
   )
   public void mount(CommandContext args, Player player, NPC npc) {
      boolean enabled = npc.hasTrait(Controllable.class) && ((Controllable)npc.getTrait(Controllable.class)).isEnabled();
      if (!enabled) {
         Messaging.sendTr(player, "citizens.commands.npc.controllable.not-controllable", npc.getName());
      } else {
         boolean success = ((Controllable)npc.getTrait(Controllable.class)).mount(player);
         if (!success) {
            Messaging.sendTr(player, "citizens.commands.npc.mount.failed", npc.getName());
         }

      }
   }

   @Command(
      aliases = {"npc"},
      usage = "moveto x:y:z:world | x y z world",
      desc = "Teleports a NPC to a given location",
      modifiers = {"moveto"},
      min = 1,
      permission = "citizens.npc.moveto"
   )
   public void moveto(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      if (!npc.isSpawned()) {
         npc.spawn(((CurrentLocation)npc.getTrait(CurrentLocation.class)).getLocation());
      }

      if (npc.getBukkitEntity() == null) {
         throw new CommandException("NPC could not be spawned.");
      } else {
         Location current = npc.getBukkitEntity().getLocation();
         Location to;
         if (args.argsLength() > 1) {
            String[] parts = (String[])Iterables.toArray(Splitter.on(':').split(args.getJoinedStrings(1, ':')), String.class);
            if (parts.length != 4 && parts.length != 3) {
               throw new CommandException("citizens.commands.npc.moveto.format");
            }

            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double z = Double.parseDouble(parts[2]);
            World world = parts.length == 4 ? Bukkit.getWorld(parts[3]) : current.getWorld();
            if (world == null) {
               throw new CommandException("citizens.commands.errors.missing-world");
            }

            to = new Location(world, x, y, z, current.getYaw(), current.getPitch());
         } else {
            to = current.clone();
            if (args.hasValueFlag("x")) {
               to.setX(args.getFlagDouble("x"));
            }

            if (args.hasValueFlag("y")) {
               to.setY(args.getFlagDouble("y"));
            }

            if (args.hasValueFlag("z")) {
               to.setZ(args.getFlagDouble("z"));
            }

            if (args.hasValueFlag("yaw")) {
               to.setYaw((float)args.getFlagDouble("yaw"));
            }

            if (args.hasValueFlag("pitch")) {
               to.setPitch((float)args.getFlagDouble("pitch"));
            }

            if (args.hasValueFlag("world")) {
               World world = Bukkit.getWorld(args.getFlag("world"));
               if (world == null) {
                  throw new CommandException("citizens.commands.errors.missing-world");
               }

               to.setWorld(world);
            }
         }

         npc.teleport(to, TeleportCause.COMMAND);
         Messaging.sendTr(sender, "citizens.commands.npc.moveto.teleported", npc.getName(), to);
      }
   }

   @Command(
      aliases = {"npc"},
      modifiers = {"name"},
      usage = "name",
      desc = "Toggle nameplate visibility",
      min = 1,
      max = 1,
      permission = "citizens.npc.name"
   )
   @Requirements(
      selected = true,
      ownership = true
   )
   public void name(CommandContext args, CommandSender sender, NPC npc) {
      npc.getBukkitEntity().setCustomNameVisible(!npc.getBukkitEntity().isCustomNameVisible());
      Messaging.sendTr(sender, "citizens.commands.npc.nameplate.toggled");
   }

   @Command(
      aliases = {"npc"},
      desc = "Show basic NPC information",
      max = 0,
      permission = "citizens.npc.info"
   )
   public void npc(CommandContext args, CommandSender sender, NPC npc) {
      Messaging.send(sender, StringHelper.wrapHeader(npc.getName()));
      Messaging.send(sender, "    <a>ID: <e>" + npc.getId());
      Messaging.send(sender, "    <a>Type: <e>" + ((MobType)npc.getTrait(MobType.class)).getType());
      if (npc.isSpawned()) {
         Location loc = npc.getBukkitEntity().getLocation();
         String format = "    <a>Spawned at <e>%d, %d, %d <a>in world<e> %s";
         Messaging.send(sender, String.format(format, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName()));
      }

      Messaging.send(sender, "    <a>Traits<e>");

      for(Trait trait : npc.getTraits()) {
         if (!CitizensAPI.getTraitFactory().isInternalTrait(trait)) {
            String message = "     <e>- <a>" + trait.getName();
            Messaging.send(sender, message);
         }
      }

   }

   @Command(
      aliases = {"npc"},
      usage = "ocelot (--type type) (-s(itting), -n(ot sitting))",
      desc = "Set the ocelot type of an NPC and whether it is sitting",
      modifiers = {"ocelot"},
      min = 1,
      max = 1,
      flags = "sn",
      permission = "citizens.npc.ocelot"
   )
   @Requirements(
      selected = true,
      ownership = true,
      types = {EntityType.OCELOT}
   )
   public void ocelot(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      OcelotModifiers trait = (OcelotModifiers)npc.getTrait(OcelotModifiers.class);
      if (args.hasFlag('s')) {
         trait.setSitting(true);
      } else if (args.hasFlag('n')) {
         trait.setSitting(false);
      }

      if (args.hasValueFlag("type")) {
         Ocelot.Type type = (Ocelot.Type)Util.matchEnum(Type.values(), args.getFlag("type"));
         if (type == null) {
            String valid = Util.listValuesPretty(Type.values());
            throw new CommandException("citizens.commands.npc.ocelot.invalid-type", new Object[]{valid});
         }

         trait.setType(type);
      }

   }

   @Command(
      aliases = {"npc"},
      usage = "owner [name]",
      desc = "Set the owner of an NPC",
      modifiers = {"owner"},
      min = 1,
      max = 2,
      permission = "citizens.npc.owner"
   )
   public void owner(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      Owner ownerTrait = (Owner)npc.getTrait(Owner.class);
      if (args.argsLength() == 1) {
         Messaging.sendTr(sender, "citizens.commands.npc.owner.owner", npc.getName(), ownerTrait.getOwner());
      } else {
         String name = args.getString(1);
         if (ownerTrait.isOwnedBy(name)) {
            throw new CommandException("citizens.commands.npc.owner.already-owner", new Object[]{name, npc.getName()});
         } else {
            ownerTrait.setOwner(name);
            boolean serverOwner = name.equalsIgnoreCase("server");
            Messaging.sendTr(sender, serverOwner ? "citizens.commands.npc.owner.set-server" : "citizens.commands.npc.owner.set", npc.getName(), name);
         }
      }
   }

   @Command(
      aliases = {"npc"},
      usage = "pathrange [range]",
      desc = "Sets an NPC's pathfinding range",
      modifiers = {"pathrange", "pathfindingrange", "prange"},
      min = 2,
      max = 2,
      permission = "citizens.npc.pathfindingrange"
   )
   public void pathfindingRange(CommandContext args, CommandSender sender, NPC npc) {
      double range = Math.max((double)1.0F, args.getDouble(1));
      npc.getNavigator().getDefaultParameters().range((float)range);
      Messaging.sendTr(sender, "citizens.commands.npc.pathfindingrange.set", range);
   }

   @Command(
      aliases = {"npc"},
      usage = "playerlist (-a,r)",
      desc = "Sets whether the NPC is put in the playerlist",
      modifiers = {"playerlist"},
      min = 1,
      max = 1,
      flags = "ar",
      permission = "citizens.npc.playerlist"
   )
   @Requirements(
      selected = true,
      ownership = true,
      types = {EntityType.PLAYER}
   )
   public void playerlist(CommandContext args, CommandSender sender, NPC npc) {
      boolean remove = !(Boolean)npc.data().get("removefromplayerlist", Settings.Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean());
      if (args.hasFlag('a')) {
         remove = false;
      } else if (args.hasFlag('r')) {
         remove = true;
      }

      npc.data().setPersistent("removefromplayerlist", remove);
      if (npc.isSpawned()) {
         NMS.addOrRemoveFromPlayerList(npc.getBukkitEntity(), remove);
      }

      Messaging.sendTr(sender, remove ? "citizens.commands.npc.playerlist.removed" : "citizens.commands.npc.playerlist.added", npc.getName());
   }

   @Command(
      aliases = {"npc"},
      usage = "pose (--save [name]|--assume [name]|--remove [name]) (-a)",
      desc = "Changes/Saves/Lists NPC's head pose(s)",
      flags = "a",
      modifiers = {"pose"},
      min = 1,
      max = 2,
      permission = "citizens.npc.pose"
   )
   public void pose(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      Poses trait = (Poses)npc.getTrait(Poses.class);
      if (args.hasValueFlag("save")) {
         if (args.getFlag("save").isEmpty()) {
            throw new CommandException("citizens.commands.npc.pose.invalid-name");
         }

         if (args.getSenderLocation() == null) {
            throw new ServerCommandException();
         }

         if (!trait.addPose(args.getFlag("save"), args.getSenderLocation())) {
            throw new CommandException("citizens.commands.npc.pose.already-exists", new Object[]{args.getFlag("save")});
         }

         Messaging.sendTr(sender, "citizens.commands.npc.pose.added");
      } else if (args.hasValueFlag("assume")) {
         String pose = args.getFlag("assume");
         if (pose.isEmpty()) {
            throw new CommandException("citizens.commands.npc.pose.invalid-name");
         }

         if (!trait.hasPose(pose)) {
            throw new CommandException("citizens.commands.npc.pose.missing", new Object[]{pose});
         }

         trait.assumePose(pose);
      } else if (args.hasValueFlag("remove")) {
         if (args.getFlag("remove").isEmpty()) {
            throw new CommandException("citizens.commands.npc.pose.invalid-name");
         }

         if (!trait.removePose(args.getFlag("remove"))) {
            throw new CommandException("citizens.commands.npc.pose.missing", new Object[]{args.getFlag("remove")});
         }

         Messaging.sendTr(sender, "citizens.commands.npc.pose.removed");
      } else if (!args.hasFlag('a')) {
         trait.describe(sender, args.getInteger(1, 1));
      }

      if (args.hasFlag('a')) {
         if (args.getSenderLocation() == null) {
            throw new ServerCommandException();
         } else {
            Location location = args.getSenderLocation();
            trait.assumePose(location);
         }
      }
   }

   @Command(
      aliases = {"npc"},
      usage = "power",
      desc = "Toggle a creeper NPC as powered",
      modifiers = {"power"},
      min = 1,
      max = 1,
      permission = "citizens.npc.power"
   )
   @Requirements(
      selected = true,
      ownership = true,
      types = {EntityType.CREEPER}
   )
   public void power(CommandContext args, CommandSender sender, NPC npc) {
      Messaging.sendTr(sender, ((Powered)npc.getTrait(Powered.class)).toggle() ? "citizens.commands.npc.powered.set" : "citizens.commands.npc.powered.stopped");
   }

   @Command(
      aliases = {"npc"},
      usage = "profession|prof [profession]",
      desc = "Set a NPC's profession",
      modifiers = {"profession", "prof"},
      min = 2,
      max = 2,
      permission = "citizens.npc.profession"
   )
   @Requirements(
      selected = true,
      ownership = true,
      types = {EntityType.VILLAGER}
   )
   public void profession(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      String profession = args.getString(1);
      Villager.Profession parsed = (Villager.Profession)Util.matchEnum(Profession.values(), profession.toUpperCase());
      if (parsed == null) {
         throw new CommandException("citizens.commands.npc.profession.invalid-profession");
      } else {
         ((VillagerProfession)npc.getTrait(VillagerProfession.class)).setProfession(parsed);
         Messaging.sendTr(sender, "citizens.commands.npc.profession.set", npc.getName(), profession);
      }
   }

   @Command(
      aliases = {"npc"},
      usage = "remove|rem (all)",
      desc = "Remove a NPC",
      modifiers = {"remove", "rem"},
      min = 1,
      max = 2
   )
   @Requirements
   public void remove(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      if (args.argsLength() == 2) {
         if (!args.getString(1).equalsIgnoreCase("all")) {
            throw new CommandException("citizens.commands.npc.remove.incorrect-syntax");
         } else if (!sender.hasPermission("citizens.admin.remove.all") && !sender.hasPermission("citizens.admin")) {
            throw new NoPermissionsException();
         } else {
            this.npcRegistry.deregisterAll();
            Messaging.sendTr(sender, "citizens.commands.npc.remove.removed-all");
         }
      } else if (npc == null) {
         throw new CommandException("citizens.commands.requirements.must-have-selected");
      } else if (!(sender instanceof ConsoleCommandSender) && !((Owner)npc.getTrait(Owner.class)).isOwnedBy(sender)) {
         throw new CommandException("citizens.commands.requirements.must-be-owner");
      } else if (!sender.hasPermission("citizens.npc.remove") && !sender.hasPermission("citizens.admin")) {
         throw new NoPermissionsException();
      } else {
         npc.destroy();
         Messaging.sendTr(sender, "citizens.commands.npc.remove.removed", npc.getName());
      }
   }

   @Command(
      aliases = {"npc"},
      usage = "rename [name]",
      desc = "Rename a NPC",
      modifiers = {"rename"},
      min = 2,
      permission = "citizens.npc.rename"
   )
   public void rename(CommandContext args, CommandSender sender, NPC npc) {
      String oldName = npc.getName();
      String newName = args.getJoinedStrings(1);
      if (newName.length() > 16) {
         Messaging.sendErrorTr(sender, "citizens.commands.npc.create.npc-name-too-long");
         newName = newName.substring(0, 15);
      }

      Location prev = npc.isSpawned() ? npc.getBukkitEntity().getLocation() : null;
      npc.despawn(DespawnReason.PENDING_RESPAWN);
      npc.setName(newName);
      if (prev != null) {
         npc.spawn(prev);
      }

      Messaging.sendTr(sender, "citizens.commands.npc.rename.renamed", oldName, newName);
   }

   @Command(
      aliases = {"npc"},
      usage = "respawn [delay in ticks]",
      desc = "Sets an NPC's respawn delay in ticks",
      modifiers = {"respawn"},
      min = 1,
      max = 2,
      permission = "citizens.npc.respawn"
   )
   public void respawn(CommandContext args, CommandSender sender, NPC npc) {
      if (args.argsLength() > 1) {
         int delay = args.getInteger(1);
         npc.data().setPersistent("respawn-delay", delay);
         Messaging.sendTr(sender, "citizens.commands.npc.respawn.delay-set", delay);
      } else {
         Messaging.sendTr(sender, "citizens.commands.npc.respawn.describe", npc.data().get("respawn-delay", -1));
      }

   }

   @Command(
      aliases = {"npc"},
      usage = "select|sel [id|name] (--r range)",
      desc = "Select a NPC with the given ID or name",
      modifiers = {"select", "sel"},
      min = 1,
      max = 2,
      permission = "citizens.npc.select"
   )
   @Requirements
   public void select(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      NPC toSelect = null;
      if (args.argsLength() <= 1) {
         if (!(sender instanceof Player)) {
            throw new ServerCommandException();
         }

         double range = Math.abs(args.getFlagDouble("r", (double)10.0F));
         Entity player = (Player)sender;
         final Location location = args.getSenderLocation();
         List<Entity> search = player.getNearbyEntities(range, range, range);
         Collections.sort(search, new Comparator() {
            public int compare(Entity o1, Entity o2) {
               double d = o1.getLocation().distanceSquared(location) - o2.getLocation().distanceSquared(location);
               return d > (double)0.0F ? 1 : (d < (double)0.0F ? -1 : 0);
            }
         });

         for(Entity possibleNPC : search) {
            NPC test = this.npcRegistry.getNPC(possibleNPC);
            if (test != null) {
               toSelect = test;
               break;
            }
         }
      } else {
         try {
            int id = args.getInteger(1);
            toSelect = this.npcRegistry.getById(id);
         } catch (NumberFormatException var13) {
            String name = args.getString(1);
            List<NPC> possible = Lists.newArrayList();
            double range = (double)-1.0F;
            if (args.hasValueFlag("r")) {
               range = Math.abs(args.getFlagDouble("r"));
            }

            for(NPC test : this.npcRegistry) {
               if (test.getName().equalsIgnoreCase(name) && (!(range > (double)0.0F) || !test.isSpawned() || Util.locationWithinRange(args.getSenderLocation(), test.getBukkitEntity().getLocation(), range))) {
                  possible.add(test);
               }
            }

            if (possible.size() == 1) {
               toSelect = (NPC)possible.get(0);
            } else if (possible.size() > 1) {
               SelectionPrompt.start(this.selector, (Player)sender, possible);
               return;
            }
         }
      }

      if (toSelect != null && ((Spawned)toSelect.getTrait(Spawned.class)).shouldSpawn()) {
         if (npc != null && toSelect.getId() == npc.getId()) {
            throw new CommandException("citizens.commands.npc.select.already-selected");
         } else {
            this.selector.select(sender, toSelect);
            Messaging.sendWithNPC(sender, Settings.Setting.SELECTION_MESSAGE.asString(), toSelect);
         }
      } else {
         throw new CommandException("citizens.notifications.npc-not-found");
      }
   }

   @Command(
      aliases = {"npc"},
      usage = "skeletontype [type]",
      desc = "Sets the NPC's skeleton type",
      modifiers = {"skeletontype", "sktype"},
      min = 2,
      max = 2,
      permission = "citizens.npc.skeletontype"
   )
   @Requirements(
      selected = true,
      ownership = true,
      types = {EntityType.SKELETON}
   )
   public void skeletonType(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      Skeleton.SkeletonType type;
      type = (type = SkeletonType.getType(args.getInteger(1))) == null ? SkeletonType.valueOf(args.getString(1)) : type;
      if (type == null) {
         throw new CommandException("citizens.commands.npc.skeletontype.invalid-type");
      } else {
         ((NPCSkeletonType)npc.getTrait(NPCSkeletonType.class)).setType(type);
         Messaging.sendTr(sender, "citizens.commands.npc.skeletontype.set", npc.getName(), type);
      }
   }

   @Command(
      aliases = {"npc"},
      usage = "size [size]",
      desc = "Sets the NPC's size",
      modifiers = {"size"},
      min = 1,
      max = 2,
      permission = "citizens.npc.size"
   )
   @Requirements(
      selected = true,
      ownership = true,
      types = {EntityType.MAGMA_CUBE, EntityType.SLIME}
   )
   public void slimeSize(CommandContext args, CommandSender sender, NPC npc) {
      SlimeSize trait = (SlimeSize)npc.getTrait(SlimeSize.class);
      if (args.argsLength() <= 1) {
         trait.describe(sender);
      } else {
         int size = Math.max(1, args.getInteger(1));
         trait.setSize(size);
         Messaging.sendTr(sender, "citizens.commands.npc.size.set", npc.getName(), size);
      }
   }

   @Command(
      aliases = {"npc"},
      usage = "spawn (id)",
      desc = "Spawn an existing NPC",
      modifiers = {"spawn"},
      min = 1,
      max = 2,
      permission = "citizens.npc.spawn"
   )
   @Requirements(
      ownership = true
   )
   public void spawn(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      NPC respawn = args.argsLength() > 1 ? this.npcRegistry.getById(args.getInteger(1)) : npc;
      if (respawn == null) {
         if (args.argsLength() > 1) {
            throw new CommandException("citizens.commands.npc.spawn.missing-npc-id", new Object[]{args.getInteger(1)});
         } else {
            throw new CommandException("citizens.commands.requirements.must-have-selected");
         }
      } else if (respawn.isSpawned()) {
         throw new CommandException("citizens.commands.npc.spawn.already-spawned", new Object[]{respawn.getName()});
      } else {
         Location location = ((CurrentLocation)respawn.getTrait(CurrentLocation.class)).getLocation();
         if (location == null || args.hasValueFlag("location")) {
            if (args.getSenderLocation() == null) {
               throw new CommandException("citizens.commands.npc.spawn.no-location");
            }

            location = args.getSenderLocation();
         }

         if (respawn.spawn(location)) {
            this.selector.select(sender, respawn);
            Messaging.sendTr(sender, "citizens.commands.npc.spawn.spawned", respawn.getName());
         }

      }
   }

   @Command(
      aliases = {"npc"},
      usage = "speak message to speak --target npcid|player_name --type vocal_type",
      desc = "Uses the NPCs SpeechController to talk",
      modifiers = {"speak"},
      min = 2,
      permission = "citizens.npc.speak"
   )
   public void speak(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      String type = ((Speech)npc.getTrait(Speech.class)).getDefaultVocalChord();
      String message = Colorizer.parseColors(args.getJoinedStrings(1));
      if (message.length() <= 0) {
         Messaging.send(sender, "Default Vocal Chord for " + npc.getName() + ": " + ((Speech)npc.getTrait(Speech.class)).getDefaultVocalChord());
      } else {
         SpeechContext context = new SpeechContext(message);
         if (args.hasValueFlag("target")) {
            if (args.getFlag("target").matches("\\d+")) {
               NPC target = CitizensAPI.getNPCRegistry().getById(Integer.valueOf(args.getFlag("target")));
               if (target != null) {
                  context.addRecipient(target.getBukkitEntity());
               }
            } else {
               Player player = Bukkit.getPlayer(args.getFlag("target"));
               if (player != null) {
                  context.addRecipient(player);
               }
            }
         }

         if (args.hasValueFlag("type") && CitizensAPI.getSpeechFactory().isRegistered(args.getFlag("type"))) {
            type = args.getFlag("type");
         }

         npc.getDefaultSpeechController().speak(context, type);
      }
   }

   @Command(
      aliases = {"npc"},
      usage = "speed [speed]",
      desc = "Sets the movement speed of an NPC as a percentage",
      modifiers = {"speed"},
      min = 2,
      max = 2,
      permission = "citizens.npc.speed"
   )
   public void speed(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      float newSpeed = (float)Math.abs(args.getDouble(1));
      if ((double)newSpeed >= Settings.Setting.MAX_SPEED.asDouble()) {
         throw new CommandException("citizens.commands.npc.speed.modifier-above-limit");
      } else {
         npc.getNavigator().getDefaultParameters().speedModifier(newSpeed);
         Messaging.sendTr(sender, "citizens.commands.npc.speed.set", newSpeed);
      }
   }

   @Command(
      aliases = {"npc"},
      usage = "targetable",
      desc = "Toggles an NPC's targetability",
      modifiers = {"targetable"},
      min = 1,
      max = 1,
      permission = "citizens.npc.targetable"
   )
   public void targetable(CommandContext args, CommandSender sender, NPC npc) {
      boolean targetable = !(Boolean)npc.data().get("protected-target", npc.data().get("protected", true));
      if (args.hasFlag('t')) {
         npc.data().set("protected-target", targetable);
      } else {
         npc.data().setPersistent("protected-target", targetable);
      }

      Messaging.sendTr(sender, targetable ? "citizens.commands.npc.targetable.set" : "citizens.commands.npc.targetable.unset", npc.getName());
   }

   @Command(
      aliases = {"npc"},
      usage = "tp",
      desc = "Teleport to a NPC",
      modifiers = {"tp", "teleport"},
      min = 1,
      max = 1,
      permission = "citizens.npc.tp"
   )
   public void tp(CommandContext args, Player player, NPC npc) {
      Location to = ((CurrentLocation)npc.getTrait(CurrentLocation.class)).getLocation();
      if (to == null) {
         Messaging.sendError(player, "citizens.commands.npc.tp.location-not-found");
      } else {
         player.teleport(to, TeleportCause.COMMAND);
         Messaging.sendTr(player, "citizens.commands.npc.tp.teleported", npc.getName());
      }
   }

   @Command(
      aliases = {"npc"},
      usage = "tphere",
      desc = "Teleport a NPC to your location",
      modifiers = {"tphere", "tph", "move"},
      min = 1,
      max = 1,
      permission = "citizens.npc.tphere"
   )
   public void tphere(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      if (args.getSenderLocation() == null) {
         throw new ServerCommandException();
      } else {
         if (!npc.isSpawned()) {
            npc.spawn(args.getSenderLocation());
            if (!sender.hasPermission("citizens.npc.tphere.multiworld") && npc.getBukkitEntity().getLocation().getWorld() != args.getSenderLocation().getWorld()) {
               npc.despawn(DespawnReason.REMOVAL);
               throw new CommandException("citizens.commands.npc.tphere.multiworld-not-allowed");
            }
         } else {
            if (!sender.hasPermission("citizens.npc.tphere.multiworld") && npc.getBukkitEntity().getLocation().getWorld() != args.getSenderLocation().getWorld()) {
               npc.despawn(DespawnReason.REMOVAL);
               throw new CommandException("citizens.commands.npc.tphere.multiworld-not-allowed");
            }

            npc.teleport(args.getSenderLocation(), TeleportCause.COMMAND);
         }

         Messaging.sendTr(sender, "citizens.commands.npc.tphere.teleported", npc.getName());
      }
   }

   @Command(
      aliases = {"npc"},
      usage = "tpto [player name|npc id] [player name|npc id]",
      desc = "Teleport an NPC or player to another NPC or player",
      modifiers = {"tpto"},
      min = 3,
      max = 3,
      permission = "citizens.npc.tpto"
   )
   @Requirements
   public void tpto(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      Entity from = null;
      Entity to = null;
      if (npc != null) {
         from = npc.getBukkitEntity();
      }

      boolean firstWasPlayer = false;

      try {
         int id = args.getInteger(1);
         NPC fromNPC = CitizensAPI.getNPCRegistry().getById(id);
         if (fromNPC != null) {
            from = fromNPC.getBukkitEntity();
         }
      } catch (NumberFormatException var9) {
         from = Bukkit.getPlayerExact(args.getString(1));
         firstWasPlayer = true;
      }

      try {
         int id = args.getInteger(2);
         NPC toNPC = CitizensAPI.getNPCRegistry().getById(id);
         if (toNPC != null) {
            to = toNPC.getBukkitEntity();
         }
      } catch (NumberFormatException var10) {
         if (!firstWasPlayer) {
            to = Bukkit.getPlayerExact(args.getString(2));
         }
      }

      if (from == null) {
         throw new CommandException("citizens.commands.npc.tpto.from-not-found");
      } else if (to == null) {
         throw new CommandException("citizens.commands.npc.tpto.to-not-found");
      } else {
         from.teleport(to);
         Messaging.sendTr(sender, "citizens.commands.npc.tpto.success");
      }
   }

   @Command(
      aliases = {"npc"},
      usage = "type [type]",
      desc = "Sets an NPC's entity type",
      modifiers = {"type"},
      min = 2,
      max = 2,
      permission = "citizens.npc.type"
   )
   public void type(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      EntityType type = Util.matchEntityType(args.getString(1));
      if (type == null) {
         throw new CommandException("citizens.commands.npc.type.invalid", new Object[]{args.getString(1)});
      } else {
         npc.setBukkitEntityType(type);
         Messaging.sendTr(sender, "citizens.commands.npc.type.set", npc.getName(), args.getString(1));
      }
   }

   @Command(
      aliases = {"npc"},
      usage = "vulnerable (-t)",
      desc = "Toggles an NPC's vulnerability",
      modifiers = {"vulnerable"},
      min = 1,
      max = 1,
      flags = "t",
      permission = "citizens.npc.vulnerable"
   )
   public void vulnerable(CommandContext args, CommandSender sender, NPC npc) {
      boolean vulnerable = !(Boolean)npc.data().get("protected", true);
      if (args.hasFlag('t')) {
         npc.data().set("protected", vulnerable);
      } else {
         npc.data().setPersistent("protected", vulnerable);
      }

      String key = vulnerable ? "citizens.commands.npc.vulnerable.stopped" : "citizens.commands.npc.vulnerable.set";
      Messaging.sendTr(sender, key, npc.getName());
   }

   @Command(
      aliases = {"npc"},
      usage = "wolf (-s(itting) a(ngry) t(amed)) --collar [hex rgb color|name]",
      desc = "Sets wolf modifiers",
      modifiers = {"wolf"},
      min = 1,
      max = 1,
      flags = "sat",
      permission = "citizens.npc.wolf"
   )
   @Requirements(
      selected = true,
      ownership = true,
      types = {EntityType.WOLF}
   )
   public void wolf(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      WolfModifiers trait = (WolfModifiers)npc.getTrait(WolfModifiers.class);
      trait.setAngry(args.hasFlag('a'));
      trait.setSitting(args.hasFlag('s'));
      trait.setTamed(args.hasFlag('t'));
      if (args.hasValueFlag("collar")) {
         String unparsed = args.getFlag("collar");
         DyeColor color = null;

         try {
            color = DyeColor.valueOf(unparsed.toUpperCase().replace(' ', '_'));
         } catch (IllegalArgumentException var9) {
            int rgb = Integer.parseInt(unparsed.replace("#", ""), 16);
            color = DyeColor.getByColor(org.bukkit.Color.fromRGB(rgb));
         }

         if (color == null) {
            throw new CommandException("citizens.commands.npc.wolf.unknown-collar-color");
         }

         trait.setCollarColor(color);
      }

   }

   @Command(
      aliases = {"npc"},
      usage = "zombiemod (-b(aby), -v(illager))",
      desc = "Sets a zombie NPC to be a baby or villager",
      modifiers = {"zombie", "zombiemod"},
      flags = "bv",
      min = 1,
      max = 1,
      permission = "citizens.npc.zombiemodifier"
   )
   @Requirements(
      selected = true,
      ownership = true,
      types = {EntityType.ZOMBIE}
   )
   public void zombieModifier(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
      ZombieModifier trait = (ZombieModifier)npc.getTrait(ZombieModifier.class);
      if (args.hasFlag('b')) {
         boolean isBaby = trait.toggleBaby();
         Messaging.sendTr(sender, isBaby ? "citizens.commands.npc.zombiemod.baby-set" : "citizens.commands.npc.zombiemod.baby-unset", npc.getName());
      }

      if (args.hasFlag('v')) {
         boolean isVillager = trait.toggleVillager();
         Messaging.sendTr(sender, isVillager ? "citizens.commands.npc.zombiemod.villager-set" : "citizens.commands.npc.zombiemod.villager-unset", npc.getName());
      }

   }
}
