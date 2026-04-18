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

public class AttackAll extends Skill {
   private static final int ID = 3;
   private int range;
   private int minDamage;
   private int maxDamage;

   public AttackAll(String s) {
      super(s);
   }

   public int getId() {
      return 3;
   }

   public void triggerDamage(LivingEntity damager) {
      CustomMonster cm0;
      try {
         cm0 = (CustomMonster)((CraftLivingEntity)damager).getHandle();
      } catch (Exception var6) {
         return;
      }

      for(Entity e : damager.getNearbyEntities((double)this.range, (double)this.range, (double)this.range)) {
         if (e instanceof Player) {
            Player p = (Player)e;
            if (p.isOnline() && !p.isDead()) {
               this.attack(cm0, damager, p);
            }
         } else {
            net.minecraft.server.v1_6_R2.Entity ee = ((CraftEntity)e).getHandle();
            if (ee instanceof CustomMonster && CustomEntityUtil.getOpposite(cm0.getEl(), ((CustomMonster)ee).getEl()).equals(CustomEntityUtil.Opposite.yes)) {
               this.attack(cm0, damager, (LivingEntity)e);
            }
         }
      }

   }

   private void attack(CustomMonster cm, LivingEntity damager, LivingEntity victim) {
      try {
         victim.damage((double)(r.nextInt(this.maxDamage - this.minDamage + 1) + this.minDamage), damager);
         if (victim instanceof Player) {
            String tip = UtilFormat.format(pn, "skillTip", new Object[]{CustomEntityUtil.getMonsterName(cm), Cus.getSkillManager().getSkillName(3)});
            ((Player)victim).sendMessage(tip);
         }
      } catch (Exception var5) {
      }

   }
}
