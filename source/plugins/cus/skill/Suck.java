package skill;

import org.bukkit.entity.LivingEntity;

public class Suck extends Skill {
   private static final int ID = 5;
   private double multiply;

   public Suck(String s) {
      super(s);
      this.multiply = Double.parseDouble(s);
   }

   public int getId() {
      return 5;
   }

   public void triggerAttack(LivingEntity damager, LivingEntity victim, double damage) {
      damager.setHealth(Math.min(damager.getMaxHealth(), damager.getHealth() + (double)((int)(damage * this.multiply))));
   }
}
