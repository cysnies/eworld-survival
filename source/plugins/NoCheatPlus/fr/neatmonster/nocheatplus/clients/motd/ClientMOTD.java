package fr.neatmonster.nocheatplus.clients.motd;

import org.bukkit.entity.Player;

public abstract class ClientMOTD {
   public ClientMOTD() {
      super();
   }

   public abstract String onPlayerJoin(String var1, Player var2, boolean var3);
}
