package org.mozilla.javascript.xml.impl.xmlbeans;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdFunctionObject;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

final class QName extends IdScriptableObject {
   static final long serialVersionUID = 416745167693026750L;
   private static final Object QNAME_TAG = "QName";
   XMLLibImpl lib;
   private String prefix;
   private String localName;
   private String uri;
   private static final int Id_localName = 1;
   private static final int Id_uri = 2;
   private static final int MAX_INSTANCE_ID = 2;
   private static final int Id_constructor = 1;
   private static final int Id_toString = 2;
   private static final int Id_toSource = 3;
   private static final int MAX_PROTOTYPE_ID = 3;

   QName(XMLLibImpl lib, String uri, String localName, String prefix) {
      super(lib.globalScope(), lib.qnamePrototype);
      if (localName == null) {
         throw new IllegalArgumentException();
      } else {
         this.lib = lib;
         this.uri = uri;
         this.prefix = prefix;
         this.localName = localName;
      }
   }

   void exportAsJSClass(boolean sealed) {
      this.exportAsJSClass(3, this.lib.globalScope(), sealed);
   }

   public String toString() {
      String result;
      if (this.uri == null) {
         result = "*::".concat(this.localName);
      } else if (this.uri.length() == 0) {
         result = this.localName;
      } else {
         result = this.uri + "::" + this.localName;
      }

      return result;
   }

   public String localName() {
      return this.localName;
   }

   String prefix() {
      return this.prefix == null ? this.prefix : "";
   }

   String uri() {
      return this.uri;
   }

   public boolean equals(Object obj) {
      return !(obj instanceof QName) ? false : this.equals((QName)obj);
   }

   public int hashCode() {
      return this.localName.hashCode() ^ (this.uri == null ? 0 : this.uri.hashCode());
   }

   protected Object equivalentValues(Object value) {
      if (!(value instanceof QName)) {
         return Scriptable.NOT_FOUND;
      } else {
         boolean result = this.equals((QName)value);
         return result ? Boolean.TRUE : Boolean.FALSE;
      }
   }

   private boolean equals(QName q) {
      boolean result;
      if (this.uri == null) {
         result = q.uri == null && this.localName.equals(q.localName);
      } else {
         result = this.uri.equals(q.uri) && this.localName.equals(q.localName);
      }

      return result;
   }

   public String getClassName() {
      return "QName";
   }

   public Object getDefaultValue(Class hint) {
      return this.toString();
   }

   protected int getMaxInstanceId() {
      return super.getMaxInstanceId() + 2;
   }

   protected int findInstanceIdInfo(String s) {
      int id = 0;
      String X = null;
      int s_length = s.length();
      if (s_length == 3) {
         X = "uri";
         id = 2;
      } else if (s_length == 9) {
         X = "localName";
         id = 1;
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
               int attr = 5;
               return instanceIdInfo(attr, super.getMaxInstanceId() + id);
            default:
               throw new IllegalStateException();
         }
      }
   }

   protected String getInstanceIdName(int id) {
      switch (id - super.getMaxInstanceId()) {
         case 1:
            return "localName";
         case 2:
            return "uri";
         default:
            return super.getInstanceIdName(id);
      }
   }

   protected Object getInstanceIdValue(int id) {
      switch (id - super.getMaxInstanceId()) {
         case 1:
            return this.localName;
         case 2:
            return this.uri;
         default:
            return super.getInstanceIdValue(id);
      }
   }

   protected int findPrototypeId(String s) {
      int id = 0;
      String X = null;
      int s_length = s.length();
      if (s_length == 8) {
         int c = s.charAt(3);
         if (c == 111) {
            X = "toSource";
            id = 3;
         } else if (c == 116) {
            X = "toString";
            id = 2;
         }
      } else if (s_length == 11) {
         X = "constructor";
         id = 1;
      }

      if (X != null && X != s && !X.equals(s)) {
         id = 0;
      }

      return id;
   }

   protected void initPrototypeId(int id) {
      int arity;
      String s;
      switch (id) {
         case 1:
            arity = 2;
            s = "constructor";
            break;
         case 2:
            arity = 0;
            s = "toString";
            break;
         case 3:
            arity = 0;
            s = "toSource";
            break;
         default:
            throw new IllegalArgumentException(String.valueOf(id));
      }

      this.initPrototypeMethod(QNAME_TAG, id, s, arity);
   }

   public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      if (!f.hasTag(QNAME_TAG)) {
         return super.execIdCall(f, cx, scope, thisObj, args);
      } else {
         int id = f.methodId();
         switch (id) {
            case 1:
               return this.jsConstructor(cx, thisObj == null, args);
            case 2:
               return this.realThis(thisObj, f).toString();
            case 3:
               return this.realThis(thisObj, f).js_toSource();
            default:
               throw new IllegalArgumentException(String.valueOf(id));
         }
      }
   }

   private QName realThis(Scriptable thisObj, IdFunctionObject f) {
      if (!(thisObj instanceof QName)) {
         throw incompatibleCallError(f);
      } else {
         return (QName)thisObj;
      }
   }

   private Object jsConstructor(Context cx, boolean inNewExpr, Object[] args) {
      if (!inNewExpr && args.length == 1) {
         return this.lib.castToQName(cx, args[0]);
      } else if (args.length == 0) {
         return this.lib.constructQName(cx, Undefined.instance);
      } else {
         return args.length == 1 ? this.lib.constructQName(cx, args[0]) : this.lib.constructQName(cx, args[0], args[1]);
      }
   }

   private String js_toSource() {
      StringBuffer sb = new StringBuffer();
      sb.append('(');
      toSourceImpl(this.uri, this.localName, this.prefix, sb);
      sb.append(')');
      return sb.toString();
   }

   private static void toSourceImpl(String uri, String localName, String prefix, StringBuffer sb) {
      sb.append("new QName(");
      if (uri == null && prefix == null) {
         if (!"*".equals(localName)) {
            sb.append("null, ");
         }
      } else {
         Namespace.toSourceImpl(prefix, uri, sb);
         sb.append(", ");
      }

      sb.append('\'');
      sb.append(ScriptRuntime.escapeString(localName, '\''));
      sb.append("')");
   }
}
