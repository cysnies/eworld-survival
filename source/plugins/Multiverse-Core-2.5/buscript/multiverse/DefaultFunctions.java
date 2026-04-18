package buscript.multiverse;

import buscript.multiverse.util.TimeTools;
import java.io.File;
import org.bukkit.entity.Player;
import org.mozilla.javascript.ScriptableObject;

class DefaultFunctions extends ScriptableObject {
   private Buscript buscript;

   DefaultFunctions(Buscript buscript) {
      super();
      this.buscript = buscript;
   }

   public String getClassName() {
      return "Buscript";
   }

   public void broadcast(String message) {
      this.buscript.getPlugin().getServer().broadcastMessage(this.buscript.stringReplace(message));
   }

   public void broadcastPerm(String message, String permission) {
      this.buscript.getPlugin().getServer().broadcast(this.buscript.stringReplace(message), permission);
   }

   public void command(String command) {
      this.buscript.getPlugin().getServer().dispatchCommand(this.buscript.getPlugin().getServer().getConsoleSender(), this.buscript.stringReplace(command));
   }

   public void commandSpoof(String name, String command) {
      Player player = this.buscript.getPlugin().getServer().getPlayerExact(this.buscript.stringReplace(name));
      if (player != null) {
         this.buscript.getPlugin().getServer().dispatchCommand(player, this.buscript.stringReplace(command));
      }

   }

   public void message(String name, String message) {
      Player player = this.buscript.getPlugin().getServer().getPlayerExact(this.buscript.stringReplace(name));
      if (player != null) {
         player.sendMessage(this.buscript.stringReplace(message));
      }

   }

   public boolean hasPerm(String name, String permission) {
      Player player = this.buscript.getPlugin().getServer().getPlayerExact(this.buscript.stringReplace(name));
      return player != null && player.hasPermission(permission);
   }

   public boolean hasPermOffline(String world, String player, String permission) {
      if (this.buscript.getPermissions() != null) {
         return this.buscript.getPermissions().has(world, this.buscript.stringReplace(player), permission);
      } else {
         throw new IllegalStateException("Vault must be installed to use hasPermOffline(world, player, perm)!");
      }
   }

   public void addPerm(String world, String player, String permission) {
      if (this.buscript.getPermissions() != null) {
         this.buscript.getPermissions().playerAdd(world, this.buscript.stringReplace(player), permission);
      } else {
         throw new IllegalStateException("Vault must be installed to use addPerm(world, player, perm)!");
      }
   }

   public void removePerm(String world, String player, String permission) {
      if (this.buscript.getPermissions() != null) {
         this.buscript.getPermissions().playerRemove(world, this.buscript.stringReplace(player), permission);
      } else {
         throw new IllegalStateException("Vault must be installed to use removePerm(world, player, perm)!");
      }
   }

   public boolean hasMoney(String player, Double money) {
      if (this.buscript.getEconomy() != null) {
         return this.buscript.getEconomy().has(this.buscript.stringReplace(player), money);
      } else {
         throw new IllegalStateException("Vault must be installed to use hasMoney(player, money)!");
      }
   }

   public boolean addMoney(String player, Double money) {
      if (this.buscript.getEconomy() != null) {
         return this.buscript.getEconomy().depositPlayer(this.buscript.stringReplace(player), money).transactionSuccess();
      } else {
         throw new IllegalStateException("Vault must be installed to use addMoney(player, money)!");
      }
   }

   public boolean removeMoney(String player, Double money) {
      if (this.buscript.getEconomy() != null) {
         return this.buscript.getEconomy().withdrawPlayer(this.buscript.stringReplace(player), money).transactionSuccess();
      } else {
         throw new IllegalStateException("Vault must be installed to use removeMoney(player, money)!");
      }
   }

   public boolean isOnline(String name) {
      return this.buscript.getPlugin().getServer().getPlayerExact(this.buscript.stringReplace(name)) != null;
   }

   public void run(String script) {
      this.buscript.executeScript(new File(this.buscript.getScriptFolder(), this.buscript.stringReplace(script)));
   }

   public void runTarget(String script, String target) {
      this.buscript.executeScript(new File(this.buscript.getScriptFolder(), this.buscript.stringReplace(script)), this.buscript.stringReplace(target));
   }

   public void runLater(String script, String delay) {
      long d = TimeTools.fromShortForm(delay);
      this.buscript.scheduleScript(new File(this.buscript.getScriptFolder(), this.buscript.stringReplace(script)), d * 1000L);
   }

   public void runLaterTarget(String script, String delay, String target) {
      long d = TimeTools.fromShortForm(delay);
      this.buscript.scheduleScript(new File(this.buscript.getScriptFolder(), this.buscript.stringReplace(script)), this.buscript.stringReplace(target), d * 1000L);
   }

   public void clearScripts(String target) {
      this.buscript.clearScheduledScripts(target);
   }

   public String stringReplace(String string) {
      return this.buscript.stringReplace(string);
   }
}
