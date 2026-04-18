package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.event.MVVersionEvent;
import com.onarandombox.MultiverseCore.utils.CoreLogging;
import com.onarandombox.MultiverseCore.utils.webpaste.BitlyURLShortener;
import com.onarandombox.MultiverseCore.utils.webpaste.PasteFailedException;
import com.onarandombox.MultiverseCore.utils.webpaste.PasteService;
import com.onarandombox.MultiverseCore.utils.webpaste.PasteServiceFactory;
import com.onarandombox.MultiverseCore.utils.webpaste.PasteServiceType;
import com.onarandombox.MultiverseCore.utils.webpaste.URLShortener;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class VersionCommand extends MultiverseCommand {
   private static final URLShortener SHORTENER = new BitlyURLShortener();

   public VersionCommand(MultiverseCore plugin) {
      super(plugin);
      this.setName("Multiverse Version");
      this.setCommandUsage("/mv version " + ChatColor.GOLD + "-[pb]");
      this.setArgRange(0, 1);
      this.addKey("mv version");
      this.addKey("mvv");
      this.addKey("mvversion");
      this.setPermission("multiverse.core.version", "Dumps version info to the console, optionally to pastie.org with -p or pastebin.com with a -b.", PermissionDefault.TRUE);
   }

   public void runCommand(final CommandSender sender, final List args) {
      if (sender instanceof Player) {
         sender.sendMessage("Version info dumped to console. Please check your server logs.");
      }

      StringBuilder buffer = new StringBuilder();
      buffer.append("[Multiverse-Core] Multiverse-Core Version: ").append(this.plugin.getDescription().getVersion()).append('\n');
      buffer.append("[Multiverse-Core] Bukkit Version: ").append(this.plugin.getServer().getVersion()).append('\n');
      buffer.append("[Multiverse-Core] Loaded Worlds: ").append(this.plugin.getMVWorldManager().getMVWorlds()).append('\n');
      buffer.append("[Multiverse-Core] Multiverse Plugins Loaded: ").append(this.plugin.getPluginCount()).append('\n');
      boolean usingVault = this.plugin.getVaultHandler().getEconomy() != null;
      buffer.append("[Multiverse-Core] Using Vault: ").append(usingVault).append('\n');
      if (usingVault) {
         buffer.append("[Multiverse-Core] Economy being used: ").append(this.plugin.getVaultHandler().getEconomy().getName()).append('\n');
      } else {
         buffer.append("[Multiverse-Core] Economy being used: ").append(this.plugin.getBank().getEconUsed()).append('\n');
      }

      buffer.append("[Multiverse-Core] Permissions Plugin: ").append(this.plugin.getMVPerms().getType()).append('\n');
      buffer.append("[Multiverse-Core] Dumping Config Values: (version ").append(this.plugin.getMVConfig().getVersion()).append(")").append('\n');
      buffer.append("[Multiverse-Core]  messagecooldown: ").append(this.plugin.getMessaging().getCooldown()).append('\n');
      buffer.append("[Multiverse-Core]  teleportcooldown: ").append(this.plugin.getMVConfig().getTeleportCooldown()).append('\n');
      buffer.append("[Multiverse-Core]  worldnameprefix: ").append(this.plugin.getMVConfig().getPrefixChat()).append('\n');
      buffer.append("[Multiverse-Core]  enforceaccess: ").append(this.plugin.getMVConfig().getEnforceAccess()).append('\n');
      buffer.append("[Multiverse-Core]  displaypermerrors: ").append(this.plugin.getMVConfig().getDisplayPermErrors()).append('\n');
      buffer.append("[Multiverse-Core]  teleportintercept: ").append(this.plugin.getMVConfig().getTeleportIntercept()).append('\n');
      buffer.append("[Multiverse-Core]  firstspawnoverride: ").append(this.plugin.getMVConfig().getFirstSpawnOverride()).append('\n');
      buffer.append("[Multiverse-Core]  firstspawnworld: ").append(this.plugin.getMVConfig().getFirstSpawnWorld()).append('\n');
      buffer.append("[Multiverse-Core]  debug: ").append(this.plugin.getMVConfig().getGlobalDebug()).append('\n');
      buffer.append("[Multiverse-Core] Special Code: FRN002").append('\n');
      MVVersionEvent versionEvent = new MVVersionEvent(buffer.toString());
      this.plugin.getServer().getPluginManager().callEvent(versionEvent);
      final String data = versionEvent.getVersionInfo();
      String[] lines = data.split("\n");

      for(String line : lines) {
         CoreLogging.info(line);
      }

      this.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(this.plugin, new Runnable() {
         public void run() {
            if (args.size() == 1) {
               String pasteUrl;
               if (((String)args.get(0)).equalsIgnoreCase("-p")) {
                  pasteUrl = VersionCommand.postToService(PasteServiceType.PASTIE, true, data);
               } else {
                  if (!((String)args.get(0)).equalsIgnoreCase("-b")) {
                     return;
                  }

                  pasteUrl = VersionCommand.postToService(PasteServiceType.PASTEBIN, true, data);
               }

               sender.sendMessage("Version info dumped here: " + ChatColor.GREEN + pasteUrl);
               CoreLogging.info("Version info dumped here: %s", pasteUrl);
            }

         }
      });
   }

   private static String postToService(PasteServiceType type, boolean isPrivate, String pasteData) {
      PasteService ps = PasteServiceFactory.getService(type, isPrivate);

      try {
         return SHORTENER.shorten(ps.postData(ps.encodeData(pasteData), ps.getPostURL()));
      } catch (PasteFailedException e) {
         System.out.print(e);
         return "Error posting to service";
      }
   }
}
