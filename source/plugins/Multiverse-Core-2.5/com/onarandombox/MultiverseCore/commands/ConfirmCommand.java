package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public class ConfirmCommand extends MultiverseCommand {
   public ConfirmCommand(MultiverseCore plugin) {
      super(plugin);
      this.setName("Confirms a command that could destroy life, the universe and everything.");
      this.setCommandUsage("/mv confirm");
      this.setArgRange(0, 0);
      this.addKey("mvconfirm");
      this.addKey("mv confirm");
      this.addCommandExample("/mv confirm");
      this.setPermission("multiverse.core.confirm", "If you have not been prompted to use this, it will not do anything.", PermissionDefault.OP);
   }

   public void runCommand(CommandSender sender, List args) {
      this.plugin.getCommandHandler().confirmQueuedCommand(sender);
   }
}
