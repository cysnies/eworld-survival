package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;

public abstract class PaginatedCoreCommand extends PaginatedCommand {
   protected MultiverseCore plugin;

   public PaginatedCoreCommand(MultiverseCore plugin) {
      super(plugin);
      this.plugin = plugin;
   }
}
