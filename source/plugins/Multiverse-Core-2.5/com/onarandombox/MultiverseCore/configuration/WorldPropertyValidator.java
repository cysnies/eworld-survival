package com.onarandombox.MultiverseCore.configuration;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiverseCore.event.MVWorldPropertyChangeEvent;
import me.main__.util.multiverse.SerializationConfig.ChangeDeniedException;
import me.main__.util.multiverse.SerializationConfig.ObjectUsingValidator;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class WorldPropertyValidator extends ObjectUsingValidator {
   public WorldPropertyValidator() {
      super();
   }

   public Object validateChange(String property, Object newValue, Object oldValue, MVWorld object) throws ChangeDeniedException {
      MVWorldPropertyChangeEvent<T> event = new MVWorldPropertyChangeEvent(object, (CommandSender)null, property, newValue);
      Bukkit.getPluginManager().callEvent(event);
      if (event.isCancelled()) {
         throw new ChangeDeniedException();
      } else {
         return event.getTheNewValue();
      }
   }
}
