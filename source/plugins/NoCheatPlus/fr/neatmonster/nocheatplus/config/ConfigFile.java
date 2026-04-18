package fr.neatmonster.nocheatplus.config;

import org.bukkit.configuration.MemorySection;

public class ConfigFile extends ConfigFileWithActions {
   public ConfigFile() {
      super();
   }

   public void setActionFactory() {
      this.factory = ConfigManager.getActionFactory(((MemorySection)this.get("strings")).getValues(false));
   }
}
