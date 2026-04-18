package net.citizensnpcs.npc;

import com.google.common.collect.Lists;
import java.util.List;
import net.citizensnpcs.Settings;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.NPCSelectEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;

public class NPCSelector implements Listener, net.citizensnpcs.api.npc.NPCSelector {
   private int consoleSelectedNPC = -1;
   private final Plugin plugin;

   public NPCSelector(Plugin plugin) {
      super();
      this.plugin = plugin;
      Bukkit.getPluginManager().registerEvents(this, plugin);
   }

   public NPC getSelected(CommandSender sender) {
      if (sender instanceof Player) {
         return this.getSelectedFromMetadatable((Player)sender);
      } else if (sender instanceof BlockCommandSender) {
         return this.getSelectedFromMetadatable(((BlockCommandSender)sender).getBlock());
      } else if (sender instanceof ConsoleCommandSender) {
         return this.consoleSelectedNPC == -1 ? null : CitizensAPI.getNPCRegistry().getById(this.consoleSelectedNPC);
      } else {
         return null;
      }
   }

   private NPC getSelectedFromMetadatable(Metadatable sender) {
      List<MetadataValue> metadata = sender.getMetadata("selected");
      return metadata.size() == 0 ? null : CitizensAPI.getNPCRegistry().getById(((MetadataValue)metadata.get(0)).asInt());
   }

   @EventHandler
   public void onNPCRemove(NPCRemoveEvent event) {
      NPC npc = event.getNPC();
      List<String> selectors = (List)npc.data().get("selectors");
      if (selectors != null) {
         for(String value : selectors) {
            if (value.equals("console")) {
               this.consoleSelectedNPC = -1;
            } else if (value.startsWith("@")) {
               String[] parts = value.substring(1, value.length()).split(":");
               World world = Bukkit.getWorld(parts[0]);
               if (world != null) {
                  Block block = world.getBlockAt(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                  this.removeMetadata(block);
               }
            } else {
               Player search = Bukkit.getPlayerExact(value);
               this.removeMetadata(search);
            }
         }

         npc.data().remove("selectors");
      }
   }

   @EventHandler
   public void onNPCRightClick(NPCRightClickEvent event) {
      Player player = event.getClicker();
      NPC npc = event.getNPC();
      List<MetadataValue> selected = player.getMetadata("selected");
      if ((selected == null || selected.size() == 0 || ((MetadataValue)selected.get(0)).asInt() != npc.getId()) && Util.matchesItemInHand(player, Settings.Setting.SELECTION_ITEM.asString()) && ((Owner)npc.getTrait(Owner.class)).isOwnedBy((CommandSender)player)) {
         player.removeMetadata("selected", this.plugin);
         this.select(player, npc);
         Messaging.sendWithNPC(player, Settings.Setting.SELECTION_MESSAGE.asString(), npc);
         if (!Settings.Setting.QUICK_SELECT.asBoolean()) {
            return;
         }
      }

   }

   private void removeMetadata(Metadatable metadatable) {
      if (metadatable != null) {
         metadatable.removeMetadata("selected", this.plugin);
      }

   }

   public void select(CommandSender sender, NPC npc) {
      List<String> selectors = (List)npc.data().get("selectors");
      if (selectors == null) {
         selectors = Lists.newArrayList();
         npc.data().set("selectors", selectors);
      }

      if (sender instanceof Player) {
         Player player = (Player)sender;
         this.setMetadata(npc, player);
         selectors.add(sender.getName());
         Editor.leave(player);
      } else if (sender instanceof BlockCommandSender) {
         Block block = ((BlockCommandSender)sender).getBlock();
         this.setMetadata(npc, block);
         selectors.add(this.toName(block));
      } else if (sender instanceof ConsoleCommandSender) {
         this.consoleSelectedNPC = npc.getId();
         selectors.add("console");
      }

      Bukkit.getPluginManager().callEvent(new NPCSelectEvent(npc, sender));
   }

   private void setMetadata(NPC npc, Metadatable metadatable) {
      if (metadatable.hasMetadata("selected")) {
         metadatable.removeMetadata("selected", this.plugin);
      }

      metadatable.setMetadata("selected", new FixedMetadataValue(this.plugin, npc.getId()));
   }

   private String toName(Block block) {
      return '@' + block.getWorld().getName() + ":" + Integer.toString(block.getX()) + ":" + Integer.toString(block.getY()) + ":" + Integer.toString(block.getZ());
   }
}
