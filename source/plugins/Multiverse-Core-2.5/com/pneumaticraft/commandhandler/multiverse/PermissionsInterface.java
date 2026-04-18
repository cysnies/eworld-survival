package com.pneumaticraft.commandhandler.multiverse;

import java.util.List;
import org.bukkit.command.CommandSender;

public interface PermissionsInterface {
   boolean hasPermission(CommandSender var1, String var2, boolean var3);

   boolean hasAnyPermission(CommandSender var1, List var2, boolean var3);

   boolean hasAllPermission(CommandSender var1, List var2, boolean var3);
}
