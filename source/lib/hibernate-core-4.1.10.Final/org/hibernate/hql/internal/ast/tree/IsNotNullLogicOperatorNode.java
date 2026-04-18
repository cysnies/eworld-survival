package org.hibernate.hql.internal.ast.tree;

public class IsNotNullLogicOperatorNode extends AbstractNullnessCheckNode {
   public IsNotNullLogicOperatorNode() {
      super();
   }

   protected int getExpansionConnectorType() {
      return 40;
   }

   protected String getExpansionConnectorText() {
      return "OR";
   }
}
