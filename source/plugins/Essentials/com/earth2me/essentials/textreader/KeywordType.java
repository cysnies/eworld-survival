package com.earth2me.essentials.textreader;

enum KeywordType {
   PLAYER(KeywordCachable.CACHEABLE),
   DISPLAYNAME(KeywordCachable.CACHEABLE),
   USERNAME(KeywordCachable.NOTCACHEABLE),
   BALANCE(KeywordCachable.CACHEABLE),
   MAILS(KeywordCachable.CACHEABLE),
   WORLD(KeywordCachable.CACHEABLE),
   ONLINE(KeywordCachable.CACHEABLE),
   UNIQUE(KeywordCachable.CACHEABLE),
   WORLDS(KeywordCachable.CACHEABLE),
   PLAYERLIST(KeywordCachable.SUBVALUE, true),
   TIME(KeywordCachable.CACHEABLE),
   DATE(KeywordCachable.CACHEABLE),
   WORLDTIME12(KeywordCachable.CACHEABLE),
   WORLDTIME24(KeywordCachable.CACHEABLE),
   WORLDDATE(KeywordCachable.CACHEABLE),
   COORDS(KeywordCachable.CACHEABLE),
   TPS(KeywordCachable.CACHEABLE),
   UPTIME(KeywordCachable.CACHEABLE),
   IP(KeywordCachable.CACHEABLE, true),
   ADDRESS(KeywordCachable.CACHEABLE, true),
   PLUGINS(KeywordCachable.CACHEABLE, true),
   VERSION(KeywordCachable.CACHEABLE, true);

   private final KeywordCachable type;
   private final boolean isPrivate;

   private KeywordType(KeywordCachable type) {
      this.type = type;
      this.isPrivate = false;
   }

   private KeywordType(KeywordCachable type, boolean isPrivate) {
      this.type = type;
      this.isPrivate = isPrivate;
   }

   public KeywordCachable getType() {
      return this.type;
   }

   public boolean isPrivate() {
      return this.isPrivate;
   }
}
