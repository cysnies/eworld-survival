package org.mozilla.javascript.xml.impl.xmlbeans;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.xmlbeans.XmlCursor;
import org.mozilla.javascript.ObjArray;

class NamespaceHelper {
   private XMLLibImpl lib;
   private final Map prefixToURI = new HashMap();
   private final Map uriToPrefix = new HashMap();
   private final Set undeclared = new HashSet();

   private NamespaceHelper(XMLLibImpl lib) {
      super();
      this.lib = lib;
      this.prefixToURI.put("", "");
      Set prefixes = new HashSet();
      prefixes.add("");
      this.uriToPrefix.put("", prefixes);
   }

   private void declareNamespace(String prefix, String uri, ObjArray declarations) {
      Set prefixes = (Set)this.uriToPrefix.get(uri);
      if (prefixes == null) {
         prefixes = new HashSet();
         this.uriToPrefix.put(uri, prefixes);
      }

      if (!prefixes.contains(prefix)) {
         String oldURI = (String)this.prefixToURI.get(prefix);
         prefixes.add(prefix);
         this.prefixToURI.put(prefix, uri);
         if (declarations != null) {
            declarations.add(new Namespace(this.lib, prefix, uri));
         }

         if (oldURI != null) {
            prefixes = (Set)this.uriToPrefix.get(oldURI);
            prefixes.remove(prefix);
         }
      }

   }

   private void processName(XmlCursor cursor, ObjArray declarations) {
      javax.xml.namespace.QName qname = cursor.getName();
      String uri = qname.getNamespaceURI();
      Set prefixes = (Set)this.uriToPrefix.get(uri);
      if (prefixes == null || prefixes.size() == 0) {
         this.undeclared.add(uri);
         if (declarations != null) {
            declarations.add(new Namespace(this.lib, uri));
         }
      }

   }

   private void update(XmlCursor cursor, ObjArray declarations) {
      cursor.push();

      while(cursor.toNextToken().isAnyAttr()) {
         if (cursor.isNamespace()) {
            javax.xml.namespace.QName name = cursor.getName();
            String prefix = name.getLocalPart();
            String uri = name.getNamespaceURI();
            this.declareNamespace(prefix, uri, declarations);
         }
      }

      cursor.pop();
      this.processName(cursor, declarations);
      cursor.push();

      for(boolean hasNext = cursor.toFirstAttribute(); hasNext; hasNext = cursor.toNextAttribute()) {
         this.processName(cursor, declarations);
      }

      cursor.pop();
   }

   public static Object[] inScopeNamespaces(XMLLibImpl lib, XmlCursor cursor) {
      ObjArray namespaces = new ObjArray();
      NamespaceHelper helper = new NamespaceHelper(lib);
      cursor.push();

      int depth;
      for(depth = 0; cursor.hasPrevToken(); cursor.toParent()) {
         if (cursor.isContainer()) {
            cursor.push();
            ++depth;
         }
      }

      for(int i = 0; i < depth; ++i) {
         cursor.pop();
         helper.update(cursor, (ObjArray)null);
      }

      for(Map.Entry entry : helper.prefixToURI.entrySet()) {
         Namespace ns = new Namespace(lib, (String)entry.getKey(), (String)entry.getValue());
         namespaces.add(ns);
      }

      Iterator var9 = helper.undeclared.iterator();

      while(var9.hasNext()) {
         Namespace ns = new Namespace(lib, (String)var9.next());
         namespaces.add(ns);
      }

      cursor.pop();
      return namespaces.toArray();
   }

   static Namespace getNamespace(XMLLibImpl lib, XmlCursor cursor, Object[] inScopeNamespaces) {
      String uri;
      String prefix;
      if (cursor.isProcinst()) {
         uri = "";
         prefix = "";
      } else {
         javax.xml.namespace.QName qname = cursor.getName();
         uri = qname.getNamespaceURI();
         prefix = qname.getPrefix();
      }

      if (inScopeNamespaces == null) {
         return new Namespace(lib, prefix, uri);
      } else {
         Namespace result = null;

         for(int i = 0; i != inScopeNamespaces.length; ++i) {
            Namespace ns = (Namespace)inScopeNamespaces[i];
            if (ns != null) {
               String nsURI = ns.uri();
               if (nsURI.equals(uri)) {
                  if (prefix.equals(ns.prefix())) {
                     result = ns;
                     break;
                  }

                  if (result == null || result.prefix() == null && ns.prefix() != null) {
                     result = ns;
                  }
               }
            }
         }

         if (result == null) {
            result = new Namespace(lib, prefix, uri);
         }

         return result;
      }
   }

   public static Object[] namespaceDeclarations(XMLLibImpl lib, XmlCursor cursor) {
      ObjArray declarations = new ObjArray();
      NamespaceHelper helper = new NamespaceHelper(lib);
      cursor.push();

      int depth;
      for(depth = 0; cursor.hasPrevToken(); cursor.toParent()) {
         if (cursor.isContainer()) {
            cursor.push();
            ++depth;
         }
      }

      for(int i = 0; i < depth - 1; ++i) {
         cursor.pop();
         helper.update(cursor, (ObjArray)null);
      }

      if (depth > 0) {
         cursor.pop();
         helper.update(cursor, declarations);
      }

      cursor.pop();
      return declarations.toArray();
   }

   public static Map getAllNamespaces(XMLLibImpl lib, XmlCursor cursor) {
      NamespaceHelper helper = new NamespaceHelper(lib);
      cursor.push();

      int depth;
      for(depth = 0; cursor.hasPrevToken(); cursor.toParent()) {
         if (cursor.isContainer()) {
            cursor.push();
            ++depth;
         }
      }

      for(int i = 0; i < depth; ++i) {
         cursor.pop();
         helper.update(cursor, (ObjArray)null);
      }

      cursor.pop();
      return helper.prefixToURI;
   }

   public static void getNamespaces(XmlCursor cursor, Map prefixToURI) {
      cursor.push();

      while(cursor.toNextToken().isAnyAttr()) {
         if (cursor.isNamespace()) {
            javax.xml.namespace.QName name = cursor.getName();
            String prefix = name.getLocalPart();
            String uri = name.getNamespaceURI();
            prefixToURI.put(prefix, uri);
         }
      }

      cursor.pop();
   }

   public static void removeNamespace(XmlCursor cursor, String prefix) {
      cursor.push();

      while(cursor.toNextToken().isAnyAttr()) {
         if (cursor.isNamespace()) {
            javax.xml.namespace.QName name = cursor.getName();
            if (name.getLocalPart().equals(prefix)) {
               cursor.removeXml();
               break;
            }
         }
      }

      cursor.pop();
   }
}
