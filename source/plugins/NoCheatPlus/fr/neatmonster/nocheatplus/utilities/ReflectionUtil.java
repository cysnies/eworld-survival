package fr.neatmonster.nocheatplus.utilities;

import java.lang.reflect.Method;

public class ReflectionUtil {
   public ReflectionUtil() {
      super();
   }

   public static void checkMembers(String prefix, String[]... specs) {
      try {
         for(String[] spec : specs) {
            Class<?> clazz = Class.forName(prefix + spec[0]);

            for(int i = 1; i < spec.length; ++i) {
               if (clazz.getField(spec[i]) == null) {
                  throw new NoSuchFieldException(prefix + spec[0] + " : " + spec[i]);
               }
            }
         }
      } catch (SecurityException var8) {
      } catch (Throwable t) {
         throw new RuntimeException(t);
      }

   }

   public static void checkMethodReturnTypesNoArgs(Class objClass, String[] methodNames, Class returnType) {
      try {
         for(String methodName : methodNames) {
            Method m = objClass.getMethod(methodName);
            if (m.getParameterTypes().length != 0) {
               throw new RuntimeException("Expect method without arguments for " + objClass.getName() + "." + methodName);
            }

            if (m.getReturnType() != returnType) {
               throw new RuntimeException("Wrong return type for: " + objClass.getName() + "." + methodName);
            }
         }
      } catch (SecurityException var8) {
      } catch (Throwable t) {
         throw new RuntimeException(t);
      }

   }

   public static Object invokeGenericMethodOneArg(Object obj, String methodName, Object arg) {
      Class<?> objClass = obj.getClass();
      Class<?> argClass = arg.getClass();
      Method methodFound = null;
      boolean denyObject = false;

      for(Method method : objClass.getDeclaredMethods()) {
         if (method.getName().equals(methodName)) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 1) {
               if (parameterTypes[0] != Object.class && !parameterTypes[0].isAssignableFrom(argClass)) {
                  denyObject = true;
               }

               if (methodFound == null && parameterTypes[0].isAssignableFrom(argClass) || methodFound != null && methodFound.getParameterTypes()[0].isAssignableFrom(parameterTypes[0])) {
                  methodFound = method;
               }
            }
         }
      }

      if (denyObject && methodFound.getParameterTypes()[0] == Object.class) {
         return null;
      } else if (methodFound != null && methodFound.getParameterTypes()[0].isAssignableFrom(argClass)) {
         try {
            Object res = methodFound.invoke(obj, arg);
            return res;
         } catch (Throwable var12) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static Object invokeMethodNoArgs(Object obj, String methodName, Class... returnTypePreference) {
      Class<?> objClass = obj.getClass();
      Method methodFound = getMethodNoArgs(objClass, methodName, returnTypePreference);
      if (methodFound == null) {
         methodFound = seekMethodNoArgs(objClass, methodName, returnTypePreference);
      }

      if (methodFound != null) {
         try {
            Object res = methodFound.invoke(obj);
            return res;
         } catch (Throwable var6) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static Method getMethodNoArgs(Class objClass, String methodName, Class[] returnTypePreference) {
      try {
         Method methodFound = objClass.getMethod(methodName);
         if (methodFound != null) {
            Class<?> returnType = methodFound.getReturnType();

            for(int i = 0; i < returnTypePreference.length; ++i) {
               if (returnType == returnTypePreference[i]) {
                  return methodFound;
               }
            }
         }
      } catch (SecurityException var6) {
      } catch (NoSuchMethodException var7) {
      }

      return null;
   }

   public static Method seekMethodNoArgs(Class objClass, String methodName, Class[] returnTypePreference) {
      Method methodFound = null;
      int returnTypeIndex = returnTypePreference.length;

      for(Method method : objClass.getMethods()) {
         if (method.getName().equals(methodName)) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 0) {
               Class<?> returnType = method.getReturnType();
               if (methodFound == null) {
                  methodFound = method;

                  for(int i = 0; i < returnTypeIndex; ++i) {
                     if (returnTypePreference[i] == returnType) {
                        returnTypeIndex = i;
                        break;
                     }
                  }
               } else {
                  for(int i = 0; i < returnTypeIndex; ++i) {
                     if (returnTypePreference[i] == returnType) {
                        methodFound = method;
                        returnTypeIndex = i;
                        break;
                     }
                  }
               }

               if (returnTypeIndex == 0) {
                  break;
               }
            }
         }
      }

      return methodFound;
   }
}
