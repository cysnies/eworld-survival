package net.citizensnpcs.trait.waypoint.triggers;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import java.util.Map;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.persistence.Persister;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.conversations.Prompt;

public class WaypointTriggerRegistry implements Persister {
   private static final Map triggerPrompts = Maps.newHashMap();
   private static final Map triggers = Maps.newHashMap();

   public WaypointTriggerRegistry() {
      super();
   }

   public WaypointTrigger create(DataKey root) {
      String type = root.getString("type");
      Class<? extends WaypointTrigger> clazz = (Class)triggers.get(type);
      return clazz == null ? null : (WaypointTrigger)PersistenceLoader.load(clazz, root);
   }

   public void save(WaypointTrigger instance, DataKey root) {
      PersistenceLoader.save(instance, root);
   }

   public static void addTrigger(String name, Class triggerClass, Class promptClass) {
      triggers.put(name, triggerClass);
      triggerPrompts.put(name, promptClass);
   }

   public static String describeValidTriggerNames() {
      return Joiner.on(", ").join(triggerPrompts.keySet());
   }

   public static Prompt getTriggerPromptFrom(String input) {
      Class<? extends Prompt> promptClass = (Class)triggerPrompts.get(input);
      if (promptClass == null) {
         return null;
      } else {
         try {
            return (Prompt)promptClass.newInstance();
         } catch (Exception var3) {
            return null;
         }
      }
   }

   static {
      addTrigger("animation", AnimationTrigger.class, AnimationTriggerPrompt.class);
      addTrigger("chat", ChatTrigger.class, ChatTriggerPrompt.class);
      addTrigger("delay", DelayTrigger.class, DelayTriggerPrompt.class);
      addTrigger("teleport", TeleportTrigger.class, TeleportTriggerPrompt.class);
   }
}
