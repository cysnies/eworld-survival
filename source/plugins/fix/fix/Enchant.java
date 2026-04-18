package fix;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

public class Enchant implements Listener {
   private Random r = new Random();
   private String pn;
   private int enchantMin;
   private HashMap enchantHash;

   public Enchant(Fix main) {
      super();
      this.pn = main.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      main.getPm().registerEvents(this, main);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onEnchantItem(EnchantItemEvent e) {
      Map<Enchantment, Integer> map = e.getEnchantsToAdd();
      Iterator<Enchantment> it = map.keySet().iterator();

      while(it.hasNext()) {
         Enchantment enchantment = (Enchantment)it.next();
         int id = enchantment.getId();
         if (!this.enchantHash.containsKey(id)) {
            return;
         }

         int chance = (Integer)this.enchantHash.get(id);
         int level = (Integer)map.get(enchantment);
         if (this.r.nextInt(100) < chance && level > this.enchantMin) {
            if (level <= 1) {
               it.remove();
            } else {
               map.put(enchantment, level - 1);
            }
         }
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.enchantMin = config.getInt("enchantMin");
      this.enchantHash = new HashMap();

      for(String s : config.getStringList("enchant")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         int chance = Integer.parseInt(s.split(" ")[1]);
         this.enchantHash.put(id, chance);
      }

   }
}
