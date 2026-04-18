package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import java.util.List;
import net.minecraft.server.v1_6_R2.BiomeBase;
import net.minecraft.server.v1_6_R2.WorldChunkManager;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.entity.Player;

public class BiomeCommand extends BaseCommand {
   public BiomeCommand(TCPlugin _plugin) {
      super(_plugin);
      this.name = "biome";
      this.perm = TCPerm.CMD_BIOME.node;
      this.usage = "biome [-f]";
      this.workOnConsole = false;
   }

   public boolean onCommand(CommandSender sender, List args) {
      Player player = (Player)sender;
      Chunk chunk = player.getWorld().getChunkAt(player.getLocation());
      player.sendMessage(ChatColor.AQUA.toString() + "You are in: ");
      WorldChunkManager biomeManager = ((CraftWorld)player.getLocation().getWorld()).getHandle().getWorldChunkManager();
      player.sendMessage(VALUE_COLOR + biomeManager.getBiome(chunk.getX() * 16 + 16, chunk.getZ() * 16 + 16).y + MESSAGE_COLOR + " chunk biome!");
      if (args.size() == 1 && ((String)args.get(0)).equals("-f")) {
         BiomeBase[] biome = new BiomeBase[1];
         float[] temp = new float[1];
         float[] humidity = new float[1];
         biomeManager.getBiomeBlock(biome, (int)player.getLocation().getX(), (int)player.getLocation().getZ(), 1, 1);
         biomeManager.getTemperatures(temp, (int)player.getLocation().getX(), (int)player.getLocation().getZ(), 1, 1);
         biomeManager.getWetness(humidity, (int)player.getLocation().getX(), (int)player.getLocation().getZ(), 1, 1);
         player.sendMessage(VALUE_COLOR + biome[0].y + MESSAGE_COLOR + " block biome!");
         player.sendMessage(VALUE_COLOR + humidity[0] + MESSAGE_COLOR + " block humidity!");
         player.sendMessage(VALUE_COLOR + temp[0] + MESSAGE_COLOR + " block temperature!");
      }

      return true;
   }
}
