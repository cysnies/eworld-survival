package org.mozilla.javascript.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mozilla.javascript.Node;

public class FunctionNode extends ScriptNode {
   public static final int FUNCTION_STATEMENT = 1;
   public static final int FUNCTION_EXPRESSION = 2;
   public static final int FUNCTION_EXPRESSION_STATEMENT = 3;
   private static final List NO_PARAMS = Collections.unmodifiableList(new ArrayList());
   private Name functionName;
   private List params;
   private AstNode body;
   private boolean isExpressionClosure;
   private Form functionForm;
   private int lp;
   private int rp;
   private int functionType;
   private boolean needsActivation;
   private boolean isGenerator;
   private List generatorResumePoints;
   private Map liveLocals;
   private AstNode memberExprNode;

   public FunctionNode() {
      super();
      this.functionForm = FunctionNode.Form.FUNCTION;
      this.lp = -1;
      this.rp = -1;
      this.type = 109;
   }

   public FunctionNode(int pos) {
      super(pos);
      this.functionForm = FunctionNode.Form.FUNCTION;
      this.lp = -1;
      this.rp = -1;
      this.type = 109;
   }

   public FunctionNode(int pos, Name name) {
      super(pos);
      this.functionForm = FunctionNode.Form.FUNCTION;
      this.lp = -1;
      this.rp = -1;
      this.type = 109;
      this.setFunctionName(name);
   }

   public Name getFunctionName() {
      return this.functionName;
   }

   public void setFunctionName(Name name) {
      this.functionName = name;
      if (name != null) {
         name.setParent(this);
      }

   }

   public String getName() {
      return this.functionName != null ? this.functionName.getIdentifier() : "";
   }

   public List getParams() {
      return this.params != null ? this.params : NO_PARAMS;
   }

   public void setParams(List params) {
      if (params == null) {
         this.params = null;
      } else {
         if (this.params != null) {
            this.params.clear();
         }

         for(AstNode param : params) {
            this.addParam(param);
         }
      }

   }

   public void addParam(AstNode param) {
      this.assertNotNull(param);
      if (this.params == null) {
         this.params = new ArrayList();
      }

      this.params.add(param);
      param.setParent(this);
   }

   public boolean isParam(AstNode node) {
      return this.params == null ? false : this.params.contains(node);
   }

   public AstNode getBody() {
      return this.body;
   }

   public void setBody(AstNode body) {
      this.assertNotNull(body);
      this.body = body;
      if (Boolean.TRUE.equals(body.getProp(25))) {
         this.setIsExpressionClosure(true);
      }

      int absEnd = body.getPosition() + body.getLength();
      body.setParent(this);
      this.setLength(absEnd - this.position);
      this.setEncodedSourceBounds(this.position, absEnd);
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

   public boolean isExpressionClosure() {
      return this.isExpressionClosure;
   }

   public void setIsExpressionClosure(boolean isExpressionClosure) {
      this.isExpressionClosure = isExpressionClosure;
   }

   public boolean requiresActivation() {
      return this.needsActivation;
   }

   public void setRequiresActivation() {
      this.needsActivation = true;
   }

   public boolean isGenerator() {
      return this.isGenerator;
   }

   public void setIsGenerator() {
      this.isGenerator = true;
   }

   public void addResumptionPoint(Node target) {
      if (this.generatorResumePoints == null) {
         this.generatorResumePoints = new ArrayList();
      }

      this.generatorResumePoints.add(target);
   }

   public List getResumptionPoints() {
      return this.generatorResumePoints;
   }

   public Map getLiveLocals() {
      return this.liveLocals;
   }

   public void addLiveLocals(Node node, int[] locals) {
      if (this.liveLocals == null) {
         this.liveLocals = new HashMap();
      }

      this.liveLocals.put(node, locals);
   }

   public int addFunction(FunctionNode fnNode) {
      int result = super.addFunction(fnNode);
      if (this.getFunctionCount() > 0) {
         this.needsActivation = true;
      }

      return result;
   }

   public int getFunctionType() {
      return this.functionType;
   }

   public void setFunctionType(int type) {
      this.functionType = type;
   }

   public boolean isGetterOrSetter() {
      return this.functionForm == FunctionNode.Form.GETTER || this.functionForm == FunctionNode.Form.SETTER;
   }

   public boolean isGetter() {
      return this.functionForm == FunctionNode.Form.GETTER;
   }

   public boolean isSetter() {
      return this.functionForm == FunctionNode.Form.SETTER;
   }

   public void setFunctionIsGetter() {
      this.functionForm = FunctionNode.Form.GETTER;
   }

   public void setFunctionIsSetter() {
      this.functionForm = FunctionNode.Form.SETTER;
   }

   public void setMemberExprNode(AstNode node) {
      this.memberExprNode = node;
      if (node != null) {
         node.setParent(this);
      }

   }

   public AstNode getMemberExprNode() {
      return this.memberExprNode;
   }

   public String toSource(int depth) {
      StringBuilder sb = new StringBuilder();
      sb.append(this.makeIndent(depth));
      sb.append("function");
      if (this.functionName != null) {
         sb.append(" ");
         sb.append(this.functionName.toSource(0));
      }

      if (this.params == null) {
         sb.append("() ");
      } else {
         sb.append("(");
         this.printList(this.params, sb);
         sb.append(") ");
      }

      if (this.isExpressionClosure) {
         AstNode body = this.getBody();
         if (body.getLastChild() instanceof ReturnStatement) {
            body = ((ReturnStatement)body.getLastChild()).getReturnValue();
            sb.append(body.toSource(0));
            if (this.functionType == 1) {
               sb.append(";");
            }
         } else {
            sb.append(" ");
            sb.append(body.toSource(0));
         }
      } else {
         sb.append(this.getBody().toSource(depth).trim());
      }

      if (this.functionType == 1) {
         sb.append("\n");
      }

      return sb.toString();
   }

   public void visit(NodeVisitor v) {
      if (v.visit(this)) {
         if (this.functionName != null) {
            this.functionName.visit(v);
         }

         for(AstNode param : this.getParams()) {
            param.visit(v);
         }

         this.getBody().visit(v);
         if (!this.isExpressionClosure && this.memberExprNode != null) {
            this.memberExprNode.visit(v);
         }
      }

   }

   public static enum Form {
      FUNCTION,
      GETTER,
      SETTER;

      private Form() {
      }
   }
}
