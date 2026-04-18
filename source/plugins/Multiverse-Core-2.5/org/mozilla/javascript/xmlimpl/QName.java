package org.mozilla.javascript.xmlimpl;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdFunctionObject;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

final class QName extends IdScriptableObject {
   static final long serialVersionUID = 416745167693026750L;
   private static final Object QNAME_TAG = "QName";
   private XMLLibImpl lib;
   private QName prototype;
   private XmlNode.QName delegate;
   private static final int Id_localName = 1;
   private static final int Id_uri = 2;
   private static final int MAX_INSTANCE_ID = 2;
   private static final int Id_constructor = 1;
   private static final int Id_toString = 2;
   private static final int Id_toSource = 3;
   private static final int MAX_PROTOTYPE_ID = 3;

   private QName() {
      super();
   }

   static QName create(XMLLibImpl lib, Scriptable scope, QName prototype, XmlNode.QName delegate) {
      QName rv = new QName();
      rv.lib = lib;
      rv.setParentScope(scope);
      rv.prototype = prototype;
      rv.setPrototype(prototype);
      rv.delegate = delegate;
      return rv;
   }

   void exportAsJSClass(boolean sealed) {
      this.exportAsJSClass(3, this.getParentScope(), sealed);
   }

   public String toString() {
      if (this.delegate.getNamespace() == null) {
         return "*::" + this.localName();
      } else {
         return this.delegate.getNamespace().isGlobal() ? this.localName() : this.uri() + "::" + this.localName();
      }
   }

   public String localName() {
      return this.delegate.getLocalName() == null ? "*" : this.delegate.getLocalName();
   }

   String prefix() {
      return this.delegate.getNamespace() == null ? null : this.delegate.getNamespace().getPrefix();
   }

   String uri() {
      return this.delegate.getNamespace() == null ? null : this.delegate.getNamespace().getUri();
   }

   /** @deprecated */
   final XmlNode.QName toNodeQname() {
      return this.delegate;
   }

   final XmlNode.QName getDelegate() {
      return this.delegate;
   }

   public boolean equals(Object obj) {
      return !(obj instanceof QName) ? false : this.equals((QName)obj);
   }

   public int hashCode() {
      return this.delegate.hashCode();
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
      return this.delegate.equals(q.delegate);
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
            return this.localName();
         case 2:
            return this.uri();
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

   QName newQName(XMLLibImpl lib, String q_uri, String q_localName, String q_prefix) {
      QName prototype = this.prototype;
      if (prototype == null) {
         prototype = this;
      }

      XmlNode.Namespace ns = null;
      if (q_prefix != null) {
         ns = XmlNode.Namespace.create(q_prefix, q_uri);
      } else if (q_uri != null) {
         ns = XmlNode.Namespace.create(q_uri);
      } else {
         ns = null;
      }

      if (q_localName != null && q_localName.equals("*")) {
         q_localName = null;
      }

      return create(lib, this.getParentScope(), prototype, XmlNode.QName.create(ns, q_localName));
   }

   QName constructQName(XMLLibImpl lib, Context cx, Object namespace, Object name) {
      String nameString = null;
      if (name instanceof QName) {
         if (namespace == Undefined.instance) {
            return (QName)name;
         }

         nameString = ((QName)name).localName();
      }

      if (name == Undefined.instance) {
         nameString = "";
      } else {
         nameString = ScriptRuntime.toString(name);
      }

      if (namespace == Undefined.instance) {
         if ("*".equals(nameString)) {
            namespace = null;
         } else {
            namespace = lib.getDefaultNamespace(cx);
         }
      }

      Namespace namespaceNamespace = null;
      if (namespace != null) {
         if (namespace instanceof Namespace) {
            namespaceNamespace = (Namespace)namespace;
         } else {
            namespaceNamespace = lib.newNamespace(ScriptRuntime.toString(namespace));
         }
      }

      String q_uri;
      String q_prefix;
      if (namespace == null) {
         q_uri = null;
         q_prefix = null;
      } else {
         q_uri = namespaceNamespace.uri();
         q_prefix = namespaceNamespace.prefix();
      }

      return this.newQName(lib, q_uri, nameString, q_prefix);
   }

   QName constructQName(XMLLibImpl lib, Context cx, Object nameValue) {
      return this.constructQName(lib, cx, Undefined.instance, nameValue);
   }

   QName castToQName(XMLLibImpl lib, Context cx, Object qnameValue) {
      return qnameValue instanceof QName ? (QName)qnameValue : this.constructQName(lib, cx, qnameValue);
   }

   private Object jsConstructor(Context cx, boolean inNewExpr, Object[] args) {
      if (!inNewExpr && args.length == 1) {
         return this.castToQName(this.lib, cx, args[0]);
      } else if (args.length == 0) {
         return this.constructQName(this.lib, cx, Undefined.instance);
      } else {
         return args.length == 1 ? this.constructQName(this.lib, cx, args[0]) : this.constructQName(this.lib, cx, args[0], args[1]);
      }
   }

   private String js_toSource() {
      StringBuffer sb = new StringBuffer();
      sb.append('(');
      toSourceImpl(this.uri(), this.localName(), this.prefix(), sb);
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
