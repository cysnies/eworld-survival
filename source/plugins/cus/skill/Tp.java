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

public class Tp extends Skill {
   private static final int ID = 1;
   private int range;

   public Tp(String s) {
      super(s);
      this.range = Integer.parseInt(s);
   }

   public int getId() {
      return 1;
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
               entity.teleport(p);
               String s = UtilFormat.format(pn, "skillTip", new Object[]{CustomEntityUtil.getMonsterName(cm), Cus.getSkillManager().getSkillName(1)});
               p.sendMessage(s);
               return;
            }
         } else {
            net.minecraft.server.v1_6_R2.Entity ee = ((CraftEntity)e).getHandle();
            if (ee instanceof CustomMonster && CustomEntityUtil.getOpposite(cm.getEl(), ((CustomMonster)ee).getEl()).equals(CustomEntityUtil.Opposite.yes)) {
               entity.teleport(e);
               return;
            }
         }
      }

   }
}
