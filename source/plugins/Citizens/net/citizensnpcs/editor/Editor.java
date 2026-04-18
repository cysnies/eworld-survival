package net.citizensnpcs.editor;

import java.util.HashMap;
import java.util.Map;
import net.citizensnpcs.api.util.Messaging;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class Editor implements Listener {
   private static final Map EDITING = new HashMap();

   public Editor() {
      super();
   }

   public abstract void begin();

   public abstract void end();

   private static void enter(Player player, Editor editor) {
      editor.begin();
      player.getServer().getPluginManager().registerEvents(editor, player.getServer().getPluginManager().getPlugin("Citizens"));
      EDITING.put(player.getName(), editor);
   }

   public static void enterOrLeave(Player player, Editor editor) {
      if (editor != null) {
         Editor edit = (Editor)EDITING.get(player.getName());
         if (edit == null) {
            enter(player, editor);
         } else if (edit.getClass() == editor.getClass()) {
            leave(player);
         } else {
            Messaging.sendErrorTr(player, "citizens.editors.already-in-editor");
         }

      }
   }

   public static boolean hasEditor(Player player) {
      return EDITING.containsKey(player.getName());
   }

   public static void leave(Player player) {
      if (hasEditor(player)) {
         Editor editor = (Editor)EDITING.remove(player.getName());
         HandlerList.unregisterAll(editor);
         editor.end();
      }
   }

   public static void leaveAll() {
      for(Map.Entry entry : EDITING.entrySet()) {
         ((Editor)entry.getValue()).end();
         HandlerList.unregisterAll((Listener)entry.getValue());
      }

      EDITING.clear();
   }
}
