package org.mozilla.javascript.xmlimpl;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdFunctionObject;
import org.mozilla.javascript.Kit;
import org.mozilla.javascript.NativeWith;
import org.mozilla.javascript.Ref;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.xml.XMLObject;

abstract class XMLObjectImpl extends XMLObject {
   private static final Object XMLOBJECT_TAG = "XMLObject";
   private XMLLibImpl lib;
   private boolean prototypeFlag;
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
   private static final int Id_elements = 13;
   private static final int Id_inScopeNamespaces = 14;
   private static final int Id_insertChildAfter = 15;
   private static final int Id_insertChildBefore = 16;
   private static final int Id_hasOwnProperty = 17;
   private static final int Id_hasComplexContent = 18;
   private static final int Id_hasSimpleContent = 19;
   private static final int Id_length = 20;
   private static final int Id_localName = 21;
   private static final int Id_name = 22;
   private static final int Id_namespace = 23;
   private static final int Id_namespaceDeclarations = 24;
   private static final int Id_nodeKind = 25;
   private static final int Id_normalize = 26;
   private static final int Id_parent = 27;
   private static final int Id_prependChild = 28;
   private static final int Id_processingInstructions = 29;
   private static final int Id_propertyIsEnumerable = 30;
   private static final int Id_removeNamespace = 31;
   private static final int Id_replace = 32;
   private static final int Id_setChildren = 33;
   private static final int Id_setLocalName = 34;
   private static final int Id_setName = 35;
   private static final int Id_setNamespace = 36;
   private static final int Id_text = 37;
   private static final int Id_toString = 38;
   private static final int Id_toSource = 39;
   private static final int Id_toXMLString = 40;
   private static final int Id_valueOf = 41;
   private static final int MAX_PROTOTYPE_ID = 41;

   protected XMLObjectImpl(XMLLibImpl lib, Scriptable scope, XMLObject prototype) {
      super();
      this.initialize(lib, scope, prototype);
   }

   final void initialize(XMLLibImpl lib, Scriptable scope, XMLObject prototype) {
      this.setParentScope(scope);
      this.setPrototype(prototype);
      this.prototypeFlag = prototype == null;
      this.lib = lib;
   }

   final boolean isPrototype() {
      return this.prototypeFlag;
   }

   XMLLibImpl getLib() {
      return this.lib;
   }

   final XML newXML(XmlNode node) {
      return this.lib.newXML(node);
   }

   XML xmlFromNode(XmlNode node) {
      if (node.getXml() == null) {
         node.setXml(this.newXML(node));
      }

      return node.getXml();
   }

   final XMLList newXMLList() {
      return this.lib.newXMLList();
   }

   final XMLList newXMLListFrom(Object o) {
      return this.lib.newXMLListFrom(o);
   }

   final XmlProcessor getProcessor() {
      return this.lib.getProcessor();
   }

   final QName newQName(String uri, String localName, String prefix) {
      return this.lib.newQName(uri, localName, prefix);
   }

   final QName newQName(XmlNode.QName name) {
      return this.lib.newQName(name);
   }

   final Namespace createNamespace(XmlNode.Namespace declaration) {
      return declaration == null ? null : this.lib.createNamespaces(new XmlNode.Namespace[]{declaration})[0];
   }

   final Namespace[] createNamespaces(XmlNode.Namespace[] declarations) {
      return this.lib.createNamespaces(declarations);
   }

   public final Scriptable getPrototype() {
      return super.getPrototype();
   }

   public final void setPrototype(Scriptable prototype) {
      super.setPrototype(prototype);
   }

   public final Scriptable getParentScope() {
      return super.getParentScope();
   }

   public final void setParentScope(Scriptable parent) {
      super.setParentScope(parent);
   }

   public final Object getDefaultValue(Class hint) {
      return this.toString();
   }

   public final boolean hasInstance(Scriptable scriptable) {
      return super.hasInstance(scriptable);
   }

   abstract boolean hasXMLProperty(XMLName var1);

   abstract Object getXMLProperty(XMLName var1);

   abstract void putXMLProperty(XMLName var1, Object var2);

   abstract void deleteXMLProperty(XMLName var1);

   abstract boolean equivalentXml(Object var1);

   abstract void addMatches(XMLList var1, XMLName var2);

   private XMLList getMatches(XMLName name) {
      XMLList rv = this.newXMLList();
      this.addMatches(rv, name);
      return rv;
   }

   abstract XML getXML();

   abstract XMLList child(int var1);

   abstract XMLList child(XMLName var1);

   abstract XMLList children();

   abstract XMLList comments();

   abstract boolean contains(Object var1);

   abstract XMLObjectImpl copy();

   abstract XMLList elements(XMLName var1);

   abstract boolean hasOwnProperty(XMLName var1);

   abstract boolean hasComplexContent();

   abstract boolean hasSimpleContent();

   abstract int length();

   abstract void normalize();

   abstract Object parent();

   abstract XMLList processingInstructions(XMLName var1);

   abstract boolean propertyIsEnumerable(Object var1);

   abstract XMLList text();

   public abstract String toString();

   abstract String toSource(int var1);

   abstract String toXMLString();

   abstract Object valueOf();

   protected abstract Object jsConstructor(Context var1, boolean var2, Object[] var3);

   protected final Object equivalentValues(Object value) {
      boolean result = this.equivalentXml(value);
      return result ? Boolean.TRUE : Boolean.FALSE;
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
      if (this.isPrototype()) {
         return super.get(id, this);
      } else {
         Scriptable proto = this.getPrototype();
         return proto instanceof XMLObject ? ((XMLObject)proto).getFunctionProperty(cx, id) : NOT_FOUND;
      }
   }

   public Object getFunctionProperty(Context cx, String name) {
      if (this.isPrototype()) {
         return super.get(name, this);
      } else {
         Scriptable proto = this.getPrototype();
         return proto instanceof XMLObject ? ((XMLObject)proto).getFunctionProperty(cx, name) : NOT_FOUND;
      }
   }

   public Ref memberRef(Context cx, Object elem, int memberTypeFlags) {
      boolean attribute = (memberTypeFlags & 2) != 0;
      boolean descendants = (memberTypeFlags & 4) != 0;
      if (!attribute && !descendants) {
         throw Kit.codeBug();
      } else {
         XmlNode.QName qname = this.lib.toNodeQName(cx, elem, attribute);
         XMLName rv = XMLName.create(qname, attribute, descendants);
         rv.initXMLObject(this);
         return rv;
      }
   }

   public Ref memberRef(Context cx, Object namespace, Object elem, int memberTypeFlags) {
      boolean attribute = (memberTypeFlags & 2) != 0;
      boolean descendants = (memberTypeFlags & 4) != 0;
      XMLName rv = XMLName.create(this.lib.toNodeQName(cx, namespace, elem), attribute, descendants);
      rv.initXMLObject(this);
      return rv;
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
      this.exportAsJSClass(41, this.getParentScope(), sealed);
   }

   protected int findPrototypeId(String s) {
      int id;
      String X;
      id = 0;
      X = null;
      label114:
      switch (s.length()) {
         case 4:
            int c = s.charAt(0);
            if (c == 99) {
               X = "copy";
               id = 11;
            } else if (c == 110) {
               X = "name";
               id = 22;
            } else if (c == 116) {
               X = "text";
               id = 37;
            }
            break;
         case 5:
            X = "child";
            id = 6;
            break;
         case 6:
            int var11 = s.charAt(0);
            if (var11 == 'l') {
               X = "length";
               id = 20;
            } else if (var11 == 'p') {
               X = "parent";
               id = 27;
            }
            break;
         case 7:
            int var10 = s.charAt(0);
            if (var10 == 'r') {
               X = "replace";
               id = 32;
            } else if (var10 == 's') {
               X = "setName";
               id = 35;
            } else if (var10 == 'v') {
               X = "valueOf";
               id = 41;
            }
            break;
         case 8:
            switch (s.charAt(2)) {
               case 'S':
                  int var9 = s.charAt(7);
                  if (var9 == 'e') {
                     X = "toSource";
                     id = 39;
                  } else if (var9 == 'g') {
                     X = "toString";
                     id = 38;
                  }
                  break label114;
               case 'd':
                  X = "nodeKind";
                  id = 25;
                  break label114;
               case 'e':
                  X = "elements";
                  id = 13;
                  break label114;
               case 'i':
                  X = "children";
                  id = 8;
                  break label114;
               case 'm':
                  X = "comments";
                  id = 9;
                  break label114;
               case 'n':
                  X = "contains";
                  id = 10;
               default:
                  break label114;
            }
         case 9:
            switch (s.charAt(2)) {
               case 'c':
                  X = "localName";
                  id = 21;
                  break label114;
               case 'm':
                  X = "namespace";
                  id = 23;
                  break label114;
               case 'r':
                  X = "normalize";
                  id = 26;
                  break label114;
               case 't':
                  X = "attribute";
                  id = 4;
               default:
                  break label114;
            }
         case 10:
            int var8 = s.charAt(0);
            if (var8 == 'a') {
               X = "attributes";
               id = 5;
            } else if (var8 == 'c') {
               X = "childIndex";
               id = 7;
            }
            break;
         case 11:
            switch (s.charAt(0)) {
               case 'a':
                  X = "appendChild";
                  id = 3;
                  break label114;
               case 'c':
                  X = "constructor";
                  id = 1;
                  break label114;
               case 'd':
                  X = "descendants";
                  id = 12;
                  break label114;
               case 's':
                  X = "setChildren";
                  id = 33;
                  break label114;
               case 't':
                  X = "toXMLString";
                  id = 40;
               default:
                  break label114;
            }
         case 12:
            int var6 = s.charAt(0);
            if (var6 == 'a') {
               X = "addNamespace";
               id = 2;
            } else if (var6 == 'p') {
               X = "prependChild";
               id = 28;
            } else if (var6 == 's') {
               var6 = s.charAt(3);
               if (var6 == 'L') {
                  X = "setLocalName";
                  id = 34;
               } else if (var6 == 'N') {
                  X = "setNamespace";
                  id = 36;
               }
            }
         case 13:
         case 18:
         case 19:
         default:
            break;
         case 14:
            X = "hasOwnProperty";
            id = 17;
            break;
         case 15:
            X = "removeNamespace";
            id = 31;
            break;
         case 16:
            int var5 = s.charAt(0);
            if (var5 == 'h') {
               X = "hasSimpleContent";
               id = 19;
            } else if (var5 == 'i') {
               X = "insertChildAfter";
               id = 15;
            }
            break;
         case 17:
            int c = s.charAt(3);
            if (c == 'C') {
               X = "hasComplexContent";
               id = 18;
            } else if (c == 'c') {
               X = "inScopeNamespaces";
               id = 14;
            } else if (c == 'e') {
               X = "insertChildBefore";
               id = 16;
            }
            break;
         case 20:
            X = "propertyIsEnumerable";
            id = 30;
            break;
         case 21:
            X = "namespaceDeclarations";
            id = 24;
            break;
         case 22:
            X = "processingInstructions";
            id = 29;
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
            arity = 1;
            s = "elements";
            break;
         case 14:
            arity = 0;
            s = "inScopeNamespaces";
            break;
         case 15:
            arity = 2;
            s = "insertChildAfter";
            break;
         case 16:
            arity = 2;
            s = "insertChildBefore";
            break;
         case 17:
            arity = 1;
            s = "hasOwnProperty";
            break;
         case 18:
            arity = 0;
            s = "hasComplexContent";
            break;
         case 19:
            arity = 0;
            s = "hasSimpleContent";
            break;
         case 20:
            arity = 0;
            s = "length";
            break;
         case 21:
            arity = 0;
            s = "localName";
            break;
         case 22:
            arity = 0;
            s = "name";
            break;
         case 23:
            arity = 1;
            s = "namespace";
            break;
         case 24:
            arity = 0;
            s = "namespaceDeclarations";
            break;
         case 25:
            arity = 0;
            s = "nodeKind";
            break;
         case 26:
            arity = 0;
            s = "normalize";
            break;
         case 27:
            arity = 0;
            s = "parent";
            break;
         case 28:
            arity = 1;
            s = "prependChild";
            break;
         case 29:
            arity = 1;
            s = "processingInstructions";
            break;
         case 30:
            arity = 1;
            s = "propertyIsEnumerable";
            break;
         case 31:
            arity = 1;
            s = "removeNamespace";
            break;
         case 32:
            arity = 2;
            s = "replace";
            break;
         case 33:
            arity = 1;
            s = "setChildren";
            break;
         case 34:
            arity = 1;
            s = "setLocalName";
            break;
         case 35:
            arity = 1;
            s = "setName";
            break;
         case 36:
            arity = 1;
            s = "setNamespace";
            break;
         case 37:
            arity = 0;
            s = "text";
            break;
         case 38:
            arity = 0;
            s = "toString";
            break;
         case 39:
            arity = 1;
            s = "toSource";
            break;
         case 40:
            arity = 1;
            s = "toXMLString";
            break;
         case 41:
            arity = 0;
            s = "valueOf";
            break;
         default:
            throw new IllegalArgumentException(String.valueOf(id));
      }

      this.initPrototypeMethod(XMLOBJECT_TAG, id, s, arity);
   }

   private Object[] toObjectArray(Object[] typed) {
      Object[] rv = new Object[typed.length];

      for(int i = 0; i < rv.length; ++i) {
         rv[i] = typed[i];
      }

      return rv;
   }

   private void xmlMethodNotFound(Object object, String name) {
      throw ScriptRuntime.notFunctionError(object, name);
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
            XML xml = realThis.getXML();
            switch (id) {
               case 2:
                  if (xml == null) {
                     this.xmlMethodNotFound(realThis, "addNamespace");
                  }

                  Namespace ns = this.lib.castToNamespace(cx, arg(args, 0));
                  return xml.addNamespace(ns);
               case 3:
                  if (xml == null) {
                     this.xmlMethodNotFound(realThis, "appendChild");
                  }

                  return xml.appendChild(arg(args, 0));
               case 4:
                  XMLName xmlName = XMLName.create(this.lib.toNodeQName(cx, arg(args, 0), true), true, false);
                  return realThis.getMatches(xmlName);
               case 5:
                  return realThis.getMatches(XMLName.create(XmlNode.QName.create((XmlNode.Namespace)null, (String)null), true, false));
               case 6:
                  XMLName xmlName = this.lib.toXMLNameOrIndex(cx, arg(args, 0));
                  if (xmlName == null) {
                     int index = (int)ScriptRuntime.lastUint32Result(cx);
                     return realThis.child(index);
                  }

                  return realThis.child(xmlName);
               case 7:
                  if (xml == null) {
                     this.xmlMethodNotFound(realThis, "childIndex");
                  }

                  return ScriptRuntime.wrapInt(xml.childIndex());
               case 8:
                  return realThis.children();
               case 9:
                  return realThis.comments();
               case 10:
                  return ScriptRuntime.wrapBoolean(realThis.contains(arg(args, 0)));
               case 11:
                  return realThis.copy();
               case 12:
                  XmlNode.QName qname = args.length == 0 ? XmlNode.QName.create((XmlNode.Namespace)null, (String)null) : this.lib.toNodeQName(cx, args[0], false);
                  return realThis.getMatches(XMLName.create(qname, false, true));
               case 13:
                  XMLName xmlName = args.length == 0 ? XMLName.formStar() : this.lib.toXMLName(cx, args[0]);
                  return realThis.elements(xmlName);
               case 14:
                  if (xml == null) {
                     this.xmlMethodNotFound(realThis, "inScopeNamespaces");
                  }

                  return cx.newArray(scope, this.toObjectArray(xml.inScopeNamespaces()));
               case 15:
                  if (xml == null) {
                     this.xmlMethodNotFound(realThis, "insertChildAfter");
                  }

                  Object arg0 = arg(args, 0);
                  if (arg0 != null && !(arg0 instanceof XML)) {
                     return Undefined.instance;
                  }

                  return xml.insertChildAfter((XML)arg0, arg(args, 1));
               case 16:
                  if (xml == null) {
                     this.xmlMethodNotFound(realThis, "insertChildBefore");
                  }

                  Object arg0 = arg(args, 0);
                  if (arg0 != null && !(arg0 instanceof XML)) {
                     return Undefined.instance;
                  }

                  return xml.insertChildBefore((XML)arg0, arg(args, 1));
               case 17:
                  XMLName xmlName = this.lib.toXMLName(cx, arg(args, 0));
                  return ScriptRuntime.wrapBoolean(realThis.hasOwnProperty(xmlName));
               case 18:
                  return ScriptRuntime.wrapBoolean(realThis.hasComplexContent());
               case 19:
                  return ScriptRuntime.wrapBoolean(realThis.hasSimpleContent());
               case 20:
                  return ScriptRuntime.wrapInt(realThis.length());
               case 21:
                  if (xml == null) {
                     this.xmlMethodNotFound(realThis, "localName");
                  }

                  return xml.localName();
               case 22:
                  if (xml == null) {
                     this.xmlMethodNotFound(realThis, "name");
                  }

                  return xml.name();
               case 23:
                  if (xml == null) {
                     this.xmlMethodNotFound(realThis, "namespace");
                  }

                  String prefix = args.length > 0 ? ScriptRuntime.toString(args[0]) : null;
                  Namespace rv = xml.namespace(prefix);
                  if (rv == null) {
                     return Undefined.instance;
                  }

                  return rv;
               case 24:
                  if (xml == null) {
                     this.xmlMethodNotFound(realThis, "namespaceDeclarations");
                  }

                  Namespace[] array = xml.namespaceDeclarations();
                  return cx.newArray(scope, this.toObjectArray(array));
               case 25:
                  if (xml == null) {
                     this.xmlMethodNotFound(realThis, "nodeKind");
                  }

                  return xml.nodeKind();
               case 26:
                  realThis.normalize();
                  return Undefined.instance;
               case 27:
                  return realThis.parent();
               case 28:
                  if (xml == null) {
                     this.xmlMethodNotFound(realThis, "prependChild");
                  }

                  return xml.prependChild(arg(args, 0));
               case 29:
                  XMLName xmlName = args.length > 0 ? this.lib.toXMLName(cx, args[0]) : XMLName.formStar();
                  return realThis.processingInstructions(xmlName);
               case 30:
                  return ScriptRuntime.wrapBoolean(realThis.propertyIsEnumerable(arg(args, 0)));
               case 31:
                  if (xml == null) {
                     this.xmlMethodNotFound(realThis, "removeNamespace");
                  }

                  Namespace ns = this.lib.castToNamespace(cx, arg(args, 0));
                  return xml.removeNamespace(ns);
               case 32:
                  if (xml == null) {
                     this.xmlMethodNotFound(realThis, "replace");
                  }

                  XMLName xmlName = this.lib.toXMLNameOrIndex(cx, arg(args, 0));
                  Object arg1 = arg(args, 1);
                  if (xmlName == null) {
                     int index = (int)ScriptRuntime.lastUint32Result(cx);
                     return xml.replace(index, arg1);
                  }

                  return xml.replace(xmlName, arg1);
               case 33:
                  if (xml == null) {
                     this.xmlMethodNotFound(realThis, "setChildren");
                  }

                  return xml.setChildren(arg(args, 0));
               case 34:
                  if (xml == null) {
                     this.xmlMethodNotFound(realThis, "setLocalName");
                  }

                  Object arg = arg(args, 0);
                  String localName;
                  if (arg instanceof QName) {
                     localName = ((QName)arg).localName();
                  } else {
                     localName = ScriptRuntime.toString(arg);
                  }

                  xml.setLocalName(localName);
                  return Undefined.instance;
               case 35:
                  if (xml == null) {
                     this.xmlMethodNotFound(realThis, "setName");
                  }

                  Object arg = args.length != 0 ? args[0] : Undefined.instance;
                  QName qname = this.lib.constructQName(cx, arg);
                  xml.setName(qname);
                  return Undefined.instance;
               case 36:
                  if (xml == null) {
                     this.xmlMethodNotFound(realThis, "setNamespace");
                  }

                  Namespace ns = this.lib.castToNamespace(cx, arg(args, 0));
                  xml.setNamespace(ns);
                  return Undefined.instance;
               case 37:
                  return realThis.text();
               case 38:
                  return realThis.toString();
               case 39:
                  int indent = ScriptRuntime.toInt32(args, 0);
                  return realThis.toSource(indent);
               case 40:
                  return realThis.toXMLString();
               case 41:
                  return realThis.valueOf();
               default:
                  throw new IllegalArgumentException(String.valueOf(id));
            }
         }
      }
   }

   private static Object arg(Object[] args, int i) {
      return i < args.length ? args[i] : Undefined.instance;
   }

   final XML newTextElementXML(XmlNode reference, XmlNode.QName qname, String value) {
      return this.lib.newTextElementXML(reference, qname, value);
   }

   final XML newXMLFromJs(Object inputObject) {
      return this.lib.newXMLFromJs(inputObject);
   }

   final XML ecmaToXml(Object object) {
      return this.lib.ecmaToXml(object);
   }

   final String ecmaEscapeAttributeValue(String s) {
      String quoted = this.lib.escapeAttributeValue(s);
      return quoted.substring(1, quoted.length() - 1);
   }

   final XML createEmptyXML() {
      return this.newXML(XmlNode.createEmpty(this.getProcessor()));
   }
}
