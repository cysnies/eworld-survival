package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseMessaging;
import com.pneumaticraft.commandhandler.multiverse.Command;
import java.util.List;
import org.bukkit.command.CommandSender;

public abstract class MultiverseCommand extends Command {
   protected MultiverseCore plugin;
   protected MultiverseMessaging messaging;

   public MultiverseCommand(MultiverseCore plugin) {
      super(plugin);
      this.plugin = plugin;
      this.messaging = this.plugin.getMessaging();
   }

   public abstract void runCommand(CommandSender var1, List var2);
}
