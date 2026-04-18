package org.mozilla.javascript.xmlimpl;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.xml.XMLObject;
import org.w3c.dom.Node;

class XML extends XMLObjectImpl {
   static final long serialVersionUID = -630969919086449092L;
   private XmlNode node;

   XML(XMLLibImpl lib, Scriptable scope, XMLObject prototype, XmlNode node) {
      super(lib, scope, prototype);
      this.initialize(node);
   }

   void initialize(XmlNode node) {
      this.node = node;
      this.node.setXml(this);
   }

   final XML getXML() {
      return this;
   }

   void replaceWith(XML value) {
      if (this.node.parent() == null) {
         this.initialize(value.node);
      } else {
         this.node.replaceWith(value.node);
      }

   }

   XML makeXmlFromString(XMLName name, String value) {
      try {
         return this.newTextElementXML(this.node, name.toQname(), value);
      } catch (Exception e) {
         throw ScriptRuntime.typeError(e.getMessage());
      }
   }

   XmlNode getAnnotation() {
      return this.node;
   }

   public Object get(int index, Scriptable start) {
      return index == 0 ? this : Scriptable.NOT_FOUND;
   }

   public boolean has(int index, Scriptable start) {
      return index == 0;
   }

   public void put(int index, Scriptable start, Object value) {
      throw ScriptRuntime.typeError("Assignment to indexed XML is not allowed");
   }

   public Object[] getIds() {
      return this.isPrototype() ? new Object[0] : new Object[]{0};
   }

   public void delete(int index) {
      if (index == 0) {
         this.remove();
      }

   }

   boolean hasXMLProperty(XMLName xmlName) {
      return this.getPropertyList(xmlName).length() > 0;
   }

   Object getXMLProperty(XMLName xmlName) {
      return this.getPropertyList(xmlName);
   }

   XmlNode.QName getNodeQname() {
      return this.node.getQname();
   }

   XML[] getChildren() {
      if (!this.isElement()) {
         return null;
      } else {
         XmlNode[] children = this.node.getMatchingChildren(XmlNode.Filter.TRUE);
         XML[] rv = new XML[children.length];

         for(int i = 0; i < rv.length; ++i) {
            rv[i] = this.toXML(children[i]);
         }

         return rv;
      }
   }

   XML[] getAttributes() {
      XmlNode[] attributes = this.node.getAttributes();
      XML[] rv = new XML[attributes.length];

      for(int i = 0; i < rv.length; ++i) {
         rv[i] = this.toXML(attributes[i]);
      }

      return rv;
   }

   XMLList getPropertyList(XMLName name) {
      return name.getMyValueOn(this);
   }

   void deleteXMLProperty(XMLName name) {
      XMLList list = this.getPropertyList(name);

      for(int i = 0; i < list.length(); ++i) {
         list.item(i).node.deleteMe();
      }

   }

   void putXMLProperty(XMLName xmlName, Object value) {
      if (!this.isPrototype()) {
         xmlName.setMyValueOn(this, value);
      }

   }

   boolean hasOwnProperty(XMLName xmlName) {
      boolean hasProperty = false;
      if (this.isPrototype()) {
         String property = xmlName.localName();
         hasProperty = 0 != this.findPrototypeId(property);
      } else {
         hasProperty = this.getPropertyList(xmlName).length() > 0;
      }

      return hasProperty;
   }

   protected Object jsConstructor(Context cx, boolean inNewExpr, Object[] args) {
      if (args.length == 0 || args[0] == null || args[0] == Undefined.instance) {
         args = new Object[]{""};
      }

      XML toXml = this.ecmaToXml(args[0]);
      return inNewExpr ? toXml.copy() : toXml;
   }

   public Scriptable getExtraMethodSource(Context cx) {
      if (this.hasSimpleContent()) {
         String src = this.toString();
         return ScriptRuntime.toObjectOrNull(cx, src);
      } else {
         return null;
      }
   }

   void removeChild(int index) {
      this.node.removeChild(index);
   }

   void normalize() {
      this.node.normalize();
   }

   private XML toXML(XmlNode node) {
      if (node.getXml() == null) {
         node.setXml(this.newXML(node));
      }

      return node.getXml();
   }

   void setAttribute(XMLName xmlName, Object value) {
      if (!this.isElement()) {
         throw new IllegalStateException("Can only set attributes on elements.");
      } else if (xmlName.uri() == null && xmlName.localName().equals("*")) {
         throw ScriptRuntime.typeError("@* assignment not supported.");
      } else {
         this.node.setAttribute(xmlName.toQname(), ScriptRuntime.toString(value));
      }
   }

   void remove() {
      this.node.deleteMe();
   }

   void addMatches(XMLList rv, XMLName name) {
      name.addMatches(rv, this);
   }

   XMLList elements(XMLName name) {
      XMLList rv = this.newXMLList();
      rv.setTargets(this, name.toQname());
      XmlNode[] elements = this.node.getMatchingChildren(XmlNode.Filter.ELEMENT);

      for(int i = 0; i < elements.length; ++i) {
         if (name.matches(this.toXML(elements[i]))) {
            rv.addToList(this.toXML(elements[i]));
         }
      }

      return rv;
   }

   XMLList child(XMLName xmlName) {
      XMLList rv = this.newXMLList();
      XmlNode[] elements = this.node.getMatchingChildren(XmlNode.Filter.ELEMENT);

      for(int i = 0; i < elements.length; ++i) {
         if (xmlName.matchesElement(elements[i].getQname())) {
            rv.addToList(this.toXML(elements[i]));
         }
      }

      rv.setTargets(this, xmlName.toQname());
      return rv;
   }

   XML replace(XMLName xmlName, Object xml) {
      this.putXMLProperty(xmlName, xml);
      return this;
   }

   XMLList children() {
      XMLList rv = this.newXMLList();
      XMLName all = XMLName.formStar();
      rv.setTargets(this, all.toQname());
      XmlNode[] children = this.node.getMatchingChildren(XmlNode.Filter.TRUE);

      for(int i = 0; i < children.length; ++i) {
         rv.addToList(this.toXML(children[i]));
      }

      return rv;
   }

   XMLList child(int index) {
      XMLList result = this.newXMLList();
      result.setTargets(this, (XmlNode.QName)null);
      if (index >= 0 && index < this.node.getChildCount()) {
         result.addToList(this.getXmlChild(index));
      }

      return result;
   }

   XML getXmlChild(int index) {
      XmlNode child = this.node.getChild(index);
      if (child.getXml() == null) {
         child.setXml(this.newXML(child));
      }

      return child.getXml();
   }

   XML getLastXmlChild() {
      int pos = this.node.getChildCount() - 1;
      return pos < 0 ? null : this.getXmlChild(pos);
   }

   int childIndex() {
      return this.node.getChildIndex();
   }

   boolean contains(Object xml) {
      return xml instanceof XML ? this.equivalentXml(xml) : false;
   }

   boolean equivalentXml(Object target) {
      boolean result = false;
      if (target instanceof XML) {
         return this.node.toXmlString(this.getProcessor()).equals(((XML)target).node.toXmlString(this.getProcessor()));
      } else {
         if (target instanceof XMLList) {
            XMLList otherList = (XMLList)target;
            if (otherList.length() == 1) {
               result = this.equivalentXml(otherList.getXML());
            }
         } else if (this.hasSimpleContent()) {
            String otherStr = ScriptRuntime.toString(target);
            result = this.toString().equals(otherStr);
         }

         return result;
      }
   }

   XMLObjectImpl copy() {
      return this.newXML(this.node.copy());
   }

   boolean hasSimpleContent() {
      if (!this.isComment() && !this.isProcessingInstruction()) {
         if (!this.isText() && !this.node.isAttributeType()) {
            return !this.node.hasChildElement();
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   boolean hasComplexContent() {
      return !this.hasSimpleContent();
   }

   int length() {
      return 1;
   }

   boolean is(XML other) {
      return this.node.isSameNode(other.node);
   }

   Object nodeKind() {
      return this.ecmaClass();
   }

   Object parent() {
      XmlNode parent = this.node.parent();
      return parent == null ? null : this.newXML(this.node.parent());
   }

   boolean propertyIsEnumerable(Object name) {
      boolean result;
      if (name instanceof Integer) {
         result = (Integer)name == 0;
      } else if (name instanceof Number) {
         double x = ((Number)name).doubleValue();
         result = x == (double)0.0F && (double)1.0F / x > (double)0.0F;
      } else {
         result = ScriptRuntime.toString(name).equals("0");
      }

      return result;
   }

   Object valueOf() {
      return this;
   }

   XMLList comments() {
      XMLList rv = this.newXMLList();
      this.node.addMatchingChildren(rv, XmlNode.Filter.COMMENT);
      return rv;
   }

   XMLList text() {
      XMLList rv = this.newXMLList();
      this.node.addMatchingChildren(rv, XmlNode.Filter.TEXT);
      return rv;
   }

   XMLList processingInstructions(XMLName xmlName) {
      XMLList rv = this.newXMLList();
      this.node.addMatchingChildren(rv, XmlNode.Filter.PROCESSING_INSTRUCTION(xmlName));
      return rv;
   }

   private XmlNode[] getNodesForInsert(Object value) {
      if (value instanceof XML) {
         return new XmlNode[]{((XML)value).node};
      } else if (!(value instanceof XMLList)) {
         return new XmlNode[]{XmlNode.createText(this.getProcessor(), ScriptRuntime.toString(value))};
      } else {
         XMLList list = (XMLList)value;
         XmlNode[] rv = new XmlNode[list.length()];

         for(int i = 0; i < list.length(); ++i) {
            rv[i] = list.item(i).node;
         }

         return rv;
      }
   }

   XML replace(int index, Object xml) {
      XMLList xlChildToReplace = this.child(index);
      if (xlChildToReplace.length() > 0) {
         XML childToReplace = xlChildToReplace.item(0);
         this.insertChildAfter(childToReplace, xml);
         this.removeChild(index);
      }

      return this;
   }

   XML prependChild(Object xml) {
      if (this.node.isParentType()) {
         this.node.insertChildrenAt(0, this.getNodesForInsert(xml));
      }

      return this;
   }

   XML appendChild(Object xml) {
      if (this.node.isParentType()) {
         XmlNode[] nodes = this.getNodesForInsert(xml);
         this.node.insertChildrenAt(this.node.getChildCount(), nodes);
      }

      return this;
   }

   private int getChildIndexOf(XML child) {
      for(int i = 0; i < this.node.getChildCount(); ++i) {
         if (this.node.getChild(i).isSameNode(child.node)) {
            return i;
         }
      }

      return -1;
   }

   XML insertChildBefore(XML child, Object xml) {
      if (child == null) {
         this.appendChild(xml);
      } else {
         XmlNode[] toInsert = this.getNodesForInsert(xml);
         int index = this.getChildIndexOf(child);
         if (index != -1) {
            this.node.insertChildrenAt(index, toInsert);
         }
      }

      return this;
   }

   XML insertChildAfter(XML child, Object xml) {
      if (child == null) {
         this.prependChild(xml);
      } else {
         XmlNode[] toInsert = this.getNodesForInsert(xml);
         int index = this.getChildIndexOf(child);
         if (index != -1) {
            this.node.insertChildrenAt(index + 1, toInsert);
         }
      }

      return this;
   }

   XML setChildren(Object xml) {
      if (!this.isElement()) {
         return this;
      } else {
         while(this.node.getChildCount() > 0) {
            this.node.removeChild(0);
         }

         XmlNode[] toInsert = this.getNodesForInsert(xml);
         this.node.insertChildrenAt(0, toInsert);
         return this;
      }
   }

   private void addInScopeNamespace(Namespace ns) {
      if (this.isElement()) {
         if (ns.prefix() != null) {
            if (ns.prefix().length() != 0 || ns.uri().length() != 0) {
               if (this.node.getQname().getNamespace().getPrefix().equals(ns.prefix())) {
                  this.node.invalidateNamespacePrefix();
               }

               this.node.declareNamespace(ns.prefix(), ns.uri());
            }
         }
      }
   }

   Namespace[] inScopeNamespaces() {
      XmlNode.Namespace[] inScope = this.node.getInScopeNamespaces();
      return this.createNamespaces(inScope);
   }

   private XmlNode.Namespace adapt(Namespace ns) {
      return ns.prefix() == null ? XmlNode.Namespace.create(ns.uri()) : XmlNode.Namespace.create(ns.prefix(), ns.uri());
   }

   XML removeNamespace(Namespace ns) {
      if (!this.isElement()) {
         return this;
      } else {
         this.node.removeNamespace(this.adapt(ns));
         return this;
      }
   }

   XML addNamespace(Namespace ns) {
      this.addInScopeNamespace(ns);
      return this;
   }

   QName name() {
      if (!this.isText() && !this.isComment()) {
         return this.isProcessingInstruction() ? this.newQName("", this.node.getQname().getLocalName(), (String)null) : this.newQName(this.node.getQname());
      } else {
         return null;
      }
   }

   Namespace[] namespaceDeclarations() {
      XmlNode.Namespace[] declarations = this.node.getNamespaceDeclarations();
      return this.createNamespaces(declarations);
   }

   Namespace namespace(String prefix) {
      return prefix == null ? this.createNamespace(this.node.getNamespaceDeclaration()) : this.createNamespace(this.node.getNamespaceDeclaration(prefix));
   }

   String localName() {
      return this.name() == null ? null : this.name().localName();
   }

   void setLocalName(String localName) {
      if (!this.isText() && !this.isComment()) {
         this.node.setLocalName(localName);
      }
   }

   void setName(QName name) {
      if (!this.isText() && !this.isComment()) {
         if (this.isProcessingInstruction()) {
            this.node.setLocalName(name.localName());
         } else {
            this.node.renameNode(name.getDelegate());
         }
      }
   }

   void setNamespace(Namespace ns) {
      if (!this.isText() && !this.isComment() && !this.isProcessingInstruction()) {
         this.setName(this.newQName(ns.uri(), this.localName(), ns.prefix()));
      }
   }

   final String ecmaClass() {
      if (this.node.isTextType()) {
         return "text";
      } else if (this.node.isAttributeType()) {
         return "attribute";
      } else if (this.node.isCommentType()) {
         return "comment";
      } else if (this.node.isProcessingInstructionType()) {
         return "processing-instruction";
      } else if (this.node.isElementType()) {
         return "element";
      } else {
         throw new RuntimeException("Unrecognized type: " + this.node);
      }
   }

   public String getClassName() {
      return "XML";
   }

   private String ecmaValue() {
      return this.node.ecmaValue();
   }

   private String ecmaToString() {
      if (!this.isAttribute() && !this.isText()) {
         if (this.hasSimpleContent()) {
            StringBuffer rv = new StringBuffer();

            for(int i = 0; i < this.node.getChildCount(); ++i) {
               XmlNode child = this.node.getChild(i);
               if (!child.isProcessingInstructionType() && !child.isCommentType()) {
                  XML x = new XML(this.getLib(), this.getParentScope(), (XMLObject)this.getPrototype(), child);
                  rv.append(x.toString());
               }
            }

            return rv.toString();
         } else {
            return this.toXMLString();
         }
      } else {
         return this.ecmaValue();
      }
   }

   public String toString() {
      return this.ecmaToString();
   }

   String toSource(int indent) {
      return this.toXMLString();
   }

   String toXMLString() {
      return this.node.ecmaToXMLString(this.getProcessor());
   }

   final boolean isAttribute() {
      return this.node.isAttributeType();
   }

   final boolean isComment() {
      return this.node.isCommentType();
   }

   final boolean isText() {
      return this.node.isTextType();
   }

   final boolean isElement() {
      return this.node.isElementType();
   }

   final boolean isProcessingInstruction() {
      return this.node.isProcessingInstructionType();
   }

   Node toDomNode() {
      return this.node.toDomNode();
   }
}
