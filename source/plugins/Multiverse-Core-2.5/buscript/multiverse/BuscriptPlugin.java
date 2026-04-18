package buscript.multiverse;

import java.io.File;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BuscriptPlugin extends JavaPlugin {
   private Buscript buscript;

   public BuscriptPlugin() {
      super();
   }

   public void onEnable() {
      this.buscript = new Buscript(this);
   }

   public Buscript getAPI() {
      return this.buscript;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length < 1) {
         return false;
      } else {
         File scriptFile = new File(this.getAPI().getScriptFolder(), args[0]);
         if (!scriptFile.exists()) {
            sender.sendMessage("Script '" + scriptFile + "' does not exist!");
            return true;
         } else {
            Player player = null;
            if (sender instanceof Player) {
               player = (Player)sender;
            }

            if (args.length == 1) {
               this.getAPI().executeScript(scriptFile, player);
               return true;
            } else if (args.length == 2) {
               this.getAPI().executeScript(scriptFile, args[1], player);
               return true;
            } else {
               return false;
            }
         }
      }
   }
}
