package com.pneumaticraft.commandhandler.multiverse;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class QueuedCommand {
   private String name;
   private List args;
   private Class[] paramTypes;
   private CommandSender sender;
   private Plugin plugin;
   private Calendar timeRequested;
   private String success;
   private String fail;
   private int expiration;
   private boolean alreadyRun;

   public QueuedCommand(String commandName, List args, Class[] partypes, CommandSender sender, Calendar instance, Plugin plugin, String success, String fail, int expiration) {
      super();
      this.plugin = plugin;
      this.name = commandName;
      this.args = args;
      this.sender = sender;
      this.timeRequested = instance;
      this.paramTypes = partypes;
      this.setSuccess(success);
      this.setFail(fail);
      this.expiration = expiration;
   }

   public CommandSender getSender() {
      return this.sender;
   }

   public boolean execute() {
      this.timeRequested.add(13, this.expiration);
      if (this.timeRequested.after(Calendar.getInstance())) {
         if (this.alreadyRun) {
            this.sender.sendMessage("This command has already been run! Please type the original command again if you want to rerun it.");
            return false;
         }

         try {
            this.alreadyRun = true;
            Method method = this.plugin.getClass().getMethod(this.name, this.paramTypes);
            Object[] listAsArray = this.args.toArray(new Object[this.args.size()]);
            Object returnVal = method.invoke(this.plugin, listAsArray);
            if (returnVal instanceof Boolean) {
               return (Boolean)returnVal;
            }

            return true;
         } catch (Exception e) {
            System.out.print(e.getMessage());
         }
      } else {
         this.sender.sendMessage("This command has expired. Please type the original command again.");
      }

      return false;
   }

   private void setSuccess(String success) {
      this.success = success;
   }

   public String getSuccess() {
      return this.success;
   }

   private void setFail(String fail) {
      this.fail = fail;
   }

   public String getFail() {
      return this.fail;
   }
}
