package org.mozilla.javascript.xml.impl.xmlbeans;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import org.apache.xmlbeans.XmlCursor;

public class LogicalEquality {
   public LogicalEquality() {
      super();
   }

   public static boolean nodesEqual(XmlCursor xmlOne, XmlCursor xmlTwo) {
      boolean result = false;
      if (xmlOne.isStartdoc()) {
         xmlOne.toFirstContentToken();
      }

      if (xmlTwo.isStartdoc()) {
         xmlTwo.toFirstContentToken();
      }

      if (xmlOne.currentTokenType() == xmlTwo.currentTokenType()) {
         if (xmlOne.isEnddoc()) {
            result = true;
         } else if (xmlOne.isAttr()) {
            result = attributesEqual(xmlOne, xmlTwo);
         } else if (xmlOne.isText()) {
            result = textNodesEqual(xmlOne, xmlTwo);
         } else if (xmlOne.isComment()) {
            result = commentsEqual(xmlOne, xmlTwo);
         } else if (xmlOne.isProcinst()) {
            result = processingInstructionsEqual(xmlOne, xmlTwo);
         } else if (xmlOne.isStart()) {
            result = elementsEqual(xmlOne, xmlTwo);
         }
      }

      return result;
   }

   private static boolean elementsEqual(XmlCursor xmlOne, XmlCursor xmlTwo) {
      boolean result = true;
      if (!qnamesEqual(xmlOne.getName(), xmlTwo.getName())) {
         result = false;
      } else {
         nextToken(xmlOne);
         nextToken(xmlTwo);

         do {
            if (xmlOne.currentTokenType() != xmlTwo.currentTokenType()) {
               result = false;
               break;
            }

            if (xmlOne.isEnd() || xmlOne.isEnddoc()) {
               break;
            }

            if (xmlOne.isAttr()) {
               result = attributeListsEqual(xmlOne, xmlTwo);
            } else {
               if (xmlOne.isText()) {
                  result = textNodesEqual(xmlOne, xmlTwo);
               } else if (xmlOne.isComment()) {
                  result = commentsEqual(xmlOne, xmlTwo);
               } else if (xmlOne.isProcinst()) {
                  result = processingInstructionsEqual(xmlOne, xmlTwo);
               } else if (xmlOne.isStart()) {
                  result = elementsEqual(xmlOne, xmlTwo);
               }

               nextToken(xmlOne);
               nextToken(xmlTwo);
            }
         } while(result);
      }

      return result;
   }

   private static boolean attributeListsEqual(XmlCursor xmlOne, XmlCursor xmlTwo) {
      boolean result = true;
      TreeMap mapOne = loadAttributeMap(xmlOne);
      TreeMap mapTwo = loadAttributeMap(xmlTwo);
      if (mapOne.size() != mapTwo.size()) {
         result = false;
      } else {
         Set keysOne = mapOne.keySet();
         Set keysTwo = mapTwo.keySet();
         Iterator itOne = keysOne.iterator();
         Iterator itTwo = keysTwo.iterator();

         while(result && itOne.hasNext()) {
            String valueOne = (String)itOne.next();
            String valueTwo = (String)itTwo.next();
            if (!valueOne.equals(valueTwo)) {
               result = false;
            } else {
               javax.xml.namespace.QName qnameOne = (javax.xml.namespace.QName)mapOne.get(valueOne);
               javax.xml.namespace.QName qnameTwo = (javax.xml.namespace.QName)mapTwo.get(valueTwo);
               if (!qnamesEqual(qnameOne, qnameTwo)) {
                  result = false;
               }
            }
         }
      }

      return result;
   }

   private static TreeMap loadAttributeMap(XmlCursor xml) {
      TreeMap result = new TreeMap();

      while(xml.isAttr()) {
         result.put(xml.getTextValue(), xml.getName());
         nextToken(xml);
      }

      return result;
   }

   private static boolean attributesEqual(XmlCursor xmlOne, XmlCursor xmlTwo) {
      boolean result = false;
      if (xmlOne.isAttr() && xmlTwo.isAttr() && qnamesEqual(xmlOne.getName(), xmlTwo.getName()) && xmlOne.getTextValue().equals(xmlTwo.getTextValue())) {
         result = true;
      }

      return result;
   }

   private static boolean textNodesEqual(XmlCursor xmlOne, XmlCursor xmlTwo) {
      boolean result = false;
      if (xmlOne.isText() && xmlTwo.isText() && xmlOne.getChars().equals(xmlTwo.getChars())) {
         result = true;
      }

      return result;
   }

   private static boolean commentsEqual(XmlCursor xmlOne, XmlCursor xmlTwo) {
      boolean result = false;
      if (xmlOne.isComment() && xmlTwo.isComment() && xmlOne.getTextValue().equals(xmlTwo.getTextValue())) {
         result = true;
      }

      return result;
   }

   private static boolean processingInstructionsEqual(XmlCursor xmlOne, XmlCursor xmlTwo) {
      boolean result = false;
      if (xmlOne.isProcinst() && xmlTwo.isProcinst() && qnamesEqual(xmlOne.getName(), xmlTwo.getName()) && xmlOne.getTextValue().equals(xmlTwo.getTextValue())) {
         result = true;
      }

      return result;
   }

   private static boolean qnamesEqual(javax.xml.namespace.QName qnameOne, javax.xml.namespace.QName qnameTwo) {
      boolean result = false;
      return qnameOne.getNamespaceURI().equals(qnameTwo.getNamespaceURI()) && qnameOne.getLocalPart().equals(qnameTwo.getLocalPart()) ? true : result;
   }

   private static void nextToken(XmlCursor xml) {
      do {
         xml.toNextToken();
      } while(xml.isText() && xml.getChars().trim().length() <= 0);

   }
}
