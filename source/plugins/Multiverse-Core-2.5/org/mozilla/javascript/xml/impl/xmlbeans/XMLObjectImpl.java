package org.mozilla.javascript.xml.impl.xmlbeans;

import org.apache.xmlbeans.XmlObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdFunctionObject;
import org.mozilla.javascript.Kit;
import org.mozilla.javascript.NativeWith;
import org.mozilla.javascript.Ref;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.xml.XMLLib;
import org.mozilla.javascript.xml.XMLObject;

abstract class XMLObjectImpl extends XMLObject {
   private static final Object XMLOBJECT_TAG = "XMLObject";
   protected final XMLLibImpl lib;
   protected boolean prototypeFlag;
   private static final int Id_constructor = 1;
   private static final int Id_addNamespace = 2;
   private static final int Id_appendChild = 3;
   private static final int Id_attribute = 4;
   private static final int Id_attributes = 5;
   private static final int Id_child = 6;
   private static final int Id_childIndex = 7;
   private static final int Id_children = 8;
   private static final int Id_comments = 9;
   private static final int Id_contains = 10;
   private static final int Id_copy = 11;
   private static final int Id_descendants = 12;
   private static final int Id_inScopeNamespaces = 13;
   private static final int Id_insertChildAfter = 14;
   private static final int Id_insertChildBefore = 15;
   private static final int Id_hasOwnProperty = 16;
   private static final int Id_hasComplexContent = 17;
   private static final int Id_hasSimpleContent = 18;
   private static final int Id_length = 19;
   private static final int Id_localName = 20;
   private static final int Id_name = 21;
   private static final int Id_namespace = 22;
   private static final int Id_namespaceDeclarations = 23;
   private static final int Id_nodeKind = 24;
   private static final int Id_normalize = 25;
   private static final int Id_parent = 26;
   private static final int Id_prependChild = 27;
   private static final int Id_processingInstructions = 28;
   private static final int Id_propertyIsEnumerable = 29;
   private static final int Id_removeNamespace = 30;
   private static final int Id_replace = 31;
   private static final int Id_setChildren = 32;
   private static final int Id_setLocalName = 33;
   private static final int Id_setName = 34;
   private static final int Id_setNamespace = 35;
   private static final int Id_text = 36;
   private static final int Id_toString = 37;
   private static final int Id_toSource = 38;
   private static final int Id_toXMLString = 39;
   private static final int Id_valueOf = 40;
   private static final int Id_getXmlObject = 41;
   private static final int MAX_PROTOTYPE_ID = 41;

   protected XMLObjectImpl(XMLLibImpl lib, XMLObject prototype) {
      super(lib.globalScope(), prototype);
      this.lib = lib;
   }

   abstract boolean hasXMLProperty(XMLName var1);

   abstract Object getXMLProperty(XMLName var1);

   abstract void putXMLProperty(XMLName var1, Object var2);

   abstract void deleteXMLProperty(XMLName var1);

   abstract boolean equivalentXml(Object var1);

   abstract XML addNamespace(Namespace var1);

   abstract XML appendChild(Object var1);

   abstract XMLList attribute(XMLName var1);

   abstract XMLList attributes();

   abstract XMLList child(long var1);

   abstract XMLList child(XMLName var1);

   abstract int childIndex();

   abstract XMLList children();

   abstract XMLList comments();

   abstract boolean contains(Object var1);

   abstract Object copy();

   abstract XMLList descendants(XMLName var1);

   abstract Object[] inScopeNamespaces();

   abstract XML insertChildAfter(Object var1, Object var2);

   abstract XML insertChildBefore(Object var1, Object var2);

   abstract boolean hasOwnProperty(XMLName var1);

   abstract boolean hasComplexContent();

   abstract boolean hasSimpleContent();

   abstract int length();

   abstract String localName();

   abstract QName name();

   abstract Object namespace(String var1);

   abstract Object[] namespaceDeclarations();

   abstract Object nodeKind();

   abstract void normalize();

   abstract Object parent();

   abstract XML prependChild(Object var1);

   abstract Object processingInstructions(XMLName var1);

   abstract boolean propertyIsEnumerable(Object var1);

   abstract XML removeNamespace(Namespace var1);

   abstract XML replace(long var1, Object var3);

   abstract XML replace(XMLName var1, Object var2);

   abstract XML setChildren(Object var1);

   abstract void setLocalName(String var1);

   abstract void setName(QName var1);

   abstract void setNamespace(Namespace var1);

   abstract XMLList text();

   public abstract String toString();

   abstract String toSource(int var1);

   abstract String toXMLString(int var1);

   abstract Object valueOf();

   abstract XmlObject getXmlObject();

   protected abstract Object jsConstructor(Context var1, boolean var2, Object[] var3);

   public final Object getDefaultValue(Class hint) {
      return this.toString();
   }

   protected final Object equivalentValues(Object value) {
      boolean result = this.equivalentXml(value);
      return result ? Boolean.TRUE : Boolean.FALSE;
   }

   public final XMLLib lib() {
      return this.lib;
   }

   public final boolean has(Context cx, Object id) {
      if (cx == null) {
         cx = Context.getCurrentContext();
      }

      XMLName xmlName = this.lib.toXMLNameOrIndex(cx, id);
      if (xmlName == null) {
         long index = ScriptRuntime.lastUint32Result(cx);
         return this.has((int)index, this);
      } else {
         return this.hasXMLProperty(xmlName);
      }
   }

   public boolean has(String name, Scriptable start) {
      Context cx = Context.getCurrentContext();
      return this.hasXMLProperty(this.lib.toXMLNameFromString(cx, name));
   }

   public final Object get(Context cx, Object id) {
      if (cx == null) {
         cx = Context.getCurrentContext();
      }

      XMLName xmlName = this.lib.toXMLNameOrIndex(cx, id);
      if (xmlName == null) {
         long index = ScriptRuntime.lastUint32Result(cx);
         Object result = this.get((int)index, this);
         if (result == Scriptable.NOT_FOUND) {
            result = Undefined.instance;
         }

         return result;
      } else {
         return this.getXMLProperty(xmlName);
      }
   }

   public Object get(String name, Scriptable start) {
      Context cx = Context.getCurrentContext();
      return this.getXMLProperty(this.lib.toXMLNameFromString(cx, name));
   }

   public final void put(Context cx, Object id, Object value) {
      if (cx == null) {
         cx = Context.getCurrentContext();
      }

      XMLName xmlName = this.lib.toXMLNameOrIndex(cx, id);
      if (xmlName == null) {
         long index = ScriptRuntime.lastUint32Result(cx);
         this.put((int)index, this, value);
      } else {
         this.putXMLProperty(xmlName, value);
      }
   }

   public void put(String name, Scriptable start, Object value) {
      Context cx = Context.getCurrentContext();
      this.putXMLProperty(this.lib.toXMLNameFromString(cx, name), value);
   }

   public final boolean delete(Context cx, Object id) {
      if (cx == null) {
         cx = Context.getCurrentContext();
      }

      XMLName xmlName = this.lib.toXMLNameOrIndex(cx, id);
      if (xmlName == null) {
         long index = ScriptRuntime.lastUint32Result(cx);
         this.delete((int)index);
         return true;
      } else {
         this.deleteXMLProperty(xmlName);
         return true;
      }
   }

   public void delete(String name) {
      Context cx = Context.getCurrentContext();
      this.deleteXMLProperty(this.lib.toXMLNameFromString(cx, name));
   }

   public Object getFunctionProperty(Context cx, int id) {
      if (this.prototypeFlag) {
         return super.get(id, this);
      } else {
         Scriptable proto = this.getPrototype();
         return proto instanceof XMLObject ? ((XMLObject)proto).getFunctionProperty(cx, id) : NOT_FOUND;
      }
   }

   public Object getFunctionProperty(Context cx, String name) {
      if (this.prototypeFlag) {
         return super.get(name, this);
      } else {
         Scriptable proto = this.getPrototype();
         return proto instanceof XMLObject ? ((XMLObject)proto).getFunctionProperty(cx, name) : NOT_FOUND;
      }
   }

   public Ref memberRef(Context cx, Object elem, int memberTypeFlags) {
      XMLName xmlName;
      if ((memberTypeFlags & 2) != 0) {
         xmlName = this.lib.toAttributeName(cx, elem);
      } else {
         if ((memberTypeFlags & 4) == 0) {
            throw Kit.codeBug();
         }

         xmlName = this.lib.toXMLName(cx, elem);
      }

      if ((memberTypeFlags & 4) != 0) {
         xmlName.setIsDescendants();
      }

      xmlName.initXMLObject(this);
      return xmlName;
   }

   public Ref memberRef(Context cx, Object namespace, Object elem, int memberTypeFlags) {
      XMLName xmlName = this.lib.toQualifiedName(cx, namespace, elem);
      if ((memberTypeFlags & 2) != 0 && !xmlName.isAttributeName()) {
         xmlName.setAttributeName();
      }

      if ((memberTypeFlags & 4) != 0) {
         xmlName.setIsDescendants();
      }

      xmlName.initXMLObject(this);
      return xmlName;
   }

   public NativeWith enterWith(Scriptable scope) {
      return new XMLWithScope(this.lib, scope, this);
   }

   public NativeWith enterDotQuery(Scriptable scope) {
      XMLWithScope xws = new XMLWithScope(this.lib, scope, this);
      xws.initAsDotQuery();
      return xws;
   }

   public final Object addValues(Context cx, boolean thisIsLeft, Object value) {
      if (value instanceof XMLObject) {
         XMLObject v1;
         XMLObject v2;
         if (thisIsLeft) {
            v1 = this;
            v2 = (XMLObject)value;
         } else {
            v1 = (XMLObject)value;
            v2 = this;
         }

         return this.lib.addXMLObjects(cx, v1, v2);
      } else {
         return value == Undefined.instance ? ScriptRuntime.toString(this) : super.addValues(cx, thisIsLeft, value);
      }
   }

   final void exportAsJSClass(boolean sealed) {
      this.prototypeFlag = true;
      this.exportAsJSClass(41, this.lib.globalScope(), sealed);
   }

   protected int findPrototypeId(String s) {
      int id;
      String X;
      id = 0;
      X = null;
      label109:
      switch (s.length()) {
         case 4:
            int c = s.charAt(0);
            if (c == 99) {
               X = "copy";
               id = 11;
            } else if (c == 110) {
               X = "name";
               id = 21;
            } else if (c == 116) {
               X = "text";
               id = 36;
            }
            break;
         case 5:
            X = "child";
            id = 6;
            break;
         case 6:
            int var9 = s.charAt(0);
            if (var9 == 'l') {
               X = "length";
               id = 19;
            } else if (var9 == 'p') {
               X = "parent";
               id = 26;
            }
            break;
         case 7:
            int var8 = s.charAt(0);
            if (var8 == 'r') {
               X = "replace";
               id = 31;
            } else if (var8 == 's') {
               X = "setName";
               id = 34;
            } else if (var8 == 'v') {
               X = "valueOf";
               id = 40;
            }
            break;
         case 8:
            switch (s.charAt(4)) {
               case 'K':
                  X = "nodeKind";
                  id = 24;
                  break label109;
               case 'a':
                  X = "contains";
                  id = 10;
                  break label109;
               case 'd':
                  X = "children";
                  id = 8;
                  break label109;
               case 'e':
                  X = "comments";
                  id = 9;
                  break label109;
               case 'r':
                  X = "toString";
                  id = 37;
                  break label109;
               case 'u':
                  X = "toSource";
                  id = 38;
               default:
                  break label109;
            }
         case 9:
            switch (s.charAt(2)) {
               case 'c':
                  X = "localName";
                  id = 20;
                  break label109;
               case 'm':
                  X = "namespace";
                  id = 22;
                  break label109;
               case 'r':
                  X = "normalize";
                  id = 25;
                  break label109;
               case 't':
                  X = "attribute";
                  id = 4;
               default:
                  break label109;
            }
         case 10:
            int var7 = s.charAt(0);
            if (var7 == 'a') {
               X = "attributes";
               id = 5;
            } else if (var7 == 'c') {
               X = "childIndex";
               id = 7;
            }
            break;
         case 11:
            switch (s.charAt(0)) {
               case 'a':
                  X = "appendChild";
                  id = 3;
                  break label109;
               case 'c':
                  X = "constructor";
                  id = 1;
                  break label109;
               case 'd':
                  X = "descendants";
                  id = 12;
                  break label109;
               case 's':
                  X = "setChildren";
                  id = 32;
                  break label109;
               case 't':
                  X = "toXMLString";
                  id = 39;
               default:
                  break label109;
            }
         case 12:
            switch (s.charAt(0)) {
               case 'a':
                  X = "addNamespace";
                  id = 2;
                  break;
               case 'g':
                  X = "getXmlObject";
                  id = 41;
                  break;
               case 'p':
                  X = "prependChild";
                  id = 27;
                  break;
               case 's':
                  int var6 = s.charAt(3);
                  if (var6 == 'L') {
                     X = "setLocalName";
                     id = 33;
                  } else if (var6 == 'N') {
                     X = "setNamespace";
                     id = 35;
                  }
            }
         case 13:
         case 18:
         case 19:
         default:
            break;
         case 14:
            X = "hasOwnProperty";
            id = 16;
            break;
         case 15:
            X = "removeNamespace";
            id = 30;
            break;
         case 16:
            int var5 = s.charAt(0);
            if (var5 == 'h') {
               X = "hasSimpleContent";
               id = 18;
            } else if (var5 == 'i') {
               X = "insertChildAfter";
               id = 14;
            }
            break;
         case 17:
            int c = s.charAt(3);
            if (c == 'C') {
               X = "hasComplexContent";
               id = 17;
            } else if (c == 'c') {
               X = "inScopeNamespaces";
               id = 13;
            } else if (c == 'e') {
               X = "insertChildBefore";
               id = 15;
            }
            break;
         case 20:
            X = "propertyIsEnumerable";
            id = 29;
            break;
         case 21:
            X = "namespaceDeclarations";
            id = 23;
            break;
         case 22:
            X = "processingInstructions";
            id = 28;
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
            IdFunctionObject ctor;
            if (this instanceof XML) {
               ctor = new XMLCtor((XML)this, XMLOBJECT_TAG, id, 1);
            } else {
               ctor = new IdFunctionObject(this, XMLOBJECT_TAG, id, 1);
            }

            this.initPrototypeConstructor(ctor);
            return;
         case 2:
            arity = 1;
            s = "addNamespace";
            break;
         case 3:
            arity = 1;
            s = "appendChild";
            break;
         case 4:
            arity = 1;
            s = "attribute";
            break;
         case 5:
            arity = 0;
            s = "attributes";
            break;
         case 6:
            arity = 1;
            s = "child";
            break;
         case 7:
            arity = 0;
            s = "childIndex";
            break;
         case 8:
            arity = 0;
            s = "children";
            break;
         case 9:
            arity = 0;
            s = "comments";
            break;
         case 10:
            arity = 1;
            s = "contains";
            break;
         case 11:
            arity = 0;
            s = "copy";
            break;
         case 12:
            arity = 1;
            s = "descendants";
            break;
         case 13:
            arity = 0;
            s = "inScopeNamespaces";
            break;
         case 14:
            arity = 2;
            s = "insertChildAfter";
            break;
         case 15:
            arity = 2;
            s = "insertChildBefore";
            break;
         case 16:
            arity = 1;
            s = "hasOwnProperty";
            break;
         case 17:
            arity = 0;
            s = "hasComplexContent";
            break;
         case 18:
            arity = 0;
            s = "hasSimpleContent";
            break;
         case 19:
            arity = 0;
            s = "length";
            break;
         case 20:
            arity = 0;
            s = "localName";
            break;
         case 21:
            arity = 0;
            s = "name";
            break;
         case 22:
            arity = 1;
            s = "namespace";
            break;
         case 23:
            arity = 0;
            s = "namespaceDeclarations";
            break;
         case 24:
            arity = 0;
            s = "nodeKind";
            break;
         case 25:
            arity = 0;
            s = "normalize";
            break;
         case 26:
            arity = 0;
            s = "parent";
            break;
         case 27:
            arity = 1;
            s = "prependChild";
            break;
         case 28:
            arity = 1;
            s = "processingInstructions";
            break;
         case 29:
            arity = 1;
            s = "propertyIsEnumerable";
            break;
         case 30:
            arity = 1;
            s = "removeNamespace";
            break;
         case 31:
            arity = 2;
            s = "replace";
            break;
         case 32:
            arity = 1;
            s = "setChildren";
            break;
         case 33:
            arity = 1;
            s = "setLocalName";
            break;
         case 34:
            arity = 1;
            s = "setName";
            break;
         case 35:
            arity = 1;
            s = "setNamespace";
            break;
         case 36:
            arity = 0;
            s = "text";
            break;
         case 37:
            arity = 0;
            s = "toString";
            break;
         case 38:
            arity = 1;
            s = "toSource";
            break;
         case 39:
            arity = 1;
            s = "toXMLString";
            break;
         case 40:
            arity = 0;
            s = "valueOf";
            break;
         case 41:
            arity = 0;
            s = "getXmlObject";
            break;
         default:
            throw new IllegalArgumentException(String.valueOf(id));
      }

      this.initPrototypeMethod(XMLOBJECT_TAG, id, s, arity);
   }

   public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      if (!f.hasTag(XMLOBJECT_TAG)) {
         return super.execIdCall(f, cx, scope, thisObj, args);
      } else {
         int id = f.methodId();
         if (id == 1) {
            return this.jsConstructor(cx, thisObj == null, args);
         } else if (!(thisObj instanceof XMLObjectImpl)) {
            throw incompatibleCallError(f);
         } else {
            XMLObjectImpl realThis = (XMLObjectImpl)thisObj;
            switch (id) {
               case 2:
                  Namespace ns = this.lib.castToNamespace(cx, arg(args, 0));
                  return realThis.addNamespace(ns);
               case 3:
                  return realThis.appendChild(arg(args, 0));
               case 4:
                  XMLName xmlName = this.lib.toAttributeName(cx, arg(args, 0));
                  return realThis.attribute(xmlName);
               case 5:
                  return realThis.attributes();
               case 6:
                  XMLName xmlName = this.lib.toXMLNameOrIndex(cx, arg(args, 0));
                  if (xmlName == null) {
                     long index = ScriptRuntime.lastUint32Result(cx);
                     return realThis.child(index);
                  }

                  return realThis.child(xmlName);
               case 7:
                  return ScriptRuntime.wrapInt(realThis.childIndex());
               case 8:
                  return realThis.children();
               case 9:
                  return realThis.comments();
               case 10:
                  return ScriptRuntime.wrapBoolean(realThis.contains(arg(args, 0)));
               case 11:
                  return realThis.copy();
               case 12:
                  XMLName xmlName = args.length == 0 ? XMLName.formStar() : this.lib.toXMLName(cx, args[0]);
                  return realThis.descendants(xmlName);
               case 13:
                  Object[] array = realThis.inScopeNamespaces();
                  return cx.newArray(scope, array);
               case 14:
                  return realThis.insertChildAfter(arg(args, 0), arg(args, 1));
               case 15:
                  return realThis.insertChildBefore(arg(args, 0), arg(args, 1));
               case 16:
                  XMLName xmlName = this.lib.toXMLName(cx, arg(args, 0));
                  return ScriptRuntime.wrapBoolean(realThis.hasOwnProperty(xmlName));
               case 17:
                  return ScriptRuntime.wrapBoolean(realThis.hasComplexContent());
               case 18:
                  return ScriptRuntime.wrapBoolean(realThis.hasSimpleContent());
               case 19:
                  return ScriptRuntime.wrapInt(realThis.length());
               case 20:
                  return realThis.localName();
               case 21:
                  return realThis.name();
               case 22:
                  String prefix = args.length > 0 ? ScriptRuntime.toString(args[0]) : null;
                  return realThis.namespace(prefix);
               case 23:
                  Object[] array = realThis.namespaceDeclarations();
                  return cx.newArray(scope, array);
               case 24:
                  return realThis.nodeKind();
               case 25:
                  realThis.normalize();
                  return Undefined.instance;
               case 26:
                  return realThis.parent();
               case 27:
                  return realThis.prependChild(arg(args, 0));
               case 28:
                  XMLName xmlName = args.length > 0 ? this.lib.toXMLName(cx, args[0]) : XMLName.formStar();
                  return realThis.processingInstructions(xmlName);
               case 29:
                  return ScriptRuntime.wrapBoolean(realThis.propertyIsEnumerable(arg(args, 0)));
               case 30:
                  Namespace ns = this.lib.castToNamespace(cx, arg(args, 0));
                  return realThis.removeNamespace(ns);
               case 31:
                  XMLName xmlName = this.lib.toXMLNameOrIndex(cx, arg(args, 0));
                  Object arg1 = arg(args, 1);
                  if (xmlName == null) {
                     long index = ScriptRuntime.lastUint32Result(cx);
                     return realThis.replace(index, arg1);
                  }

                  return realThis.replace(xmlName, arg1);
               case 32:
                  return realThis.setChildren(arg(args, 0));
               case 33:
                  Object arg = arg(args, 0);
                  String localName;
                  if (arg instanceof QName) {
                     localName = ((QName)arg).localName();
                  } else {
                     localName = ScriptRuntime.toString(arg);
                  }

                  realThis.setLocalName(localName);
                  return Undefined.instance;
               case 34:
                  Object arg = args.length != 0 ? args[0] : Undefined.instance;
                  QName qname;
                  if (arg instanceof QName) {
                     qname = (QName)arg;
                     if (qname.uri() == null) {
                        qname = this.lib.constructQNameFromString(cx, qname.localName());
                     } else {
                        qname = this.lib.constructQName(cx, qname);
                     }
                  } else {
                     qname = this.lib.constructQName(cx, arg);
                  }

                  realThis.setName(qname);
                  return Undefined.instance;
               case 35:
                  Namespace ns = this.lib.castToNamespace(cx, arg(args, 0));
                  realThis.setNamespace(ns);
                  return Undefined.instance;
               case 36:
                  return realThis.text();
               case 37:
                  return realThis.toString();
               case 38:
                  int indent = ScriptRuntime.toInt32(args, 0);
                  return realThis.toSource(indent);
               case 39:
                  int indent = ScriptRuntime.toInt32(args, 0);
                  return realThis.toXMLString(indent);
               case 40:
                  return realThis.valueOf();
               case 41:
                  XmlObject xmlObject = realThis.getXmlObject();
                  return Context.javaToJS(xmlObject, scope);
               default:
                  throw new IllegalArgumentException(String.valueOf(id));
            }
         }
      }
   }

   private static Object arg(Object[] args, int i) {
      return i < args.length ? args[i] : Undefined.instance;
   }
}
