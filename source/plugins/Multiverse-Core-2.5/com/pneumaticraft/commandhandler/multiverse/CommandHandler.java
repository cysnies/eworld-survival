package com.pneumaticraft.commandhandler.multiverse;

import com.lithium3141.shellparser.ShellParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class CommandHandler {
   protected Plugin plugin;
   protected List queuedCommands;
   protected List allCommands;
   protected PermissionsInterface permissions;
   private Properties props = new Properties();
   private double version;

   public CommandHandler(Plugin plugin, PermissionsInterface permissions) {
      super();

      try {
         this.props.load(this.getClass().getResourceAsStream("/commandhandler.properties"));
         this.version = (double)Integer.parseInt(this.props.getProperty("version", "-1"));
      } catch (NumberFormatException var4) {
         this.logBadCH(plugin);
      } catch (FileNotFoundException var5) {
         this.logBadCH(plugin);
      } catch (IOException var6) {
         this.logBadCH(plugin);
      }

      this.plugin = plugin;
      this.allCommands = new ArrayList();
      this.queuedCommands = new ArrayList();
      this.permissions = permissions;
   }

   private void logBadCH(Plugin plugin) {
      plugin.getLogger().log(Level.SEVERE, String.format("CommandHandler looks corrupted, meaning this plugin (%s) is corrupted too!", plugin.getDescription().getName()));
   }

   public double getVersion() {
      return this.version;
   }

   public List getCommands(CommandSender sender) {
      List<Command> permissiveCommands = new ArrayList();

      for(Command c : this.allCommands) {
         if (this.permissions.hasAnyPermission(sender, c.getAllPermissionStrings(), c.isOpRequired())) {
            permissiveCommands.add(c);
         }
      }

      return permissiveCommands;
   }

   public List getAllCommands() {
      return this.allCommands;
   }

   public boolean locateAndRunCommand(CommandSender sender, List args) {
      return this.locateAndRunCommand(sender, args, true);
   }

   public boolean locateAndRunCommand(CommandSender sender, List args, boolean notifySender) {
      List<String> parsedArgs = this.parseAllQuotedStrings(args);
      CommandKey key = null;
      Iterator<Command> iterator = this.allCommands.iterator();
      Command foundCommand = null;
      List<Command> foundCommands = new ArrayList();
      List<CommandKey> foundKeys = new ArrayList();

      while(iterator.hasNext()) {
         foundCommand = (Command)iterator.next();
         key = foundCommand.getKey(parsedArgs);
         if (key != null) {
            foundCommands.add(foundCommand);
            foundKeys.add(key);
         }
      }

      this.processFoundCommands(foundCommands, foundKeys, sender, parsedArgs, notifySender);
      return true;
   }

   private void processFoundCommands(List foundCommands, List foundKeys, CommandSender sender, List parsedArgs) {
      this.processFoundCommands(foundCommands, foundKeys, sender, parsedArgs, true);
   }

   private void processFoundCommands(List foundCommands, List foundKeys, CommandSender sender, List parsedArgs, boolean notifySender) {
      if (foundCommands.size() != 0) {
         Command bestMatch = null;
         CommandKey matchingKey = null;
         int bestMatchInt = 0;

         for(int i = 0; i < foundCommands.size(); ++i) {
            List<String> parsedCopy = new ArrayList(parsedArgs);
            ((Command)foundCommands.get(i)).removeKeyArgs(parsedCopy, ((CommandKey)foundKeys.get(i)).getKey());
            if (((Command)foundCommands.get(i)).getNumKeyArgs(((CommandKey)foundKeys.get(i)).getKey()) > bestMatchInt) {
               bestMatch = (Command)foundCommands.get(i);
               matchingKey = (CommandKey)foundKeys.get(i);
               bestMatchInt = bestMatch.getNumKeyArgs(matchingKey.getKey());
            } else if (((Command)foundCommands.get(i)).getNumKeyArgs(((CommandKey)foundKeys.get(i)).getKey()) == bestMatchInt && ((CommandKey)foundKeys.get(i)).hasValidNumberOfArgs(parsedCopy.size())) {
               bestMatch = (Command)foundCommands.get(i);
               matchingKey = (CommandKey)foundKeys.get(i);
            }
         }

         if (bestMatch != null) {
            bestMatch.removeKeyArgs(parsedArgs, matchingKey.getKey());
            if (parsedArgs.size() == 1 && ((String)parsedArgs.get(0)).equals("?") && this.permissions.hasAnyPermission(sender, bestMatch.getAllPermissionStrings(), bestMatch.isOpRequired())) {
               bestMatch.showHelp(sender);
            } else {
               this.checkAndRunCommand(sender, parsedArgs, bestMatch, notifySender);
            }
         }

      }
   }

   public void registerCommand(Command command) {
      this.allCommands.add(command);
   }

   private List parseAllQuotedStrings(List args) {
      String arg = null;
      if (args.size() == 0) {
         arg = "";
      } else {
         arg = (String)args.get(0);

         for(int i = 1; i < args.size(); ++i) {
            arg = arg + " " + (String)args.get(i);
         }
      }

      List<String> result = ShellParser.safeParseString(arg);
      return (List)(result == null ? new ArrayList() : result);
   }

   public void queueCommand(CommandSender sender, String commandName, String methodName, List args, Class[] paramTypes, String message, String message2, String success, String fail, int seconds) {
      this.cancelQueuedCommand(sender);
      this.queuedCommands.add(new QueuedCommand(methodName, args, paramTypes, sender, Calendar.getInstance(), this.plugin, success, fail, seconds));
      if (message == null) {
         message = "The command " + ChatColor.RED + commandName + ChatColor.WHITE + " has been halted due to the fact that it could break something!";
      } else {
         message = message.replace("{CMD}", ChatColor.RED + commandName + ChatColor.WHITE);
      }

      if (message2 == null) {
         message2 = "If you still wish to execute " + ChatColor.RED + commandName + ChatColor.WHITE;
      } else {
         message2 = message2.replace("{CMD}", ChatColor.RED + commandName + ChatColor.WHITE);
      }

      sender.sendMessage(message);
      sender.sendMessage(message2);
      sender.sendMessage("please type: " + ChatColor.GREEN + "/mvconfirm");
      sender.sendMessage(ChatColor.GREEN + "/mvconfirm" + ChatColor.WHITE + " will only be available for " + seconds + " seconds.");
   }

   public void queueCommand(CommandSender sender, String commandName, String methodName, List args, Class[] paramTypes, String success, String fail) {
      this.queueCommand(sender, commandName, methodName, args, paramTypes, (String)null, (String)null, success, fail, 10);
   }

   public boolean confirmQueuedCommand(CommandSender sender) {
      for(QueuedCommand com : this.queuedCommands) {
         if (com.getSender().equals(sender)) {
            if (com.execute()) {
               if (com.getSuccess() != null && com.getSuccess().length() > 0) {
                  sender.sendMessage(com.getSuccess());
               }

               return true;
            }

            if (com.getFail() != null && com.getFail().length() > 0) {
               sender.sendMessage(com.getFail());
               return false;
            }
         }
      }

      return false;
   }

   public void cancelQueuedCommand(CommandSender sender) {
      QueuedCommand c = null;

      for(QueuedCommand com : this.queuedCommands) {
         if (com.getSender().equals(sender)) {
            c = com;
         }
      }

      if (c != null) {
         this.queuedCommands.remove(c);
      }

   }

   public static String getFlag(String flag, List args) {
      int i = 0;

      try {
         for(String s : args) {
            if (s.equalsIgnoreCase(flag)) {
               return (String)args.get(i + 1);
            }

            ++i;
         }
      } catch (IndexOutOfBoundsException var5) {
      }

      return null;
   }

   private void checkAndRunCommand(CommandSender sender, List parsedArgs, Command foundCommand, boolean notifySender) {
      if (this.permissions.hasAnyPermission(sender, foundCommand.getAllPermissionStrings(), foundCommand.isOpRequired())) {
         if (foundCommand.checkArgLength(parsedArgs)) {
            foundCommand.runCommand(sender, parsedArgs);
         } else {
            foundCommand.showHelp(sender);
         }
      } else if (notifySender) {
         sender.sendMessage("You do not have any of the required permission(s):");

         for(String perm : foundCommand.getAllPermissionStrings()) {
            sender.sendMessage(" - " + ChatColor.GREEN + perm);
         }
      }

   }

   private void checkAndRunCommand(CommandSender sender, List parsedArgs, Command foundCommand) {
      this.checkAndRunCommand(sender, parsedArgs, foundCommand, true);
   }
}
