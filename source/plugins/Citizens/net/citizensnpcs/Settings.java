package net.citizensnpcs;

import com.google.common.collect.Lists;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.api.util.YamlStorage;

public class Settings {
   private final Storage config;
   private final DataKey root;

   public Settings(File folder) {
      super();
      this.config = new YamlStorage(new File(folder, "config.yml"), "Citizens Configuration");
      this.root = this.config.getKey("");
      this.config.load();

      for(Setting setting : Settings.Setting.values()) {
         if (!this.root.keyExists(setting.path)) {
            setting.setAtKey(this.root);
         } else {
            setting.loadFromKey(this.root);
         }
      }

      this.updateMessagingSettings();
      this.save();
   }

   public void reload() {
      this.config.load();

      for(Setting setting : Settings.Setting.values()) {
         if (this.root.keyExists(setting.path)) {
            setting.loadFromKey(this.root);
         }
      }

      this.updateMessagingSettings();
      this.save();
   }

   public void save() {
      this.config.save();
   }

   private void updateMessagingSettings() {
      Messaging.configure(Settings.Setting.DEBUG_MODE.asBoolean(), Settings.Setting.MESSAGE_COLOUR.asString(), Settings.Setting.HIGHLIGHT_COLOUR.asString());
   }

   public static enum Setting {
      CHAT_BYSTANDERS_HEAR_TARGETED_CHAT("npc.chat.options.bystanders-hear-targeted-chat", true),
      CHAT_FORMAT("npc.chat.format.no-targets", "[<npc>]: <text>"),
      CHAT_FORMAT_TO_BYSTANDERS("npc.chat.format.with-target-to-bystanders", "[<npc>] -> [<target>]: <text>"),
      CHAT_FORMAT_TO_TARGET("npc.chat.format.to-target", "[<npc>] -> You: <text>"),
      CHAT_FORMAT_WITH_TARGETS_TO_BYSTANDERS("npc.chat.format.with-targets-to-bystanders", "[<npc>] -> [<targets>]: <text>"),
      CHAT_MAX_NUMBER_OF_TARGETS("npc.chat.options.max-number-of-targets-to-show", 2),
      CHAT_MULTIPLE_TARGETS_FORMAT("npc.chat.options.multiple-targets-format", "<target>|, <target>| & <target>| & others"),
      CHAT_RANGE("npc.chat.options.range", 5),
      CHECK_MINECRAFT_VERSION("advanced.check-minecraft-version", true),
      DATABASE_DRIVER("storage.database.driver", ""),
      DATABASE_PASSWORD("storage.database.password", ""),
      DATABASE_URL("storage.database.url", ""),
      DATABASE_USERNAME("storage.database.username", ""),
      DEBUG_MODE("general.debug-mode", false),
      DEFAULT_LOOK_CLOSE("npc.default.look-close.enabled", false),
      DEFAULT_LOOK_CLOSE_RANGE("npc.default.look-close.range", 5),
      DEFAULT_NPC_LIMIT("npc.limits.default-limit", 10),
      DEFAULT_PATHFINDING_RANGE("npc.default.pathfinding.range", 25.0F),
      DEFAULT_RANDOM_TALKER("npc.default.random-talker", true),
      DEFAULT_REALISTIC_LOOKING("npc.default.realistic-looking", false),
      DEFAULT_STATIONARY_TICKS("npc.default.stationary-ticks", -1),
      DEFAULT_TALK_CLOSE("npc.default.talk-close.enabled", false),
      DEFAULT_TALK_CLOSE_RANGE("npc.default.talk-close.range", 5),
      DEFAULT_TEXT("npc.default.text.0", "Hi, I'm <npc>!") {
         public void loadFromKey(DataKey root) {
            List<String> list = new ArrayList();

            for(DataKey key : root.getRelative("npc.default.text").getSubKeys()) {
               list.add(key.getString(""));
            }

            this.value = list;
         }
      },
      HIGHLIGHT_COLOUR("general.color-scheme.message-highlight", "<e>"),
      KEEP_CHUNKS_LOADED("npc.chunks.always-keep-loaded", false),
      LOCALE("general.translation.locale", ""),
      MAX_NPC_LIMIT_CHECKS("npc.limits.max-permission-checks", 100),
      MAX_SPEED("npc.limits.max-speed", 100),
      MAX_TEXT_RANGE("npc.chat.options.max-text-range", 500),
      MESSAGE_COLOUR("general.color-scheme.message", "<a>"),
      NPC_ATTACK_DISTANCE("npc.pathfinding.attack-range", (double)3.0625F),
      NPC_COST("economy.npc.cost", (double)100.0F),
      QUICK_SELECT("npc.selection.quick-select", false),
      REMOVE_PLAYERS_FROM_PLAYER_LIST("npc.player.remove-from-list", true),
      SAVE_TASK_DELAY("storage.save-task.delay", 72000),
      SELECTION_ITEM("npc.selection.item", "280"),
      SELECTION_MESSAGE("npc.selection.message", "<b>You selected <a><npc><b>!"),
      SERVER_OWNS_NPCS("npc.server-ownership", false),
      STORAGE_FILE("storage.file", "saves.yml"),
      STORAGE_TYPE("storage.type", "yaml"),
      SUBPLUGIN_FOLDER("subplugins.folder", "plugins"),
      TALK_CLOSE_MAXIMUM_COOLDOWN("npc.text.max-talk-cooldown", 5),
      TALK_CLOSE_MINIMUM_COOLDOWN("npc.text.min-talk-cooldown", 10),
      TALK_ITEM("npc.text.talk-item", "340"),
      USE_BOAT_CONTROLS("npc.controllable.use-boat-controls", true),
      USE_NEW_PATHFINDER("npc.pathfinding.use-new-finder", false);

      protected String path;
      protected Object value;

      private Setting(String path, Object value) {
         this.path = path;
         this.value = value;
      }

      public boolean asBoolean() {
         return (Boolean)this.value;
      }

      public double asDouble() {
         return ((Number)this.value).doubleValue();
      }

      public float asFloat() {
         return ((Number)this.value).floatValue();
      }

      public int asInt() {
         return this.value instanceof String ? Integer.parseInt(this.value.toString()) : ((Number)this.value).intValue();
      }

      public List asList() {
         if (!(this.value instanceof List)) {
            this.value = Lists.newArrayList(new Object[]{this.value});
         }

         return (List)this.value;
      }

      public long asLong() {
         return ((Number)this.value).longValue();
      }

      public String asString() {
         return this.value.toString();
      }

      protected void loadFromKey(DataKey root) {
         this.value = root.getRaw(this.path);
      }

      protected void setAtKey(DataKey root) {
         root.setRaw(this.path, this.value);
      }
   }
}
