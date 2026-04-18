package net.citizensnpcs.api.scripting;

public interface Script {
   Object convertToInterface(Object var1, Class var2);

   Object getAttribute(String var1);

   Object invoke(Object var1, String var2, Object... var3);

   Object invoke(String var1, Object... var2);

   void setAttribute(String var1, Object var2);
}
