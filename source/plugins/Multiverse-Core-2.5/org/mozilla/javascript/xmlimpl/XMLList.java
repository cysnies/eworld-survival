package org.mozilla.javascript.xmlimpl;

import java.util.ArrayList;
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
   private XmlNode.InternalList _annos = new XmlNode.InternalList();
   private XMLObjectImpl targetObject = null;
   private XmlNode.QName targetProperty = null;

   XMLList(XMLLibImpl lib, Scriptable scope, XMLObject prototype) {
      super(lib, scope, prototype);
   }

   XmlNode.InternalList getNodeList() {
      return this._annos;
   }

   void setTargets(XMLObjectImpl object, XmlNode.QName property) {
      this.targetObject = object;
      this.targetProperty = property;
   }

   private XML getXmlFromAnnotation(int index) {
      return this.getXML(this._annos, index);
   }

   XML getXML() {
      return this.length() == 1 ? this.getXmlFromAnnotation(0) : null;
   }

   private void internalRemoveFromList(int index) {
      this._annos.remove(index);
   }

   void replace(int index, XML xml) {
      if (index < this.length()) {
         XmlNode.InternalList newAnnoList = new XmlNode.InternalList();
         newAnnoList.add(this._annos, 0, index);
         newAnnoList.add(xml);
         newAnnoList.add(this._annos, index + 1, this.length());
         this._annos = newAnnoList;
      }

   }

   private void insert(int index, XML xml) {
      if (index < this.length()) {
         XmlNode.InternalList newAnnoList = new XmlNode.InternalList();
         newAnnoList.add(this._annos, 0, index);
         newAnnoList.add(xml);
         newAnnoList.add(this._annos, index, this.length());
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
         throw ScriptRuntime.typeError("Assignment to lists with more than one item is not supported");
      } else {
         if (this.length() == 0) {
            if (this.targetObject == null || this.targetProperty == null || this.targetProperty.getLocalName() == null || this.targetProperty.getLocalName().length() <= 0) {
               throw ScriptRuntime.typeError("Assignment to empty XMLList without targets not supported");
            }

            XML xmlValue = this.newTextElementXML((XmlNode)null, this.targetProperty, (String)null);
            this.addToList(xmlValue);
            if (xmlName.isAttributeName()) {
               this.setAttribute(xmlName, value);
            } else {
               XML xml = this.item(0);
               xml.putXMLProperty(xmlName, value);
               this.replace(0, this.item(0));
            }

            XMLName name2 = XMLName.formProperty(this.targetProperty.getNamespace().getUri(), this.targetProperty.getLocalName());
            this.targetObject.putXMLProperty(name2, this);
            this.replace(0, this.targetObject.getXML().getLastXmlChild());
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

   private void replaceNode(XML xml, XML with) {
      xml.replaceWith(with);
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
         xmlValue = this.newXMLFromJs(value.toString());
      } else {
         xmlValue = this.item(index);
         if (xmlValue == null) {
            XML x = this.item(0);
            xmlValue = (XMLObject)(x == null ? this.newTextElementXML((XmlNode)null, this.targetProperty, (String)null) : x.copy());
         }

         ((XML)xmlValue).setChildren(value);
      }

      if (index < this.length()) {
         parent = this.item(index).parent();
      } else if (this.length() == 0) {
         parent = this.targetObject != null ? this.targetObject.getXML() : this.parent();
      } else {
         parent = this.parent();
      }

      if (parent instanceof XML) {
         XML xmlParent = (XML)parent;
         if (index < this.length()) {
            XML xmlNode = this.getXmlFromAnnotation(index);
            if (xmlValue instanceof XML) {
               this.replaceNode(xmlNode, (XML)xmlValue);
               this.replace(index, xmlNode);
            } else if (xmlValue instanceof XMLList) {
               XMLList list = (XMLList)xmlValue;
               if (list.length() > 0) {
                  int lastIndexAdded = xmlNode.childIndex();
                  this.replaceNode(xmlNode, list.item(0));
                  this.replace(index, list.item(0));

                  for(int i = 1; i < list.length(); ++i) {
                     xmlParent.insertChildAfter(xmlParent.getXmlChild(lastIndexAdded), list.item(i));
                     ++lastIndexAdded;
                     this.insert(index + i, list.item(i));
                  }
               }
            }
         } else {
            xmlParent.appendChild(xmlValue);
            this.addToList(xmlParent.getLastXmlChild());
         }
      } else if (index < this.length()) {
         XML xmlNode = this.getXML(this._annos, index);
         if (xmlValue instanceof XML) {
            this.replaceNode(xmlNode, (XML)xmlValue);
            this.replace(index, xmlNode);
         } else if (xmlValue instanceof XMLList) {
            XMLList list = (XMLList)xmlValue;
            if (list.length() > 0) {
               this.replaceNode(xmlNode, list.item(0));
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

   private XML getXML(XmlNode.InternalList _annos, int index) {
      return index >= 0 && index < this.length() ? this.xmlFromNode(_annos.item(index)) : null;
   }

   void deleteXMLProperty(XMLName name) {
      for(int i = 0; i < this.length(); ++i) {
         XML xml = this.getXmlFromAnnotation(i);
         if (xml.isElement()) {
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
      if (this.isPrototype()) {
         enumObjs = new Object[0];
      } else {
         enumObjs = new Object[this.length()];

         for(int i = 0; i < enumObjs.length; ++i) {
            enumObjs[i] = i;
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
      return this._annos != null ? this.getXmlFromAnnotation(index) : this.createEmptyXML();
   }

   private void setAttribute(XMLName xmlName, Object value) {
      for(int i = 0; i < this.length(); ++i) {
         XML xml = this.getXmlFromAnnotation(i);
         xml.setAttribute(xmlName, value);
      }

   }

   void addToList(Object toAdd) {
      this._annos.addToList(toAdd);
   }

   XMLList child(int index) {
      XMLList result = this.newXMLList();

      for(int i = 0; i < this.length(); ++i) {
         result.addToList(this.getXmlFromAnnotation(i).child(index));
      }

      return result;
   }

   XMLList child(XMLName xmlName) {
      XMLList result = this.newXMLList();

      for(int i = 0; i < this.length(); ++i) {
         result.addToList(this.getXmlFromAnnotation(i).child(xmlName));
      }

      return result;
   }

   void addMatches(XMLList rv, XMLName name) {
      for(int i = 0; i < this.length(); ++i) {
         this.getXmlFromAnnotation(i).addMatches(rv, name);
      }

   }

   XMLList children() {
      ArrayList<XML> list = new ArrayList();

      for(int i = 0; i < this.length(); ++i) {
         XML xml = this.getXmlFromAnnotation(i);
         if (xml != null) {
            XMLList childList = xml.children();
            int cChildren = childList.length();

            for(int j = 0; j < cChildren; ++j) {
               list.add(childList.item(j));
            }
         }
      }

      XMLList allChildren = this.newXMLList();
      int sz = list.size();

      for(int i = 0; i < sz; ++i) {
         allChildren.addToList(list.get(i));
      }

      return allChildren;
   }

   XMLList comments() {
      XMLList result = this.newXMLList();

      for(int i = 0; i < this.length(); ++i) {
         XML xml = this.getXmlFromAnnotation(i);
         result.addToList(xml.comments());
      }

      return result;
   }

   XMLList elements(XMLName name) {
      XMLList rv = this.newXMLList();

      for(int i = 0; i < this.length(); ++i) {
         XML xml = this.getXmlFromAnnotation(i);
         rv.addToList(xml.elements(name));
      }

      return rv;
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

   XMLObjectImpl copy() {
      XMLList result = this.newXMLList();

      for(int i = 0; i < this.length(); ++i) {
         XML xml = this.getXmlFromAnnotation(i);
         result.addToList(xml.copy());
      }

      return result;
   }

   boolean hasOwnProperty(XMLName xmlName) {
      if (this.isPrototype()) {
         String property = xmlName.localName();
         return this.findPrototypeId(property) != 0;
      } else {
         return this.getPropertyList(xmlName).length() > 0;
      }
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
            if (nextElement.isElement()) {
               complexContent = true;
               break;
            }
         }
      }

      return complexContent;
   }

   boolean hasSimpleContent() {
      if (this.length() == 0) {
         return true;
      } else if (this.length() == 1) {
         return this.getXmlFromAnnotation(0).hasSimpleContent();
      } else {
         for(int i = 0; i < this.length(); ++i) {
            XML nextElement = this.getXmlFromAnnotation(i);
            if (nextElement.isElement()) {
               return false;
            }
         }

         return true;
      }
   }

   int length() {
      int result = 0;
      if (this._annos != null) {
         result = this._annos.length();
      }

      return result;
   }

   void normalize() {
      for(int i = 0; i < this.length(); ++i) {
         this.getXmlFromAnnotation(i).normalize();
      }

   }

   Object parent() {
      if (this.length() == 0) {
         return Undefined.instance;
      } else {
         XML candidateParent = null;

         for(int i = 0; i < this.length(); ++i) {
            Object currParent = this.getXmlFromAnnotation(i).parent();
            if (!(currParent instanceof XML)) {
               return Undefined.instance;
            }

            XML xml = (XML)currParent;
            if (i == 0) {
               candidateParent = xml;
            } else if (!candidateParent.is(xml)) {
               return Undefined.instance;
            }
         }

         return candidateParent;
      }
   }

   XMLList processingInstructions(XMLName xmlName) {
      XMLList result = this.newXMLList();

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

   XMLList text() {
      XMLList result = this.newXMLList();

      for(int i = 0; i < this.length(); ++i) {
         result.addToList(this.getXmlFromAnnotation(i).text());
      }

      return result;
   }

   public String toString() {
      if (this.hasSimpleContent()) {
         StringBuffer sb = new StringBuffer();

         for(int i = 0; i < this.length(); ++i) {
            XML next = this.getXmlFromAnnotation(i);
            if (!next.isComment() && !next.isProcessingInstruction()) {
               sb.append(next.toString());
            }
         }

         return sb.toString();
      } else {
         return this.toXMLString();
      }
   }

   String toSource(int indent) {
      return this.toXMLString();
   }

   String toXMLString() {
      StringBuffer sb = new StringBuffer();

      for(int i = 0; i < this.length(); ++i) {
         if (this.getProcessor().isPrettyPrinting() && i != 0) {
            sb.append('\n');
         }

         sb.append(this.getXmlFromAnnotation(i).toXMLString());
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
      XMLList propertyList = this.newXMLList();
      XmlNode.QName qname = null;
      if (!name.isDescendants() && !name.isAttributeName()) {
         qname = name.toQname();
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
         return this.newXMLList();
      } else {
         Object arg0 = args[0];
         return !inNewExpr && arg0 instanceof XMLList ? arg0 : this.newXMLListFrom(arg0);
      }
   }

   public Scriptable getExtraMethodSource(Context cx) {
      return this.length() == 1 ? this.getXmlFromAnnotation(0) : null;
   }

   public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      if (this.targetProperty == null) {
         throw ScriptRuntime.notFunctionError(this);
      } else {
         String methodName = this.targetProperty.getLocalName();
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
}
