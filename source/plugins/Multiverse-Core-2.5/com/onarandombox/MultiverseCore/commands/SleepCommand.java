package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class SleepCommand extends MultiverseCommand {
   public SleepCommand(MultiverseCore plugin) {
      super(plugin);
      this.setName("Go to Sleep");
      this.setCommandUsage("/mv sleep");
      this.setArgRange(0, 0);
      this.addKey("mv sleep");
      this.setPermission("multiverse.core.sleep", "Takes you the latest bed you've slept in (Currently BROKEN).", PermissionDefault.OP);
   }

   public void runCommand(CommandSender sender, List args) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p != null) {
         ;
      }
   }
}
