package org.mozilla.javascript.optimizer;

import org.mozilla.javascript.Node;
import org.mozilla.javascript.ObjArray;
import org.mozilla.javascript.ast.ScriptNode;

class Optimizer {
   static final int NoType = 0;
   static final int NumberType = 1;
   static final int AnyType = 3;
   private boolean inDirectCallFunction;
   OptFunctionNode theFunction;
   private boolean parameterUsedInNumberContext;

   Optimizer() {
      super();
   }

   void optimize(ScriptNode scriptOrFn) {
      int functionCount = scriptOrFn.getFunctionCount();

      for(int i = 0; i != functionCount; ++i) {
         OptFunctionNode f = OptFunctionNode.get(scriptOrFn, i);
         this.optimizeFunction(f);
      }

   }

   private void optimizeFunction(OptFunctionNode theFunction) {
      if (!theFunction.fnode.requiresActivation()) {
         this.inDirectCallFunction = theFunction.isTargetOfDirectCall();
         this.theFunction = theFunction;
         ObjArray statementsArray = new ObjArray();
         buildStatementList_r(theFunction.fnode, statementsArray);
         Node[] theStatementNodes = new Node[statementsArray.size()];
         statementsArray.toArray(theStatementNodes);
         Block.runFlowAnalyzes(theFunction, theStatementNodes);
         if (!theFunction.fnode.requiresActivation()) {
            this.parameterUsedInNumberContext = false;

            for(Node theStatementNode : theStatementNodes) {
               this.rewriteForNumberVariables(theStatementNode, 1);
            }

            theFunction.setParameterNumberContext(this.parameterUsedInNumberContext);
         }

      }
   }

   private void markDCPNumberContext(Node n) {
      if (this.inDirectCallFunction && n.getType() == 55) {
         int varIndex = this.theFunction.getVarIndex(n);
         if (this.theFunction.isParameter(varIndex)) {
            this.parameterUsedInNumberContext = true;
         }
      }

   }

   private boolean convertParameter(Node n) {
      if (this.inDirectCallFunction && n.getType() == 55) {
         int varIndex = this.theFunction.getVarIndex(n);
         if (this.theFunction.isParameter(varIndex)) {
            n.removeProp(8);
            return true;
         }
      }

      return false;
   }

   private int rewriteForNumberVariables(Node n, int desired) {
      switch (n.getType()) {
         case 9:
         case 10:
         case 11:
         case 18:
         case 19:
         case 22:
         case 23:
         case 24:
         case 25:
            Node lChild = n.getFirstChild();
            Node rChild = lChild.getNext();
            int lType = this.rewriteForNumberVariables(lChild, 1);
            int rType = this.rewriteForNumberVariables(rChild, 1);
            this.markDCPNumberContext(lChild);
            this.markDCPNumberContext(rChild);
            if (lType == 1) {
               if (rType == 1) {
                  n.putIntProp(8, 0);
                  return 1;
               }

               if (!this.convertParameter(rChild)) {
                  n.removeChild(rChild);
                  n.addChildToBack(new Node(150, rChild));
                  n.putIntProp(8, 0);
               }

               return 1;
            } else {
               if (rType == 1) {
                  if (!this.convertParameter(lChild)) {
                     n.removeChild(lChild);
                     n.addChildToFront(new Node(150, lChild));
                     n.putIntProp(8, 0);
                  }

                  return 1;
               }

               if (!this.convertParameter(lChild)) {
                  n.removeChild(lChild);
                  n.addChildToFront(new Node(150, lChild));
               }

               if (!this.convertParameter(rChild)) {
                  n.removeChild(rChild);
                  n.addChildToBack(new Node(150, rChild));
               }

               n.putIntProp(8, 0);
               return 1;
            }
         case 14:
         case 15:
         case 16:
         case 17:
            Node lChild = n.getFirstChild();
            Node rChild = lChild.getNext();
            int lType = this.rewriteForNumberVariables(lChild, 1);
            int rType = this.rewriteForNumberVariables(rChild, 1);
            this.markDCPNumberContext(lChild);
            this.markDCPNumberContext(rChild);
            if (this.convertParameter(lChild)) {
               if (this.convertParameter(rChild)) {
                  return 0;
               }

               if (rType == 1) {
                  n.putIntProp(8, 2);
               }
            } else if (this.convertParameter(rChild)) {
               if (lType == 1) {
                  n.putIntProp(8, 1);
               }
            } else if (lType == 1) {
               if (rType == 1) {
                  n.putIntProp(8, 0);
               } else {
                  n.putIntProp(8, 1);
               }
            } else if (rType == 1) {
               n.putIntProp(8, 2);
            }

            return 0;
         case 21:
            Node lChild = n.getFirstChild();
            Node rChild = lChild.getNext();
            int lType = this.rewriteForNumberVariables(lChild, 1);
            int rType = this.rewriteForNumberVariables(rChild, 1);
            if (this.convertParameter(lChild)) {
               if (this.convertParameter(rChild)) {
                  return 0;
               }

               if (rType == 1) {
                  n.putIntProp(8, 2);
               }
            } else if (this.convertParameter(rChild)) {
               if (lType == 1) {
                  n.putIntProp(8, 1);
               }
            } else if (lType == 1) {
               if (rType == 1) {
                  n.putIntProp(8, 0);
                  return 1;
               }

               n.putIntProp(8, 1);
            } else if (rType == 1) {
               n.putIntProp(8, 2);
            }

            return 0;
         case 36:
            Node arrayBase = n.getFirstChild();
            Node arrayIndex = arrayBase.getNext();
            int baseType = this.rewriteForNumberVariables(arrayBase, 1);
            if (baseType == 1 && !this.convertParameter(arrayBase)) {
               n.removeChild(arrayBase);
               n.addChildToFront(new Node(149, arrayBase));
            }

            int indexType = this.rewriteForNumberVariables(arrayIndex, 1);
            if (indexType == 1 && !this.convertParameter(arrayIndex)) {
               n.putIntProp(8, 2);
            }

            return 0;
         case 37:
         case 140:
            Node arrayBase = n.getFirstChild();
            Node arrayIndex = arrayBase.getNext();
            Node rValue = arrayIndex.getNext();
            int baseType = this.rewriteForNumberVariables(arrayBase, 1);
            if (baseType == 1 && !this.convertParameter(arrayBase)) {
               n.removeChild(arrayBase);
               n.addChildToFront(new Node(149, arrayBase));
            }

            int indexType = this.rewriteForNumberVariables(arrayIndex, 1);
            if (indexType == 1 && !this.convertParameter(arrayIndex)) {
               n.putIntProp(8, 1);
            }

            int rValueType = this.rewriteForNumberVariables(rValue, 1);
            if (rValueType == 1 && !this.convertParameter(rValue)) {
               n.removeChild(rValue);
               n.addChildToBack(new Node(149, rValue));
            }

            return 0;
         case 38:
            Node child = n.getFirstChild();
            this.rewriteAsObjectChildren(child, child.getFirstChild());
            child = child.getNext();
            OptFunctionNode target = (OptFunctionNode)n.getProp(9);
            if (target != null) {
               for(; child != null; child = child.getNext()) {
                  int type = this.rewriteForNumberVariables(child, 1);
                  if (type == 1) {
                     this.markDCPNumberContext(child);
                  }
               }
            } else {
               this.rewriteAsObjectChildren(n, child);
            }

            return 0;
         case 40:
            n.putIntProp(8, 0);
            return 1;
         case 55:
            int varIndex = this.theFunction.getVarIndex(n);
            if (this.inDirectCallFunction && this.theFunction.isParameter(varIndex) && desired == 1) {
               n.putIntProp(8, 0);
               return 1;
            } else {
               if (this.theFunction.isNumberVar(varIndex)) {
                  n.putIntProp(8, 0);
                  return 1;
               }

               return 0;
            }
         case 56:
            Node lChild = n.getFirstChild();
            Node rChild = lChild.getNext();
            int rType = this.rewriteForNumberVariables(rChild, 1);
            int varIndex = this.theFunction.getVarIndex(n);
            if (this.inDirectCallFunction && this.theFunction.isParameter(varIndex)) {
               if (rType == 1) {
                  if (!this.convertParameter(rChild)) {
                     n.putIntProp(8, 0);
                     return 1;
                  }

                  this.markDCPNumberContext(rChild);
                  return 0;
               }

               return rType;
            } else {
               if (this.theFunction.isNumberVar(varIndex)) {
                  if (rType != 1) {
                     n.removeChild(rChild);
                     n.addChildToBack(new Node(150, rChild));
                  }

                  n.putIntProp(8, 0);
                  this.markDCPNumberContext(rChild);
                  return 1;
               }

               if (rType == 1 && !this.convertParameter(rChild)) {
                  n.removeChild(rChild);
                  n.addChildToBack(new Node(149, rChild));
               }

               return 0;
            }
         case 106:
         case 107:
            Node child = n.getFirstChild();
            if (child.getType() == 55) {
               if (this.rewriteForNumberVariables(child, 1) == 1 && !this.convertParameter(child)) {
                  n.putIntProp(8, 0);
                  this.markDCPNumberContext(child);
                  return 1;
               }

               return 0;
            } else {
               if (child.getType() != 36 && child.getType() != 33) {
                  return 0;
               }

               return this.rewriteForNumberVariables(child, 1);
            }
         case 133:
            Node child = n.getFirstChild();
            int type = this.rewriteForNumberVariables(child, 1);
            if (type == 1) {
               n.putIntProp(8, 0);
            }

            return 0;
         default:
            this.rewriteAsObjectChildren(n, n.getFirstChild());
            return 0;
      }
   }

   private void rewriteAsObjectChildren(Node n, Node child) {
      Node nextChild;
      for(; child != null; child = nextChild) {
         nextChild = child.getNext();
         int type = this.rewriteForNumberVariables(child, 0);
         if (type == 1 && !this.convertParameter(child)) {
            n.removeChild(child);
            Node nuChild = new Node(149, child);
            if (nextChild == null) {
               n.addChildToBack(nuChild);
            } else {
               n.addChildBefore(nuChild, nextChild);
            }
         }
      }

   }

   private static void buildStatementList_r(Node node, ObjArray statements) {
      int type = node.getType();
      if (type != 129 && type != 141 && type != 132 && type != 109) {
         statements.add(node);
      } else {
         for(Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            buildStatementList_r(child, statements);
         }
      }

   }
}
