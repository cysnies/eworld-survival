package org.hibernate.dialect;

import org.hibernate.sql.JoinFragment;
import org.hibernate.sql.Sybase11JoinFragment;

public class Sybase11Dialect extends SybaseDialect {
   public Sybase11Dialect() {
      super();
   }

   public JoinFragment createOuterJoinFragment() {
      return new Sybase11JoinFragment();
   }

   public String getCrossJoinSeparator() {
      return ", ";
   }
}
