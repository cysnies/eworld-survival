package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class Commandsuicide extends EssentialsCommand {
   public Commandsuicide() {
      super("suicide");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      EntityDamageEvent ede = new EntityDamageEvent(user.getBase(), DamageCause.SUICIDE, 32767);
      server.getPluginManager().callEvent(ede);
      user.damage((double)32767.0F);
      if (user.getHealth() > (double)0.0F) {
         user.setHealth((double)0.0F);
      }

      user.sendMessage(I18n._("suicideMessage"));
      user.setDisplayNick();
      this.ess.broadcastMessage(user, I18n._("suicideSuccess", user.getDisplayName()));
   }
}
