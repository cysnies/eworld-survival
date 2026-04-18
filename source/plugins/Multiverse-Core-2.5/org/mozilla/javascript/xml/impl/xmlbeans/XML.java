package org.mozilla.javascript.xml.impl.xmlbeans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlObject.Factory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ObjArray;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

class XML extends XMLObjectImpl {
   static final long serialVersionUID = -630969919086449092L;
   private XScriptAnnotation _anno;
   private static final int APPEND_CHILD = 1;
   private static final int PREPEND_CHILD = 2;

   private XML(XMLLibImpl lib, XScriptAnnotation anno) {
      super(lib, lib.xmlPrototype);
      this._anno = anno;
      this._anno._xScriptXML = this;
   }

   static XML createEmptyXML(XMLLibImpl lib) {
      XmlObject xo = Factory.newInstance();
      XmlCursor curs = xo.newCursor();

      XScriptAnnotation anno;
      try {
         anno = new XScriptAnnotation(curs);
         curs.setBookmark(anno);
      } finally {
         curs.dispose();
      }

      return new XML(lib, anno);
   }

   private static XML createXML(XMLLibImpl lib, XmlCursor curs) {
      if (curs.currentTokenType().isStartdoc()) {
         curs.toFirstContentToken();
      }

      XScriptAnnotation anno = findAnnotation(curs);
      return new XML(lib, anno);
   }

   private static XML createAttributeXML(XMLLibImpl lib, XmlCursor cursor) {
      if (!cursor.isAttr()) {
         throw new IllegalArgumentException();
      } else {
         XScriptAnnotation anno = new XScriptAnnotation(cursor);
         cursor.setBookmark(anno);
         return new XML(lib, anno);
      }
   }

   static XML createTextElement(XMLLibImpl lib, javax.xml.namespace.QName qname, String value) {
      XmlObject xo = Factory.newInstance();
      XmlCursor cursor = xo.newCursor();

      XScriptAnnotation anno;
      try {
         cursor.toNextToken();
         cursor.beginElement(qname.getLocalPart(), qname.getNamespaceURI());
         cursor.insertChars(value);
         cursor.toStartDoc();
         cursor.toNextToken();
         anno = new XScriptAnnotation(cursor);
         cursor.setBookmark(anno);
      } finally {
         cursor.dispose();
      }

      return new XML(lib, anno);
   }

   static XML createFromXmlObject(XMLLibImpl lib, XmlObject xo) {
      XmlCursor curs = xo.newCursor();
      if (curs.currentTokenType().isStartdoc()) {
         curs.toFirstContentToken();
      }

      XScriptAnnotation anno;
      try {
         anno = new XScriptAnnotation(curs);
         curs.setBookmark(anno);
      } finally {
         curs.dispose();
      }

      return new XML(lib, anno);
   }

   static XML createFromJS(XMLLibImpl lib, Object inputObject) {
      boolean isText = false;
      String frag;
      if (inputObject != null && inputObject != Undefined.instance) {
         if (inputObject instanceof XMLObjectImpl) {
            frag = ((XMLObjectImpl)inputObject).toXMLString(0);
         } else {
            if (inputObject instanceof Wrapper) {
               Object wrapped = ((Wrapper)inputObject).unwrap();
               if (wrapped instanceof XmlObject) {
                  return createFromXmlObject(lib, (XmlObject)wrapped);
               }
            }

            frag = ScriptRuntime.toString(inputObject);
         }
      } else {
         frag = "";
      }

      if (frag.trim().startsWith("<>")) {
         throw ScriptRuntime.typeError("Invalid use of XML object anonymous tags <></>.");
      } else {
         if (frag.indexOf("<") == -1) {
            isText = true;
            frag = "<textFragment>" + frag + "</textFragment>";
         }

         XmlOptions options = new XmlOptions();
         if (lib.ignoreComments) {
            options.put("LOAD_STRIP_COMMENTS");
         }

         if (lib.ignoreProcessingInstructions) {
            options.put("LOAD_STRIP_PROCINSTS");
         }

         if (lib.ignoreWhitespace) {
            options.put("LOAD_STRIP_WHITESPACE");
         }

         XmlObject xo;
         try {
            xo = Factory.parse(frag, options);
            Context cx = Context.getCurrentContext();
            String defaultURI = lib.getDefaultNamespaceURI(cx);
            if (defaultURI.length() > 0) {
               XmlCursor cursor = xo.newCursor();
               boolean isRoot = true;

               while(!cursor.toNextToken().isEnddoc()) {
                  if (cursor.isStart()) {
                     boolean defaultNSDeclared = false;
                     cursor.push();

                     while(cursor.toNextToken().isAnyAttr()) {
                        if (cursor.isNamespace() && cursor.getName().getLocalPart().length() == 0) {
                           defaultNSDeclared = true;
                           break;
                        }
                     }

                     cursor.pop();
                     if (defaultNSDeclared) {
                        cursor.toEndToken();
                     } else {
                        javax.xml.namespace.QName qname = cursor.getName();
                        if (qname.getNamespaceURI().length() == 0) {
                           qname = new javax.xml.namespace.QName(defaultURI, qname.getLocalPart());
                           cursor.setName(qname);
                        }

                        if (isRoot) {
                           cursor.push();
                           cursor.toNextToken();
                           cursor.insertNamespace("", defaultURI);
                           cursor.pop();
                           isRoot = false;
                        }
                     }
                  }
               }

               cursor.dispose();
            }
         } catch (XmlException xe) {
            String errMsg = xe.getMessage();
            if (!errMsg.equals("error: Unexpected end of file after null")) {
               throw ScriptRuntime.typeError(xe.getMessage());
            }

            xo = Factory.newInstance();
         } catch (Throwable var18) {
            throw ScriptRuntime.typeError("Not Parsable as XML");
         }

         XmlCursor curs = xo.newCursor();
         if (curs.currentTokenType().isStartdoc()) {
            curs.toFirstContentToken();
         }

         if (isText) {
            curs.toFirstContentToken();
         }

         XScriptAnnotation anno;
         try {
            anno = new XScriptAnnotation(curs);
            curs.setBookmark(anno);
         } finally {
            curs.dispose();
         }

         return new XML(lib, anno);
      }
   }

   static XML getFromAnnotation(XMLLibImpl lib, XScriptAnnotation anno) {
      if (anno._xScriptXML == null) {
         anno._xScriptXML = new XML(lib, anno);
      }

      return anno._xScriptXML;
   }

   private static XmlCursor.TokenType skipNonElements(XmlCursor curs) {
      XmlCursor.TokenType tt;
      for(tt = curs.currentTokenType(); tt.isComment() || tt.isProcinst(); tt = curs.toNextToken()) {
      }

      return tt;
   }

   protected static XScriptAnnotation findAnnotation(XmlCursor curs) {
      XmlCursor.XmlBookmark anno = curs.getBookmark(XScriptAnnotation.class);
      if (anno == null) {
         anno = new XScriptAnnotation(curs);
         curs.setBookmark(anno);
      }

      return (XScriptAnnotation)anno;
   }

   private XmlOptions getOptions() {
      XmlOptions options = new XmlOptions();
      if (this.lib.ignoreComments) {
         options.put("LOAD_STRIP_COMMENTS");
      }

      if (this.lib.ignoreProcessingInstructions) {
         options.put("LOAD_STRIP_PROCINSTS");
      }

      if (this.lib.ignoreWhitespace) {
         options.put("LOAD_STRIP_WHITESPACE");
      }

      if (this.lib.prettyPrinting) {
         options.put("SAVE_PRETTY_PRINT", (Object)null);
         options.put("SAVE_PRETTY_PRINT_INDENT", new Integer(this.lib.prettyIndent));
      }

      return options;
   }

   private static String dumpNode(XmlCursor cursor, XmlOptions opts) {
      if (cursor.isText()) {
         return cursor.getChars();
      } else if (cursor.isFinish()) {
         return "";
      } else {
         cursor.push();
         boolean wanRawText = cursor.isStartdoc() && !cursor.toFirstChild();
         cursor.pop();
         return wanRawText ? cursor.getTextValue() : cursor.xmlText(opts);
      }
   }

   private XmlCursor newCursor() {
      XmlCursor curs;
      if (this._anno != null) {
         curs = this._anno.createCursor();
         if (curs == null) {
            XmlObject doc = Factory.newInstance();
            curs = doc.newCursor();
            if (this._anno._name != null) {
               curs.toNextToken();
               curs.insertElement(this._anno._name);
               curs.toPrevSibling();
            }

            curs.setBookmark(this._anno);
         }
      } else {
         XmlObject doc = Factory.newInstance();
         curs = doc.newCursor();
      }

      return curs;
   }

   private boolean moveToChild(XmlCursor curs, long index, boolean fFirstChild, boolean fUseStartDoc) {
      if (index < 0L) {
         throw new IllegalArgumentException();
      } else {
         long idxChild = 0L;
         if (!fUseStartDoc && curs.currentTokenType().isStartdoc()) {
            curs.toFirstContentToken();
         }

         XmlCursor.TokenType tt = curs.toFirstContentToken();
         if (!tt.isNone() && !tt.isEnd()) {
            while(true) {
               if (index == idxChild) {
                  return true;
               }

               tt = curs.currentTokenType();
               if (tt.isText()) {
                  curs.toNextToken();
               } else {
                  if (!tt.isStart()) {
                     if (!tt.isComment() && !tt.isProcinst()) {
                        break;
                     }
                     continue;
                  }

                  curs.toEndToken();
                  curs.toNextToken();
               }

               ++idxChild;
            }
         } else if (fFirstChild && index == 0L) {
            return true;
         }

         return false;
      }
   }

   XmlCursor.TokenType tokenType() {
      XmlCursor curs = this.newCursor();
      if (curs.isStartdoc()) {
         curs.toFirstContentToken();
      }

      XmlCursor.TokenType result = curs.currentTokenType();
      curs.dispose();
      return result;
   }

   private boolean moveSrcToDest(XmlCursor srcCurs, XmlCursor destCurs, boolean fDontMoveIfSame) {
      boolean fMovedSomething = true;

      XmlCursor.TokenType tt;
      do {
         if (fDontMoveIfSame && srcCurs.isInSameDocument(destCurs) && srcCurs.comparePosition(destCurs) == 0) {
            fMovedSomething = false;
            break;
         }

         if (destCurs.currentTokenType().isStartdoc()) {
            destCurs.toNextToken();
         }

         XmlCursor copyCurs = this.copy(srcCurs);
         copyCurs.moveXml(destCurs);
         copyCurs.dispose();
         tt = srcCurs.currentTokenType();
      } while(!tt.isStart() && !tt.isEnd() && !tt.isEnddoc());

      return fMovedSomething;
   }

   private XmlCursor copy(XmlCursor cursToCopy) {
      XmlObject xo = Factory.newInstance();
      XmlCursor copyCurs = null;
      if (cursToCopy.currentTokenType().isText()) {
         try {
            copyCurs = Factory.parse("<x:fragment xmlns:x=\"http://www.openuri.org/fragment\">" + cursToCopy.getChars() + "</x:fragment>").newCursor();
            if (!cursToCopy.toNextSibling() && cursToCopy.currentTokenType().isText()) {
               cursToCopy.toNextToken();
            }
         } catch (Exception ex) {
            throw ScriptRuntime.typeError(ex.getMessage());
         }
      } else {
         copyCurs = xo.newCursor();
         copyCurs.toFirstContentToken();
         if (cursToCopy.currentTokenType() == TokenType.STARTDOC) {
            cursToCopy.toNextToken();
         }

         cursToCopy.copyXml(copyCurs);
         if (!cursToCopy.toNextSibling() && cursToCopy.currentTokenType().isText()) {
            cursToCopy.toNextToken();
         }
      }

      copyCurs.toStartDoc();
      copyCurs.toFirstContentToken();
      return copyCurs;
   }

   private void insertChild(XmlCursor curs, Object xmlToInsert) {
      if (xmlToInsert != null && !(xmlToInsert instanceof Undefined)) {
         if (xmlToInsert instanceof XmlCursor) {
            this.moveSrcToDest((XmlCursor)xmlToInsert, curs, true);
         } else if (xmlToInsert instanceof XML) {
            XML xmlValue = (XML)xmlToInsert;
            if (xmlValue.tokenType() == TokenType.ATTR) {
               this.insertChild(curs, xmlValue.toString());
            } else {
               XmlCursor cursToInsert = ((XML)xmlToInsert).newCursor();
               this.moveSrcToDest(cursToInsert, curs, true);
               cursToInsert.dispose();
            }
         } else if (xmlToInsert instanceof XMLList) {
            XMLList list = (XMLList)xmlToInsert;

            for(int i = 0; i < list.length(); ++i) {
               this.insertChild(curs, list.item(i));
            }
         } else {
            String xmlStr = ScriptRuntime.toString(xmlToInsert);
            XmlObject xo = Factory.newInstance();
            XmlCursor sourceCurs = xo.newCursor();
            sourceCurs.toNextToken();
            sourceCurs.insertChars(xmlStr);
            sourceCurs.toPrevToken();
            this.moveSrcToDest(sourceCurs, curs, true);
         }
      }

   }

   private void insertChild(XML childToMatch, Object xmlToInsert, int addToType) {
      XmlCursor curs = this.newCursor();
      XmlCursor.TokenType tt = curs.currentTokenType();
      XmlCursor xmlChildCursor = childToMatch.newCursor();
      if (tt.isStartdoc()) {
         tt = curs.toFirstContentToken();
      }

      if (tt.isContainer()) {
         for(XmlCursor.TokenType var7 = curs.toNextToken(); !var7.isEnd(); var7 = curs.toNextToken()) {
            if (var7.isStart() && curs.comparePosition(xmlChildCursor) == 0) {
               if (addToType == 1) {
                  curs.toEndToken();
                  curs.toNextToken();
               }

               this.insertChild(curs, xmlToInsert);
               break;
            }

            if (var7.isStart()) {
               tt = curs.toEndToken();
            }
         }
      }

      xmlChildCursor.dispose();
      curs.dispose();
   }

   protected void removeToken(XmlCursor curs) {
      XmlObject xo = Factory.newInstance();
      XmlCursor tmpCurs = xo.newCursor();
      tmpCurs.toFirstContentToken();
      curs.moveXml(tmpCurs);
      tmpCurs.dispose();
   }

   protected void removeChild(long index) {
      XmlCursor curs = this.newCursor();
      if (this.moveToChild(curs, index, false, false)) {
         this.removeToken(curs);
      }

      curs.dispose();
   }

   protected static javax.xml.namespace.QName computeQName(Object name) {
      if (name instanceof String) {
         String ns = null;
         String localName = null;
         String fullName = (String)name;
         localName = fullName;
         if (fullName.startsWith("\"")) {
            int idx = fullName.indexOf(":");
            if (idx != -1) {
               ns = fullName.substring(1, idx - 1);
               localName = fullName.substring(idx + 1);
            }
         }

         return ns == null ? new javax.xml.namespace.QName(localName) : new javax.xml.namespace.QName(ns, localName);
      } else {
         return null;
      }
   }

   private void replace(XmlCursor destCurs, XML newValue) {
      if (destCurs.isStartdoc()) {
         destCurs.toFirstContentToken();
      }

      this.removeToken(destCurs);
      XmlCursor srcCurs = newValue.newCursor();
      if (srcCurs.currentTokenType().isStartdoc()) {
         srcCurs.toFirstContentToken();
      }

      this.moveSrcToDest(srcCurs, destCurs, false);
      if (!destCurs.toPrevSibling()) {
         destCurs.toPrevToken();
      }

      destCurs.setBookmark(new XScriptAnnotation(destCurs));
      destCurs.toEndToken();
      destCurs.toNextToken();
      srcCurs.dispose();
   }

   private boolean doPut(XMLName name, XML currXMLNode, XMLObjectImpl xmlValue) {
      boolean result = false;
      XmlCursor curs = currXMLNode.newCursor();

      try {
         int toAssignLen = xmlValue.length();

         for(int i = 0; i < toAssignLen; ++i) {
            XML xml;
            if (xmlValue instanceof XMLList) {
               xml = ((XMLList)xmlValue).item(i);
            } else {
               xml = (XML)xmlValue;
            }

            XmlCursor.TokenType tt = xml.tokenType();
            if (tt == TokenType.ATTR || tt == TokenType.TEXT) {
               xml = this.makeXmlFromString(this.lib, name, xml.toString());
            }

            if (i == 0) {
               this.replace(curs, xml);
            } else {
               this.insertChild(curs, xml);
            }
         }

         result = true;
      } catch (Exception ex) {
         ex.printStackTrace();
         throw ScriptRuntime.typeError(ex.getMessage());
      } finally {
         curs.dispose();
      }

      return result;
   }

   private XML makeXmlFromString(XMLLibImpl lib, XMLName name, String value) {
      javax.xml.namespace.QName qname;
      try {
         qname = new javax.xml.namespace.QName(name.uri(), name.localName());
      } catch (Exception e) {
         throw ScriptRuntime.typeError(e.getMessage());
      }

      XML result = createTextElement(lib, qname, value);
      return result;
   }

   private XMLList matchAttributes(XMLName xmlName) {
      XMLList result = new XMLList(this.lib);
      XmlCursor curs = this.newCursor();
      if (curs.currentTokenType().isStartdoc()) {
         curs.toFirstContentToken();
      }

      if (curs.isStart() && curs.toFirstAttribute()) {
         do {
            if (this.qnameMatches(xmlName, curs.getName())) {
               result.addToList(this.createAttributeObject(curs));
            }
         } while(curs.toNextAttribute());
      }

      curs.dispose();
      return result;
   }

   private XML createAttributeObject(XmlCursor attrCurs) {
      XML result = null;
      if (attrCurs.currentTokenType().isAttr()) {
         result = createAttributeXML(this.lib, attrCurs);
      }

      return result;
   }

   public String getClassName() {
      return "XML";
   }

   public Object get(int index, Scriptable start) {
      return index == 0 ? this : Scriptable.NOT_FOUND;
   }

   boolean hasXMLProperty(XMLName xmlName) {
      return this.getPropertyList(xmlName).length() > 0;
   }

   public boolean has(int index, Scriptable start) {
      return index == 0;
   }

   public Object[] getIds() {
      Object[] enumObjs;
      if (this.prototypeFlag) {
         enumObjs = new Object[0];
      } else {
         enumObjs = new Object[1];
         enumObjs[0] = new Integer(0);
      }

      return enumObjs;
   }

   public Object[] getIdsForDebug() {
      return this.getIds();
   }

   Object getXMLProperty(XMLName xmlName) {
      return this.getPropertyList(xmlName);
   }

   void putXMLProperty(XMLName xmlName, Object value) {
      if (!this.prototypeFlag) {
         if (value == null) {
            value = "null";
         } else if (value instanceof Undefined) {
            value = "undefined";
         }

         if (xmlName.isAttributeName()) {
            this.setAttribute(xmlName, value);
         } else if (xmlName.uri() == null && xmlName.localName().equals("*")) {
            this.setChildren(value);
         } else {
            XMLObjectImpl xmlValue = null;
            if (value instanceof XMLObjectImpl) {
               xmlValue = (XMLObjectImpl)value;
               if (xmlValue instanceof XML && ((XML)xmlValue).tokenType() == TokenType.ATTR) {
                  xmlValue = this.makeXmlFromString(this.lib, xmlName, xmlValue.toString());
               }

               if (xmlValue instanceof XMLList) {
                  for(int i = 0; i < xmlValue.length(); ++i) {
                     XML xml = ((XMLList)xmlValue).item(i);
                     if (xml.tokenType() == TokenType.ATTR) {
                        ((XMLList)xmlValue).replace(i, this.makeXmlFromString(this.lib, xmlName, xml.toString()));
                     }
                  }
               }
            } else {
               xmlValue = this.makeXmlFromString(this.lib, xmlName, ScriptRuntime.toString(value));
            }

            XMLList matches = this.getPropertyList(xmlName);
            if (matches.length() == 0) {
               this.appendChild(xmlValue);
            } else {
               for(int i = 1; i < matches.length(); ++i) {
                  this.removeChild((long)matches.item(i).childIndex());
               }

               this.doPut(xmlName, matches.item(0), xmlValue);
            }
         }
      }

   }

   public void put(int index, Scriptable start, Object value) {
      throw ScriptRuntime.typeError("Assignment to indexed XML is not allowed");
   }

   void deleteXMLProperty(XMLName name) {
      if (!name.isDescendants() && name.isAttributeName()) {
         XmlCursor curs = this.newCursor();
         if (name.localName().equals("*")) {
            if (curs.toFirstAttribute()) {
               while(curs.currentTokenType().isAttr()) {
                  curs.removeXml();
               }
            }
         } else {
            javax.xml.namespace.QName qname = new javax.xml.namespace.QName(name.uri(), name.localName());
            curs.removeAttribute(qname);
         }

         curs.dispose();
      } else {
         XMLList matches = this.getPropertyList(name);
         matches.remove();
      }

   }

   public void delete(int index) {
      if (index == 0) {
         this.remove();
      }

   }

   protected XScriptAnnotation getAnnotation() {
      return this._anno;
   }

   protected void changeNS(String oldURI, String newURI) {
      XmlCursor curs = this.newCursor();

      while(curs.toParent()) {
      }

      XmlCursor.TokenType tt = curs.currentTokenType();
      if (tt.isStartdoc()) {
         tt = curs.toFirstContentToken();
      }

      if (tt.isStart()) {
         do {
            if (tt.isStart() || tt.isAttr() || tt.isNamespace()) {
               javax.xml.namespace.QName currQName = curs.getName();
               if (oldURI.equals(currQName.getNamespaceURI())) {
                  curs.setName(new javax.xml.namespace.QName(newURI, currQName.getLocalPart()));
               }
            }

            tt = curs.toNextToken();
         } while(!tt.isEnddoc() && !tt.isNone());
      }

      curs.dispose();
   }

   void remove() {
      XmlCursor childCurs = this.newCursor();
      if (childCurs.currentTokenType().isStartdoc()) {
         for(XmlCursor.TokenType tt = childCurs.toFirstContentToken(); !tt.isEnd() && !tt.isEnddoc(); tt = childCurs.currentTokenType()) {
            this.removeToken(childCurs);
         }
      } else {
         this.removeToken(childCurs);
      }

      childCurs.dispose();
   }

   void replaceAll(XML value) {
      XmlCursor curs = this.newCursor();
      this.replace(curs, value);
      this._anno = value._anno;
      curs.dispose();
   }

   void setAttribute(XMLName xmlName, Object value) {
      if (xmlName.uri() == null && xmlName.localName().equals("*")) {
         throw ScriptRuntime.typeError("@* assignment not supported.");
      } else {
         XmlCursor curs = this.newCursor();
         String strValue = ScriptRuntime.toString(value);
         if (curs.currentTokenType().isStartdoc()) {
            curs.toFirstContentToken();
         }

         javax.xml.namespace.QName qName;
         try {
            qName = new javax.xml.namespace.QName(xmlName.uri(), xmlName.localName());
         } catch (Exception e) {
            throw ScriptRuntime.typeError(e.getMessage());
         }

         if (!curs.setAttributeText(qName, strValue)) {
            if (curs.currentTokenType().isStart()) {
               curs.toNextToken();
            }

            curs.insertAttributeWithValue(qName, strValue);
         }

         curs.dispose();
      }
   }

   private XMLList allChildNodes(String namespace) {
      XMLList result = new XMLList(this.lib);
      XmlCursor curs = this.newCursor();
      XmlCursor.TokenType tt = curs.currentTokenType();
      javax.xml.namespace.QName targetProperty = new javax.xml.namespace.QName(namespace, "*");
      if (tt.isStartdoc()) {
         tt = curs.toFirstContentToken();
      }

      if (tt.isContainer()) {
         for(XmlCursor.TokenType var6 = curs.toFirstContentToken(); !var6.isEnd(); var6 = curs.toNextToken()) {
            if (!var6.isStart()) {
               result.addToList(findAnnotation(curs));
               targetProperty = null;
            } else if (namespace == null || namespace.length() == 0 || namespace.equals("*") || curs.getName().getNamespaceURI().equals(namespace)) {
               result.addToList(findAnnotation(curs));
               if (targetProperty != null) {
                  if (targetProperty.getLocalPart().equals("*")) {
                     targetProperty = curs.getName();
                  } else if (!targetProperty.getLocalPart().equals(curs.getName().getLocalPart())) {
                     targetProperty = null;
                  }
               }
            }

            if (var6.isStart()) {
               tt = curs.toEndToken();
            }
         }
      }

      curs.dispose();
      result.setTargets(this, targetProperty);
      return result;
   }

   private XMLList matchDescendantAttributes(XMLName xmlName) {
      XMLList result = new XMLList(this.lib);
      XmlCursor curs = this.newCursor();
      XmlCursor.TokenType tt = curs.currentTokenType();
      result.setTargets(this, (javax.xml.namespace.QName)null);
      if (tt.isStartdoc()) {
         tt = curs.toFirstContentToken();
      }

      if (tt.isContainer()) {
         int nestLevel = 1;

         while(nestLevel > 0) {
            tt = curs.toNextToken();
            if (tt.isAttr() && this.qnameMatches(xmlName, curs.getName())) {
               result.addToList(findAnnotation(curs));
            }

            if (tt.isStart()) {
               ++nestLevel;
            } else if (tt.isEnd()) {
               --nestLevel;
            } else if (tt.isEnddoc()) {
               break;
            }
         }
      }

      curs.dispose();
      return result;
   }

   private XMLList matchDescendantChildren(XMLName xmlName) {
      XMLList result = new XMLList(this.lib);
      XmlCursor curs = this.newCursor();
      XmlCursor.TokenType tt = curs.currentTokenType();
      result.setTargets(this, (javax.xml.namespace.QName)null);
      if (tt.isStartdoc()) {
         tt = curs.toFirstContentToken();
      }

      if (tt.isContainer()) {
         int nestLevel = 1;

         while(nestLevel > 0) {
            tt = curs.toNextToken();
            if (!tt.isAttr() && !tt.isEnd() && !tt.isEnddoc()) {
               if (!tt.isStart() && !tt.isProcinst()) {
                  if (xmlName.localName().equals("*")) {
                     result.addToList(findAnnotation(curs));
                  }
               } else if (this.qnameMatches(xmlName, curs.getName())) {
                  result.addToList(findAnnotation(curs));
               }
            }

            if (tt.isStart()) {
               ++nestLevel;
            } else if (tt.isEnd()) {
               --nestLevel;
            } else if (tt.isEnddoc()) {
               break;
            }
         }
      }

      curs.dispose();
      return result;
   }

   private XMLList matchChildren(XmlCursor.TokenType tokenType) {
      return this.matchChildren(tokenType, XMLName.formStar());
   }

   private XMLList matchChildren(XmlCursor.TokenType tokenType, XMLName name) {
      XMLList result = new XMLList(this.lib);
      XmlCursor curs = this.newCursor();
      XmlCursor.TokenType tt = curs.currentTokenType();
      javax.xml.namespace.QName qname = new javax.xml.namespace.QName(name.uri(), name.localName());
      javax.xml.namespace.QName targetProperty = qname;
      if (tt.isStartdoc()) {
         tt = curs.toFirstContentToken();
      }

      if (tt.isContainer()) {
         for(XmlCursor.TokenType var8 = curs.toFirstContentToken(); !var8.isEnd(); var8 = curs.toNextToken()) {
            if (var8 == tokenType) {
               if (!var8.isStart() && !var8.isProcinst()) {
                  result.addToList(findAnnotation(curs));
                  targetProperty = null;
               } else if (this.qnameMatches(name, curs.getName())) {
                  result.addToList(findAnnotation(curs));
                  if (targetProperty != null) {
                     if (targetProperty.getLocalPart().equals("*")) {
                        targetProperty = curs.getName();
                     } else if (!targetProperty.getLocalPart().equals(curs.getName().getLocalPart())) {
                        targetProperty = null;
                     }
                  }
               }
            }

            if (var8.isStart()) {
               tt = curs.toEndToken();
            }
         }
      }

      curs.dispose();
      if (tokenType == TokenType.START) {
         result.setTargets(this, targetProperty);
      }

      return result;
   }

   private boolean qnameMatches(XMLName template, javax.xml.namespace.QName match) {
      boolean matches = false;
      if ((template.uri() == null || template.uri().equals(match.getNamespaceURI())) && (template.localName().equals("*") || template.localName().equals(match.getLocalPart()))) {
         matches = true;
      }

      return matches;
   }

   XML addNamespace(Namespace ns) {
      String nsPrefix = ns.prefix();
      if (nsPrefix == null) {
         return this;
      } else {
         XmlCursor cursor = this.newCursor();

         XML var4;
         try {
            if (cursor.isContainer()) {
               javax.xml.namespace.QName qname = cursor.getName();
               if (qname.getNamespaceURI().equals("") && nsPrefix.equals("")) {
                  XML var13 = this;
                  return var13;
               }

               Map prefixToURI = NamespaceHelper.getAllNamespaces(this.lib, cursor);
               String uri = (String)prefixToURI.get(nsPrefix);
               if (uri != null) {
                  if (uri.equals(ns.uri())) {
                     XML var14 = this;
                     return var14;
                  }

                  cursor.push();

                  while(cursor.toNextToken().isAnyAttr()) {
                     if (cursor.isNamespace()) {
                        qname = cursor.getName();
                        String prefix = qname.getLocalPart();
                        if (prefix.equals(nsPrefix)) {
                           cursor.removeXml();
                           break;
                        }
                     }
                  }

                  cursor.pop();
               }

               cursor.toNextToken();
               cursor.insertNamespace(nsPrefix, ns.uri());
               return this;
            }

            var4 = this;
         } finally {
            cursor.dispose();
         }

         return var4;
      }
   }

   XML appendChild(Object xml) {
      XmlCursor curs = this.newCursor();
      if (curs.isStartdoc()) {
         curs.toFirstContentToken();
      }

      if (curs.isStart()) {
         curs.toEndToken();
      }

      this.insertChild(curs, xml);
      curs.dispose();
      return this;
   }

   XMLList attribute(XMLName xmlName) {
      return this.matchAttributes(xmlName);
   }

   XMLList attributes() {
      XMLName xmlName = XMLName.formStar();
      return this.matchAttributes(xmlName);
   }

   XMLList child(long index) {
      XMLList result = new XMLList(this.lib);
      result.setTargets(this, (javax.xml.namespace.QName)null);
      result.addToList(this.getXmlChild(index));
      return result;
   }

   XMLList child(XMLName xmlName) {
      if (xmlName == null) {
         return new XMLList(this.lib);
      } else {
         XMLList result;
         if (xmlName.localName().equals("*")) {
            result = this.allChildNodes(xmlName.uri());
         } else {
            result = this.matchChildren(TokenType.START, xmlName);
         }

         return result;
      }
   }

   XML getXmlChild(long index) {
      XML result = null;
      XmlCursor curs = this.newCursor();
      if (this.moveToChild(curs, index, false, true)) {
         result = createXML(this.lib, curs);
      }

      curs.dispose();
      return result;
   }

   int childIndex() {
      int index = 0;
      XmlCursor curs = this.newCursor();
      XmlCursor.TokenType tt = curs.currentTokenType();

      while(true) {
         if (tt.isText()) {
            ++index;
            if (!curs.toPrevSibling()) {
               break;
            }
         } else if (tt.isStart()) {
            tt = curs.toPrevToken();
            if (!tt.isEnd()) {
               break;
            }

            curs.toNextToken();
            if (!curs.toPrevSibling()) {
               break;
            }

            ++index;
         } else {
            if (!tt.isComment() && !tt.isProcinst()) {
               break;
            }

            curs.toPrevToken();
         }

         tt = curs.currentTokenType();
      }

      index = curs.currentTokenType().isStartdoc() ? -1 : index;
      curs.dispose();
      return index;
   }

   XMLList children() {
      return this.allChildNodes((String)null);
   }

   XMLList comments() {
      return this.matchChildren(TokenType.COMMENT);
   }

   boolean contains(Object xml) {
      boolean result = false;
      if (xml instanceof XML) {
         result = this.equivalentXml(xml);
      }

      return result;
   }

   Object copy() {
      XmlCursor srcCurs = this.newCursor();
      if (srcCurs.isStartdoc()) {
         srcCurs.toFirstContentToken();
      }

      XML xml = createEmptyXML(this.lib);
      XmlCursor destCurs = xml.newCursor();
      destCurs.toFirstContentToken();
      srcCurs.copyXml(destCurs);
      destCurs.dispose();
      srcCurs.dispose();
      return xml;
   }

   XMLList descendants(XMLName xmlName) {
      XMLList result;
      if (xmlName.isAttributeName()) {
         result = this.matchDescendantAttributes(xmlName);
      } else {
         result = this.matchDescendantChildren(xmlName);
      }

      return result;
   }

   Object[] inScopeNamespaces() {
      XmlCursor cursor = this.newCursor();
      Object[] namespaces = NamespaceHelper.inScopeNamespaces(this.lib, cursor);
      cursor.dispose();
      return namespaces;
   }

   XML insertChildAfter(Object child, Object xml) {
      if (child == null) {
         this.prependChild(xml);
      } else if (child instanceof XML) {
         this.insertChild((XML)child, xml, 1);
      }

      return this;
   }

   XML insertChildBefore(Object child, Object xml) {
      if (child == null) {
         this.appendChild(xml);
      } else if (child instanceof XML) {
         this.insertChild((XML)child, xml, 2);
      }

      return this;
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
      return !this.hasSimpleContent();
   }

   boolean hasSimpleContent() {
      boolean simpleContent = false;
      XmlCursor curs = this.newCursor();
      if (!curs.isAttr() && !curs.isText()) {
         if (curs.isStartdoc()) {
            curs.toFirstContentToken();
         }

         simpleContent = !curs.toFirstChild();
         curs.dispose();
         return simpleContent;
      } else {
         return true;
      }
   }

   int length() {
      return 1;
   }

   String localName() {
      XmlCursor cursor = this.newCursor();
      if (cursor.isStartdoc()) {
         cursor.toFirstContentToken();
      }

      String name = null;
      if (cursor.isStart() || cursor.isAttr() || cursor.isProcinst()) {
         javax.xml.namespace.QName qname = cursor.getName();
         name = qname.getLocalPart();
      }

      cursor.dispose();
      return name;
   }

   QName name() {
      XmlCursor cursor = this.newCursor();
      if (cursor.isStartdoc()) {
         cursor.toFirstContentToken();
      }

      QName name = null;
      if (cursor.isStart() || cursor.isAttr() || cursor.isProcinst()) {
         javax.xml.namespace.QName qname = cursor.getName();
         if (cursor.isProcinst()) {
            name = new QName(this.lib, "", qname.getLocalPart(), "");
         } else {
            String uri = qname.getNamespaceURI();
            String prefix = qname.getPrefix();
            name = new QName(this.lib, uri, qname.getLocalPart(), prefix);
         }
      }

      cursor.dispose();
      return name;
   }

   Object namespace(String prefix) {
      XmlCursor cursor = this.newCursor();
      if (cursor.isStartdoc()) {
         cursor.toFirstContentToken();
      }

      Object result = null;
      if (prefix == null) {
         if (cursor.isStart() || cursor.isAttr()) {
            Object[] inScopeNS = NamespaceHelper.inScopeNamespaces(this.lib, cursor);
            XmlCursor cursor2 = this.newCursor();
            if (cursor2.isStartdoc()) {
               cursor2.toFirstContentToken();
            }

            result = NamespaceHelper.getNamespace(this.lib, cursor2, inScopeNS);
            cursor2.dispose();
         }
      } else {
         Map prefixToURI = NamespaceHelper.getAllNamespaces(this.lib, cursor);
         String uri = (String)prefixToURI.get(prefix);
         result = uri == null ? Undefined.instance : new Namespace(this.lib, prefix, uri);
      }

      cursor.dispose();
      return result;
   }

   Object[] namespaceDeclarations() {
      XmlCursor cursor = this.newCursor();
      Object[] namespaces = NamespaceHelper.namespaceDeclarations(this.lib, cursor);
      cursor.dispose();
      return namespaces;
   }

   Object nodeKind() {
      XmlCursor.TokenType tt = this.tokenType();
      String result;
      if (tt == TokenType.ATTR) {
         result = "attribute";
      } else if (tt == TokenType.TEXT) {
         result = "text";
      } else if (tt == TokenType.COMMENT) {
         result = "comment";
      } else if (tt == TokenType.PROCINST) {
         result = "processing-instruction";
      } else if (tt == TokenType.START) {
         result = "element";
      } else {
         result = "text";
      }

      return result;
   }

   void normalize() {
      XmlCursor curs = this.newCursor();
      XmlCursor.TokenType tt = curs.currentTokenType();
      if (tt.isStartdoc()) {
         tt = curs.toFirstContentToken();
      }

      if (tt.isContainer()) {
         int nestLevel = 1;
         String previousText = null;

         while(nestLevel > 0) {
            tt = curs.toNextToken();
            if (tt == TokenType.TEXT) {
               String currentText = curs.getChars().trim();
               if (currentText.trim().length() == 0) {
                  this.removeToken(curs);
                  curs.toPrevToken();
               } else if (previousText == null) {
                  previousText = currentText;
               } else {
                  String newText = previousText + currentText;
                  curs.toPrevToken();
                  this.removeToken(curs);
                  this.removeToken(curs);
                  curs.insertChars(newText);
               }
            } else {
               previousText = null;
            }

            if (tt.isStart()) {
               ++nestLevel;
            } else if (tt.isEnd()) {
               --nestLevel;
            } else if (tt.isEnddoc()) {
               break;
            }
         }
      }

      curs.dispose();
   }

   Object parent() {
      XmlCursor curs = this.newCursor();
      Object parent;
      if (curs.isStartdoc()) {
         parent = Undefined.instance;
      } else if (curs.toParent()) {
         if (curs.isStartdoc()) {
            parent = Undefined.instance;
         } else {
            parent = getFromAnnotation(this.lib, findAnnotation(curs));
         }
      } else {
         parent = Undefined.instance;
      }

      curs.dispose();
      return parent;
   }

   XML prependChild(Object xml) {
      XmlCursor curs = this.newCursor();
      if (curs.isStartdoc()) {
         curs.toFirstContentToken();
      }

      curs.toFirstContentToken();
      this.insertChild(curs, xml);
      curs.dispose();
      return this;
   }

   Object processingInstructions(XMLName xmlName) {
      return this.matchChildren(TokenType.PROCINST, xmlName);
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

   XML removeNamespace(Namespace ns) {
      XmlCursor cursor = this.newCursor();

      XML var3;
      try {
         if (cursor.isStartdoc()) {
            cursor.toFirstContentToken();
         }

         if (cursor.isStart()) {
            String nsPrefix = ns.prefix();
            String nsURI = ns.uri();
            Map prefixToURI = new HashMap();
            int depth = 1;

            while(!cursor.isEnd() || depth != 0) {
               if (cursor.isStart()) {
                  prefixToURI.clear();
                  NamespaceHelper.getNamespaces(cursor, prefixToURI);
                  ObjArray inScopeNSBag = new ObjArray();

                  for(Map.Entry entry : prefixToURI.entrySet()) {
                     ns = new Namespace(this.lib, (String)entry.getKey(), (String)entry.getValue());
                     inScopeNSBag.add(ns);
                  }

                  ns = new Namespace(this.lib, nsURI);
                  inScopeNSBag.add(ns);
                  Object[] inScopeNS = inScopeNSBag.toArray();
                  Namespace n = NamespaceHelper.getNamespace(this.lib, cursor, inScopeNS);
                  if (nsURI.equals(n.uri()) && (nsPrefix == null || nsPrefix.equals(n.prefix()))) {
                     XML var22 = this;
                     return var22;
                  }

                  cursor.push();

                  for(boolean hasNext = cursor.toFirstAttribute(); hasNext; hasNext = cursor.toNextAttribute()) {
                     n = NamespaceHelper.getNamespace(this.lib, cursor, inScopeNS);
                     if (nsURI.equals(n.uri()) && (nsPrefix == null || nsPrefix.equals(n.prefix()))) {
                        XML var12 = this;
                        return var12;
                     }
                  }

                  cursor.pop();
                  if (nsPrefix == null) {
                     for(Map.Entry entry : prefixToURI.entrySet()) {
                        if (entry.getValue().equals(nsURI)) {
                           NamespaceHelper.removeNamespace(cursor, (String)entry.getKey());
                        }
                     }
                  } else if (nsURI.equals(prefixToURI.get(nsPrefix))) {
                     NamespaceHelper.removeNamespace(cursor, String.valueOf(nsPrefix));
                  }
               }

               switch (cursor.toNextToken().intValue()) {
                  case 3:
                     ++depth;
                     break;
                  case 4:
                     --depth;
               }
            }

            return this;
         }

         var3 = this;
      } finally {
         cursor.dispose();
      }

      return var3;
   }

   XML replace(long index, Object xml) {
      XMLList xlChildToReplace = this.child(index);
      if (xlChildToReplace.length() > 0) {
         XML childToReplace = xlChildToReplace.item(0);
         this.insertChildAfter(childToReplace, xml);
         this.removeChild(index);
      }

      return this;
   }

   XML replace(XMLName xmlName, Object xml) {
      this.putXMLProperty(xmlName, xml);
      return this;
   }

   XML setChildren(Object xml) {
      XMLName xmlName = XMLName.formStar();
      XMLList matches = this.getPropertyList(xmlName);
      matches.remove();
      this.appendChild(xml);
      return this;
   }

   void setLocalName(String localName) {
      XmlCursor cursor = this.newCursor();

      try {
         if (cursor.isStartdoc()) {
            cursor.toFirstContentToken();
         }

         if (!cursor.isText() && !cursor.isComment()) {
            javax.xml.namespace.QName qname = cursor.getName();
            cursor.setName(new javax.xml.namespace.QName(qname.getNamespaceURI(), localName, qname.getPrefix()));
            return;
         }
      } finally {
         cursor.dispose();
      }

   }

   void setName(QName qname) {
      XmlCursor cursor = this.newCursor();

      try {
         if (cursor.isStartdoc()) {
            cursor.toFirstContentToken();
         }

         if (!cursor.isText() && !cursor.isComment()) {
            if (cursor.isProcinst()) {
               String localName = qname.localName();
               cursor.setName(new javax.xml.namespace.QName(localName));
            } else {
               String prefix = qname.prefix();
               if (prefix == null) {
                  prefix = "";
               }

               cursor.setName(new javax.xml.namespace.QName(qname.uri(), qname.localName(), prefix));
            }

            return;
         }
      } finally {
         cursor.dispose();
      }

   }

   void setNamespace(Namespace ns) {
      XmlCursor cursor = this.newCursor();

      try {
         if (cursor.isStartdoc()) {
            cursor.toFirstContentToken();
         }

         if (!cursor.isText() && !cursor.isComment() && !cursor.isProcinst()) {
            String prefix = ns.prefix();
            if (prefix == null) {
               prefix = "";
            }

            cursor.setName(new javax.xml.namespace.QName(ns.uri(), this.localName(), prefix));
            return;
         }
      } finally {
         cursor.dispose();
      }

   }

   XMLList text() {
      return this.matchChildren(TokenType.TEXT);
   }

   public String toString() {
      XmlCursor curs = this.newCursor();
      if (curs.isStartdoc()) {
         curs.toFirstContentToken();
      }

      String result;
      if (curs.isText()) {
         result = curs.getChars();
      } else if (curs.isStart() && this.hasSimpleContent()) {
         result = curs.getTextValue();
      } else {
         result = this.toXMLString(0);
      }

      return result;
   }

   String toSource(int indent) {
      return this.toXMLString(indent);
   }

   String toXMLString(int indent) {
      XmlCursor curs = this.newCursor();
      if (curs.isStartdoc()) {
         curs.toFirstContentToken();
      }

      String result;
      try {
         if (curs.isText()) {
            result = curs.getChars();
         } else if (curs.isAttr()) {
            result = curs.getTextValue();
         } else if (!curs.isComment() && !curs.isProcinst()) {
            result = dumpNode(curs, this.getOptions());
         } else {
            result = dumpNode(curs, this.getOptions());
            String start = "<xml-fragment>";
            String end = "</xml-fragment>";
            if (result.startsWith(start)) {
               result = result.substring(start.length());
            }

            if (result.endsWith(end)) {
               result = result.substring(0, result.length() - end.length());
            }
         }
      } finally {
         curs.dispose();
      }

      return result;
   }

   Object valueOf() {
      return this;
   }

   boolean equivalentXml(Object target) {
      boolean result = false;
      if (target instanceof XML) {
         XML otherXml = (XML)target;
         XmlCursor.TokenType thisTT = this.tokenType();
         XmlCursor.TokenType otherTT = otherXml.tokenType();
         if (thisTT != TokenType.ATTR && otherTT != TokenType.ATTR && thisTT != TokenType.TEXT && otherTT != TokenType.TEXT) {
            XmlCursor cursOne = this.newCursor();
            XmlCursor cursTwo = otherXml.newCursor();
            result = LogicalEquality.nodesEqual(cursOne, cursTwo);
            cursOne.dispose();
            cursTwo.dispose();
         } else {
            result = this.toString().equals(otherXml.toString());
         }
      } else if (target instanceof XMLList) {
         XMLList otherList = (XMLList)target;
         if (otherList.length() == 1) {
            result = this.equivalentXml(otherList.getXmlFromAnnotation(0));
         }
      } else if (this.hasSimpleContent()) {
         String otherStr = ScriptRuntime.toString(target);
         result = this.toString().equals(otherStr);
      }

      return result;
   }

   XMLList getPropertyList(XMLName name) {
      XMLList result;
      if (name.isDescendants()) {
         result = this.descendants(name);
      } else if (name.isAttributeName()) {
         result = this.attribute(name);
      } else {
         result = this.child(name);
      }

      return result;
   }

   protected Object jsConstructor(Context cx, boolean inNewExpr, Object[] args) {
      if (args.length == 0) {
         return createFromJS(this.lib, "");
      } else {
         Object arg0 = args[0];
         return !inNewExpr && arg0 instanceof XML ? arg0 : createFromJS(this.lib, arg0);
      }
   }

   public Scriptable getExtraMethodSource(Context cx) {
      if (this.hasSimpleContent()) {
         String src = this.toString();
         return ScriptRuntime.toObjectOrNull(cx, src);
      } else {
         return null;
      }
   }

   XmlObject getXmlObject() {
      XmlCursor cursor = this.newCursor();

      XmlObject xo;
      try {
         xo = cursor.getObject();
      } finally {
         cursor.dispose();
      }

      return xo;
   }

   static final class XScriptAnnotation extends XmlCursor.XmlBookmark implements Serializable {
      private static final long serialVersionUID = 1L;
      javax.xml.namespace.QName _name;
      XML _xScriptXML;

      XScriptAnnotation(XmlCursor curs) {
         super();
         this._name = curs.getName();
      }
   }

   static final class NamespaceDeclarations {
      private int _prefixIdx = 0;
      private StringBuffer _namespaceDecls = new StringBuffer();
      private String _defaultNSURI;

      NamespaceDeclarations(XmlCursor curs) {
         super();
         XML.skipNonElements(curs);
         this._defaultNSURI = curs.namespaceForPrefix("");
         if (this.isAnyDefaultNamespace()) {
            this.addDecl("", this._defaultNSURI);
         }

      }

      private void addDecl(String prefix, String ns) {
         this._namespaceDecls.append((prefix.length() > 0 ? "declare namespace " + prefix : "default element namespace") + " = \"" + ns + "\"" + "\n");
      }

      String getNextPrefix(String ns) {
         String prefix = "NS" + this._prefixIdx++;
         this._namespaceDecls.append("declare namespace " + prefix + " = " + "\"" + ns + "\"" + "\n");
         return prefix;
      }

      boolean isAnyDefaultNamespace() {
         return this._defaultNSURI != null ? this._defaultNSURI.length() > 0 : false;
      }

      String getDeclarations() {
         return this._namespaceDecls.toString();
      }
   }
}
