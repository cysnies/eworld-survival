package com.mysql.jdbc.util;

import com.mysql.jdbc.SQLError;

public class ErrorMappingsDocGenerator {
   public ErrorMappingsDocGenerator() {
      super();
   }

   public static void main(String[] args) throws Exception {
      SQLError.dumpSqlStatesMappingsAsXml();
   }
}
