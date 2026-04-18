package org.mozilla.javascript.tools.debugger;

public interface GuiCallback {
   void updateSourceText(Dim.SourceInfo var1);

   void enterInterrupt(Dim.StackFrame var1, String var2, String var3);

   boolean isGuiEventThread();

   void dispatchNextGuiEvent() throws InterruptedException;
}
