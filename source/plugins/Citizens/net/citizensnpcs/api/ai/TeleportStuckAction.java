package net.citizensnpcs.api.ai;

import java.util.EnumSet;
import java.util.Set;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class TeleportStuckAction implements StuckAction {
   public static TeleportStuckAction INSTANCE = new TeleportStuckAction();
   private static final int MAX_ITERATIONS = 10;
   private static final double RANGE = (double)10.0F;
   private static final Set TELEPORTABLE_BLOCKS;

   private TeleportStuckAction() {
      super();
   }

   private boolean canStand(Block block) {
      return TELEPORTABLE_BLOCKS.contains(block.getType()) && TELEPORTABLE_BLOCKS.contains(block.getRelative(BlockFace.UP).getType());
   }

   public boolean run(NPC npc, Navigator navigator) {
      if (!npc.isSpawned()) {
         return false;
      } else {
         Location base = navigator.getTargetAsLocation();
         if (npc.getBukkitEntity().getLocation().distanceSquared(base) <= (double)10.0F) {
            return true;
         } else {
            Block block = base.getBlock();

            for(int iterations = 0; !this.canStand(block); block = block.getRelative(BlockFace.UP)) {
               if (iterations++ >= 10) {
                  block = base.getBlock();
                  break;
               }
            }

            npc.teleport(block.getLocation(), TeleportCause.PLUGIN);
            return false;
         }
      }
   }

   static {
      TELEPORTABLE_BLOCKS = EnumSet.of(Material.AIR, Material.BIRCH_WOOD_STAIRS, Material.BRICK_STAIRS, Material.CAKE_BLOCK, Material.COBBLESTONE_STAIRS, Material.DEAD_BUSH, Material.JUNGLE_WOOD_STAIRS, Material.LADDER, Material.LONG_GRASS, Material.RAILS, Material.POWERED_RAIL, Material.REDSTONE_WIRE, Material.REDSTONE_TORCH_ON, Material.REDSTONE_TORCH_OFF, Material.TRAP_DOOR, Material.TRIPWIRE, Material.TRIPWIRE_HOOK, Material.VINE, Material.WALL_SIGN, Material.WATER, Material.WOOD_STAIRS, Material.WOOD_STEP, Material.WOODEN_DOOR, Material.YELLOW_FLOWER, Material.RED_ROSE, Material.SIGN, Material.SIGN_POST, Material.WATER);
   }
}
