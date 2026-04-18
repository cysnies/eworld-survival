package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import java.text.DecimalFormat;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class CoordCommand extends MultiverseCommand {
   private MVWorldManager worldManager;

   public CoordCommand(MultiverseCore plugin) {
      super(plugin);
      this.setName("Coordinates");
      this.setCommandUsage("/mv coord");
      this.setArgRange(0, 0);
      this.addKey("mv coord");
      this.addKey("mvcoord");
      this.addKey("mvco");
      this.addCommandExample("/mv coord");
      this.setPermission("multiverse.core.coord", "Returns detailed information on the Players where abouts.", PermissionDefault.OP);
      this.worldManager = this.plugin.getMVWorldManager();
   }

   public void runCommand(CommandSender sender, List args) {
      if (sender instanceof Player) {
         Player p = (Player)sender;
         World world = p.getWorld();
         if (!this.worldManager.isMVWorld(world.getName())) {
            this.plugin.showNotMVWorldMessage(sender, world.getName());
            return;
         }

         MultiverseWorld mvworld = this.worldManager.getMVWorld(world.getName());
         p.sendMessage(ChatColor.AQUA + "--- Location Information ---");
         p.sendMessage(ChatColor.AQUA + "World: " + ChatColor.WHITE + world.getName());
         p.sendMessage(ChatColor.AQUA + "Alias: " + mvworld.getColoredWorldString());
         p.sendMessage(ChatColor.AQUA + "World Scale: " + ChatColor.WHITE + mvworld.getScaling());
         DecimalFormat df = new DecimalFormat();
         df.setMinimumFractionDigits(0);
         df.setMaximumFractionDigits(2);
         p.sendMessage(ChatColor.AQUA + "Coordinates: " + ChatColor.WHITE + this.plugin.getLocationManipulation().strCoords(p.getLocation()));
         p.sendMessage(ChatColor.AQUA + "Direction: " + ChatColor.WHITE + this.plugin.getLocationManipulation().getDirection(p.getLocation()));
         p.sendMessage(ChatColor.AQUA + "Block: " + ChatColor.WHITE + Material.getMaterial(world.getBlockTypeIdAt(p.getLocation())));
      } else {
         sender.sendMessage("This command needs to be used from a Player.");
      }

   }
}
