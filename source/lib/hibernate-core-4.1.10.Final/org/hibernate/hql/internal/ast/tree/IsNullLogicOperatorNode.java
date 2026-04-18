package org.hibernate.hql.internal.ast.tree;

public class IsNullLogicOperatorNode extends AbstractNullnessCheckNode {
   public IsNullLogicOperatorNode() {
      super();
   }

   protected int getExpansionConnectorType() {
      return 6;
   }

   protected String getExpansionConnectorText() {
      return "AND";
   }
}
