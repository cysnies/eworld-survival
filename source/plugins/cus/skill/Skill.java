package skill;

import cus.Cus;
import java.util.Random;
import org.bukkit.entity.LivingEntity;

public abstract class Skill {
   static Random r = new Random();
   static String pn;

   public Skill(String s) {
      super();
   }

   public static void init(Cus cus) {
      pn = cus.getPn();
   }

   public abstract int getId();

   public void triggerAttack(LivingEntity damager, LivingEntity victim, double damage) {
   }

   public void triggerDamage(LivingEntity entity) {
   }

   public void triggerDeath(LivingEntity entity) {
   }
}
