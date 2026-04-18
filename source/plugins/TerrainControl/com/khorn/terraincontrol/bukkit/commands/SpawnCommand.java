package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.Rotation;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

public class SpawnCommand extends BaseCommand {
   public SpawnCommand(TCPlugin _plugin) {
      super(_plugin);
      this.name = "spawn";
      this.perm = TCPerm.CMD_SPAWN.node;
      this.usage = "spawn Name [World]";
      this.workOnConsole = false;
   }

   public boolean onCommand(CommandSender sender, List args) {
      Player me = (Player)sender;
      Random random = new Random();
      BukkitWorld bukkitWorld = this.getWorld(me, args.size() > 1 ? (String)args.get(1) : "");
      if (args.size() == 0) {
         me.sendMessage(ERROR_COLOR + "You must enter the name of the BO2.");
         return true;
      } else {
         CustomObject spawnObject = null;
         if (bukkitWorld != null) {
            spawnObject = TerrainControl.getCustomObjectManager().getObjectFromString((String)args.get(0), (LocalWorld)bukkitWorld);
         }

         if (spawnObject == null) {
            sender.sendMessage(ERROR_COLOR + "Object not found, use '/tc list' to list the available ones.");
            return true;
         } else {
            Block block = this.getWatchedBlock(me, true);
            if (block == null) {
               return true;
            } else {
               if (spawnObject.spawnForced(bukkitWorld, random, Rotation.NORTH, block.getX(), block.getY(), block.getZ())) {
                  me.sendMessage(BaseCommand.MESSAGE_COLOR + spawnObject.getName() + " was spawned.");
               } else {
                  me.sendMessage(BaseCommand.ERROR_COLOR + "Object can't be spawned over there.");
               }

               return true;
            }
         }
      }
   }

   public Block getWatchedBlock(Player me, boolean verbose) {
      if (me == null) {
         return null;
      } else {
         Block previousBlock = null;

         Block block;
         for(Iterator<Block> itr = new BlockIterator(me, 200); itr.hasNext(); previousBlock = block) {
            block = (Block)itr.next();
            if (block.getTypeId() != DefaultMaterial.AIR.id && block.getTypeId() != DefaultMaterial.LONG_GRASS.id) {
               return previousBlock;
            }
         }

         if (verbose) {
            me.sendMessage(ERROR_COLOR + "No block in sight.");
         }

         return null;
      }
   }
}
