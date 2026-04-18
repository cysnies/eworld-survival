package org.mozilla.javascript;

final class Arguments extends IdScriptableObject {
   static final long serialVersionUID = 4275508002492040609L;
   private static final String FTAG = "Arguments";
   private static final int Id_callee = 1;
   private static final int Id_length = 2;
   private static final int Id_caller = 3;
   private static final int Id_constructor = 4;
   private static final int MAX_INSTANCE_ID = 4;
   private Object callerObj;
   private Object calleeObj;
   private Object lengthObj;
   private Object constructor;
   private NativeCall activation;
   private Object[] args;

   public Arguments(NativeCall activation) {
      super();
      this.activation = activation;
      Scriptable parent = activation.getParentScope();
      this.setParentScope(parent);
      this.setPrototype(ScriptableObject.getObjectPrototype(parent));
      this.args = activation.originalArgs;
      this.lengthObj = this.args.length;
      NativeFunction f = activation.function;
      this.calleeObj = f;
      Scriptable topLevel = getTopLevelScope(parent);
      this.constructor = getProperty(topLevel, "Object");
      int version = f.getLanguageVersion();
      if (version <= 130 && version != 0) {
         this.callerObj = null;
      } else {
         this.callerObj = NOT_FOUND;
      }

   }

   public String getClassName() {
      return "Arguments";
   }

   private Object arg(int index) {
      return index >= 0 && this.args.length > index ? this.args[index] : NOT_FOUND;
   }

   private void putIntoActivation(int index, Object value) {
      String argName = this.activation.function.getParamOrVarName(index);
      this.activation.put(argName, this.activation, value);
   }

   private Object getFromActivation(int index) {
      String argName = this.activation.function.getParamOrVarName(index);
      return this.activation.get(argName, this.activation);
   }

   private void replaceArg(int index, Object value) {
      if (this.sharedWithActivation(index)) {
         this.putIntoActivation(index, value);
      }

      synchronized(this) {
         if (this.args == this.activation.originalArgs) {
            this.args = this.args.clone();
         }

         this.args[index] = value;
      }
   }

   private void removeArg(int index) {
      synchronized(this) {
         if (this.args[index] != NOT_FOUND) {
            if (this.args == this.activation.originalArgs) {
               this.args = this.args.clone();
            }

            this.args[index] = NOT_FOUND;
         }

      }
   }

   public boolean has(int index, Scriptable start) {
      return this.arg(index) != NOT_FOUND ? true : super.has(index, start);
   }

   public Object get(int index, Scriptable start) {
      Object value = this.arg(index);
      if (value == NOT_FOUND) {
         return super.get(index, start);
      } else {
         return this.sharedWithActivation(index) ? this.getFromActivation(index) : value;
      }
   }

   private boolean sharedWithActivation(int index) {
      NativeFunction f = this.activation.function;
      int definedCount = f.getParamCount();
      if (index >= definedCount) {
         return false;
      } else {
         if (index < definedCount - 1) {
            String argName = f.getParamOrVarName(index);

            for(int i = index + 1; i < definedCount; ++i) {
               if (argName.equals(f.getParamOrVarName(i))) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   public void put(int index, Scriptable start, Object value) {
      if (this.arg(index) == NOT_FOUND) {
         super.put(index, start, value);
      } else {
         this.replaceArg(index, value);
      }

   }

   public void delete(int index) {
      if (0 <= index && index < this.args.length) {
         this.removeArg(index);
      }

      super.delete(index);
   }

   protected int getMaxInstanceId() {
      return 4;
   }

   protected int findInstanceIdInfo(String s) {
      int id = 0;
      String X = null;
      int s_length = s.length();
      if (s_length == 6) {
         int c = s.charAt(5);
         if (c == 101) {
            X = "callee";
            id = 1;
         } else if (c == 104) {
            X = "length";
            id = 2;
         } else if (c == 114) {
            X = "caller";
            id = 3;
         }
      } else if (s_length == 11) {
         X = "constructor";
         id = 4;
      }

      if (X != null && X != s && !X.equals(s)) {
         id = 0;
      }

      if (id == 0) {
         return super.findInstanceIdInfo(s);
      } else {
         switch (id) {
            case 1:
            case 2:
            case 3:
            case 4:
               int attr = 2;
               return instanceIdInfo(attr, id);
            default:
               throw new IllegalStateException();
         }
      }
   }

   protected String getInstanceIdName(int id) {
      switch (id) {
         case 1:
            return "callee";
         case 2:
            return "length";
         case 3:
            return "caller";
         case 4:
            return "constructor";
         default:
            return null;
      }
   }

   protected Object getInstanceIdValue(int id) {
      switch (id) {
         case 1:
            return this.calleeObj;
         case 2:
            return this.lengthObj;
         case 3:
            Object value = this.callerObj;
            if (value == UniqueTag.NULL_VALUE) {
               value = null;
            } else if (value == null) {
               NativeCall caller = this.activation.parentActivationCall;
               if (caller != null) {
                  value = caller.get("arguments", caller);
               }
            }

            return value;
         case 4:
            return this.constructor;
         default:
            return super.getInstanceIdValue(id);
      }
   }

   protected void setInstanceIdValue(int id, Object value) {
      switch (id) {
         case 1:
            this.calleeObj = value;
            return;
         case 2:
            this.lengthObj = value;
            return;
         case 3:
            this.callerObj = value != null ? value : UniqueTag.NULL_VALUE;
            return;
         case 4:
            this.constructor = value;
            return;
         default:
            super.setInstanceIdValue(id, value);
      }
   }

   Object[] getIds(boolean getAll) {
      Object[] ids = super.getIds(getAll);
      if (this.args.length != 0) {
         boolean[] present = new boolean[this.args.length];

         int extraCount;
         for(Object id : this.args) {
            if (id instanceof Integer) {
               int index = (Integer)id;
               if (0 <= index && index < this.args.length && !present[index]) {
                  present[index] = true;
                  --extraCount;
               }
            }
         }

         if (!getAll) {
            for(int i = 0; i < present.length; ++i) {
               if (!present[i] && super.has(i, this)) {
                  present[i] = true;
                  --extraCount;
               }
            }
         }

         if (extraCount != 0) {
            Object[] tmp = new Object[extraCount + ids.length];
            System.arraycopy(ids, 0, tmp, extraCount, ids.length);
            ids = tmp;
            int offset = 0;

            for(int i = 0; i != this.args.length; ++i) {
               if (present == null || !present[i]) {
                  ids[offset] = i;
                  ++offset;
               }
            }

            if (offset != extraCount) {
               Kit.codeBug();
            }
         }
      }

      return ids;
   }

   protected ScriptableObject getOwnPropertyDescriptor(Context cx, Object id) {
      double d = ScriptRuntime.toNumber(id);
      int index = (int)d;
      if (d != (double)index) {
         return super.getOwnPropertyDescriptor(cx, id);
      } else {
         Object value = this.arg(index);
         if (value == NOT_FOUND) {
            return super.getOwnPropertyDescriptor(cx, id);
         } else {
            if (this.sharedWithActivation(index)) {
               value = this.getFromActivation(index);
            }

            if (super.has(index, this)) {
               ScriptableObject desc = super.getOwnPropertyDescriptor(cx, id);
               desc.put("value", desc, value);
               return desc;
            } else {
               Scriptable scope = this.getParentScope();
               if (scope == null) {
                  scope = this;
               }

               return buildDataDescriptor(scope, value, 0);
            }
         }
      }
   }

   protected void defineOwnProperty(Context cx, Object id, ScriptableObject desc, boolean checkValid) {
      super.defineOwnProperty(cx, id, desc, checkValid);
      double d = ScriptRuntime.toNumber(id);
      int index = (int)d;
      if (d == (double)index) {
         Object value = this.arg(index);
         if (value != NOT_FOUND) {
            if (this.isAccessorDescriptor(desc)) {
               this.removeArg(index);
            } else {
               Object newValue = getProperty(desc, "value");
               if (newValue != NOT_FOUND) {
                  this.replaceArg(index, newValue);
                  if (isFalse(getProperty(desc, "writable"))) {
                     this.removeArg(index);
                  }

               }
            }
         }
      }
   }
}
