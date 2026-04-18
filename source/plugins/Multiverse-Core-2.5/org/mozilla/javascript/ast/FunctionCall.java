package org.mozilla.javascript.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FunctionCall extends AstNode {
   protected static final List NO_ARGS = Collections.unmodifiableList(new ArrayList());
   protected AstNode target;
   protected List arguments;
   protected int lp = -1;
   protected int rp = -1;

   public FunctionCall() {
      super();
      this.type = 38;
   }

   public FunctionCall(int pos) {
      super(pos);
      this.type = 38;
   }

   public FunctionCall(int pos, int len) {
      super(pos, len);
      this.type = 38;
   }

   public AstNode getTarget() {
      return this.target;
   }

   public void setTarget(AstNode target) {
      this.assertNotNull(target);
      this.target = target;
      target.setParent(this);
   }

   public List getArguments() {
      return this.arguments != null ? this.arguments : NO_ARGS;
   }

   public void setArguments(List arguments) {
      if (arguments == null) {
         this.arguments = null;
      } else {
         if (this.arguments != null) {
            this.arguments.clear();
         }

         for(AstNode arg : arguments) {
            this.addArgument(arg);
         }
      }

   }

   public void addArgument(AstNode arg) {
      this.assertNotNull(arg);
      if (this.arguments == null) {
         this.arguments = new ArrayList();
      }

      this.arguments.add(arg);
      arg.setParent(this);
   }

   public int getLp() {
      return this.lp;
   }

   public void setLp(int lp) {
      this.lp = lp;
   }

   public int getRp() {
      return this.rp;
   }

   public void setRp(int rp) {
      this.rp = rp;
   }

   public void setParens(int lp, int rp) {
      this.lp = lp;
      this.rp = rp;
   }

   public String toSource(int depth) {
      StringBuilder sb = new StringBuilder();
      sb.append(this.makeIndent(depth));
      sb.append(this.target.toSource(0));
      sb.append("(");
      if (this.arguments != null) {
         this.printList(this.arguments, sb);
      }

      sb.append(")");
      return sb.toString();
   }

   public void visit(NodeVisitor v) {
      if (v.visit(this)) {
         this.target.visit(v);

         for(AstNode arg : this.getArguments()) {
            arg.visit(v);
         }
      }

   }
}
