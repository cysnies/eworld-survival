package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.event.MVConfigReloadEvent;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public class ReloadCommand extends MultiverseCommand {
   public ReloadCommand(MultiverseCore plugin) {
      super(plugin);
      this.setName("Reload Configs");
      this.setCommandUsage("/mv reload");
      this.setArgRange(0, 0);
      this.addKey("mvreload");
      this.addKey("mv reload");
      this.addCommandExample("/mv reload");
      this.setPermission("multiverse.core.reload", "Reloads worlds.yml and config.yml.", PermissionDefault.OP);
   }

   public void runCommand(CommandSender sender, List args) {
      sender.sendMessage(ChatColor.GOLD + "Reloading all Multiverse Plugin configs...");
      this.plugin.loadConfigs();
      this.plugin.getAnchorManager().loadAnchors();
      this.plugin.getMVWorldManager().loadWorlds(true);
      List<String> configsLoaded = new ArrayList();
      configsLoaded.add("Multiverse-Core - config.yml");
      configsLoaded.add("Multiverse-Core - worlds.yml");
      configsLoaded.add("Multiverse-Core - anchors.yml");
      MVConfigReloadEvent configReload = new MVConfigReloadEvent(configsLoaded);
      this.plugin.getServer().getPluginManager().callEvent(configReload);

      for(String s : configReload.getAllConfigsLoaded()) {
         sender.sendMessage(s);
      }

      sender.sendMessage(ChatColor.GREEN + "Reload Complete!");
   }
}
