package net.citizensnpcs.editor;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.trait.CurrentLocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class CopierEditor extends Editor {
   private final String name;
   private final NPC npc;
   private final Player player;

   public CopierEditor(Player player, NPC npc) {
      super();
      this.player = player;
      this.npc = npc;
      this.name = npc.getFullName();
   }

   public void begin() {
      Messaging.sendTr(this.player, "citizens.editors.copier.begin");
   }

   public void end() {
      Messaging.sendTr(this.player, "citizens.editors.copier.end");
   }

   @EventHandler
   public void onBlockClick(PlayerInteractEvent event) {
      if (event.getClickedBlock() != null) {
         NPC copy = this.npc.clone();
         if (!copy.getFullName().equals(this.name)) {
            copy.setName(this.name);
         }

         if (copy.isSpawned() && this.player.isOnline()) {
            Location location = this.player.getLocation();
            location.getChunk().load();
            copy.teleport(location, TeleportCause.PLUGIN);
            ((CurrentLocation)copy.getTrait(CurrentLocation.class)).setLocation(location);
         }

         Messaging.sendTr(this.player, "citizens.commands.npc.copy.copied", this.npc.getName());
      }
   }
}
