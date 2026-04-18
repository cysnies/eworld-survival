package org.mozilla.javascript.ast;

import java.util.ArrayList;
import java.util.List;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;

public class VariableDeclaration extends AstNode {
   private List variables = new ArrayList();
   private boolean isStatement;

   public VariableDeclaration() {
      super();
      this.type = 122;
   }

   public VariableDeclaration(int pos) {
      super(pos);
      this.type = 122;
   }

   public VariableDeclaration(int pos, int len) {
      super(pos, len);
      this.type = 122;
   }

   public List getVariables() {
      return this.variables;
   }

   public void setVariables(List variables) {
      this.assertNotNull(variables);
      this.variables.clear();

      for(VariableInitializer vi : variables) {
         this.addVariable(vi);
      }

   }

   public void addVariable(VariableInitializer v) {
      this.assertNotNull(v);
      this.variables.add(v);
      v.setParent(this);
   }

   public Node setType(int type) {
      if (type != 122 && type != 154 && type != 153) {
         throw new IllegalArgumentException("invalid decl type: " + type);
      } else {
         return super.setType(type);
      }
   }

   public boolean isVar() {
      return this.type == 122;
   }

   public boolean isConst() {
      return this.type == 154;
   }

   public boolean isLet() {
      return this.type == 153;
   }

   public boolean isStatement() {
      return this.isStatement;
   }

   public void setIsStatement(boolean isStatement) {
      this.isStatement = isStatement;
   }

   private String declTypeName() {
      return Token.typeToName(this.type).toLowerCase();
   }

   public String toSource(int depth) {
      StringBuilder sb = new StringBuilder();
      sb.append(this.makeIndent(depth));
      sb.append(this.declTypeName());
      sb.append(" ");
      this.printList(this.variables, sb);
      if (this.isStatement()) {
         sb.append(";\n");
      }

      return sb.toString();
   }

   public void visit(NodeVisitor v) {
      if (v.visit(this)) {
         for(AstNode var : this.variables) {
            var.visit(v);
         }
      }

   }
}
