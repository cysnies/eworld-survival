package org.mozilla.javascript.xmlimpl;

import java.io.Serializable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Kit;
import org.mozilla.javascript.Ref;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.xml.XMLLib;
import org.mozilla.javascript.xml.XMLObject;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public final class XMLLibImpl extends XMLLib implements Serializable {
   private static final long serialVersionUID = 1L;
   private Scriptable globalScope;
   private XML xmlPrototype;
   private XMLList xmlListPrototype;
   private Namespace namespacePrototype;
   private QName qnamePrototype;
   private XmlProcessor options = new XmlProcessor();

   public static Node toDomNode(Object xmlObject) {
      if (xmlObject instanceof XML) {
         return ((XML)xmlObject).toDomNode();
      } else {
         throw new IllegalArgumentException("xmlObject is not an XML object in JavaScript.");
      }
   }

   public static void init(Context cx, Scriptable scope, boolean sealed) {
      XMLLibImpl lib = new XMLLibImpl(scope);
      XMLLib bound = lib.bindToScope(scope);
      if (bound == lib) {
         lib.exportToScope(sealed);
      }

   }

   public void setIgnoreComments(boolean b) {
      this.options.setIgnoreComments(b);
   }

   public void setIgnoreWhitespace(boolean b) {
      this.options.setIgnoreWhitespace(b);
   }

   public void setIgnoreProcessingInstructions(boolean b) {
      this.options.setIgnoreProcessingInstructions(b);
   }

   public void setPrettyPrinting(boolean b) {
      this.options.setPrettyPrinting(b);
   }

   public void setPrettyIndent(int i) {
      this.options.setPrettyIndent(i);
   }

   public boolean isIgnoreComments() {
      return this.options.isIgnoreComments();
   }

   public boolean isIgnoreProcessingInstructions() {
      return this.options.isIgnoreProcessingInstructions();
   }

   public boolean isIgnoreWhitespace() {
      return this.options.isIgnoreWhitespace();
   }

   public boolean isPrettyPrinting() {
      return this.options.isPrettyPrinting();
   }

   public int getPrettyIndent() {
      return this.options.getPrettyIndent();
   }

   private XMLLibImpl(Scriptable globalScope) {
      super();
      this.globalScope = globalScope;
   }

   /** @deprecated */
   QName qnamePrototype() {
      return this.qnamePrototype;
   }

   /** @deprecated */
   Scriptable globalScope() {
      return this.globalScope;
   }

   XmlProcessor getProcessor() {
      return this.options;
   }

   private void exportToScope(boolean sealed) {
      this.xmlPrototype = this.newXML(XmlNode.createText(this.options, ""));
      this.xmlListPrototype = this.newXMLList();
      this.namespacePrototype = Namespace.create(this.globalScope, (Namespace)null, XmlNode.Namespace.GLOBAL);
      this.qnamePrototype = QName.create(this, this.globalScope, (QName)null, XmlNode.QName.create(XmlNode.Namespace.create(""), ""));
      this.xmlPrototype.exportAsJSClass(sealed);
      this.xmlListPrototype.exportAsJSClass(sealed);
      this.namespacePrototype.exportAsJSClass(sealed);
      this.qnamePrototype.exportAsJSClass(sealed);
   }

   /** @deprecated */
   XMLName toAttributeName(Context cx, Object nameValue) {
      if (nameValue instanceof XMLName) {
         return (XMLName)nameValue;
      } else if (nameValue instanceof QName) {
         return XMLName.create(((QName)nameValue).getDelegate(), true, false);
      } else if (!(nameValue instanceof Boolean) && !(nameValue instanceof Number) && nameValue != Undefined.instance && nameValue != null) {
         String localName = null;
         if (nameValue instanceof String) {
            localName = (String)nameValue;
         } else {
            localName = ScriptRuntime.toString(nameValue);
         }

         if (localName != null && localName.equals("*")) {
            localName = null;
         }

         return XMLName.create(XmlNode.QName.create(XmlNode.Namespace.create(""), localName), true, false);
      } else {
         throw badXMLName(nameValue);
      }
   }

   private static RuntimeException badXMLName(Object value) {
      String msg;
      if (value instanceof Number) {
         msg = "Can not construct XML name from number: ";
      } else if (value instanceof Boolean) {
         msg = "Can not construct XML name from boolean: ";
      } else {
         if (value != Undefined.instance && value != null) {
            throw new IllegalArgumentException(value.toString());
         }

         msg = "Can not construct XML name from ";
      }

      return ScriptRuntime.typeError(msg + ScriptRuntime.toString(value));
   }

   XMLName toXMLNameFromString(Context cx, String name) {
      return XMLName.create(this.getDefaultNamespaceURI(cx), name);
   }

   XMLName toXMLName(Context cx, Object nameValue) {
      XMLName result;
      if (nameValue instanceof XMLName) {
         result = (XMLName)nameValue;
      } else if (nameValue instanceof QName) {
         QName qname = (QName)nameValue;
         result = XMLName.formProperty(qname.uri(), qname.localName());
      } else if (nameValue instanceof String) {
         result = this.toXMLNameFromString(cx, (String)nameValue);
      } else {
         if (nameValue instanceof Boolean || nameValue instanceof Number || nameValue == Undefined.instance || nameValue == null) {
            throw badXMLName(nameValue);
         }

         String name = ScriptRuntime.toString(nameValue);
         result = this.toXMLNameFromString(cx, name);
      }

      return result;
   }

   XMLName toXMLNameOrIndex(Context cx, Object value) {
      XMLName result;
      if (value instanceof XMLName) {
         result = (XMLName)value;
      } else if (value instanceof String) {
         String str = (String)value;
         long test = ScriptRuntime.testUint32String(str);
         if (test >= 0L) {
            ScriptRuntime.storeUint32Result(cx, test);
            result = null;
         } else {
            result = this.toXMLNameFromString(cx, str);
         }
      } else if (value instanceof Number) {
         double d = ((Number)value).doubleValue();
         long l = (long)d;
         if ((double)l != d || 0L > l || l > 4294967295L) {
            throw badXMLName(value);
         }

         ScriptRuntime.storeUint32Result(cx, l);
         result = null;
      } else if (value instanceof QName) {
         QName qname = (QName)value;
         String uri = qname.uri();
         boolean number = false;
         result = null;
         if (uri != null && uri.length() == 0) {
            long test = ScriptRuntime.testUint32String(uri);
            if (test >= 0L) {
               ScriptRuntime.storeUint32Result(cx, test);
               number = true;
            }
         }

         if (!number) {
            result = XMLName.formProperty(uri, qname.localName());
         }
      } else {
         if (value instanceof Boolean || value == Undefined.instance || value == null) {
            throw badXMLName(value);
         }

         String str = ScriptRuntime.toString(value);
         long test = ScriptRuntime.testUint32String(str);
         if (test >= 0L) {
            ScriptRuntime.storeUint32Result(cx, test);
            result = null;
         } else {
            result = this.toXMLNameFromString(cx, str);
         }
      }

      return result;
   }

   Object addXMLObjects(Context cx, XMLObject obj1, XMLObject obj2) {
      XMLList listToAdd = this.newXMLList();
      if (obj1 instanceof XMLList) {
         XMLList list1 = (XMLList)obj1;
         if (list1.length() == 1) {
            listToAdd.addToList(list1.item(0));
         } else {
            listToAdd = this.newXMLListFrom(obj1);
         }
      } else {
         listToAdd.addToList(obj1);
      }

      if (obj2 instanceof XMLList) {
         XMLList list2 = (XMLList)obj2;

         for(int i = 0; i < list2.length(); ++i) {
            listToAdd.addToList(list2.item(i));
         }
      } else if (obj2 instanceof XML) {
         listToAdd.addToList(obj2);
      }

      return listToAdd;
   }

   private Ref xmlPrimaryReference(Context cx, XMLName xmlName, Scriptable scope) {
      XMLObjectImpl firstXml = null;

      XMLObjectImpl xmlObj;
      while(true) {
         if (scope instanceof XMLWithScope) {
            xmlObj = (XMLObjectImpl)scope.getPrototype();
            if (xmlObj.hasXMLProperty(xmlName)) {
               break;
            }

            if (firstXml == null) {
               firstXml = xmlObj;
            }
         }

         scope = scope.getParentScope();
         if (scope == null) {
            xmlObj = firstXml;
            break;
         }
      }

      if (xmlObj != null) {
         xmlName.initXMLObject(xmlObj);
      }

      return xmlName;
   }

   Namespace castToNamespace(Context cx, Object namespaceObj) {
      return this.namespacePrototype.castToNamespace(namespaceObj);
   }

   private String getDefaultNamespaceURI(Context cx) {
      return this.getDefaultNamespace(cx).uri();
   }

   Namespace newNamespace(String uri) {
      return this.namespacePrototype.newNamespace(uri);
   }

   Namespace getDefaultNamespace(Context cx) {
      if (cx == null) {
         cx = Context.getCurrentContext();
         if (cx == null) {
            return this.namespacePrototype;
         }
      }

      Object ns = ScriptRuntime.searchDefaultNamespace(cx);
      if (ns == null) {
         return this.namespacePrototype;
      } else {
         return ns instanceof Namespace ? (Namespace)ns : this.namespacePrototype;
      }
   }

   Namespace[] createNamespaces(XmlNode.Namespace[] declarations) {
      Namespace[] rv = new Namespace[declarations.length];

      for(int i = 0; i < declarations.length; ++i) {
         rv[i] = this.namespacePrototype.newNamespace(declarations[i].getPrefix(), declarations[i].getUri());
      }

      return rv;
   }

   QName constructQName(Context cx, Object namespace, Object name) {
      return this.qnamePrototype.constructQName(this, cx, namespace, name);
   }

   QName newQName(String uri, String localName, String prefix) {
      return this.qnamePrototype.newQName(this, uri, localName, prefix);
   }

   QName constructQName(Context cx, Object nameValue) {
      return this.qnamePrototype.constructQName(this, cx, nameValue);
   }

   QName castToQName(Context cx, Object qnameValue) {
      return this.qnamePrototype.castToQName(this, cx, qnameValue);
   }

   QName newQName(XmlNode.QName qname) {
      return QName.create(this, this.globalScope, this.qnamePrototype, qname);
   }

   XML newXML(XmlNode node) {
      return new XML(this, this.globalScope, this.xmlPrototype, node);
   }

   final XML newXMLFromJs(Object inputObject) {
      String frag;
      if (inputObject != null && inputObject != Undefined.instance) {
         if (inputObject instanceof XMLObjectImpl) {
            frag = ((XMLObjectImpl)inputObject).toXMLString();
         } else {
            frag = ScriptRuntime.toString(inputObject);
         }
      } else {
         frag = "";
      }

      if (frag.trim().startsWith("<>")) {
         throw ScriptRuntime.typeError("Invalid use of XML object anonymous tags <></>.");
      } else {
         return frag.indexOf("<") == -1 ? this.newXML(XmlNode.createText(this.options, frag)) : this.parse(frag);
      }
   }

   private XML parse(String frag) {
      try {
         return this.newXML(XmlNode.createElement(this.options, this.getDefaultNamespaceURI(Context.getCurrentContext()), frag));
      } catch (SAXException e) {
         throw ScriptRuntime.typeError("Cannot parse XML: " + e.getMessage());
      }
   }

   final XML ecmaToXml(Object object) {
      if (object != null && object != Undefined.instance) {
         if (object instanceof XML) {
            return (XML)object;
         } else if (object instanceof XMLList) {
            XMLList list = (XMLList)object;
            if (list.getXML() != null) {
               return list.getXML();
            } else {
               throw ScriptRuntime.typeError("Cannot convert list of >1 element to XML");
            }
         } else {
            if (object instanceof Wrapper) {
               object = ((Wrapper)object).unwrap();
            }

            if (object instanceof Node) {
               Node node = (Node)object;
               return this.newXML(XmlNode.createElementFromNode(node));
            } else {
               String s = ScriptRuntime.toString(object);
               return s.length() > 0 && s.charAt(0) == '<' ? this.parse(s) : this.newXML(XmlNode.createText(this.options, s));
            }
         }
      } else {
         throw ScriptRuntime.typeError("Cannot convert " + object + " to XML");
      }
   }

   final XML newTextElementXML(XmlNode reference, XmlNode.QName qname, String value) {
      return this.newXML(XmlNode.newElementWithText(this.options, reference, qname, value));
   }

   XMLList newXMLList() {
      return new XMLList(this, this.globalScope, this.xmlListPrototype);
   }

   final XMLList newXMLListFrom(Object inputObject) {
      XMLList rv = this.newXMLList();
      if (inputObject != null && !(inputObject instanceof Undefined)) {
         if (inputObject instanceof XML) {
            XML xml = (XML)inputObject;
            rv.getNodeList().add(xml);
            return rv;
         } else if (inputObject instanceof XMLList) {
            XMLList xmll = (XMLList)inputObject;
            rv.getNodeList().add(xmll.getNodeList());
            return rv;
         } else {
            String frag = ScriptRuntime.toString(inputObject).trim();
            if (!frag.startsWith("<>")) {
               frag = "<>" + frag + "</>";
            }

            frag = "<fragment>" + frag.substring(2);
            if (!frag.endsWith("</>")) {
               throw ScriptRuntime.typeError("XML with anonymous tag missing end anonymous tag");
            } else {
               frag = frag.substring(0, frag.length() - 3) + "</fragment>";
               XML orgXML = this.newXMLFromJs(frag);
               XMLList children = orgXML.children();

               for(int i = 0; i < children.getNodeList().length(); ++i) {
                  rv.getNodeList().add((XML)children.item(i).copy());
               }

               return rv;
            }
         }
      } else {
         return rv;
      }
   }

   XmlNode.QName toNodeQName(Context cx, Object namespaceValue, Object nameValue) {
      String localName;
      if (nameValue instanceof QName) {
         QName qname = (QName)nameValue;
         localName = qname.localName();
      } else {
         localName = ScriptRuntime.toString(nameValue);
      }

      XmlNode.Namespace ns;
      if (namespaceValue == Undefined.instance) {
         if ("*".equals(localName)) {
            ns = null;
         } else {
            ns = this.getDefaultNamespace(cx).getDelegate();
         }
      } else if (namespaceValue == null) {
         ns = null;
      } else if (namespaceValue instanceof Namespace) {
         ns = ((Namespace)namespaceValue).getDelegate();
      } else {
         ns = this.namespacePrototype.constructNamespace(namespaceValue).getDelegate();
      }

      if (localName != null && localName.equals("*")) {
         localName = null;
      }

      return XmlNode.QName.create(ns, localName);
   }

   XmlNode.QName toNodeQName(Context cx, String name, boolean attribute) {
      XmlNode.Namespace defaultNamespace = this.getDefaultNamespace(cx).getDelegate();
      if (name != null && name.equals("*")) {
         return XmlNode.QName.create((XmlNode.Namespace)null, (String)null);
      } else {
         return attribute ? XmlNode.QName.create(XmlNode.Namespace.GLOBAL, name) : XmlNode.QName.create(defaultNamespace, name);
      }
   }

   XmlNode.QName toNodeQName(Context cx, Object nameValue, boolean attribute) {
      if (nameValue instanceof XMLName) {
         return ((XMLName)nameValue).toQname();
      } else if (nameValue instanceof QName) {
         QName qname = (QName)nameValue;
         return qname.getDelegate();
      } else if (!(nameValue instanceof Boolean) && !(nameValue instanceof Number) && nameValue != Undefined.instance && nameValue != null) {
         String local = null;
         if (nameValue instanceof String) {
            local = (String)nameValue;
         } else {
            local = ScriptRuntime.toString(nameValue);
         }

         return this.toNodeQName(cx, local, attribute);
      } else {
         throw badXMLName(nameValue);
      }
   }

   public boolean isXMLName(Context _cx, Object nameObj) {
      return XMLName.accept(nameObj);
   }

   public Object toDefaultXmlNamespace(Context cx, Object uriValue) {
      return this.namespacePrototype.constructNamespace(uriValue);
   }

   public String escapeTextValue(Object o) {
      return this.options.escapeTextValue(o);
   }

   public String escapeAttributeValue(Object o) {
      return this.options.escapeAttributeValue(o);
   }

   public Ref nameRef(Context cx, Object name, Scriptable scope, int memberTypeFlags) {
      if ((memberTypeFlags & 2) == 0) {
         throw Kit.codeBug();
      } else {
         XMLName xmlName = this.toAttributeName(cx, name);
         return this.xmlPrimaryReference(cx, xmlName, scope);
      }
   }

   public Ref nameRef(Context cx, Object namespace, Object name, Scriptable scope, int memberTypeFlags) {
      XMLName xmlName = XMLName.create(this.toNodeQName(cx, namespace, name), false, false);
      if ((memberTypeFlags & 2) != 0 && !xmlName.isAttributeName()) {
         xmlName.setAttributeName();
      }

      return this.xmlPrimaryReference(cx, xmlName, scope);
   }
}
