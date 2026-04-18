package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.BlockSafety;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class SetSpawnCommand extends MultiverseCommand {
   public SetSpawnCommand(MultiverseCore plugin) {
      super(plugin);
      this.setName("Set World Spawn");
      this.setCommandUsage("/mv setspawn");
      this.setArgRange(0, 0);
      this.addKey("mvsetspawn");
      this.addKey("mvss");
      this.addKey("mv set spawn");
      this.addKey("mv setspawn");
      this.addKey("mvset spawn");
      this.addCommandExample("/mv set spawn");
      this.setPermission("multiverse.core.spawn.set", "Sets the spawn for the current world.", PermissionDefault.OP);
   }

   public void runCommand(CommandSender sender, List args) {
      this.setWorldSpawn(sender);
   }

   protected void setWorldSpawn(CommandSender sender) {
      if (sender instanceof Player) {
         Player p = (Player)sender;
         Location l = p.getLocation();
         World w = p.getWorld();
         MultiverseWorld foundWorld = this.plugin.getMVWorldManager().getMVWorld(w.getName());
         if (foundWorld != null) {
            foundWorld.setSpawnLocation(p.getLocation());
            BlockSafety bs = this.plugin.getBlockSafety();
            if (!bs.playerCanSpawnHereSafely(p.getLocation()) && foundWorld.getAdjustSpawn()) {
               sender.sendMessage("It looks like that location would normally be unsafe. But I trust you.");
               sender.sendMessage("I'm turning off the Safe-T-Teleporter for spawns to this world.");
               sender.sendMessage("If you want this turned back on just do:");
               sender.sendMessage(ChatColor.AQUA + "/mvm set adjustspawn true " + foundWorld.getAlias());
               foundWorld.setAdjustSpawn(false);
            }

            sender.sendMessage("Spawn was set to: " + this.plugin.getLocationManipulation().strCoords(p.getLocation()));
            if (!this.plugin.saveWorldConfig()) {
               sender.sendMessage(ChatColor.RED + "There was an issue saving worlds.yml!  Your changes will only be temporary!");
            }
         } else {
            w.setSpawnLocation(l.getBlockX(), l.getBlockY(), l.getBlockZ());
            sender.sendMessage("Multiverse does not know about this world, only X,Y and Z set. Please import it to set the spawn fully (Pitch/Yaws).");
         }
      } else {
         sender.sendMessage("You cannot use this command from the console.");
      }

   }
}
