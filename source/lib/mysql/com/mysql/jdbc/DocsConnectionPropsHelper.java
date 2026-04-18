package com.mysql.jdbc;

public class DocsConnectionPropsHelper extends ConnectionPropertiesImpl {
   public DocsConnectionPropsHelper() {
      super();
   }

   public static void main(String[] args) throws Exception {
      System.out.println((new DocsConnectionPropsHelper()).exposeAsXml());
   }
}
