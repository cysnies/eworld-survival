package com.onarandombox.MultiverseCore.api;

import java.util.logging.Level;
import org.bukkit.Server;

public interface LoggablePlugin {
   void log(Level var1, String var2);

   Server getServer();
}
