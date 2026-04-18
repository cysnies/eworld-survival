package com.onarandombox.MultiverseCore.event;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MVWorldPropertyChangeEvent extends Event implements Cancellable {
   private MultiverseWorld world;
   private CommandSender changer;
   private boolean isCancelled = false;
   private String name;
   private Object value;
   private static final HandlerList HANDLERS = new HandlerList();

   public MVWorldPropertyChangeEvent(MultiverseWorld world, CommandSender changer, String name, Object value) {
      super();
      this.world = world;
      this.changer = changer;
      this.name = name;
      this.value = value;
   }

   public HandlerList getHandlers() {
      return HANDLERS;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS;
   }

   public String getPropertyName() {
      return this.name;
   }

   /** @deprecated */
   @Deprecated
   public String getNewValue() {
      return this.value.toString();
   }

   public Object getTheNewValue() {
      return this.value;
   }

   /** @deprecated */
   @Deprecated
   public void setNewValue(String value) {
      throw new UnsupportedOperationException();
   }

   public void setTheNewValue(Object value) {
      this.value = value;
   }

   public MultiverseWorld getWorld() {
      return this.world;
   }

   public CommandSender getCommandSender() {
      return this.changer;
   }

   public boolean isCancelled() {
      return this.isCancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.isCancelled = cancelled;
   }
}
