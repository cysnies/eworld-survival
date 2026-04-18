package com.onarandombox.MultiverseCore.api;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

public interface WorldPurger {
   void purgeWorlds(List var1);

   void purgeWorld(MultiverseWorld var1);

   void purgeWorld(MultiverseWorld var1, List var2, boolean var3, boolean var4);

   void purgeWorld(MultiverseWorld var1, List var2, boolean var3, boolean var4, CommandSender var5);

   boolean shouldWeKillThisCreature(Entity var1, List var2, boolean var3, boolean var4);

   boolean shouldWeKillThisCreature(MultiverseWorld var1, Entity var2);
}
