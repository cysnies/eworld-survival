package com.pneumaticraft.commandhandler.multiverse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

public abstract class Command {
   protected Plugin plugin;
   private int minimumArgLength;
   private int maximumArgLength;
   private String commandName;
   private String commandUsage;
   private List commandKeys;
   private List examples;
   private Permission permission;
   private List auxPerms;

   public Command(Plugin plugin) {
      super();
      this.plugin = plugin;
      this.auxPerms = new ArrayList();
      this.commandKeys = new ArrayList();
      this.examples = new ArrayList();
   }

   public List getKeyStrings() {
      List<String> returnList = new ArrayList();

      for(CommandKey ck : this.commandKeys) {
         returnList.add(ck.getKey());
      }

      return returnList;
   }

   public List getKeys() {
      return this.commandKeys;
   }

   public abstract void runCommand(CommandSender var1, List var2);

   public boolean checkArgLength(List args) {
      return (this.minimumArgLength == -1 || this.minimumArgLength <= args.size()) && (args.size() <= this.maximumArgLength || this.maximumArgLength == -1);
   }

   private String getArgsString(List args) {
      String returnString = "";

      for(String s : args) {
         returnString = returnString + s + " ";
      }

      return returnString.length() > 0 ? returnString.substring(0, returnString.length() - 1) : "";
   }

   public void addAdditonalPermission(Permission otherPerm) {
      if (this.plugin.getServer().getPluginManager().getPermission(otherPerm.getName()) == null) {
         this.plugin.getServer().getPluginManager().addPermission(otherPerm);
         this.addToParentPerms(otherPerm.getName());
      }

      this.auxPerms.add(otherPerm);
   }

   public CommandKey getKey(List parsedArgs) {
      String argsString = this.getArgsString(parsedArgs);

      for(CommandKey ck : this.commandKeys) {
         String identifier = ck.getKey().toLowerCase();
         if (argsString.matches(identifier + "(\\s+.*|\\s*)")) {
            return ck;
         }
      }

      return null;
   }

   public List removeKeyArgs(List args, String key) {
      int identifierLength = key.split(" ").length;

      for(int i = 0; i < identifierLength; ++i) {
         args.remove(0);
      }

      return args;
   }

   public int getNumKeyArgs(String key) {
      int identifierLength = key.split(" ").length;
      return identifierLength;
   }

   public String getPermissionString() {
      return this.permission.getName();
   }

   public Permission getPermission() {
      return this.permission;
   }

   public void setPermission(String p, String desc, PermissionDefault defaultPerm) {
      this.setPermission(new Permission(p, desc, defaultPerm));
   }

   public void setPermission(Permission perm) {
      this.permission = perm;

      try {
         this.plugin.getServer().getPluginManager().addPermission(this.permission);
         this.addToParentPerms(this.permission.getName());
      } catch (IllegalArgumentException var3) {
      }

   }

   private void addToParentPerms(String permString) {
      String permStringChopped = permString.replace(".*", "");
      String[] seperated = permStringChopped.split("\\.");
      String parentPermString = this.getParentPerm(seperated);
      if (parentPermString == null) {
         this.addToRootPermission("*", permStringChopped);
         this.addToRootPermission("*.*", permStringChopped);
      } else {
         Permission parentPermission = this.plugin.getServer().getPluginManager().getPermission(parentPermString);
         if (parentPermission == null) {
            parentPermission = new Permission(parentPermString);
            this.plugin.getServer().getPluginManager().addPermission(parentPermission);
            this.addToParentPerms(parentPermString);
         }

         Permission actualPermission = this.plugin.getServer().getPluginManager().getPermission(permString);
         if (actualPermission == null) {
            actualPermission = new Permission(permString);
            this.plugin.getServer().getPluginManager().addPermission(actualPermission);
         }

         if (!parentPermission.getChildren().containsKey(permString)) {
            parentPermission.getChildren().put(actualPermission.getName(), true);
            this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(parentPermission);
         }

      }
   }

   private void addToRootPermission(String rootPerm, String permStringChopped) {
      Permission rootPermission = this.plugin.getServer().getPluginManager().getPermission(rootPerm);
      if (rootPermission == null) {
         rootPermission = new Permission(rootPerm);
         this.plugin.getServer().getPluginManager().addPermission(rootPermission);
      }

      rootPermission.getChildren().put(permStringChopped + ".*", true);
      this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(rootPermission);
   }

   private String getParentPerm(String[] seperated) {
      if (seperated.length == 1) {
         return null;
      } else {
         String returnString = "";

         for(int i = 0; i < seperated.length - 1; ++i) {
            returnString = returnString + seperated[i] + ".";
         }

         return returnString + "*";
      }
   }

   public boolean isOpRequired() {
      return this.permission.getDefault() == PermissionDefault.OP;
   }

   public String getCommandName() {
      return this.commandName;
   }

   public String getCommandDesc() {
      return this.permission.getDescription();
   }

   public List getCommandExamples() {
      return this.examples;
   }

   public String getCommandUsage() {
      return this.commandUsage;
   }

   public void addCommandExample(String example) {
      this.examples.add(example);
   }

   public void setCommandUsage(String usage) {
      this.commandUsage = usage;
   }

   public void setArgRange(int min, int max) {
      this.minimumArgLength = min;
      this.maximumArgLength = max;
   }

   public void setName(String name) {
      this.commandName = name;
   }

   public void addKey(String key) {
      this.commandKeys.add(new CommandKey(key, this));
      Collections.sort(this.commandKeys, new ReverseLengthSorter());
   }

   public void addKey(String key, int minArgs, int maxArgs) {
      this.commandKeys.add(new CommandKey(key, this, minArgs, maxArgs));
      Collections.sort(this.commandKeys, new ReverseLengthSorter());
   }

   protected Plugin getPlugin() {
      return this.plugin;
   }

   public Integer getMaxArgs() {
      return this.maximumArgLength;
   }

   public Integer getMinArgs() {
      return this.minimumArgLength;
   }

   public List getAllPermissionStrings() {
      List<String> permStrings = new ArrayList();
      permStrings.add(this.permission.getName());

      for(Permission p : this.auxPerms) {
         permStrings.add(p.getName());
      }

      return permStrings;
   }

   public void showHelp(CommandSender sender) {
      sender.sendMessage(ChatColor.AQUA + "--- " + this.getCommandName() + " ---");
      sender.sendMessage(ChatColor.YELLOW + this.getCommandDesc());
      sender.sendMessage(ChatColor.DARK_AQUA + this.getCommandUsage());
      sender.sendMessage("Permission: " + ChatColor.GREEN + this.getPermissionString());
      String keys = "";

      for(String key : this.getKeyStrings()) {
         keys = keys + key + ", ";
      }

      keys = keys.substring(0, keys.length() - 2);
      sender.sendMessage(ChatColor.BLUE + "Aliases: " + ChatColor.RED + keys);
      if (this.getCommandExamples().size() > 0) {
         sender.sendMessage(ChatColor.LIGHT_PURPLE + "Examples:");
         if (sender instanceof Player) {
            for(int i = 0; i < 4 && i < this.getCommandExamples().size(); ++i) {
               sender.sendMessage((String)this.getCommandExamples().get(i));
            }
         } else {
            for(String c : this.getCommandExamples()) {
               sender.sendMessage(c);
            }
         }
      }

   }
}
