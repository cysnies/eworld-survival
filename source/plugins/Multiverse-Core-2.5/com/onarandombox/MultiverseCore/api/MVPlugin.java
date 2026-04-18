package com.onarandombox.MultiverseCore.api;

import com.onarandombox.MultiverseCore.MultiverseCore;

public interface MVPlugin extends LoggablePlugin {
   /** @deprecated */
   @Deprecated
   String dumpVersionInfo(String var1);

   MultiverseCore getCore();

   void setCore(MultiverseCore var1);

   int getProtocolVersion();
}
