package org.mozilla.javascript.xml.impl.xmlbeans;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Kit;
import org.mozilla.javascript.Ref;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Undefined;

class XMLName extends Ref {
   static final long serialVersionUID = 3832176310755686977L;
   private String uri;
   private String localName;
   private boolean isAttributeName;
   private boolean isDescendants;
   private XMLObjectImpl xmlObject;

   private XMLName(String uri, String localName) {
      super();
      this.uri = uri;
      this.localName = localName;
   }

   static XMLName formStar() {
      return new XMLName((String)null, "*");
   }

   static XMLName formProperty(String uri, String localName) {
      return new XMLName(uri, localName);
   }

   void initXMLObject(XMLObjectImpl xmlObject) {
      if (xmlObject == null) {
         throw new IllegalArgumentException();
      } else if (this.xmlObject != null) {
         throw new IllegalStateException();
      } else {
         this.xmlObject = xmlObject;
      }
   }

   String uri() {
      return this.uri;
   }

   String localName() {
      return this.localName;
   }

   boolean isAttributeName() {
      return this.isAttributeName;
   }

   void setAttributeName() {
      if (this.isAttributeName) {
         throw new IllegalStateException();
      } else {
         this.isAttributeName = true;
      }
   }

   boolean isDescendants() {
      return this.isDescendants;
   }

   void setIsDescendants() {
      if (this.isDescendants) {
         throw new IllegalStateException();
      } else {
         this.isDescendants = true;
      }
   }

   public boolean has(Context cx) {
      return this.xmlObject == null ? false : this.xmlObject.hasXMLProperty(this);
   }

   public Object get(Context cx) {
      if (this.xmlObject == null) {
         throw ScriptRuntime.undefReadError(Undefined.instance, this.toString());
      } else {
         return this.xmlObject.getXMLProperty(this);
      }
   }

   public Object set(Context cx, Object value) {
      if (this.xmlObject == null) {
         throw ScriptRuntime.undefWriteError(Undefined.instance, this.toString(), value);
      } else if (this.isDescendants) {
         throw Kit.codeBug();
      } else {
         this.xmlObject.putXMLProperty(this, value);
         return value;
      }
   }

   public boolean delete(Context cx) {
      if (this.xmlObject == null) {
         return true;
      } else {
         this.xmlObject.deleteXMLProperty(this);
         return !this.xmlObject.hasXMLProperty(this);
      }
   }

   public String toString() {
      StringBuffer buff = new StringBuffer();
      if (this.isDescendants) {
         buff.append("..");
      }

      if (this.isAttributeName) {
         buff.append('@');
      }

      if (this.uri == null) {
         buff.append('*');
         if (this.localName().equals("*")) {
            return buff.toString();
         }
      } else {
         buff.append('"').append(this.uri()).append('"');
      }

      buff.append(':').append(this.localName());
      return buff.toString();
   }
}
