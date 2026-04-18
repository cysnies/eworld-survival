package org.mozilla.javascript.xmlimpl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class XmlProcessor implements Serializable {
   private static final long serialVersionUID = 6903514433204808713L;
   private boolean ignoreComments;
   private boolean ignoreProcessingInstructions;
   private boolean ignoreWhitespace;
   private boolean prettyPrint;
   private int prettyIndent;
   private transient DocumentBuilderFactory dom;
   private transient TransformerFactory xform;
   private transient LinkedBlockingDeque documentBuilderPool;
   private RhinoSAXErrorHandler errorHandler = new RhinoSAXErrorHandler();

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.dom = DocumentBuilderFactory.newInstance();
      this.dom.setNamespaceAware(true);
      this.dom.setIgnoringComments(false);
      this.xform = TransformerFactory.newInstance();
      int poolSize = Runtime.getRuntime().availableProcessors() * 2;
      this.documentBuilderPool = new LinkedBlockingDeque(poolSize);
   }

   XmlProcessor() {
      super();
      this.setDefault();
      this.dom = DocumentBuilderFactory.newInstance();
      this.dom.setNamespaceAware(true);
      this.dom.setIgnoringComments(false);
      this.xform = TransformerFactory.newInstance();
      int poolSize = Runtime.getRuntime().availableProcessors() * 2;
      this.documentBuilderPool = new LinkedBlockingDeque(poolSize);
   }

   final void setDefault() {
      this.setIgnoreComments(true);
      this.setIgnoreProcessingInstructions(true);
      this.setIgnoreWhitespace(true);
      this.setPrettyPrinting(true);
      this.setPrettyIndent(2);
   }

   final void setIgnoreComments(boolean b) {
      this.ignoreComments = b;
   }

   final void setIgnoreWhitespace(boolean b) {
      this.ignoreWhitespace = b;
   }

   final void setIgnoreProcessingInstructions(boolean b) {
      this.ignoreProcessingInstructions = b;
   }

   final void setPrettyPrinting(boolean b) {
      this.prettyPrint = b;
   }

   final void setPrettyIndent(int i) {
      this.prettyIndent = i;
   }

   final boolean isIgnoreComments() {
      return this.ignoreComments;
   }

   final boolean isIgnoreProcessingInstructions() {
      return this.ignoreProcessingInstructions;
   }

   final boolean isIgnoreWhitespace() {
      return this.ignoreWhitespace;
   }

   final boolean isPrettyPrinting() {
      return this.prettyPrint;
   }

   final int getPrettyIndent() {
      return this.prettyIndent;
   }

   private String toXmlNewlines(String rv) {
      StringBuffer nl = new StringBuffer();

      for(int i = 0; i < rv.length(); ++i) {
         if (rv.charAt(i) == '\r') {
            if (rv.charAt(i + 1) != '\n') {
               nl.append('\n');
            }
         } else {
            nl.append(rv.charAt(i));
         }
      }

      return nl.toString();
   }

   private DocumentBuilderFactory getDomFactory() {
      return this.dom;
   }

   private DocumentBuilder getDocumentBuilderFromPool() throws ParserConfigurationException {
      DocumentBuilder builder = (DocumentBuilder)this.documentBuilderPool.pollFirst();
      if (builder == null) {
         builder = this.getDomFactory().newDocumentBuilder();
      }

      builder.setErrorHandler(this.errorHandler);
      return builder;
   }

   private void returnDocumentBuilderToPool(DocumentBuilder db) {
      try {
         db.reset();
         this.documentBuilderPool.offerFirst(db);
      } catch (UnsupportedOperationException var3) {
      }

   }

   private void addProcessingInstructionsTo(List list, Node node) {
      if (node instanceof ProcessingInstruction) {
         list.add(node);
      }

      if (node.getChildNodes() != null) {
         for(int i = 0; i < node.getChildNodes().getLength(); ++i) {
            this.addProcessingInstructionsTo(list, node.getChildNodes().item(i));
         }
      }

   }

   private void addCommentsTo(List list, Node node) {
      if (node instanceof Comment) {
         list.add(node);
      }

      if (node.getChildNodes() != null) {
         for(int i = 0; i < node.getChildNodes().getLength(); ++i) {
            this.addProcessingInstructionsTo(list, node.getChildNodes().item(i));
         }
      }

   }

   private void addTextNodesToRemoveAndTrim(List toRemove, Node node) {
      if (node instanceof Text) {
         Text text = (Text)node;
         boolean BUG_369394_IS_VALID = false;
         if (!BUG_369394_IS_VALID) {
            text.setData(text.getData().trim());
         } else if (text.getData().trim().length() == 0) {
            text.setData("");
         }

         if (text.getData().length() == 0) {
            toRemove.add(node);
         }
      }

      if (node.getChildNodes() != null) {
         for(int i = 0; i < node.getChildNodes().getLength(); ++i) {
            this.addTextNodesToRemoveAndTrim(toRemove, node.getChildNodes().item(i));
         }
      }

   }

   final Node toXml(String defaultNamespaceUri, String xml) throws SAXException {
      DocumentBuilder builder = null;

      Node var25;
      try {
         String syntheticXml = "<parent xmlns=\"" + defaultNamespaceUri + "\">" + xml + "</parent>";
         builder = this.getDocumentBuilderFromPool();
         Document document = builder.parse(new InputSource(new StringReader(syntheticXml)));
         if (this.ignoreProcessingInstructions) {
            List<Node> list = new ArrayList();
            this.addProcessingInstructionsTo(list, document);

            for(Node node : list) {
               node.getParentNode().removeChild(node);
            }
         }

         if (this.ignoreComments) {
            List<Node> list = new ArrayList();
            this.addCommentsTo(list, document);

            for(Node node : list) {
               node.getParentNode().removeChild(node);
            }
         }

         if (this.ignoreWhitespace) {
            List<Node> list = new ArrayList();
            this.addTextNodesToRemoveAndTrim(list, document);

            for(Node node : list) {
               node.getParentNode().removeChild(node);
            }
         }

         NodeList rv = document.getDocumentElement().getChildNodes();
         if (rv.getLength() > 1) {
            throw ScriptRuntime.constructError("SyntaxError", "XML objects may contain at most one node.");
         }

         if (rv.getLength() != 0) {
            Node node = rv.item(0);
            document.getDocumentElement().removeChild(node);
            var25 = node;
            return var25;
         }

         Node node = document.createTextNode("");
         var25 = node;
      } catch (IOException var13) {
         throw new RuntimeException("Unreachable.");
      } catch (ParserConfigurationException e) {
         throw new RuntimeException(e);
      } finally {
         if (builder != null) {
            this.returnDocumentBuilderToPool(builder);
         }

      }

      return var25;
   }

   Document newDocument() {
      DocumentBuilder builder = null;

      Document var2;
      try {
         builder = this.getDocumentBuilderFromPool();
         var2 = builder.newDocument();
      } catch (ParserConfigurationException ex) {
         throw new RuntimeException(ex);
      } finally {
         if (builder != null) {
            this.returnDocumentBuilderToPool(builder);
         }

      }

      return var2;
   }

   private String toString(Node node) {
      DOMSource source = new DOMSource(node);
      StringWriter writer = new StringWriter();
      StreamResult result = new StreamResult(writer);

      try {
         Transformer transformer = this.xform.newTransformer();
         transformer.setOutputProperty("omit-xml-declaration", "yes");
         transformer.setOutputProperty("indent", "no");
         transformer.setOutputProperty("method", "xml");
         transformer.transform(source, result);
      } catch (TransformerConfigurationException ex) {
         throw new RuntimeException(ex);
      } catch (TransformerException ex) {
         throw new RuntimeException(ex);
      }

      return this.toXmlNewlines(writer.toString());
   }

   String escapeAttributeValue(Object value) {
      String text = ScriptRuntime.toString(value);
      if (text.length() == 0) {
         return "";
      } else {
         Document dom = this.newDocument();
         Element e = dom.createElement("a");
         e.setAttribute("b", text);
         String elementText = this.toString(e);
         int begin = elementText.indexOf(34);
         int end = elementText.lastIndexOf(34);
         return elementText.substring(begin + 1, end);
      }
   }

   String escapeTextValue(Object value) {
      if (value instanceof XMLObjectImpl) {
         return ((XMLObjectImpl)value).toXMLString();
      } else {
         String text = ScriptRuntime.toString(value);
         if (text.length() == 0) {
            return text;
         } else {
            Document dom = this.newDocument();
            Element e = dom.createElement("a");
            e.setTextContent(text);
            String elementText = this.toString(e);
            int begin = elementText.indexOf(62) + 1;
            int end = elementText.lastIndexOf(60);
            return begin < end ? elementText.substring(begin, end) : "";
         }
      }
   }

   private String escapeElementValue(String s) {
      return this.escapeTextValue(s);
   }

   private String elementToXmlString(Element element) {
      Element copy = (Element)element.cloneNode(true);
      if (this.prettyPrint) {
         this.beautifyElement(copy, 0);
      }

      return this.toString(copy);
   }

   final String ecmaToXmlString(Node node) {
      StringBuffer s = new StringBuffer();
      int indentLevel = 0;
      if (this.prettyPrint) {
         for(int i = 0; i < indentLevel; ++i) {
            s.append(' ');
         }
      }

      if (node instanceof Text) {
         String data = ((Text)node).getData();
         String v = this.prettyPrint ? data.trim() : data;
         s.append(this.escapeElementValue(v));
         return s.toString();
      } else if (node instanceof Attr) {
         String value = ((Attr)node).getValue();
         s.append(this.escapeAttributeValue(value));
         return s.toString();
      } else if (node instanceof Comment) {
         s.append("<!--" + ((Comment)node).getNodeValue() + "-->");
         return s.toString();
      } else if (node instanceof ProcessingInstruction) {
         ProcessingInstruction pi = (ProcessingInstruction)node;
         s.append("<?" + pi.getTarget() + " " + pi.getData() + "?>");
         return s.toString();
      } else {
         s.append(this.elementToXmlString((Element)node));
         return s.toString();
      }
   }

   private void beautifyElement(Element e, int indent) {
      StringBuffer s = new StringBuffer();
      s.append('\n');

      for(int i = 0; i < indent; ++i) {
         s.append(' ');
      }

      String afterContent = s.toString();

      for(int i = 0; i < this.prettyIndent; ++i) {
         s.append(' ');
      }

      String beforeContent = s.toString();
      ArrayList<Node> toIndent = new ArrayList();
      boolean indentChildren = false;

      for(int i = 0; i < e.getChildNodes().getLength(); ++i) {
         if (i == 1) {
            indentChildren = true;
         }

         if (e.getChildNodes().item(i) instanceof Text) {
            toIndent.add(e.getChildNodes().item(i));
         } else {
            indentChildren = true;
            toIndent.add(e.getChildNodes().item(i));
         }
      }

      if (indentChildren) {
         for(int i = 0; i < toIndent.size(); ++i) {
            e.insertBefore(e.getOwnerDocument().createTextNode(beforeContent), (Node)toIndent.get(i));
         }
      }

      NodeList nodes = e.getChildNodes();
      ArrayList<Element> list = new ArrayList();

      for(int i = 0; i < nodes.getLength(); ++i) {
         if (nodes.item(i) instanceof Element) {
            list.add((Element)nodes.item(i));
         }
      }

      for(Element elem : list) {
         this.beautifyElement(elem, indent + this.prettyIndent);
      }

      if (indentChildren) {
         e.appendChild(e.getOwnerDocument().createTextNode(afterContent));
      }

   }

   private static class RhinoSAXErrorHandler implements ErrorHandler, Serializable {
      private static final long serialVersionUID = 6918417235413084055L;

      private RhinoSAXErrorHandler() {
         super();
      }

      private void throwError(SAXParseException e) {
         throw ScriptRuntime.constructError("TypeError", e.getMessage(), e.getLineNumber() - 1);
      }

      public void error(SAXParseException e) {
         this.throwError(e);
      }

      public void fatalError(SAXParseException e) {
         this.throwError(e);
      }

      public void warning(SAXParseException e) {
         Context.reportWarning(e.getMessage());
      }
   }
}
