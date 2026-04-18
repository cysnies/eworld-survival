package org.mozilla.javascript.xml.impl.xmlbeans;

import java.io.Serializable;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.Kit;
import org.mozilla.javascript.Ref;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.xml.XMLLib;
import org.mozilla.javascript.xml.XMLObject;

public final class XMLLibImpl extends XMLLib implements Serializable {
   private static final long serialVersionUID = 1L;
   private Scriptable globalScope;
   XML xmlPrototype;
   XMLList xmlListPrototype;
   Namespace namespacePrototype;
   QName qnamePrototype;
   boolean ignoreComments;
   boolean ignoreProcessingInstructions;
   boolean ignoreWhitespace;
   boolean prettyPrinting;
   int prettyIndent;

   Scriptable globalScope() {
      return this.globalScope;
   }

   private XMLLibImpl(Scriptable globalScope) {
      super();
      this.globalScope = globalScope;
      this.defaultSettings();
   }

   public static void init(Context cx, Scriptable scope, boolean sealed) {
      XmlObject.class.getName();
      XMLLibImpl lib = new XMLLibImpl(scope);
      XMLLib bound = lib.bindToScope(scope);
      if (bound == lib) {
         lib.exportToScope(sealed);
      }

   }

   private void exportToScope(boolean sealed) {
      this.xmlPrototype = XML.createEmptyXML(this);
      this.xmlListPrototype = new XMLList(this);
      this.namespacePrototype = new Namespace(this, "", "");
      this.qnamePrototype = new QName(this, "", "", "");
      this.xmlPrototype.exportAsJSClass(sealed);
      this.xmlListPrototype.exportAsJSClass(sealed);
      this.namespacePrototype.exportAsJSClass(sealed);
      this.qnamePrototype.exportAsJSClass(sealed);
   }

   void defaultSettings() {
      this.ignoreComments = true;
      this.ignoreProcessingInstructions = true;
      this.ignoreWhitespace = true;
      this.prettyPrinting = true;
      this.prettyIndent = 2;
   }

   XMLName toAttributeName(Context cx, Object nameValue) {
      String uri;
      String localName;
      if (nameValue instanceof String) {
         uri = "";
         localName = (String)nameValue;
      } else {
         if (nameValue instanceof XMLName) {
            XMLName xmlName = (XMLName)nameValue;
            if (!xmlName.isAttributeName()) {
               xmlName.setAttributeName();
            }

            return xmlName;
         }

         if (nameValue instanceof QName) {
            QName qname = (QName)nameValue;
            uri = qname.uri();
            localName = qname.localName();
         } else {
            if (nameValue instanceof Boolean || nameValue instanceof Number || nameValue == Undefined.instance || nameValue == null) {
               throw badXMLName(nameValue);
            }

            uri = "";
            localName = ScriptRuntime.toString(nameValue);
         }
      }

      XMLName xmlName = XMLName.formProperty(uri, localName);
      xmlName.setAttributeName();
      return xmlName;
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

   XMLName toXMLNameFromString(Context cx, String name) {
      if (name == null) {
         throw new IllegalArgumentException();
      } else {
         int l = name.length();
         if (l != 0) {
            char firstChar = name.charAt(0);
            if (firstChar == '*') {
               if (l == 1) {
                  return XMLName.formStar();
               }
            } else if (firstChar == '@') {
               XMLName xmlName = XMLName.formProperty("", name.substring(1));
               xmlName.setAttributeName();
               return xmlName;
            }
         }

         String uri = this.getDefaultNamespaceURI(cx);
         return XMLName.formProperty(uri, name);
      }
   }

   Namespace constructNamespace(Context cx, Object uriValue) {
      String prefix;
      String uri;
      if (uriValue instanceof Namespace) {
         Namespace ns = (Namespace)uriValue;
         prefix = ns.prefix();
         uri = ns.uri();
      } else if (uriValue instanceof QName) {
         QName qname = (QName)uriValue;
         uri = qname.uri();
         if (uri != null) {
            prefix = qname.prefix();
         } else {
            uri = qname.toString();
            prefix = null;
         }
      } else {
         uri = ScriptRuntime.toString(uriValue);
         prefix = uri.length() == 0 ? "" : null;
      }

      return new Namespace(this, prefix, uri);
   }

   Namespace castToNamespace(Context cx, Object namescapeObj) {
      return namescapeObj instanceof Namespace ? (Namespace)namescapeObj : this.constructNamespace(cx, namescapeObj);
   }

   Namespace constructNamespace(Context cx) {
      return new Namespace(this, "", "");
   }

   public Namespace constructNamespace(Context cx, Object prefixValue, Object uriValue) {
      String uri;
      if (uriValue instanceof QName) {
         QName qname = (QName)uriValue;
         uri = qname.uri();
         if (uri == null) {
            uri = qname.toString();
         }
      } else {
         uri = ScriptRuntime.toString(uriValue);
      }

      String prefix;
      if (uri.length() == 0) {
         if (prefixValue == Undefined.instance) {
            prefix = "";
         } else {
            prefix = ScriptRuntime.toString(prefixValue);
            if (prefix.length() != 0) {
               throw ScriptRuntime.typeError("Illegal prefix '" + prefix + "' for 'no namespace'.");
            }
         }
      } else if (prefixValue == Undefined.instance) {
         prefix = "";
      } else if (!this.isXMLName(cx, prefixValue)) {
         prefix = "";
      } else {
         prefix = ScriptRuntime.toString(prefixValue);
      }

      return new Namespace(this, prefix, uri);
   }

   String getDefaultNamespaceURI(Context cx) {
      String uri = "";
      if (cx == null) {
         cx = Context.getCurrentContext();
      }

      if (cx != null) {
         Object ns = ScriptRuntime.searchDefaultNamespace(cx);
         if (ns != null && ns instanceof Namespace) {
            uri = ((Namespace)ns).uri();
         }
      }

      return uri;
   }

   Namespace getDefaultNamespace(Context cx) {
      if (cx == null) {
         cx = Context.getCurrentContext();
         if (cx == null) {
            return this.namespacePrototype;
         }
      }

      Object ns = ScriptRuntime.searchDefaultNamespace(cx);
      Namespace result;
      if (ns == null) {
         result = this.namespacePrototype;
      } else if (ns instanceof Namespace) {
         result = (Namespace)ns;
      } else {
         result = this.namespacePrototype;
      }

      return result;
   }

   QName castToQName(Context cx, Object qnameValue) {
      return qnameValue instanceof QName ? (QName)qnameValue : this.constructQName(cx, qnameValue);
   }

   QName constructQName(Context cx, Object nameValue) {
      QName result;
      if (nameValue instanceof QName) {
         QName qname = (QName)nameValue;
         result = new QName(this, qname.uri(), qname.localName(), qname.prefix());
      } else {
         String localName = ScriptRuntime.toString(nameValue);
         result = this.constructQNameFromString(cx, localName);
      }

      return result;
   }

   QName constructQNameFromString(Context cx, String localName) {
      if (localName == null) {
         throw new IllegalArgumentException();
      } else {
         String uri;
         String prefix;
         if ("*".equals(localName)) {
            uri = null;
            prefix = null;
         } else {
            Namespace ns = this.getDefaultNamespace(cx);
            uri = ns.uri();
            prefix = ns.prefix();
         }

         return new QName(this, uri, localName, prefix);
      }
   }

   QName constructQName(Context cx, Object namespaceValue, Object nameValue) {
      String localName;
      if (nameValue instanceof QName) {
         QName qname = (QName)nameValue;
         localName = qname.localName();
      } else {
         localName = ScriptRuntime.toString(nameValue);
      }

      Namespace ns;
      if (namespaceValue == Undefined.instance) {
         if ("*".equals(localName)) {
            ns = null;
         } else {
            ns = this.getDefaultNamespace(cx);
         }
      } else if (namespaceValue == null) {
         ns = null;
      } else if (namespaceValue instanceof Namespace) {
         ns = (Namespace)namespaceValue;
      } else {
         ns = this.constructNamespace(cx, namespaceValue);
      }

      String uri;
      String prefix;
      if (ns == null) {
         uri = null;
         prefix = null;
      } else {
         uri = ns.uri();
         prefix = ns.prefix();
      }

      return new QName(this, uri, localName, prefix);
   }

   Object addXMLObjects(Context cx, XMLObject obj1, XMLObject obj2) {
      XMLList listToAdd = new XMLList(this);
      if (obj1 instanceof XMLList) {
         XMLList list1 = (XMLList)obj1;
         if (list1.length() == 1) {
            listToAdd.addToList(list1.item(0));
         } else {
            listToAdd = new XMLList(this, obj1);
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

   public boolean isXMLName(Context cx, Object nameObj) {
      String name;
      try {
         name = ScriptRuntime.toString(nameObj);
      } catch (EcmaError ee) {
         if ("TypeError".equals(ee.getName())) {
            return false;
         }

         throw ee;
      }

      int length = name.length();
      if (length != 0 && isNCNameStartChar(name.charAt(0))) {
         for(int i = 1; i != length; ++i) {
            if (!isNCNameChar(name.charAt(i))) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean isNCNameStartChar(int c) {
      if ((c & -128) == 0) {
         if (c >= 97) {
            return c <= 122;
         }

         if (c >= 65) {
            if (c <= 90) {
               return true;
            }

            return c == 95;
         }
      } else if ((c & -8192) == 0) {
         return 192 <= c && c <= 214 || 216 <= c && c <= 246 || 248 <= c && c <= 767 || 880 <= c && c <= 893 || 895 <= c;
      }

      return 8204 <= c && c <= 8205 || 8304 <= c && c <= 8591 || 11264 <= c && c <= 12271 || 12289 <= c && c <= 55295 || 63744 <= c && c <= 64975 || 65008 <= c && c <= 65533 || 65536 <= c && c <= 983039;
   }

   private static boolean isNCNameChar(int c) {
      if ((c & -128) == 0) {
         if (c >= 97) {
            return c <= 122;
         } else if (c >= 65) {
            if (c <= 90) {
               return true;
            } else {
               return c == 95;
            }
         } else if (c >= 48) {
            return c <= 57;
         } else {
            return c == 45 || c == 46;
         }
      } else if ((c & -8192) == 0) {
         return isNCNameStartChar(c) || c == 183 || 768 <= c && c <= 879;
      } else {
         return isNCNameStartChar(c) || 8255 <= c && c <= 8256;
      }
   }

   XMLName toQualifiedName(Context cx, Object namespaceValue, Object nameValue) {
      String localName;
      if (nameValue instanceof QName) {
         QName qname = (QName)nameValue;
         localName = qname.localName();
      } else {
         localName = ScriptRuntime.toString(nameValue);
      }

      Namespace ns;
      if (namespaceValue == Undefined.instance) {
         if ("*".equals(localName)) {
            ns = null;
         } else {
            ns = this.getDefaultNamespace(cx);
         }
      } else if (namespaceValue == null) {
         ns = null;
      } else if (namespaceValue instanceof Namespace) {
         ns = (Namespace)namespaceValue;
      } else {
         ns = this.constructNamespace(cx, namespaceValue);
      }

      String uri;
      if (ns == null) {
         uri = null;
      } else {
         uri = ns.uri();
      }

      return XMLName.formProperty(uri, localName);
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
      XMLName xmlName = this.toQualifiedName(cx, namespace, name);
      if ((memberTypeFlags & 2) != 0 && !xmlName.isAttributeName()) {
         xmlName.setAttributeName();
      }

      return this.xmlPrimaryReference(cx, xmlName, scope);
   }

   private Ref xmlPrimaryReference(Context cx, XMLName xmlName, Scriptable scope) {
      XMLObjectImpl firstXmlObject = null;

      XMLObjectImpl xmlObj;
      while(true) {
         if (scope instanceof XMLWithScope) {
            xmlObj = (XMLObjectImpl)scope.getPrototype();
            if (xmlObj.hasXMLProperty(xmlName)) {
               break;
            }

            if (firstXmlObject == null) {
               firstXmlObject = xmlObj;
            }
         }

         scope = scope.getParentScope();
         if (scope == null) {
            xmlObj = firstXmlObject;
            break;
         }
      }

      if (xmlObj != null) {
         xmlName.initXMLObject(xmlObj);
      }

      return xmlName;
   }

   public String escapeAttributeValue(Object value) {
      String text = ScriptRuntime.toString(value);
      if (text.length() == 0) {
         return "";
      } else {
         XmlObject xo = org.apache.xmlbeans.XmlObject.Factory.newInstance();
         XmlCursor cursor = xo.newCursor();
         cursor.toNextToken();
         cursor.beginElement("a");
         cursor.insertAttributeWithValue("a", text);
         cursor.dispose();
         String elementText = xo.toString();
         int begin = elementText.indexOf(34);
         int end = elementText.lastIndexOf(34);
         return elementText.substring(begin + 1, end);
      }
   }

   public String escapeTextValue(Object value) {
      if (value instanceof XMLObjectImpl) {
         return ((XMLObjectImpl)value).toXMLString(0);
      } else {
         String text = ScriptRuntime.toString(value);
         if (text.length() == 0) {
            return text;
         } else {
            XmlObject xo = org.apache.xmlbeans.XmlObject.Factory.newInstance();
            XmlCursor cursor = xo.newCursor();
            cursor.toNextToken();
            cursor.beginElement("a");
            cursor.insertChars(text);
            cursor.dispose();
            String elementText = xo.toString();
            int begin = elementText.indexOf(62) + 1;
            int end = elementText.lastIndexOf(60);
            return begin < end ? elementText.substring(begin, end) : "";
         }
      }
   }

   public Object toDefaultXmlNamespace(Context cx, Object uriValue) {
      return this.constructNamespace(cx, uriValue);
   }
}
