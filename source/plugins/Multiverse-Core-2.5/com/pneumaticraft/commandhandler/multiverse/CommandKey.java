package com.pneumaticraft.commandhandler.multiverse;

public class CommandKey {
   private Integer minArgs;
   private Integer maxArgs;
   private String key;
   private Command cmd;

   public CommandKey(String key, Command cmd) {
      super();
      this.minArgs = null;
      this.maxArgs = null;
      this.key = "";
      this.key = key;
      this.cmd = cmd;
   }

   public CommandKey(String key, Command cmd, int minArgs, int maxArgs) {
      this(key, cmd);
      this.minArgs = minArgs;
      this.maxArgs = maxArgs;
   }

   public String getKey() {
      return this.key;
   }

   public boolean hasValidNumberOfArgs(int args) {
      if (this.minArgs == null) {
         this.minArgs = this.cmd.getMinArgs();
      }

      if (this.maxArgs == null) {
         this.maxArgs = this.cmd.getMaxArgs();
      }

      if (this.minArgs <= args && this.maxArgs >= args) {
         return true;
      } else {
         return this.minArgs <= args && this.maxArgs == -1;
      }
   }

   public String toString() {
      return "[" + this.key + "(" + this.minArgs + ", " + this.maxArgs + ")" + "]";
   }
}
