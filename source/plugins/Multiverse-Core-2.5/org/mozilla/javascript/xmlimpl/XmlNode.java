package org.mozilla.javascript.xmlimpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mozilla.javascript.Undefined;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;
import org.xml.sax.SAXException;

class XmlNode implements Serializable {
   private static final String XML_NAMESPACES_NAMESPACE_URI = "http://www.w3.org/2000/xmlns/";
   private static final String USER_DATA_XMLNODE_KEY = XmlNode.class.getName();
   private static final boolean DOM_LEVEL_3 = true;
   private static final long serialVersionUID = 1L;
   private UserDataHandler events = new XmlNodeUserDataHandler();
   private Node dom;
   private XML xml;

   private static XmlNode getUserData(Node node) {
      return (XmlNode)node.getUserData(USER_DATA_XMLNODE_KEY);
   }

   private static void setUserData(Node node, XmlNode wrap) {
      node.setUserData(USER_DATA_XMLNODE_KEY, wrap, wrap.events);
   }

   private static XmlNode createImpl(Node node) {
      if (node instanceof Document) {
         throw new IllegalArgumentException();
      } else {
         XmlNode rv = null;
         if (getUserData(node) == null) {
            rv = new XmlNode();
            rv.dom = node;
            setUserData(node, rv);
         } else {
            rv = getUserData(node);
         }

         return rv;
      }
   }

   static XmlNode newElementWithText(XmlProcessor processor, XmlNode reference, QName qname, String value) {
      if (reference instanceof Document) {
         throw new IllegalArgumentException("Cannot use Document node as reference");
      } else {
         Document document = null;
         if (reference != null) {
            document = reference.dom.getOwnerDocument();
         } else {
            document = processor.newDocument();
         }

         Node referenceDom = reference != null ? reference.dom : null;
         Namespace ns = qname.getNamespace();
         Element e = ns != null && ns.getUri().length() != 0 ? document.createElementNS(ns.getUri(), qname.qualify(referenceDom)) : document.createElementNS((String)null, qname.getLocalName());
         if (value != null) {
            e.appendChild(document.createTextNode(value));
         }

         return createImpl(e);
      }
   }

   static XmlNode createText(XmlProcessor processor, String value) {
      return createImpl(processor.newDocument().createTextNode(value));
   }

   static XmlNode createElementFromNode(Node node) {
      if (node instanceof Document) {
         node = ((Document)node).getDocumentElement();
      }

      return createImpl(node);
   }

   static XmlNode createElement(XmlProcessor processor, String namespaceUri, String xml) throws SAXException {
      return createImpl(processor.toXml(namespaceUri, xml));
   }

   static XmlNode createEmpty(XmlProcessor processor) {
      return createText(processor, "");
   }

   private static XmlNode copy(XmlNode other) {
      return createImpl(other.dom.cloneNode(true));
   }

   private XmlNode() {
      super();
   }

   String debug() {
      XmlProcessor raw = new XmlProcessor();
      raw.setIgnoreComments(false);
      raw.setIgnoreProcessingInstructions(false);
      raw.setIgnoreWhitespace(false);
      raw.setPrettyPrinting(false);
      return raw.ecmaToXmlString(this.dom);
   }

   public String toString() {
      return "XmlNode: type=" + this.dom.getNodeType() + " dom=" + this.dom.toString();
   }

   XML getXml() {
      return this.xml;
   }

   void setXml(XML xml) {
      this.xml = xml;
   }

   int getChildCount() {
      return this.dom.getChildNodes().getLength();
   }

   XmlNode parent() {
      Node domParent = this.dom.getParentNode();
      if (domParent instanceof Document) {
         return null;
      } else {
         return domParent == null ? null : createImpl(domParent);
      }
   }

   int getChildIndex() {
      if (this.isAttributeType()) {
         return -1;
      } else if (this.parent() == null) {
         return -1;
      } else {
         NodeList siblings = this.dom.getParentNode().getChildNodes();

         for(int i = 0; i < siblings.getLength(); ++i) {
            if (siblings.item(i) == this.dom) {
               return i;
            }
         }

         throw new RuntimeException("Unreachable.");
      }
   }

   void removeChild(int index) {
      this.dom.removeChild(this.dom.getChildNodes().item(index));
   }

   String toXmlString(XmlProcessor processor) {
      return processor.ecmaToXmlString(this.dom);
   }

   String ecmaValue() {
      if (this.isTextType()) {
         return ((Text)this.dom).getData();
      } else if (this.isAttributeType()) {
         return ((Attr)this.dom).getValue();
      } else if (this.isProcessingInstructionType()) {
         return ((ProcessingInstruction)this.dom).getData();
      } else if (this.isCommentType()) {
         return ((Comment)this.dom).getNodeValue();
      } else if (this.isElementType()) {
         throw new RuntimeException("Unimplemented ecmaValue() for elements.");
      } else {
         throw new RuntimeException("Unimplemented for node " + this.dom);
      }
   }

   void deleteMe() {
      if (this.dom instanceof Attr) {
         Attr attr = (Attr)this.dom;
         attr.getOwnerElement().getAttributes().removeNamedItemNS(attr.getNamespaceURI(), attr.getLocalName());
      } else if (this.dom.getParentNode() != null) {
         this.dom.getParentNode().removeChild(this.dom);
      }

   }

   void normalize() {
      this.dom.normalize();
   }

   void insertChildAt(int index, XmlNode node) {
      Node parent = this.dom;
      Node child = parent.getOwnerDocument().importNode(node.dom, true);
      if (parent.getChildNodes().getLength() < index) {
         throw new IllegalArgumentException("index=" + index + " length=" + parent.getChildNodes().getLength());
      } else {
         if (parent.getChildNodes().getLength() == index) {
            parent.appendChild(child);
         } else {
            parent.insertBefore(child, parent.getChildNodes().item(index));
         }

      }
   }

   void insertChildrenAt(int index, XmlNode[] nodes) {
      for(int i = 0; i < nodes.length; ++i) {
         this.insertChildAt(index + i, nodes[i]);
      }

   }

   XmlNode getChild(int index) {
      Node child = this.dom.getChildNodes().item(index);
      return createImpl(child);
   }

   boolean hasChildElement() {
      NodeList nodes = this.dom.getChildNodes();

      for(int i = 0; i < nodes.getLength(); ++i) {
         if (nodes.item(i).getNodeType() == 1) {
            return true;
         }
      }

      return false;
   }

   boolean isSameNode(XmlNode other) {
      return this.dom == other.dom;
   }

   private String toUri(String ns) {
      return ns == null ? "" : ns;
   }

   private void addNamespaces(Namespaces rv, Element element) {
      if (element == null) {
         throw new RuntimeException("element must not be null");
      } else {
         String myDefaultNamespace = this.toUri(element.lookupNamespaceURI((String)null));
         String parentDefaultNamespace = "";
         if (element.getParentNode() != null) {
            parentDefaultNamespace = this.toUri(element.getParentNode().lookupNamespaceURI((String)null));
         }

         if (!myDefaultNamespace.equals(parentDefaultNamespace) || !(element.getParentNode() instanceof Element)) {
            rv.declare(XmlNode.Namespace.create("", myDefaultNamespace));
         }

         NamedNodeMap attributes = element.getAttributes();

         for(int i = 0; i < attributes.getLength(); ++i) {
            Attr attr = (Attr)attributes.item(i);
            if (attr.getPrefix() != null && attr.getPrefix().equals("xmlns")) {
               rv.declare(XmlNode.Namespace.create(attr.getLocalName(), attr.getValue()));
            }
         }

      }
   }

   private Namespaces getAllNamespaces() {
      Namespaces rv = new Namespaces();
      Node target = this.dom;
      if (target instanceof Attr) {
         target = ((Attr)target).getOwnerElement();
      }

      for(; target != null; target = target.getParentNode()) {
         if (target instanceof Element) {
            this.addNamespaces(rv, (Element)target);
         }
      }

      rv.declare(XmlNode.Namespace.create("", ""));
      return rv;
   }

   Namespace[] getInScopeNamespaces() {
      Namespaces rv = this.getAllNamespaces();
      return rv.getNamespaces();
   }

   Namespace[] getNamespaceDeclarations() {
      if (this.dom instanceof Element) {
         Namespaces rv = new Namespaces();
         this.addNamespaces(rv, (Element)this.dom);
         return rv.getNamespaces();
      } else {
         return new Namespace[0];
      }
   }

   Namespace getNamespaceDeclaration(String prefix) {
      if (prefix.equals("") && this.dom instanceof Attr) {
         return XmlNode.Namespace.create("", "");
      } else {
         Namespaces rv = this.getAllNamespaces();
         return rv.getNamespace(prefix);
      }
   }

   Namespace getNamespaceDeclaration() {
      return this.dom.getPrefix() == null ? this.getNamespaceDeclaration("") : this.getNamespaceDeclaration(this.dom.getPrefix());
   }

   final XmlNode copy() {
      return copy(this);
   }

   final boolean isParentType() {
      return this.isElementType();
   }

   final boolean isTextType() {
      return this.dom.getNodeType() == 3 || this.dom.getNodeType() == 4;
   }

   final boolean isAttributeType() {
      return this.dom.getNodeType() == 2;
   }

   final boolean isProcessingInstructionType() {
      return this.dom.getNodeType() == 7;
   }

   final boolean isCommentType() {
      return this.dom.getNodeType() == 8;
   }

   final boolean isElementType() {
      return this.dom.getNodeType() == 1;
   }

   final void renameNode(QName qname) {
      this.dom = this.dom.getOwnerDocument().renameNode(this.dom, qname.getNamespace().getUri(), qname.qualify(this.dom));
   }

   void invalidateNamespacePrefix() {
      if (!(this.dom instanceof Element)) {
         throw new IllegalStateException();
      } else {
         String prefix = this.dom.getPrefix();
         QName after = XmlNode.QName.create(this.dom.getNamespaceURI(), this.dom.getLocalName(), (String)null);
         this.renameNode(after);
         NamedNodeMap attrs = this.dom.getAttributes();

         for(int i = 0; i < attrs.getLength(); ++i) {
            if (attrs.item(i).getPrefix().equals(prefix)) {
               createImpl(attrs.item(i)).renameNode(XmlNode.QName.create(attrs.item(i).getNamespaceURI(), attrs.item(i).getLocalName(), (String)null));
            }
         }

      }
   }

   private void declareNamespace(Element e, String prefix, String uri) {
      if (prefix.length() > 0) {
         e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + prefix, uri);
      } else {
         e.setAttribute("xmlns", uri);
      }

   }

   void declareNamespace(String prefix, String uri) {
      if (!(this.dom instanceof Element)) {
         throw new IllegalStateException();
      } else {
         if (this.dom.lookupNamespaceURI(uri) == null || !this.dom.lookupNamespaceURI(uri).equals(prefix)) {
            Element e = (Element)this.dom;
            this.declareNamespace(e, prefix, uri);
         }

      }
   }

   private Namespace getDefaultNamespace() {
      String prefix = "";
      String uri = this.dom.lookupNamespaceURI((String)null) == null ? "" : this.dom.lookupNamespaceURI((String)null);
      return XmlNode.Namespace.create(prefix, uri);
   }

   private String getExistingPrefixFor(Namespace namespace) {
      return this.getDefaultNamespace().getUri().equals(namespace.getUri()) ? "" : this.dom.lookupPrefix(namespace.getUri());
   }

   private Namespace getNodeNamespace() {
      String uri = this.dom.getNamespaceURI();
      String prefix = this.dom.getPrefix();
      if (uri == null) {
         uri = "";
      }

      if (prefix == null) {
         prefix = "";
      }

      return XmlNode.Namespace.create(prefix, uri);
   }

   Namespace getNamespace() {
      return this.getNodeNamespace();
   }

   void removeNamespace(Namespace namespace) {
      Namespace current = this.getNodeNamespace();
      if (!namespace.is(current)) {
         NamedNodeMap attrs = this.dom.getAttributes();

         for(int i = 0; i < attrs.getLength(); ++i) {
            XmlNode attr = createImpl(attrs.item(i));
            if (namespace.is(attr.getNodeNamespace())) {
               return;
            }
         }

         String existingPrefix = this.getExistingPrefixFor(namespace);
         if (existingPrefix != null) {
            if (namespace.isUnspecifiedPrefix()) {
               this.declareNamespace(existingPrefix, this.getDefaultNamespace().getUri());
            } else if (existingPrefix.equals(namespace.getPrefix())) {
               this.declareNamespace(existingPrefix, this.getDefaultNamespace().getUri());
            }
         }

      }
   }

   private void setProcessingInstructionName(String localName) {
      ProcessingInstruction pi = (ProcessingInstruction)this.dom;
      pi.getParentNode().replaceChild(pi, pi.getOwnerDocument().createProcessingInstruction(localName, pi.getData()));
   }

   final void setLocalName(String localName) {
      if (this.dom instanceof ProcessingInstruction) {
         this.setProcessingInstructionName(localName);
      } else {
         String prefix = this.dom.getPrefix();
         if (prefix == null) {
            prefix = "";
         }

         this.dom = this.dom.getOwnerDocument().renameNode(this.dom, this.dom.getNamespaceURI(), XmlNode.QName.qualify(prefix, localName));
      }

   }

   final QName getQname() {
      String uri = this.dom.getNamespaceURI() == null ? "" : this.dom.getNamespaceURI();
      String prefix = this.dom.getPrefix() == null ? "" : this.dom.getPrefix();
      return XmlNode.QName.create(uri, this.dom.getLocalName(), prefix);
   }

   void addMatchingChildren(XMLList result, Filter filter) {
      Node node = this.dom;
      NodeList children = node.getChildNodes();

      for(int i = 0; i < children.getLength(); ++i) {
         Node childnode = children.item(i);
         XmlNode child = createImpl(childnode);
         if (filter.accept(childnode)) {
            result.addToList(child);
         }
      }

   }

   XmlNode[] getMatchingChildren(Filter filter) {
      ArrayList<XmlNode> rv = new ArrayList();
      NodeList nodes = this.dom.getChildNodes();

      for(int i = 0; i < nodes.getLength(); ++i) {
         Node node = nodes.item(i);
         if (filter.accept(node)) {
            rv.add(createImpl(node));
         }
      }

      return (XmlNode[])rv.toArray(new XmlNode[rv.size()]);
   }

   XmlNode[] getAttributes() {
      NamedNodeMap attrs = this.dom.getAttributes();
      if (attrs == null) {
         throw new IllegalStateException("Must be element.");
      } else {
         XmlNode[] rv = new XmlNode[attrs.getLength()];

         for(int i = 0; i < attrs.getLength(); ++i) {
            rv[i] = createImpl(attrs.item(i));
         }

         return rv;
      }
   }

   String getAttributeValue() {
      return ((Attr)this.dom).getValue();
   }

   void setAttribute(QName name, String value) {
      if (!(this.dom instanceof Element)) {
         throw new IllegalStateException("Can only set attribute on elements.");
      } else {
         name.setAttribute((Element)this.dom, value);
      }
   }

   void replaceWith(XmlNode other) {
      Node replacement = other.dom;
      if (replacement.getOwnerDocument() != this.dom.getOwnerDocument()) {
         replacement = this.dom.getOwnerDocument().importNode(replacement, true);
      }

      this.dom.getParentNode().replaceChild(replacement, this.dom);
   }

   String ecmaToXMLString(XmlProcessor processor) {
      if (!this.isElementType()) {
         return processor.ecmaToXmlString(this.dom);
      } else {
         Element copy = (Element)this.dom.cloneNode(true);
         Namespace[] inScope = this.getInScopeNamespaces();

         for(int i = 0; i < inScope.length; ++i) {
            this.declareNamespace(copy, inScope[i].getPrefix(), inScope[i].getUri());
         }

         return processor.ecmaToXmlString(copy);
      }
   }

   Node toDomNode() {
      return this.dom;
   }

   static class XmlNodeUserDataHandler implements UserDataHandler, Serializable {
      private static final long serialVersionUID = 4666895518900769588L;

      XmlNodeUserDataHandler() {
         super();
      }

      public void handle(short operation, String key, Object data, Node src, Node dest) {
      }
   }

   private static class Namespaces {
      private Map map = new HashMap();
      private Map uriToPrefix = new HashMap();

      Namespaces() {
         super();
      }

      void declare(Namespace n) {
         if (this.map.get(n.prefix) == null) {
            this.map.put(n.prefix, n.uri);
         }

         if (this.uriToPrefix.get(n.uri) == null) {
            this.uriToPrefix.put(n.uri, n.prefix);
         }

      }

      Namespace getNamespaceByUri(String uri) {
         return this.uriToPrefix.get(uri) == null ? null : XmlNode.Namespace.create(uri, (String)this.uriToPrefix.get(uri));
      }

      Namespace getNamespace(String prefix) {
         return this.map.get(prefix) == null ? null : XmlNode.Namespace.create(prefix, (String)this.map.get(prefix));
      }

      Namespace[] getNamespaces() {
         ArrayList<Namespace> rv = new ArrayList();

         for(String prefix : this.map.keySet()) {
            String uri = (String)this.map.get(prefix);
            Namespace n = XmlNode.Namespace.create(prefix, uri);
            if (!n.isEmpty()) {
               rv.add(n);
            }
         }

         return (Namespace[])rv.toArray(new Namespace[rv.size()]);
      }
   }

   static class Namespace implements Serializable {
      private static final long serialVersionUID = 4073904386884677090L;
      static final Namespace GLOBAL = create("", "");
      private String prefix;
      private String uri;

      static Namespace create(String prefix, String uri) {
         if (prefix == null) {
            throw new IllegalArgumentException("Empty string represents default namespace prefix");
         } else if (uri == null) {
            throw new IllegalArgumentException("Namespace may not lack a URI");
         } else {
            Namespace rv = new Namespace();
            rv.prefix = prefix;
            rv.uri = uri;
            return rv;
         }
      }

      static Namespace create(String uri) {
         Namespace rv = new Namespace();
         rv.uri = uri;
         if (uri == null || uri.length() == 0) {
            rv.prefix = "";
         }

         return rv;
      }

      private Namespace() {
         super();
      }

      public String toString() {
         return this.prefix == null ? "XmlNode.Namespace [" + this.uri + "]" : "XmlNode.Namespace [" + this.prefix + "{" + this.uri + "}]";
      }

      boolean isUnspecifiedPrefix() {
         return this.prefix == null;
      }

      boolean is(Namespace other) {
         return this.prefix != null && other.prefix != null && this.prefix.equals(other.prefix) && this.uri.equals(other.uri);
      }

      boolean isEmpty() {
         return this.prefix != null && this.prefix.equals("") && this.uri.equals("");
      }

      boolean isDefault() {
         return this.prefix != null && this.prefix.equals("");
      }

      boolean isGlobal() {
         return this.uri != null && this.uri.equals("");
      }

      private void setPrefix(String prefix) {
         if (prefix == null) {
            throw new IllegalArgumentException();
         } else {
            this.prefix = prefix;
         }
      }

      String getPrefix() {
         return this.prefix;
      }

      String getUri() {
         return this.uri;
      }
   }

   static class QName implements Serializable {
      private static final long serialVersionUID = -6587069811691451077L;
      private Namespace namespace;
      private String localName;

      static QName create(Namespace namespace, String localName) {
         if (localName != null && localName.equals("*")) {
            throw new RuntimeException("* is not valid localName");
         } else {
            QName rv = new QName();
            rv.namespace = namespace;
            rv.localName = localName;
            return rv;
         }
      }

      /** @deprecated */
      static QName create(String uri, String localName, String prefix) {
         return create(XmlNode.Namespace.create(prefix, uri), localName);
      }

      static String qualify(String prefix, String localName) {
         if (prefix == null) {
            throw new IllegalArgumentException("prefix must not be null");
         } else {
            return prefix.length() > 0 ? prefix + ":" + localName : localName;
         }
      }

      private QName() {
         super();
      }

      public String toString() {
         return "XmlNode.QName [" + this.localName + "," + this.namespace + "]";
      }

      private boolean equals(String one, String two) {
         if (one == null && two == null) {
            return true;
         } else {
            return one != null && two != null ? one.equals(two) : false;
         }
      }

      private boolean namespacesEqual(Namespace one, Namespace two) {
         if (one == null && two == null) {
            return true;
         } else {
            return one != null && two != null ? this.equals(one.getUri(), two.getUri()) : false;
         }
      }

      final boolean equals(QName other) {
         if (!this.namespacesEqual(this.namespace, other.namespace)) {
            return false;
         } else {
            return this.equals(this.localName, other.localName);
         }
      }

      public boolean equals(Object obj) {
         return !(obj instanceof QName) ? false : this.equals((QName)obj);
      }

      public int hashCode() {
         return this.localName == null ? 0 : this.localName.hashCode();
      }

      void lookupPrefix(Node node) {
         if (node == null) {
            throw new IllegalArgumentException("node must not be null");
         } else {
            String prefix = node.lookupPrefix(this.namespace.getUri());
            if (prefix == null) {
               String defaultNamespace = node.lookupNamespaceURI((String)null);
               if (defaultNamespace == null) {
                  defaultNamespace = "";
               }

               String nodeNamespace = this.namespace.getUri();
               if (nodeNamespace.equals(defaultNamespace)) {
                  prefix = "";
               }
            }

            int i = 0;

            while(prefix == null) {
               String generatedPrefix = "e4x_" + i++;
               String generatedUri = node.lookupNamespaceURI(generatedPrefix);
               if (generatedUri == null) {
                  prefix = generatedPrefix;

                  Node top;
                  for(top = node; top.getParentNode() != null && top.getParentNode() instanceof Element; top = top.getParentNode()) {
                  }

                  ((Element)top).setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + generatedPrefix, this.namespace.getUri());
               }
            }

            this.namespace.setPrefix(prefix);
         }
      }

      String qualify(Node node) {
         if (this.namespace.getPrefix() == null) {
            if (node != null) {
               this.lookupPrefix(node);
            } else if (this.namespace.getUri().equals("")) {
               this.namespace.setPrefix("");
            } else {
               this.namespace.setPrefix("");
            }
         }

         return qualify(this.namespace.getPrefix(), this.localName);
      }

      void setAttribute(Element element, String value) {
         if (this.namespace.getPrefix() == null) {
            this.lookupPrefix(element);
         }

         element.setAttributeNS(this.namespace.getUri(), qualify(this.namespace.getPrefix(), this.localName), value);
      }

      Namespace getNamespace() {
         return this.namespace;
      }

      String getLocalName() {
         return this.localName;
      }
   }

   static class InternalList implements Serializable {
      private static final long serialVersionUID = -3633151157292048978L;
      private List list = new ArrayList();

      InternalList() {
         super();
      }

      private void _add(XmlNode n) {
         this.list.add(n);
      }

      XmlNode item(int index) {
         return (XmlNode)this.list.get(index);
      }

      void remove(int index) {
         this.list.remove(index);
      }

      void add(InternalList other) {
         for(int i = 0; i < other.length(); ++i) {
            this._add(other.item(i));
         }

      }

      void add(InternalList from, int startInclusive, int endExclusive) {
         for(int i = startInclusive; i < endExclusive; ++i) {
            this._add(from.item(i));
         }

      }

      void add(XmlNode node) {
         this._add(node);
      }

      void add(XML xml) {
         this._add(xml.getAnnotation());
      }

      void addToList(Object toAdd) {
         if (!(toAdd instanceof Undefined)) {
            if (toAdd instanceof XMLList) {
               XMLList xmlSrc = (XMLList)toAdd;

               for(int i = 0; i < xmlSrc.length(); ++i) {
                  this._add(xmlSrc.item(i).getAnnotation());
               }
            } else if (toAdd instanceof XML) {
               this._add(((XML)((XML)toAdd)).getAnnotation());
            } else if (toAdd instanceof XmlNode) {
               this._add((XmlNode)toAdd);
            }

         }
      }

      int length() {
         return this.list.size();
      }
   }

   abstract static class Filter {
      static final Filter COMMENT = new Filter() {
         boolean accept(Node node) {
            return node.getNodeType() == 8;
         }
      };
      static final Filter TEXT = new Filter() {
         boolean accept(Node node) {
            return node.getNodeType() == 3;
         }
      };
      static Filter ELEMENT = new Filter() {
         boolean accept(Node node) {
            return node.getNodeType() == 1;
         }
      };
      static Filter TRUE = new Filter() {
         boolean accept(Node node) {
            return true;
         }
      };

      Filter() {
         super();
      }

      static Filter PROCESSING_INSTRUCTION(final XMLName name) {
         return new Filter() {
            boolean accept(Node node) {
               if (node.getNodeType() == 7) {
                  ProcessingInstruction pi = (ProcessingInstruction)node;
                  return name.matchesLocalName(pi.getTarget());
               } else {
                  return false;
               }
            }
         };
      }

      abstract boolean accept(Node var1);
   }
}
