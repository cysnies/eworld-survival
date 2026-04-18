package com.onarandombox.MultiverseCore.api;

import java.util.Collection;
import org.bukkit.command.CommandSender;

public interface MultiverseMessaging {
   void setCooldown(int var1);

   boolean sendMessage(CommandSender var1, String var2, boolean var3);

   boolean sendMessages(CommandSender var1, String[] var2, boolean var3);

   boolean sendMessages(CommandSender var1, Collection var2, boolean var3);

   int getCooldown();
}
