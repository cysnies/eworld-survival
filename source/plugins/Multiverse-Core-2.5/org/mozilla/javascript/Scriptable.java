package org.mozilla.javascript;

public interface Scriptable {
   Object NOT_FOUND = UniqueTag.NOT_FOUND;

   String getClassName();

   Object get(String var1, Scriptable var2);

   Object get(int var1, Scriptable var2);

   boolean has(String var1, Scriptable var2);

   boolean has(int var1, Scriptable var2);

   void put(String var1, Scriptable var2, Object var3);

   void put(int var1, Scriptable var2, Object var3);

   void delete(String var1);

   void delete(int var1);

   Scriptable getPrototype();

   void setPrototype(Scriptable var1);

   Scriptable getParentScope();

   void setParentScope(Scriptable var1);

   Object[] getIds();

   Object getDefaultValue(Class var1);

   boolean hasInstance(Scriptable var1);
}
