package skill;

import cus.Cus;
import cus.CustomEntityUtil;
import cus.CustomMonster;
import lib.util.UtilFormat;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Rape extends Skill {
   private static final int ID = 7;
   private int range;

   public Rape(String s) {
      super(s);
      this.range = Integer.parseInt(s);
   }

   public int getId() {
      return 7;
   }

   public void triggerDamage(LivingEntity entity) {
      CustomMonster cm;
      try {
         cm = (CustomMonster)((CraftLivingEntity)entity).getHandle();
      } catch (Exception var7) {
         return;
      }

      for(Entity e : entity.getNearbyEntities((double)this.range, (double)this.range, (double)this.range)) {
         if (e instanceof Player) {
            Player p = (Player)e;
            if (p.isOnline() && !p.isDead()) {
               p.teleport(entity);
               String tip = UtilFormat.format(pn, "skillTip", new Object[]{CustomEntityUtil.getMonsterName(cm), Cus.getSkillManager().getSkillName(7)});
               p.sendMessage(tip);
               return;
            }
         } else {
            net.minecraft.server.v1_6_R2.Entity ee = ((CraftEntity)e).getHandle();
            if (ee instanceof CustomMonster && CustomEntityUtil.getOpposite(cm.getEl(), ((CustomMonster)ee).getEl()).equals(CustomEntityUtil.Opposite.yes)) {
               e.teleport(entity);
               return;
            }
         }
      }

   }
}
