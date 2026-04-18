package org.mozilla.javascript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Jump;
import org.mozilla.javascript.ast.Scope;
import org.mozilla.javascript.ast.ScriptNode;

public class NodeTransformer {
   private ObjArray loops;
   private ObjArray loopEnds;
   private boolean hasFinally;

   public NodeTransformer() {
      super();
   }

   public final void transform(ScriptNode tree) {
      this.transformCompilationUnit(tree);

      for(int i = 0; i != tree.getFunctionCount(); ++i) {
         FunctionNode fn = tree.getFunctionNode(i);
         this.transform(fn);
      }

   }

   private void transformCompilationUnit(ScriptNode tree) {
      this.loops = new ObjArray();
      this.loopEnds = new ObjArray();
      this.hasFinally = false;
      boolean createScopeObjects = tree.getType() != 109 || ((FunctionNode)tree).requiresActivation();
      tree.flattenSymbolTable(!createScopeObjects);
      boolean inStrictMode = tree instanceof AstRoot && ((AstRoot)tree).isInStrictMode();
      this.transformCompilationUnit_r(tree, tree, tree, createScopeObjects, inStrictMode);
   }

   private void transformCompilationUnit_r(ScriptNode tree, Node parent, Scope scope, boolean createScopeObjects, boolean inStrictMode) {
      Node node = null;

      while(true) {
         Node previous = null;
         if (node == null) {
            node = parent.getFirstChild();
         } else {
            previous = node;
            node = node.getNext();
         }

         if (node == null) {
            return;
         }

         int type = node.getType();
         if (createScopeObjects && (type == 129 || type == 132 || type == 157) && node instanceof Scope) {
            Scope newScope = (Scope)node;
            if (newScope.getSymbolTable() != null) {
               Node let = new Node(type == 157 ? 158 : 153);
               Node innerLet = new Node(153);
               let.addChildToBack(innerLet);

               for(String name : newScope.getSymbolTable().keySet()) {
                  innerLet.addChildToBack(Node.newString(39, name));
               }

               newScope.setSymbolTable((Map)null);
               Node oldNode = node;
               node = replaceCurrent(parent, previous, node, let);
               type = node.getType();
               let.addChildToBack(oldNode);
            }
         }

         label266:
         switch (type) {
            case 3:
            case 131:
               if (!this.loopEnds.isEmpty() && this.loopEnds.peek() == node) {
                  this.loopEnds.pop();
                  this.loops.pop();
               }
               break;
            case 4:
               boolean isGenerator = tree.getType() == 109 && ((FunctionNode)tree).isGenerator();
               if (isGenerator) {
                  node.putIntProp(20, 1);
               }

               if (this.hasFinally) {
                  Node unwindBlock = null;

                  for(int i = this.loops.size() - 1; i >= 0; --i) {
                     Node n = (Node)this.loops.get(i);
                     int elemtype = n.getType();
                     if (elemtype == 81 || elemtype == 123) {
                        Node unwind;
                        if (elemtype == 81) {
                           Jump jsrnode = new Jump(135);
                           Node jsrtarget = ((Jump)n).getFinally();
                           jsrnode.target = jsrtarget;
                           unwind = jsrnode;
                        } else {
                           unwind = new Node(3);
                        }

                        if (unwindBlock == null) {
                           unwindBlock = new Node(129, node.getLineno());
                        }

                        unwindBlock.addChildToBack(unwind);
                     }
                  }

                  if (unwindBlock != null) {
                     Node returnNode = node;
                     Node returnExpr = node.getFirstChild();
                     node = replaceCurrent(parent, previous, node, unwindBlock);
                     if (returnExpr != null && !isGenerator) {
                        Node store = new Node(134, returnExpr);
                        unwindBlock.addChildToFront(store);
                        returnNode = new Node(64);
                        unwindBlock.addChildToBack(returnNode);
                        this.transformCompilationUnit_r(tree, store, scope, createScopeObjects, inStrictMode);
                     } else {
                        unwindBlock.addChildToBack(returnNode);
                     }
                     continue;
                  }
               }
               break;
            case 7:
            case 32:
               Node child = node.getFirstChild();
               if (type == 7) {
                  while(child.getType() == 26) {
                     child = child.getFirstChild();
                  }

                  if (child.getType() == 12 || child.getType() == 13) {
                     Node first = child.getFirstChild();
                     Node last = child.getLastChild();
                     if (first.getType() == 39 && first.getString().equals("undefined")) {
                        child = last;
                     } else if (last.getType() == 39 && last.getString().equals("undefined")) {
                        child = first;
                     }
                  }
               }

               if (child.getType() == 33) {
                  child.setType(34);
               }
               break;
            case 8:
               if (inStrictMode) {
                  node.setType(73);
               }
            case 31:
            case 39:
            case 155:
               if (!createScopeObjects) {
                  label246: {
                     Node nameSource;
                     if (type == 39) {
                        nameSource = node;
                     } else {
                        nameSource = node.getFirstChild();
                        if (nameSource.getType() != 49) {
                           if (type != 31) {
                              throw Kit.codeBug();
                           }
                           break label246;
                        }
                     }

                     if (nameSource.getScope() == null) {
                        String name = nameSource.getString();
                        Scope defining = scope.getDefiningScope(name);
                        if (defining != null) {
                           nameSource.setScope(defining);
                           if (type == 39) {
                              node.setType(55);
                           } else if (type != 8 && type != 73) {
                              if (type == 155) {
                                 node.setType(156);
                                 nameSource.setType(41);
                              } else {
                                 if (type != 31) {
                                    throw Kit.codeBug();
                                 }

                                 Node n = new Node(44);
                                 node = replaceCurrent(parent, previous, node, n);
                              }
                           } else {
                              node.setType(56);
                              nameSource.setType(41);
                           }
                        }
                     }
                  }
               }
               break;
            case 30:
               this.visitNew(node, tree);
               break;
            case 38:
               this.visitCall(node, tree);
               break;
            case 72:
               ((FunctionNode)tree).addResumptionPoint(node);
               break;
            case 81:
               Jump jump = (Jump)node;
               Node finallytarget = jump.getFinally();
               if (finallytarget != null) {
                  this.hasFinally = true;
                  this.loops.push(node);
                  this.loopEnds.push(finallytarget);
               }
               break;
            case 114:
            case 130:
            case 132:
               this.loops.push(node);
               this.loopEnds.push(((Jump)node).target);
               break;
            case 120:
            case 121:
               Jump jump = (Jump)node;
               Jump jumpStatement = jump.getJumpStatement();
               if (jumpStatement == null) {
                  Kit.codeBug();
               }

               int i = this.loops.size();

               while(i != 0) {
                  --i;
                  Node n = (Node)this.loops.get(i);
                  if (n == jumpStatement) {
                     if (type == 120) {
                        jump.target = jumpStatement.target;
                     } else {
                        jump.target = jumpStatement.getContinue();
                     }

                     jump.setType(5);
                     break label266;
                  }

                  int elemtype = n.getType();
                  if (elemtype == 123) {
                     Node leave = new Node(3);
                     previous = addBeforeCurrent(parent, previous, node, leave);
                  } else if (elemtype == 81) {
                     Jump tryNode = (Jump)n;
                     Jump jsrFinally = new Jump(135);
                     jsrFinally.target = tryNode.getFinally();
                     previous = addBeforeCurrent(parent, previous, node, jsrFinally);
                  }
               }

               throw Kit.codeBug();
            case 123:
               this.loops.push(node);
               Node leave = node.getNext();
               if (leave.getType() != 3) {
                  Kit.codeBug();
               }

               this.loopEnds.push(leave);
               break;
            case 137:
               Scope defining = scope.getDefiningScope(node.getString());
               if (defining != null) {
                  node.setScope(defining);
               }
               break;
            case 153:
            case 158:
               Node child = node.getFirstChild();
               if (child.getType() == 153) {
                  boolean createWith = tree.getType() != 109 || ((FunctionNode)tree).requiresActivation();
                  node = this.visitLet(createWith, parent, previous, node);
                  break;
               }
            case 122:
            case 154:
               Node result = new Node(129);
               Node cursor = node.getFirstChild();

               while(cursor != null) {
                  Node n = cursor;
                  cursor = cursor.getNext();
                  if (n.getType() == 39) {
                     if (!n.hasChildren()) {
                        continue;
                     }

                     Node init = n.getFirstChild();
                     n.removeChild(init);
                     n.setType(49);
                     n = new Node(type == 154 ? 155 : 8, n, init);
                  } else if (n.getType() != 158) {
                     throw Kit.codeBug();
                  }

                  Node pop = new Node(133, n, node.getLineno());
                  result.addChildToBack(pop);
               }

               node = replaceCurrent(parent, previous, node, result);
         }

         this.transformCompilationUnit_r(tree, node, node instanceof Scope ? (Scope)node : scope, createScopeObjects, inStrictMode);
      }
   }

   protected void visitNew(Node node, ScriptNode tree) {
   }

   protected void visitCall(Node node, ScriptNode tree) {
   }

   protected Node visitLet(boolean createWith, Node parent, Node previous, Node scopeNode) {
      Node vars = scopeNode.getFirstChild();
      Node body = vars.getNext();
      scopeNode.removeChild(vars);
      scopeNode.removeChild(body);
      boolean isExpression = scopeNode.getType() == 158;
      Node result;
      if (!createWith) {
         result = new Node(isExpression ? 89 : 129);
         result = replaceCurrent(parent, previous, scopeNode, result);
         Node newVars = new Node(89);

         for(Node v = vars.getFirstChild(); v != null; v = v.getNext()) {
            Node current = v;
            if (v.getType() == 158) {
               Node c = v.getFirstChild();
               if (c.getType() != 153) {
                  throw Kit.codeBug();
               }

               if (isExpression) {
                  body = new Node(89, c.getNext(), body);
               } else {
                  body = new Node(129, new Node(133, c.getNext()), body);
               }

               Scope.joinScopes((Scope)v, (Scope)scopeNode);
               current = c.getFirstChild();
            }

            if (current.getType() != 39) {
               throw Kit.codeBug();
            }

            Node stringNode = Node.newString(current.getString());
            stringNode.setScope((Scope)scopeNode);
            Node init = current.getFirstChild();
            if (init == null) {
               init = new Node(126, Node.newNumber((double)0.0F));
            }

            newVars.addChildToBack(new Node(56, stringNode, init));
         }

         if (isExpression) {
            result.addChildToBack(newVars);
            scopeNode.setType(89);
            result.addChildToBack(scopeNode);
            scopeNode.addChildToBack(body);
            if (body instanceof Scope) {
               Scope scopeParent = ((Scope)body).getParentScope();
               ((Scope)body).setParentScope((Scope)scopeNode);
               ((Scope)scopeNode).setParentScope(scopeParent);
            }
         } else {
            result.addChildToBack(new Node(133, newVars));
            scopeNode.setType(129);
            result.addChildToBack(scopeNode);
            scopeNode.addChildrenToBack(body);
            if (body instanceof Scope) {
               Scope scopeParent = ((Scope)body).getParentScope();
               ((Scope)body).setParentScope((Scope)scopeNode);
               ((Scope)scopeNode).setParentScope(scopeParent);
            }
         }
      } else {
         result = new Node(isExpression ? 159 : 129);
         result = replaceCurrent(parent, previous, scopeNode, result);
         ArrayList<Object> list = new ArrayList();
         Node objectLiteral = new Node(66);

         for(Node v = vars.getFirstChild(); v != null; v = v.getNext()) {
            Node current = v;
            if (v.getType() == 158) {
               List<?> destructuringNames = (List)v.getProp(22);
               Node c = v.getFirstChild();
               if (c.getType() != 153) {
                  throw Kit.codeBug();
               }

               if (isExpression) {
                  body = new Node(89, c.getNext(), body);
               } else {
                  body = new Node(129, new Node(133, c.getNext()), body);
               }

               if (destructuringNames != null) {
                  list.addAll(destructuringNames);

                  for(int i = 0; i < destructuringNames.size(); ++i) {
                     objectLiteral.addChildToBack(new Node(126, Node.newNumber((double)0.0F)));
                  }
               }

               current = c.getFirstChild();
            }

            if (current.getType() != 39) {
               throw Kit.codeBug();
            }

            list.add(ScriptRuntime.getIndexObject(current.getString()));
            Node init = current.getFirstChild();
            if (init == null) {
               init = new Node(126, Node.newNumber((double)0.0F));
            }

            objectLiteral.addChildToBack(init);
         }

         objectLiteral.putProp(12, list.toArray());
         Node newVars = new Node(2, objectLiteral);
         result.addChildToBack(newVars);
         result.addChildToBack(new Node(123, body));
         result.addChildToBack(new Node(3));
      }

      return result;
   }

   private static Node addBeforeCurrent(Node parent, Node previous, Node current, Node toAdd) {
      if (previous == null) {
         if (current != parent.getFirstChild()) {
            Kit.codeBug();
         }

         parent.addChildToFront(toAdd);
      } else {
         if (current != previous.getNext()) {
            Kit.codeBug();
         }

         parent.addChildAfter(toAdd, previous);
      }

      return toAdd;
   }

   private static Node replaceCurrent(Node parent, Node previous, Node current, Node replacement) {
      if (previous == null) {
         if (current != parent.getFirstChild()) {
            Kit.codeBug();
         }

         parent.replaceChild(current, replacement);
      } else if (previous.next == current) {
         parent.replaceChildAfter(previous, replacement);
      } else {
         parent.replaceChild(current, replacement);
      }

      return replacement;
   }
}
