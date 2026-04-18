package com.onarandombox.MultiverseCore.configuration;

/** @deprecated */
@Deprecated
public interface MVConfigProperty {
   String getName();

   Object getValue();

   String toString();

   String getHelp();

   boolean setValue(Object var1);

   boolean parseValue(String var1);

   String getConfigNode();
}
