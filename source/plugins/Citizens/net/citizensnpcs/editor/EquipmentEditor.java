package net.citizensnpcs.editor;

import com.google.common.collect.Maps;
import java.util.Map;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class EquipmentEditor extends Editor {
   private final NPC npc;
   private final Player player;
   private static final Map EQUIPPERS = Maps.newEnumMap(EntityType.class);

   public EquipmentEditor(Player player, NPC npc) {
      super();
      this.player = player;
      this.npc = npc;
   }

   public void begin() {
      Messaging.sendTr(this.player, "citizens.editors.equipment.begin");
   }

   public void end() {
      Messaging.sendTr(this.player, "citizens.editors.equipment.end");
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_AIR && Editor.hasEditor(event.getPlayer())) {
         event.setUseItemInHand(Result.DENY);
      }

   }

   @EventHandler
   public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
      if (this.npc.isSpawned() && event.getPlayer().equals(this.player) && this.npc.equals(CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked()))) {
         Equipper equipper = (Equipper)EQUIPPERS.get(this.npc.getBukkitEntity().getType());
         if (equipper == null) {
            equipper = new GenericEquipper();
         }

         equipper.equip(event.getPlayer(), this.npc);
         event.setCancelled(true);
      }
   }

   static {
      EQUIPPERS.put(EntityType.PIG, new PigEquipper());
      EQUIPPERS.put(EntityType.SHEEP, new SheepEquipper());
      EQUIPPERS.put(EntityType.ENDERMAN, new EndermanEquipper());
      EQUIPPERS.put(EntityType.HORSE, new HorseEquipper());
   }
}
