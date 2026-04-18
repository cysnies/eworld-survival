package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import com.khorn.terraincontrol.customobjects.CustomObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.command.CommandSender;

public class ListCommand extends BaseCommand {
   public ListCommand(TCPlugin _plugin) {
      super(_plugin);
      this.name = "list";
      this.perm = TCPerm.CMD_LIST.node;
      this.usage = "list [-w World] [page]";
      this.workOnConsole = false;
   }

   public boolean onCommand(CommandSender sender, List args) {
      int page = 1;
      if (args.size() > 1 && ((String)args.get(0)).equals("-w")) {
         String worldName = (String)args.get(1);
         if (args.size() > 2) {
            try {
               page = Integer.parseInt((String)args.get(2));
            } catch (Exception var9) {
               sender.sendMessage(ERROR_COLOR + "Wrong page number " + (String)args.get(2));
            }
         }

         BukkitWorld world = this.getWorld(sender, worldName);
         if (world != null) {
            if (world.getSettings().customObjects.size() == 0) {
               sender.sendMessage(MESSAGE_COLOR + "This world does not have custom objects");
            }

            List<String> pluginList = new ArrayList();

            for(CustomObject object : world.getSettings().customObjects) {
               pluginList.add(VALUE_COLOR + object.getName());
            }

            this.ListMessage(sender, pluginList, page, new String[]{"World objects"});
         } else {
            sender.sendMessage(ERROR_COLOR + "World not found " + worldName);
         }

         return true;
      } else {
         if (args.size() > 0) {
            try {
               page = Integer.parseInt((String)args.get(0));
            } catch (Exception var10) {
               sender.sendMessage(ERROR_COLOR + "Wrong page number " + (String)args.get(0));
            }
         }

         Collection<CustomObject> globalObjects = TerrainControl.getCustomObjectManager().globalObjects.values();
         if (globalObjects.size() == 0) {
            sender.sendMessage(MESSAGE_COLOR + "This global directory does not have custom objects");
         }

         List<String> pluginList = new ArrayList();

         for(CustomObject object : globalObjects) {
            if (object.canSpawnAsObject()) {
               pluginList.add(VALUE_COLOR + object.getName());
            }
         }

         this.ListMessage(sender, pluginList, page, new String[]{"Global objects", "Use /tc list -w [world] for world objects"});
         return true;
      }
   }
}
