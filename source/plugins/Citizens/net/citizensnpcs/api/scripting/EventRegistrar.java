package net.citizensnpcs.api.scripting;

import com.google.common.collect.Maps;
import java.util.Map;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class EventRegistrar implements ContextProvider {
   private final Plugin plugin;

   public EventRegistrar(Plugin plugin) {
      super();
      if (plugin != null && plugin.isEnabled()) {
         this.plugin = plugin;
      } else {
         throw new IllegalArgumentException("Invalid plugin passed to EventRegistrar. Is it enabled?");
      }
   }

   public void provide(Script script) {
      script.setAttribute("events", new Events(this.plugin));
   }

   public static class Events {
      private final Map anonymousListeners = Maps.newHashMap();
      private final Plugin plugin;

      public Events(Plugin plugin) {
         super();
         this.plugin = plugin;
      }

      public void deregister(EventHandler handler) {
         if (handler != null) {
            HandlerList.unregisterAll((Listener)this.anonymousListeners.remove(handler));
         }

      }

      public void on(Class eventClass, EventHandler handler) {
         this.registerEvent(handler, eventClass);
      }

      private void registerEvent(final EventHandler handler, final Class eventClass) {
         if (!this.plugin.isEnabled()) {
            throw new IllegalStateException("Plugin is no longer valid.");
         } else {
            Listener bukkitListener = new Listener() {
            };
            this.anonymousListeners.put(handler, bukkitListener);
            PluginManager manager = this.plugin.getServer().getPluginManager();
            manager.registerEvent(eventClass, bukkitListener, EventPriority.NORMAL, new EventExecutor() {
               public void execute(Listener bukkitListener, Event event) throws EventException {
                  try {
                     if (eventClass.isAssignableFrom(event.getClass())) {
                        handler.handle(event);
                     }
                  } catch (Throwable t) {
                     throw new EventException(t);
                  }
               }
            }, this.plugin);
         }
      }
   }
}
