package skill;

import cus.Cus;
import cus.CustomEntityUtil;
import cus.CustomMonster;
import lib.util.Util;
import lib.util.UtilFormat;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Kick extends Skill {
   private static final int ID = 9;
   private double multiply;

   public Kick(String s) {
      super(s);
      this.multiply = Double.parseDouble(s);
   }

   public int getId() {
      return 9;
   }

   public void triggerAttack(LivingEntity damager, LivingEntity victim, double damage) {
      Util.eject(victim, damager.getLocation(), victim.getEyeLocation(), this.multiply);
      if (victim instanceof Player) {
         CustomMonster cm;
         try {
            cm = (CustomMonster)((CraftLivingEntity)damager).getHandle();
         } catch (Exception var7) {
            return;
         }

         String tip = UtilFormat.format(pn, "skillTip", new Object[]{CustomEntityUtil.getMonsterName(cm), Cus.getSkillManager().getSkillName(9)});
         ((Player)victim).sendMessage(tip);
      }

   }
}
