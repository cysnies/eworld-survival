package org.mozilla.javascript;

public interface RegExpProxy {
   int RA_MATCH = 1;
   int RA_REPLACE = 2;
   int RA_SEARCH = 3;

   boolean isRegExp(Scriptable var1);

   Object compileRegExp(Context var1, String var2, String var3);

   Scriptable wrapRegExp(Context var1, Scriptable var2, Object var3);

   Object action(Context var1, Scriptable var2, Scriptable var3, Object[] var4, int var5);

   int find_split(Context var1, Scriptable var2, String var3, String var4, Scriptable var5, int[] var6, int[] var7, boolean[] var8, String[][] var9);

   Object js_split(Context var1, Scriptable var2, String var3, Object[] var4);
}
