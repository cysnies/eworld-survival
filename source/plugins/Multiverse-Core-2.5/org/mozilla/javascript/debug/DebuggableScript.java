package org.mozilla.javascript.debug;

public interface DebuggableScript {
   boolean isTopLevel();

   boolean isFunction();

   String getFunctionName();

   int getParamCount();

   int getParamAndVarCount();

   String getParamOrVarName(int var1);

   String getSourceName();

   boolean isGeneratedScript();

   int[] getLineNumbers();

   int getFunctionCount();

   DebuggableScript getFunction(int var1);

   DebuggableScript getParent();
}
