package level;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LevelNameSetEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private Player p;
   private Level level;
   private String show;

   public LevelNameSetEvent(Player p, Level level, String show) {
      super();
      this.p = p;
      this.level = level;
      this.show = show;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public Player getP() {
      return this.p;
   }

   public Level getLevel() {
      return this.level;
   }

   public String getShow() {
      return this.show;
   }

   public void setShow(String show) {
      this.show = show;
   }
}
