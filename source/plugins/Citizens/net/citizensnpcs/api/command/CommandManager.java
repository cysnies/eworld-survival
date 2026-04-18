package net.citizensnpcs.api.command;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.command.exception.CommandUsageException;
import net.citizensnpcs.api.command.exception.NoPermissionsException;
import net.citizensnpcs.api.command.exception.ServerCommandException;
import net.citizensnpcs.api.command.exception.UnhandledCommandException;
import net.citizensnpcs.api.command.exception.WrappedCommandException;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Paginator;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class CommandManager {
   private final Map annotationProcessors = Maps.newHashMap();
   private final Map commands = new HashMap();
   private Injector injector;
   private final Map instances = new HashMap();
   private final ListMultimap registeredAnnotations = ArrayListMultimap.create();
   private final Set serverCommands = new HashSet();
   private static final String COMMAND_FORMAT = "<7>/<c>%s%s <7>- <e>%s";
   private static final Logger logger = Logger.getLogger(CommandManager.class.getCanonicalName());

   public CommandManager() {
      super();
      this.registerAnnotationProcessor(new RequirementsProcessor());
   }

   public void execute(org.bukkit.command.Command command, String[] args, CommandSender sender, Object... methodArgs) throws CommandException {
      String[] newArgs = new String[args.length + 1];
      System.arraycopy(args, 0, newArgs, 1, args.length);
      newArgs[0] = command.getName().toLowerCase();
      Object[] newMethodArgs = new Object[methodArgs.length + 1];
      System.arraycopy(methodArgs, 0, newMethodArgs, 1, methodArgs.length);
      this.executeMethod(newArgs, sender, newMethodArgs);
   }

   private void executeHelp(String[] args, CommandSender sender) throws CommandException {
      if (!sender.hasPermission("citizens." + args[0] + ".help")) {
         throw new NoPermissionsException();
      } else {
         int page = 1;

         try {
            page = args.length == 3 ? Integer.parseInt(args[2]) : page;
         } catch (NumberFormatException var5) {
            this.sendSpecificHelp(sender, args[0], args[2]);
         }

         this.sendHelp(sender, args[0], page);
      }
   }

   private void executeMethod(String[] args, CommandSender sender, Object[] methodArgs) throws CommandException {
      String cmdName = args[0].toLowerCase();
      String modifier = args.length > 1 ? args[1] : "";
      boolean help = modifier.toLowerCase().equals("help");
      Method method = (Method)this.commands.get(cmdName + " " + modifier.toLowerCase());
      if (method == null && !help) {
         method = (Method)this.commands.get(cmdName + " *");
      }

      if (method == null && help) {
         this.executeHelp(args, sender);
      } else if (method == null) {
         throw new UnhandledCommandException();
      } else if (!this.serverCommands.contains(method) && sender instanceof ConsoleCommandSender) {
         throw new ServerCommandException();
      } else if (!this.hasPermission(method, sender)) {
         throw new NoPermissionsException();
      } else {
         Command cmd = (Command)method.getAnnotation(Command.class);
         CommandContext context = new CommandContext(sender, args);
         if (context.argsLength() < cmd.min()) {
            throw new CommandUsageException("citizens.commands.requirements.too-few-arguments", this.getUsage(args, cmd));
         } else if (cmd.max() != -1 && context.argsLength() > cmd.max()) {
            throw new CommandUsageException("citizens.commands.requirements.too-many-arguments", this.getUsage(args, cmd));
         } else {
            if (!cmd.flags().contains("*")) {
               for(char flag : context.getFlags()) {
                  if (cmd.flags().indexOf(String.valueOf(flag)) == -1) {
                     throw new CommandUsageException("Unknown flag: " + flag, this.getUsage(args, cmd));
                  }
               }
            }

            methodArgs[0] = context;

            for(Annotation annotation : this.registeredAnnotations.get(method)) {
               CommandAnnotationProcessor processor = (CommandAnnotationProcessor)this.annotationProcessors.get(annotation.annotationType());
               processor.process(sender, context, annotation, methodArgs);
            }

            Object instance = this.instances.get(method);

            try {
               method.invoke(instance, methodArgs);
            } catch (IllegalArgumentException e) {
               logger.log(Level.SEVERE, "Failed to execute command", e);
            } catch (IllegalAccessException e) {
               logger.log(Level.SEVERE, "Failed to execute command", e);
            } catch (InvocationTargetException e) {
               if (e.getCause() instanceof CommandException) {
                  throw (CommandException)e.getCause();
               }

               throw new WrappedCommandException(e.getCause());
            }

         }
      }
   }

   public boolean executeSafe(org.bukkit.command.Command command, String[] args, CommandSender sender, Object... methodArgs) {
      try {
         try {
            this.execute(command, args, sender, methodArgs);
         } catch (ServerCommandException var6) {
            Messaging.sendTr(sender, "citizens.commands.requirements.must-be-ingame");
         } catch (CommandUsageException ex) {
            Messaging.sendError(sender, ex.getMessage());
            Messaging.sendError(sender, ex.getUsage());
         } catch (UnhandledCommandException var8) {
            return false;
         } catch (WrappedCommandException ex) {
            throw ex.getCause();
         } catch (CommandException ex) {
            Messaging.sendError(sender, ex.getMessage());
         } catch (NumberFormatException var11) {
            Messaging.sendErrorTr(sender, "citizens.commands.invalid-number");
         }
      } catch (Throwable ex) {
         ex.printStackTrace();
         if (sender instanceof Player) {
            Messaging.sendErrorTr(sender, "citizens.commands.console-error");
            Messaging.sendError(sender, ex.getClass().getName() + ": " + ex.getMessage());
         }
      }

      return true;
   }

   public String getClosestCommandModifier(String command, String modifier) {
      int minDist = Integer.MAX_VALUE;
      command = command.toLowerCase();
      String closest = "";

      for(String cmd : this.commands.keySet()) {
         String[] split = cmd.split(" ");
         if (split.length > 1 && split[0].equals(command)) {
            int distance = getLevenshteinDistance(modifier, split[1]);
            if (minDist > distance) {
               minDist = distance;
               closest = split[1];
            }
         }
      }

      return closest;
   }

   public CommandInfo getCommand(String rootCommand, String modifier) {
      String joined = Joiner.on(' ').join(rootCommand, modifier, new Object[0]);

      for(Map.Entry entry : this.commands.entrySet()) {
         if (((String)entry.getKey()).equalsIgnoreCase(joined) && entry.getValue() != null) {
            Command commandAnnotation = (Command)((Method)entry.getValue()).getAnnotation(Command.class);
            if (commandAnnotation != null) {
               return new CommandInfo(commandAnnotation);
            }
         }
      }

      return null;
   }

   public List getCommands(String command) {
      List<CommandInfo> cmds = Lists.newArrayList();
      command = command.toLowerCase();

      for(Map.Entry entry : this.commands.entrySet()) {
         if (((String)entry.getKey()).startsWith(command) && entry.getValue() != null) {
            Command commandAnnotation = (Command)((Method)entry.getValue()).getAnnotation(Command.class);
            if (commandAnnotation != null) {
               cmds.add(new CommandInfo(commandAnnotation));
            }
         }
      }

      return cmds;
   }

   private List getLines(CommandSender sender, String baseCommand) {
      Set<CommandInfo> processed = Sets.newHashSet();
      List<String> lines = new ArrayList();

      for(CommandInfo info : this.getCommands(baseCommand)) {
         Command command = info.getCommandAnnotation();
         if (!processed.contains(info) && (sender.hasPermission("citizens.admin") || sender.hasPermission("citizens." + command.permission()))) {
            lines.add(format(command, baseCommand));
            if (command.modifiers().length > 1) {
               processed.add(info);
            }
         }
      }

      return lines;
   }

   private String getUsage(String[] args, Command cmd) {
      return "/" + args[0] + " " + cmd.usage();
   }

   public boolean hasCommand(org.bukkit.command.Command cmd, String modifier) {
      String cmdName = cmd.getName().toLowerCase();
      return this.commands.containsKey(cmdName + " " + modifier.toLowerCase()) || this.commands.containsKey(cmdName + " *");
   }

   private boolean hasPermission(CommandSender sender, String perm) {
      return sender.hasPermission(perm);
   }

   private boolean hasPermission(Method method, CommandSender sender) {
      Command cmd = (Command)method.getAnnotation(Command.class);
      return cmd.permission().isEmpty() || this.hasPermission(sender, cmd.permission()) || this.hasPermission(sender, "admin");
   }

   public void register(Class clazz) {
      this.registerMethods(clazz, (Method)null);
   }

   public void registerAnnotationProcessor(CommandAnnotationProcessor processor) {
      this.annotationProcessors.put(processor.getAnnotationClass(), processor);
   }

   private void registerMethods(Class clazz, Method parent) {
      Object obj = this.injector != null ? this.injector.getInstance(clazz) : null;
      this.registerMethods(clazz, parent, obj);
   }

   private void registerMethods(Class clazz, Method parent, Object obj) {
      for(Method method : clazz.getMethods()) {
         if (method.isAnnotationPresent(Command.class)) {
            if (!Modifier.isStatic(method.getModifiers())) {
               if (obj == null) {
                  continue;
               }

               this.instances.put(method, obj);
            }

            Command cmd = (Command)method.getAnnotation(Command.class);

            for(String alias : cmd.aliases()) {
               for(String modifier : cmd.modifiers()) {
                  this.commands.put(alias + " " + modifier, method);
               }

               if (!this.commands.containsKey(alias + " help")) {
                  this.commands.put(alias + " help", (Object)null);
               }
            }

            List<Annotation> annotations = Lists.newArrayList();

            for(Annotation annotation : method.getDeclaringClass().getAnnotations()) {
               Class<? extends Annotation> annotationClass = annotation.annotationType();
               if (this.annotationProcessors.containsKey(annotationClass)) {
                  annotations.add(annotation);
               }
            }

            for(Annotation annotation : method.getAnnotations()) {
               Class<? extends Annotation> annotationClass = annotation.annotationType();
               if (this.annotationProcessors.containsKey(annotationClass)) {
                  Iterator<Annotation> itr = annotations.iterator();

                  while(itr.hasNext()) {
                     Annotation previous = (Annotation)itr.next();
                     if (previous.annotationType() == annotationClass) {
                        itr.remove();
                     }
                  }

                  annotations.add(annotation);
               }
            }

            if (annotations.size() > 0) {
               this.registeredAnnotations.putAll(method, annotations);
            }

            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length <= 1 || parameterTypes[1] == CommandSender.class) {
               this.serverCommands.add(method);
            }
         }
      }

   }

   private void sendHelp(CommandSender sender, String name, int page) throws CommandException {
      if (name.equalsIgnoreCase("npc")) {
         name = "NPC";
      }

      Paginator paginator = (new Paginator()).header(capitalize(name) + " " + Messaging.tr("citizens.commands.help.header"));

      for(String line : this.getLines(sender, name.toLowerCase())) {
         paginator.addLine(line);
      }

      if (!paginator.sendPage(sender, page)) {
         throw new CommandException("citizens.commands.page-missing", new Object[]{page});
      }
   }

   private void sendSpecificHelp(CommandSender sender, String rootCommand, String modifier) throws CommandException {
      CommandInfo info = this.getCommand(rootCommand, modifier);
      if (info == null) {
         throw new CommandException("citizens.commands.help.command-missing", new Object[]{rootCommand + " " + modifier});
      } else {
         Messaging.send(sender, format(info.getCommandAnnotation(), rootCommand));
         String help = Messaging.tryTranslate(info.getCommandAnnotation().help());
         if (!help.isEmpty()) {
            Messaging.send(sender, ChatColor.AQUA + help);
         }
      }
   }

   public void setInjector(Injector injector) {
      this.injector = injector;
   }

   private static String capitalize(Object string) {
      String capitalize = string.toString();
      return capitalize.length() == 0 ? "" : Character.toUpperCase(capitalize.charAt(0)) + capitalize.substring(1, capitalize.length());
   }

   private static String format(Command command, String alias) {
      return String.format("<7>/<c>%s%s <7>- <e>%s", alias, command.usage().isEmpty() ? "" : " " + command.usage(), Messaging.tryTranslate(command.desc()));
   }

   private static int getLevenshteinDistance(String s, String t) {
      if (s != null && t != null) {
         int n = s.length();
         int m = t.length();
         if (n == 0) {
            return m;
         } else if (m == 0) {
            return n;
         } else {
            int[] p = new int[n + 1];
            int[] d = new int[n + 1];

            for(int i = 0; i <= n; p[i] = i++) {
            }

            for(int j = 1; j <= m; ++j) {
               char t_j = t.charAt(j - 1);
               d[0] = j;

               for(int var11 = 1; var11 <= n; ++var11) {
                  int cost = s.charAt(var11 - 1) == t_j ? 0 : 1;
                  d[var11] = Math.min(Math.min(d[var11 - 1] + 1, p[var11] + 1), p[var11 - 1] + cost);
               }

               int[] _d = p;
               p = d;
               d = _d;
            }

            return p[n];
         }
      } else {
         throw new IllegalArgumentException("Strings must not be null");
      }
   }

   public static class CommandInfo {
      private final Command commandAnnotation;

      public CommandInfo(Command commandAnnotation) {
         super();
         this.commandAnnotation = commandAnnotation;
      }

      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            CommandInfo other = (CommandInfo)obj;
            if (this.commandAnnotation == null) {
               if (other.commandAnnotation != null) {
                  return false;
               }
            } else if (!this.commandAnnotation.equals(other.commandAnnotation)) {
               return false;
            }

            return true;
         } else {
            return false;
         }
      }

      public Command getCommandAnnotation() {
         return this.commandAnnotation;
      }

      public int hashCode() {
         return 31 + (this.commandAnnotation == null ? 0 : this.commandAnnotation.hashCode());
      }
   }
}
