package org.mozilla.javascript.xml.impl.xmlbeans;

import java.util.Vector;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.xml.XMLObject;

class XMLList extends XMLObjectImpl implements Function {
   static final long serialVersionUID = -4543618751670781135L;
   private AnnotationList _annos;
   private XMLObjectImpl targetObject = null;
   private javax.xml.namespace.QName targetProperty = null;

   XMLList(XMLLibImpl lib) {
      super(lib, lib.xmlListPrototype);
      this._annos = new AnnotationList();
   }

   XMLList(XMLLibImpl lib, Object inputObject) {
      super(lib, lib.xmlListPrototype);
      if (inputObject != null && !(inputObject instanceof Undefined)) {
         if (inputObject instanceof XML) {
            XML xml = (XML)inputObject;
            this._annos = new AnnotationList();
            this._annos.add(xml.getAnnotation());
         } else if (inputObject instanceof XMLList) {
            XMLList xmll = (XMLList)inputObject;
            this._annos = new AnnotationList();

            for(int i = 0; i < xmll._annos.length(); ++i) {
               this._annos.add(xmll._annos.item(i));
            }
         } else {
            String frag = ScriptRuntime.toString(inputObject).trim();
            if (!frag.startsWith("<>")) {
               frag = "<>" + frag + "</>";
            }

            frag = "<fragment>" + frag.substring(2);
            if (!frag.endsWith("</>")) {
               throw ScriptRuntime.typeError("XML with anonymous tag missing end anonymous tag");
            }

            frag = frag.substring(0, frag.length() - 3) + "</fragment>";
            XML orgXML = XML.createFromJS(lib, frag);
            XMLList children = orgXML.children();
            this._annos = new AnnotationList();

            for(int i = 0; i < children._annos.length(); ++i) {
               this._annos.add(((XML)children.item(i).copy()).getAnnotation());
            }
         }
      } else {
         String frag = "";
      }

   }

   void setTargets(XMLObjectImpl object, javax.xml.namespace.QName property) {
      this.targetObject = object;
      this.targetProperty = property;
   }

   XML getXmlFromAnnotation(int index) {
      XML retVal;
      if (index >= 0 && index < this.length()) {
         XML.XScriptAnnotation anno = this._annos.item(index);
         retVal = XML.getFromAnnotation(this.lib, anno);
      } else {
         retVal = null;
      }

      return retVal;
   }

   private void internalRemoveFromList(int index) {
      this._annos.remove(index);
   }

   void replace(int index, XML xml) {
      if (index < this.length()) {
         AnnotationList newAnnoList = new AnnotationList();

         for(int i = 0; i < index; ++i) {
            newAnnoList.add(this._annos.item(i));
         }

         newAnnoList.add(xml.getAnnotation());

         for(int i = index + 1; i < this.length(); ++i) {
            newAnnoList.add(this._annos.item(i));
         }

         this._annos = newAnnoList;
      }

   }

   private void insert(int index, XML xml) {
      if (index < this.length()) {
         AnnotationList newAnnoList = new AnnotationList();

         for(int i = 0; i < index; ++i) {
            newAnnoList.add(this._annos.item(i));
         }

         newAnnoList.add(xml.getAnnotation());

         for(int i = index; i < this.length(); ++i) {
            newAnnoList.add(this._annos.item(i));
         }

         this._annos = newAnnoList;
      }

   }

   public String getClassName() {
      return "XMLList";
   }

   public Object get(int index, Scriptable start) {
      return index >= 0 && index < this.length() ? this.getXmlFromAnnotation(index) : Scriptable.NOT_FOUND;
   }

   boolean hasXMLProperty(XMLName xmlName) {
      return this.getPropertyList(xmlName).length() > 0;
   }

   public boolean has(int index, Scriptable start) {
      return 0 <= index && index < this.length();
   }

   void putXMLProperty(XMLName xmlName, Object value) {
      if (value == null) {
         value = "null";
      } else if (value instanceof Undefined) {
         value = "undefined";
      }

      if (this.length() > 1) {
         throw ScriptRuntime.typeError("Assignment to lists with more that one item is not supported");
      } else {
         if (this.length() == 0) {
            if (this.targetObject == null || this.targetProperty == null || this.targetProperty.getLocalPart().equals("*")) {
               throw ScriptRuntime.typeError("Assignment to empty XMLList without targets not supported");
            }

            XML xmlValue = XML.createTextElement(this.lib, this.targetProperty, "");
            this.addToList(xmlValue);
            if (xmlName.isAttributeName()) {
               this.setAttribute(xmlName, value);
            } else {
               XML xml = this.item(0);
               xml.putXMLProperty(xmlName, value);
               this.replace(0, this.item(0));
            }

            XMLName name2 = XMLName.formProperty(this.targetProperty.getNamespaceURI(), this.targetProperty.getLocalPart());
            this.targetObject.putXMLProperty(name2, this);
         } else if (xmlName.isAttributeName()) {
            this.setAttribute(xmlName, value);
         } else {
            XML xml = this.item(0);
            xml.putXMLProperty(xmlName, value);
            this.replace(0, this.item(0));
         }

      }
   }

   Object getXMLProperty(XMLName name) {
      return this.getPropertyList(name);
   }

   public void put(int index, Scriptable start, Object value) {
      Object parent = Undefined.instance;
      if (value == null) {
         value = "null";
      } else if (value instanceof Undefined) {
         value = "undefined";
      }

      XMLObject xmlValue;
      if (value instanceof XMLObject) {
         xmlValue = (XMLObject)value;
      } else if (this.targetProperty == null) {
         xmlValue = XML.createFromJS(this.lib, value.toString());
      } else {
         xmlValue = XML.createTextElement(this.lib, this.targetProperty, value.toString());
      }

      if (index < this.length()) {
         parent = this.item(index).parent();
      } else {
         parent = this.parent();
      }

      if (parent instanceof XML) {
         XML xmlParent = (XML)parent;
         if (index < this.length()) {
            XML xmlNode = this.getXmlFromAnnotation(index);
            if (xmlValue instanceof XML) {
               xmlNode.replaceAll((XML)xmlValue);
               this.replace(index, xmlNode);
            } else if (xmlValue instanceof XMLList) {
               XMLList list = (XMLList)xmlValue;
               if (list.length() > 0) {
                  int lastIndexAdded = xmlNode.childIndex();
                  xmlNode.replaceAll(list.item(0));
                  this.replace(index, list.item(0));

                  for(int i = 1; i < list.length(); ++i) {
                     xmlParent.insertChildAfter(xmlParent.getXmlChild((long)lastIndexAdded), list.item(i));
                     ++lastIndexAdded;
                     this.insert(index + i, list.item(i));
                  }
               }
            }
         } else {
            xmlParent.appendChild(xmlValue);
            this.addToList(xmlParent.getXmlChild((long)index));
         }
      } else if (index < this.length()) {
         XML xmlNode = XML.getFromAnnotation(this.lib, this._annos.item(index));
         if (xmlValue instanceof XML) {
            xmlNode.replaceAll((XML)xmlValue);
            this.replace(index, xmlNode);
         } else if (xmlValue instanceof XMLList) {
            XMLList list = (XMLList)xmlValue;
            if (list.length() > 0) {
               xmlNode.replaceAll(list.item(0));
               this.replace(index, list.item(0));

               for(int i = 1; i < list.length(); ++i) {
                  this.insert(index + i, list.item(i));
               }
            }
         }
      } else {
         this.addToList(xmlValue);
      }

   }

   void deleteXMLProperty(XMLName name) {
      for(int i = 0; i < this.length(); ++i) {
         XML xml = this.getXmlFromAnnotation(i);
         if (xml.tokenType() == TokenType.START) {
            xml.deleteXMLProperty(name);
         }
      }

   }

   public void delete(int index) {
      if (index >= 0 && index < this.length()) {
         XML xml = this.getXmlFromAnnotation(index);
         xml.remove();
         this.internalRemoveFromList(index);
      }

   }

   public Object[] getIds() {
      Object[] enumObjs;
      if (this.prototypeFlag) {
         enumObjs = new Object[0];
      } else {
         enumObjs = new Object[this.length()];

         for(int i = 0; i < enumObjs.length; ++i) {
            enumObjs[i] = new Integer(i);
         }
      }

      return enumObjs;
   }

   public Object[] getIdsForDebug() {
      return this.getIds();
   }

   void remove() {
      int nLen = this.length();

      for(int i = nLen - 1; i >= 0; --i) {
         XML xml = this.getXmlFromAnnotation(i);
         if (xml != null) {
            xml.remove();
            this.internalRemoveFromList(i);
         }
      }

   }

   XML item(int index) {
      return this._annos != null ? this.getXmlFromAnnotation(index) : XML.createEmptyXML(this.lib);
   }

   private void setAttribute(XMLName xmlName, Object value) {
      for(int i = 0; i < this.length(); ++i) {
         XML xml = this.getXmlFromAnnotation(i);
         xml.setAttribute(xmlName, value);
      }

   }

   void addToList(Object toAdd) {
      if (!(toAdd instanceof Undefined)) {
         if (toAdd instanceof XMLList) {
            XMLList xmlSrc = (XMLList)toAdd;

            for(int i = 0; i < xmlSrc.length(); ++i) {
               this._annos.add(xmlSrc.item(i).getAnnotation());
            }
         } else if (toAdd instanceof XML) {
            this._annos.add(((XML)((XML)toAdd)).getAnnotation());
         } else if (toAdd instanceof XML.XScriptAnnotation) {
            this._annos.add((XML.XScriptAnnotation)toAdd);
         }

      }
   }

   XML addNamespace(Namespace ns) {
      if (this.length() == 1) {
         return this.getXmlFromAnnotation(0).addNamespace(ns);
      } else {
         throw ScriptRuntime.typeError("The addNamespace method works only on lists containing one item");
      }
   }

   XML appendChild(Object xml) {
      if (this.length() == 1) {
         return this.getXmlFromAnnotation(0).appendChild(xml);
      } else {
         throw ScriptRuntime.typeError("The appendChild method works only on lists containing one item");
      }
   }

   XMLList attribute(XMLName xmlName) {
      XMLList result = new XMLList(this.lib);

      for(int i = 0; i < this.length(); ++i) {
         XML xml = this.getXmlFromAnnotation(i);
         result.addToList(xml.attribute(xmlName));
      }

      return result;
   }

   XMLList attributes() {
      XMLList result = new XMLList(this.lib);

      for(int i = 0; i < this.length(); ++i) {
         XML xml = this.getXmlFromAnnotation(i);
         result.addToList(xml.attributes());
      }

      return result;
   }

   XMLList child(long index) {
      XMLList result = new XMLList(this.lib);

      for(int i = 0; i < this.length(); ++i) {
         result.addToList(this.getXmlFromAnnotation(i).child(index));
      }

      return result;
   }

   XMLList child(XMLName xmlName) {
      XMLList result = new XMLList(this.lib);

      for(int i = 0; i < this.length(); ++i) {
         result.addToList(this.getXmlFromAnnotation(i).child(xmlName));
      }

      return result;
   }

   int childIndex() {
      if (this.length() == 1) {
         return this.getXmlFromAnnotation(0).childIndex();
      } else {
         throw ScriptRuntime.typeError("The childIndex method works only on lists containing one item");
      }
   }

   XMLList children() {
      Vector v = new Vector();

      for(int i = 0; i < this.length(); ++i) {
         XML xml = this.getXmlFromAnnotation(i);
         if (xml != null) {
            Object o = xml.children();
            if (o instanceof XMLList) {
               XMLList childList = (XMLList)o;
               int cChildren = childList.length();

               for(int j = 0; j < cChildren; ++j) {
                  v.addElement(childList.item(j));
               }
            }
         }
      }

      XMLList allChildren = new XMLList(this.lib);
      int sz = v.size();

      for(int i = 0; i < sz; ++i) {
         allChildren.addToList(v.get(i));
      }

      return allChildren;
   }

   XMLList comments() {
      XMLList result = new XMLList(this.lib);

      for(int i = 0; i < this.length(); ++i) {
         XML xml = this.getXmlFromAnnotation(i);
         result.addToList(xml.comments());
      }

      return result;
   }

   boolean contains(Object xml) {
      boolean result = false;

      for(int i = 0; i < this.length(); ++i) {
         XML member = this.getXmlFromAnnotation(i);
         if (member.equivalentXml(xml)) {
            result = true;
            break;
         }
      }

      return result;
   }

   Object copy() {
      XMLList result = new XMLList(this.lib);

      for(int i = 0; i < this.length(); ++i) {
         XML xml = this.getXmlFromAnnotation(i);
         result.addToList(xml.copy());
      }

      return result;
   }

   XMLList descendants(XMLName xmlName) {
      XMLList result = new XMLList(this.lib);

      for(int i = 0; i < this.length(); ++i) {
         XML xml = this.getXmlFromAnnotation(i);
         result.addToList(xml.descendants(xmlName));
      }

      return result;
   }

   Object[] inScopeNamespaces() {
      if (this.length() == 1) {
         return this.getXmlFromAnnotation(0).inScopeNamespaces();
      } else {
         throw ScriptRuntime.typeError("The inScopeNamespaces method works only on lists containing one item");
      }
   }

   XML insertChildAfter(Object child, Object xml) {
      if (this.length() == 1) {
         return this.getXmlFromAnnotation(0).insertChildAfter(child, xml);
      } else {
         throw ScriptRuntime.typeError("The insertChildAfter method works only on lists containing one item");
      }
   }

   XML insertChildBefore(Object child, Object xml) {
      if (this.length() == 1) {
         return this.getXmlFromAnnotation(0).insertChildAfter(child, xml);
      } else {
         throw ScriptRuntime.typeError("The insertChildBefore method works only on lists containing one item");
      }
   }

   boolean hasOwnProperty(XMLName xmlName) {
      boolean hasProperty = false;
      if (this.prototypeFlag) {
         String property = xmlName.localName();
         hasProperty = 0 != this.findPrototypeId(property);
      } else {
         hasProperty = this.getPropertyList(xmlName).length() > 0;
      }

      return hasProperty;
   }

   boolean hasComplexContent() {
      int length = this.length();
      boolean complexContent;
      if (length == 0) {
         complexContent = false;
      } else if (length == 1) {
         complexContent = this.getXmlFromAnnotation(0).hasComplexContent();
      } else {
         complexContent = false;

         for(int i = 0; i < length; ++i) {
            XML nextElement = this.getXmlFromAnnotation(i);
            if (nextElement.tokenType() == TokenType.START) {
               complexContent = true;
               break;
            }
         }
      }

      return complexContent;
   }

   boolean hasSimpleContent() {
      int length = this.length();
      boolean simpleContent;
      if (length == 0) {
         simpleContent = true;
      } else if (length == 1) {
         simpleContent = this.getXmlFromAnnotation(0).hasSimpleContent();
      } else {
         simpleContent = true;

         for(int i = 0; i < length; ++i) {
            XML nextElement = this.getXmlFromAnnotation(i);
            if (nextElement.tokenType() == TokenType.START) {
               simpleContent = false;
               break;
            }
         }
      }

      return simpleContent;
   }

   int length() {
      int result = 0;
      if (this._annos != null) {
         result = this._annos.length();
      }

      return result;
   }

   String localName() {
      if (this.length() == 1) {
         return this.name().localName();
      } else {
         throw ScriptRuntime.typeError("The localName method works only on lists containing one item");
      }
   }

   QName name() {
      if (this.length() == 1) {
         return this.getXmlFromAnnotation(0).name();
      } else {
         throw ScriptRuntime.typeError("The name method works only on lists containing one item");
      }
   }

   Object namespace(String prefix) {
      if (this.length() == 1) {
         return this.getXmlFromAnnotation(0).namespace(prefix);
      } else {
         throw ScriptRuntime.typeError("The namespace method works only on lists containing one item");
      }
   }

   Object[] namespaceDeclarations() {
      if (this.length() == 1) {
         return this.getXmlFromAnnotation(0).namespaceDeclarations();
      } else {
         throw ScriptRuntime.typeError("The namespaceDeclarations method works only on lists containing one item");
      }
   }

   Object nodeKind() {
      if (this.length() == 1) {
         return this.getXmlFromAnnotation(0).nodeKind();
      } else {
         throw ScriptRuntime.typeError("The nodeKind method works only on lists containing one item");
      }
   }

   void normalize() {
      for(int i = 0; i < this.length(); ++i) {
         this.getXmlFromAnnotation(i).normalize();
      }

   }

   Object parent() {
      Object sameParent = Undefined.instance;
      if (this.length() == 0 && this.targetObject != null && this.targetObject instanceof XML) {
         sameParent = this.targetObject;
      } else {
         for(int i = 0; i < this.length(); ++i) {
            Object currParent = this.getXmlFromAnnotation(i).parent();
            if (i == 0) {
               sameParent = currParent;
            } else if (sameParent != currParent) {
               sameParent = Undefined.instance;
               break;
            }
         }
      }

      return sameParent;
   }

   XML prependChild(Object xml) {
      if (this.length() == 1) {
         return this.getXmlFromAnnotation(0).prependChild(xml);
      } else {
         throw ScriptRuntime.typeError("The prependChild method works only on lists containing one item");
      }
   }

   Object processingInstructions(XMLName xmlName) {
      XMLList result = new XMLList(this.lib);

      for(int i = 0; i < this.length(); ++i) {
         XML xml = this.getXmlFromAnnotation(i);
         result.addToList(xml.processingInstructions(xmlName));
      }

      return result;
   }

   boolean propertyIsEnumerable(Object name) {
      long index;
      if (name instanceof Integer) {
         index = (long)(Integer)name;
      } else if (name instanceof Number) {
         double x = ((Number)name).doubleValue();
         index = (long)x;
         if ((double)index != x) {
            return false;
         }

         if (index == 0L && (double)1.0F / x < (double)0.0F) {
            return false;
         }
      } else {
         String s = ScriptRuntime.toString(name);
         index = ScriptRuntime.testUint32String(s);
      }

      return 0L <= index && index < (long)this.length();
   }

   XML removeNamespace(Namespace ns) {
      if (this.length() == 1) {
         return this.getXmlFromAnnotation(0).removeNamespace(ns);
      } else {
         throw ScriptRuntime.typeError("The removeNamespace method works only on lists containing one item");
      }
   }

   XML replace(long index, Object xml) {
      if (this.length() == 1) {
         return this.getXmlFromAnnotation(0).replace(index, xml);
      } else {
         throw ScriptRuntime.typeError("The replace method works only on lists containing one item");
      }
   }

   XML replace(XMLName xmlName, Object xml) {
      if (this.length() == 1) {
         return this.getXmlFromAnnotation(0).replace(xmlName, xml);
      } else {
         throw ScriptRuntime.typeError("The replace method works only on lists containing one item");
      }
   }

   XML setChildren(Object xml) {
      if (this.length() == 1) {
         return this.getXmlFromAnnotation(0).setChildren(xml);
      } else {
         throw ScriptRuntime.typeError("The setChildren method works only on lists containing one item");
      }
   }

   void setLocalName(String localName) {
      if (this.length() == 1) {
         this.getXmlFromAnnotation(0).setLocalName(localName);
      } else {
         throw ScriptRuntime.typeError("The setLocalName method works only on lists containing one item");
      }
   }

   void setName(QName qname) {
      if (this.length() == 1) {
         this.getXmlFromAnnotation(0).setName(qname);
      } else {
         throw ScriptRuntime.typeError("The setName method works only on lists containing one item");
      }
   }

   void setNamespace(Namespace ns) {
      if (this.length() == 1) {
         this.getXmlFromAnnotation(0).setNamespace(ns);
      } else {
         throw ScriptRuntime.typeError("The setNamespace method works only on lists containing one item");
      }
   }

   XMLList text() {
      XMLList result = new XMLList(this.lib);

      for(int i = 0; i < this.length(); ++i) {
         result.addToList(this.getXmlFromAnnotation(i).text());
      }

      return result;
   }

   public String toString() {
      if (!this.hasSimpleContent()) {
         return this.toXMLString(0);
      } else {
         StringBuffer sb = new StringBuffer();

         for(int i = 0; i < this.length(); ++i) {
            XML next = this.getXmlFromAnnotation(i);
            sb.append(next.toString());
         }

         return sb.toString();
      }
   }

   String toSource(int indent) {
      return "<>" + this.toXMLString(0) + "</>";
   }

   String toXMLString(int indent) {
      StringBuffer sb = new StringBuffer();

      for(int i = 0; i < this.length(); ++i) {
         if (i > 0) {
            sb.append('\n');
         }

         sb.append(this.getXmlFromAnnotation(i).toXMLString(indent));
      }

      return sb.toString();
   }

   Object valueOf() {
      return this;
   }

   boolean equivalentXml(Object target) {
      boolean result = false;
      if (target instanceof Undefined && this.length() == 0) {
         result = true;
      } else if (this.length() == 1) {
         result = this.getXmlFromAnnotation(0).equivalentXml(target);
      } else if (target instanceof XMLList) {
         XMLList otherList = (XMLList)target;
         if (otherList.length() == this.length()) {
            result = true;

            for(int i = 0; i < this.length(); ++i) {
               if (!this.getXmlFromAnnotation(i).equivalentXml(otherList.getXmlFromAnnotation(i))) {
                  result = false;
                  break;
               }
            }
         }
      }

      return result;
   }

   private XMLList getPropertyList(XMLName name) {
      XMLList propertyList = new XMLList(this.lib);
      javax.xml.namespace.QName qname = null;
      if (!name.isDescendants() && !name.isAttributeName()) {
         qname = new javax.xml.namespace.QName(name.uri(), name.localName());
      }

      propertyList.setTargets(this, qname);

      for(int i = 0; i < this.length(); ++i) {
         propertyList.addToList(this.getXmlFromAnnotation(i).getPropertyList(name));
      }

      return propertyList;
   }

   private Object applyOrCall(boolean isApply, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      String methodName = isApply ? "apply" : "call";
      if (thisObj instanceof XMLList && ((XMLList)thisObj).targetProperty != null) {
         return ScriptRuntime.applyOrCall(isApply, cx, scope, thisObj, args);
      } else {
         throw ScriptRuntime.typeError1("msg.isnt.function", methodName);
      }
   }

   protected Object jsConstructor(Context cx, boolean inNewExpr, Object[] args) {
      if (args.length == 0) {
         return new XMLList(this.lib);
      } else {
         Object arg0 = args[0];
         return !inNewExpr && arg0 instanceof XMLList ? arg0 : new XMLList(this.lib, arg0);
      }
   }

   XmlObject getXmlObject() {
      if (this.length() == 1) {
         return this.getXmlFromAnnotation(0).getXmlObject();
      } else {
         throw ScriptRuntime.typeError("getXmlObject method works only on lists containing one item");
      }
   }

   public Scriptable getExtraMethodSource(Context cx) {
      return this.length() == 1 ? this.getXmlFromAnnotation(0) : null;
   }

   public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      if (this.targetProperty == null) {
         throw ScriptRuntime.notFunctionError(this);
      } else {
         String methodName = this.targetProperty.getLocalPart();
         boolean isApply = methodName.equals("apply");
         if (!isApply && !methodName.equals("call")) {
            if (!(thisObj instanceof XMLObject)) {
               throw ScriptRuntime.typeError1("msg.incompat.call", methodName);
            } else {
               Object func = null;
               Scriptable sobj = thisObj;

               while(sobj instanceof XMLObject) {
                  XMLObject xmlObject = (XMLObject)sobj;
                  func = xmlObject.getFunctionProperty(cx, methodName);
                  if (func != Scriptable.NOT_FOUND) {
                     break;
                  }

                  sobj = xmlObject.getExtraMethodSource(cx);
                  if (sobj != null) {
                     thisObj = sobj;
                     if (!(sobj instanceof XMLObject)) {
                        func = ScriptableObject.getProperty(sobj, methodName);
                     }
                  }
               }

               if (!(func instanceof Callable)) {
                  throw ScriptRuntime.notFunctionError(thisObj, func, methodName);
               } else {
                  return ((Callable)func).call(cx, scope, thisObj, args);
               }
            }
         } else {
            return this.applyOrCall(isApply, cx, scope, thisObj, args);
         }
      }
   }

   public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
      throw ScriptRuntime.typeError1("msg.not.ctor", "XMLList");
   }

   static class AnnotationList {
      private Vector v = new Vector();

      AnnotationList() {
         super();
      }

      void add(XML.XScriptAnnotation n) {
         this.v.add(n);
      }

      XML.XScriptAnnotation item(int index) {
         return (XML.XScriptAnnotation)this.v.get(index);
      }

      void remove(int index) {
         this.v.remove(index);
      }

      int length() {
         return this.v.size();
      }
   }
}
