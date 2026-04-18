package org.mozilla.javascript;

import java.util.ArrayList;
import java.util.List;
import org.mozilla.javascript.ast.ArrayComprehension;
import org.mozilla.javascript.ast.ArrayComprehensionLoop;
import org.mozilla.javascript.ast.ArrayLiteral;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.BreakStatement;
import org.mozilla.javascript.ast.CatchClause;
import org.mozilla.javascript.ast.ConditionalExpression;
import org.mozilla.javascript.ast.ContinueStatement;
import org.mozilla.javascript.ast.DestructuringForm;
import org.mozilla.javascript.ast.DoLoop;
import org.mozilla.javascript.ast.ElementGet;
import org.mozilla.javascript.ast.EmptyExpression;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.ForInLoop;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.GeneratorExpression;
import org.mozilla.javascript.ast.GeneratorExpressionLoop;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.Jump;
import org.mozilla.javascript.ast.Label;
import org.mozilla.javascript.ast.LabeledStatement;
import org.mozilla.javascript.ast.LetNode;
import org.mozilla.javascript.ast.Loop;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NewExpression;
import org.mozilla.javascript.ast.NumberLiteral;
import org.mozilla.javascript.ast.ObjectLiteral;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.ParenthesizedExpression;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.RegExpLiteral;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.Scope;
import org.mozilla.javascript.ast.ScriptNode;
import org.mozilla.javascript.ast.StringLiteral;
import org.mozilla.javascript.ast.SwitchCase;
import org.mozilla.javascript.ast.SwitchStatement;
import org.mozilla.javascript.ast.Symbol;
import org.mozilla.javascript.ast.ThrowStatement;
import org.mozilla.javascript.ast.TryStatement;
import org.mozilla.javascript.ast.UnaryExpression;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.VariableInitializer;
import org.mozilla.javascript.ast.WhileLoop;
import org.mozilla.javascript.ast.WithStatement;
import org.mozilla.javascript.ast.XmlDotQuery;
import org.mozilla.javascript.ast.XmlElemRef;
import org.mozilla.javascript.ast.XmlExpression;
import org.mozilla.javascript.ast.XmlFragment;
import org.mozilla.javascript.ast.XmlLiteral;
import org.mozilla.javascript.ast.XmlMemberGet;
import org.mozilla.javascript.ast.XmlPropRef;
import org.mozilla.javascript.ast.XmlRef;
import org.mozilla.javascript.ast.XmlString;
import org.mozilla.javascript.ast.Yield;

public final class IRFactory extends Parser {
   private static final int LOOP_DO_WHILE = 0;
   private static final int LOOP_WHILE = 1;
   private static final int LOOP_FOR = 2;
   private static final int ALWAYS_TRUE_BOOLEAN = 1;
   private static final int ALWAYS_FALSE_BOOLEAN = -1;
   private Decompiler decompiler;

   public IRFactory() {
      super();
      this.decompiler = new Decompiler();
   }

   public IRFactory(CompilerEnvirons env) {
      this(env, env.getErrorReporter());
   }

   public IRFactory(CompilerEnvirons env, ErrorReporter errorReporter) {
      super(env, errorReporter);
      this.decompiler = new Decompiler();
   }

   public ScriptNode transformTree(AstRoot root) {
      this.currentScriptOrFn = root;
      this.inUseStrictDirective = root.isInStrictMode();
      int sourceStartOffset = this.decompiler.getCurrentOffset();
      ScriptNode script = (ScriptNode)this.transform(root);
      int sourceEndOffset = this.decompiler.getCurrentOffset();
      script.setEncodedSourceBounds(sourceStartOffset, sourceEndOffset);
      if (this.compilerEnv.isGeneratingSource()) {
         script.setEncodedSource(this.decompiler.getEncodedSource());
      }

      this.decompiler = null;
      return script;
   }

   public Node transform(AstNode node) {
      switch (node.getType()) {
         case 4:
            return this.transformReturn((ReturnStatement)node);
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 31:
         case 32:
         case 34:
         case 35:
         case 37:
         case 46:
         case 47:
         case 49:
         case 51:
         case 52:
         case 53:
         case 54:
         case 55:
         case 56:
         case 57:
         case 58:
         case 59:
         case 60:
         case 61:
         case 62:
         case 63:
         case 64:
         case 67:
         case 68:
         case 69:
         case 70:
         case 71:
         case 73:
         case 74:
         case 75:
         case 76:
         case 77:
         case 78:
         case 79:
         case 80:
         case 82:
         case 83:
         case 84:
         case 85:
         case 86:
         case 87:
         case 88:
         case 89:
         case 90:
         case 91:
         case 92:
         case 93:
         case 94:
         case 95:
         case 96:
         case 97:
         case 98:
         case 99:
         case 100:
         case 101:
         case 103:
         case 104:
         case 105:
         case 106:
         case 107:
         case 108:
         case 110:
         case 111:
         case 113:
         case 115:
         case 116:
         case 122:
         case 124:
         case 125:
         case 126:
         case 127:
         case 130:
         case 131:
         case 132:
         case 133:
         case 134:
         case 135:
         case 137:
         case 138:
         case 139:
         case 140:
         case 141:
         case 142:
         case 143:
         case 144:
         case 145:
         case 146:
         case 147:
         case 148:
         case 149:
         case 150:
         case 151:
         case 152:
         case 153:
         case 154:
         case 155:
         case 156:
         case 158:
         case 159:
         case 161:
         default:
            if (node instanceof ExpressionStatement) {
               return this.transformExprStmt((ExpressionStatement)node);
            } else if (node instanceof Assignment) {
               return this.transformAssignment((Assignment)node);
            } else if (node instanceof UnaryExpression) {
               return this.transformUnary((UnaryExpression)node);
            } else if (node instanceof XmlMemberGet) {
               return this.transformXmlMemberGet((XmlMemberGet)node);
            } else if (node instanceof InfixExpression) {
               return this.transformInfix((InfixExpression)node);
            } else if (node instanceof VariableDeclaration) {
               return this.transformVariables((VariableDeclaration)node);
            } else if (node instanceof ParenthesizedExpression) {
               return this.transformParenExpr((ParenthesizedExpression)node);
            } else if (node instanceof LabeledStatement) {
               return this.transformLabeledStatement((LabeledStatement)node);
            } else if (node instanceof LetNode) {
               return this.transformLetNode((LetNode)node);
            } else if (node instanceof XmlRef) {
               return this.transformXmlRef((XmlRef)node);
            } else {
               if (node instanceof XmlLiteral) {
                  return this.transformXmlLiteral((XmlLiteral)node);
               }

               throw new IllegalArgumentException("Can't transform: " + node);
            }
         case 30:
            return this.transformNewExpr((NewExpression)node);
         case 33:
            return this.transformPropertyGet((PropertyGet)node);
         case 36:
            return this.transformElementGet((ElementGet)node);
         case 38:
            return this.transformFunctionCall((FunctionCall)node);
         case 39:
            return this.transformName((Name)node);
         case 40:
            return this.transformNumber((NumberLiteral)node);
         case 41:
            return this.transformString((StringLiteral)node);
         case 42:
         case 43:
         case 44:
         case 45:
         case 160:
            return this.transformLiteral(node);
         case 48:
            return this.transformRegExp((RegExpLiteral)node);
         case 50:
            return this.transformThrow((ThrowStatement)node);
         case 65:
            return this.transformArrayLiteral((ArrayLiteral)node);
         case 66:
            return this.transformObjectLiteral((ObjectLiteral)node);
         case 72:
            return this.transformYield((Yield)node);
         case 81:
            return this.transformTry((TryStatement)node);
         case 102:
            return this.transformCondExpr((ConditionalExpression)node);
         case 109:
            return this.transformFunction((FunctionNode)node);
         case 112:
            return this.transformIf((IfStatement)node);
         case 114:
            return this.transformSwitch((SwitchStatement)node);
         case 117:
            return this.transformWhileLoop((WhileLoop)node);
         case 118:
            return this.transformDoLoop((DoLoop)node);
         case 119:
            if (node instanceof ForInLoop) {
               return this.transformForInLoop((ForInLoop)node);
            }

            return this.transformForLoop((ForLoop)node);
         case 120:
            return this.transformBreak((BreakStatement)node);
         case 121:
            return this.transformContinue((ContinueStatement)node);
         case 123:
            return this.transformWith((WithStatement)node);
         case 128:
            return node;
         case 129:
            return this.transformBlock(node);
         case 136:
            return this.transformScript((ScriptNode)node);
         case 157:
            return this.transformArrayComp((ArrayComprehension)node);
         case 162:
            return this.transformGenExpr((GeneratorExpression)node);
      }
   }

   private Node transformArrayComp(ArrayComprehension node) {
      int lineno = node.getLineno();
      Scope scopeNode = this.createScopeNode(157, lineno);
      String arrayName = this.currentScriptOrFn.getNextTempName();
      this.pushScope(scopeNode);

      Scope var8;
      try {
         this.defineSymbol(153, arrayName, false);
         Node block = new Node(129, lineno);
         Node newArray = this.createCallOrNew(30, this.createName("Array"));
         Node init = new Node(133, this.createAssignment(90, this.createName(arrayName), newArray), lineno);
         block.addChildToBack(init);
         block.addChildToBack(this.arrayCompTransformHelper(node, arrayName));
         scopeNode.addChildToBack(block);
         scopeNode.addChildToBack(this.createName(arrayName));
         var8 = scopeNode;
      } finally {
         this.popScope();
      }

      return var8;
   }

   private Node arrayCompTransformHelper(ArrayComprehension node, String arrayName) {
      this.decompiler.addToken(83);
      int lineno = node.getLineno();
      Node expr = this.transform(node.getResult());
      List<ArrayComprehensionLoop> loops = node.getLoops();
      int numLoops = loops.size();
      Node[] iterators = new Node[numLoops];
      Node[] iteratedObjs = new Node[numLoops];

      for(int i = 0; i < numLoops; ++i) {
         ArrayComprehensionLoop acl = (ArrayComprehensionLoop)loops.get(i);
         this.decompiler.addName(" ");
         this.decompiler.addToken(119);
         if (acl.isForEach()) {
            this.decompiler.addName("each ");
         }

         this.decompiler.addToken(87);
         AstNode iter = acl.getIterator();
         String name = null;
         if (iter.getType() == 39) {
            name = iter.getString();
            this.decompiler.addName(name);
         } else {
            this.decompile(iter);
            name = this.currentScriptOrFn.getNextTempName();
            this.defineSymbol(87, name, false);
            expr = this.createBinary(89, this.createAssignment(90, iter, this.createName(name)), expr);
         }

         Node init = this.createName(name);
         this.defineSymbol(153, name, false);
         iterators[i] = init;
         this.decompiler.addToken(52);
         iteratedObjs[i] = this.transform(acl.getIteratedObject());
         this.decompiler.addToken(88);
      }

      Node call = this.createCallOrNew(38, this.createPropertyGet(this.createName(arrayName), (String)null, "push", 0));
      Node body = new Node(133, call, lineno);
      if (node.getFilter() != null) {
         this.decompiler.addName(" ");
         this.decompiler.addToken(112);
         this.decompiler.addToken(87);
         body = this.createIf(this.transform(node.getFilter()), body, (Node)null, lineno);
         this.decompiler.addToken(88);
      }

      int pushed = 0;
      boolean var18 = false;

      try {
         var18 = true;

         for(int var24 = numLoops - 1; var24 >= 0; --var24) {
            ArrayComprehensionLoop acl = (ArrayComprehensionLoop)loops.get(var24);
            Scope loop = this.createLoopNode((Node)null, acl.getLineno());
            this.pushScope(loop);
            ++pushed;
            body = this.createForIn(153, loop, iterators[var24], iteratedObjs[var24], body, acl.isForEach());
         }

         var18 = false;
      } finally {
         if (var18) {
            for(int i = 0; i < pushed; ++i) {
               this.popScope();
            }

         }
      }

      for(int i = 0; i < pushed; ++i) {
         this.popScope();
      }

      this.decompiler.addToken(84);
      call.addChildToBack(expr);
      return body;
   }

   private Node transformArrayLiteral(ArrayLiteral node) {
      if (node.isDestructuring()) {
         return node;
      } else {
         this.decompiler.addToken(83);
         List<AstNode> elems = node.getElements();
         Node array = new Node(65);
         List<Integer> skipIndexes = null;

         for(int i = 0; i < elems.size(); ++i) {
            AstNode elem = (AstNode)elems.get(i);
            if (elem.getType() != 128) {
               array.addChildToBack(this.transform(elem));
            } else {
               if (skipIndexes == null) {
                  skipIndexes = new ArrayList();
               }

               skipIndexes.add(i);
            }

            if (i < elems.size() - 1) {
               this.decompiler.addToken(89);
            }
         }

         this.decompiler.addToken(84);
         array.putIntProp(21, node.getDestructuringLength());
         if (skipIndexes != null) {
            int[] skips = new int[skipIndexes.size()];

            for(int i = 0; i < skipIndexes.size(); ++i) {
               skips[i] = (Integer)skipIndexes.get(i);
            }

            array.putProp(11, skips);
         }

         return array;
      }
   }

   private Node transformAssignment(Assignment node) {
      AstNode left = this.removeParens(node.getLeft());
      Node target = null;
      if (this.isDestructuring(left)) {
         this.decompile(left);
         target = left;
      } else {
         target = this.transform(left);
      }

      this.decompiler.addToken(node.getType());
      return this.createAssignment(node.getType(), target, this.transform(node.getRight()));
   }

   private Node transformBlock(AstNode node) {
      if (node instanceof Scope) {
         this.pushScope((Scope)node);
      }

      AstNode var9;
      try {
         List<Node> kids = new ArrayList();

         for(Node kid : node) {
            kids.add(this.transform((AstNode)kid));
         }

         node.removeChildren();

         for(Node kid : kids) {
            node.addChildToBack(kid);
         }

         var9 = node;
      } finally {
         if (node instanceof Scope) {
            this.popScope();
         }

      }

      return var9;
   }

   private Node transformBreak(BreakStatement node) {
      this.decompiler.addToken(120);
      if (node.getBreakLabel() != null) {
         this.decompiler.addName(node.getBreakLabel().getIdentifier());
      }

      this.decompiler.addEOL(82);
      return node;
   }

   private Node transformCondExpr(ConditionalExpression node) {
      Node test = this.transform(node.getTestExpression());
      this.decompiler.addToken(102);
      Node ifTrue = this.transform(node.getTrueExpression());
      this.decompiler.addToken(103);
      Node ifFalse = this.transform(node.getFalseExpression());
      return this.createCondExpr(test, ifTrue, ifFalse);
   }

   private Node transformContinue(ContinueStatement node) {
      this.decompiler.addToken(121);
      if (node.getLabel() != null) {
         this.decompiler.addName(node.getLabel().getIdentifier());
      }

      this.decompiler.addEOL(82);
      return node;
   }

   private Node transformDoLoop(DoLoop loop) {
      loop.setType(132);
      this.pushScope(loop);

      Node var4;
      try {
         this.decompiler.addToken(118);
         this.decompiler.addEOL(85);
         Node body = this.transform(loop.getBody());
         this.decompiler.addToken(86);
         this.decompiler.addToken(117);
         this.decompiler.addToken(87);
         Node cond = this.transform(loop.getCondition());
         this.decompiler.addToken(88);
         this.decompiler.addEOL(82);
         var4 = this.createLoop(loop, 0, body, cond, (Node)null, (Node)null);
      } finally {
         this.popScope();
      }

      return var4;
   }

   private Node transformElementGet(ElementGet node) {
      Node target = this.transform(node.getTarget());
      this.decompiler.addToken(83);
      Node element = this.transform(node.getElement());
      this.decompiler.addToken(84);
      return new Node(36, target, element);
   }

   private Node transformExprStmt(ExpressionStatement node) {
      Node expr = this.transform(node.getExpression());
      this.decompiler.addEOL(82);
      return new Node(node.getType(), expr, node.getLineno());
   }

   private Node transformForInLoop(ForInLoop loop) {
      this.decompiler.addToken(119);
      if (loop.isForEach()) {
         this.decompiler.addName("each ");
      }

      this.decompiler.addToken(87);
      loop.setType(132);
      this.pushScope(loop);

      Node var7;
      try {
         int declType = -1;
         AstNode iter = loop.getIterator();
         if (iter instanceof VariableDeclaration) {
            declType = ((VariableDeclaration)iter).getType();
         }

         Node lhs = this.transform(iter);
         this.decompiler.addToken(52);
         Node obj = this.transform(loop.getIteratedObject());
         this.decompiler.addToken(88);
         this.decompiler.addEOL(85);
         Node body = this.transform(loop.getBody());
         this.decompiler.addEOL(86);
         var7 = this.createForIn(declType, loop, lhs, obj, body, loop.isForEach());
      } finally {
         this.popScope();
      }

      return var7;
   }

   private Node transformForLoop(ForLoop loop) {
      this.decompiler.addToken(119);
      this.decompiler.addToken(87);
      loop.setType(132);
      Scope savedScope = this.currentScope;
      this.currentScope = loop;

      Node var7;
      try {
         Node init = this.transform(loop.getInitializer());
         this.decompiler.addToken(82);
         Node test = this.transform(loop.getCondition());
         this.decompiler.addToken(82);
         Node incr = this.transform(loop.getIncrement());
         this.decompiler.addToken(88);
         this.decompiler.addEOL(85);
         Node body = this.transform(loop.getBody());
         this.decompiler.addEOL(86);
         var7 = this.createFor(loop, init, test, incr, body);
      } finally {
         this.currentScope = savedScope;
      }

      return var7;
   }

   private Node transformFunction(FunctionNode fn) {
      int functionType = fn.getFunctionType();
      int start = this.decompiler.markFunctionStart(functionType);
      Node mexpr = this.decompileFunctionHeader(fn);
      int index = this.currentScriptOrFn.addFunction(fn);
      Parser.PerFunctionVariables savedVars = new Parser.PerFunctionVariables(fn);

      Node var12;
      try {
         Node destructuring = (Node)fn.getProp(23);
         fn.removeProp(23);
         int lineno = fn.getBody().getLineno();
         ++this.nestingOfFunction;
         Node body = this.transform(fn.getBody());
         if (!fn.isExpressionClosure()) {
            this.decompiler.addToken(86);
         }

         fn.setEncodedSourceBounds(start, this.decompiler.markFunctionEnd(start));
         if (functionType != 2 && !fn.isExpressionClosure()) {
            this.decompiler.addToken(1);
         }

         if (destructuring != null) {
            body.addChildToFront(new Node(133, destructuring, lineno));
         }

         int syntheticType = fn.getFunctionType();
         Node pn = this.initFunction(fn, index, body, syntheticType);
         if (mexpr != null) {
            pn = this.createAssignment(90, mexpr, pn);
            if (syntheticType != 2) {
               pn = this.createExprStatementNoReturn(pn, fn.getLineno());
            }
         }

         var12 = pn;
      } finally {
         --this.nestingOfFunction;
         savedVars.restore();
      }

      return var12;
   }

   private Node transformFunctionCall(FunctionCall node) {
      Node call = this.createCallOrNew(38, this.transform(node.getTarget()));
      call.setLineno(node.getLineno());
      this.decompiler.addToken(87);
      List<AstNode> args = node.getArguments();

      for(int i = 0; i < args.size(); ++i) {
         AstNode arg = (AstNode)args.get(i);
         call.addChildToBack(this.transform(arg));
         if (i < args.size() - 1) {
            this.decompiler.addToken(89);
         }
      }

      this.decompiler.addToken(88);
      return call;
   }

   private Node transformGenExpr(GeneratorExpression node) {
      FunctionNode fn = new FunctionNode();
      fn.setSourceName(this.currentScriptOrFn.getNextTempName());
      fn.setIsGenerator();
      fn.setFunctionType(2);
      fn.setRequiresActivation();
      int functionType = fn.getFunctionType();
      int start = this.decompiler.markFunctionStart(functionType);
      Node mexpr = this.decompileFunctionHeader(fn);
      int index = this.currentScriptOrFn.addFunction(fn);
      Parser.PerFunctionVariables savedVars = new Parser.PerFunctionVariables(fn);

      Node pn;
      try {
         Node destructuring = (Node)fn.getProp(23);
         fn.removeProp(23);
         int lineno = node.lineno;
         ++this.nestingOfFunction;
         Node body = this.genExprTransformHelper(node);
         if (!fn.isExpressionClosure()) {
            this.decompiler.addToken(86);
         }

         fn.setEncodedSourceBounds(start, this.decompiler.markFunctionEnd(start));
         if (functionType != 2 && !fn.isExpressionClosure()) {
            this.decompiler.addToken(1);
         }

         if (destructuring != null) {
            body.addChildToFront(new Node(133, destructuring, lineno));
         }

         int syntheticType = fn.getFunctionType();
         pn = this.initFunction(fn, index, body, syntheticType);
         if (mexpr != null) {
            pn = this.createAssignment(90, mexpr, pn);
            if (syntheticType != 2) {
               pn = this.createExprStatementNoReturn(pn, fn.getLineno());
            }
         }
      } finally {
         --this.nestingOfFunction;
         savedVars.restore();
      }

      Node call = this.createCallOrNew(38, pn);
      call.setLineno(node.getLineno());
      this.decompiler.addToken(87);
      this.decompiler.addToken(88);
      return call;
   }

   private Node genExprTransformHelper(GeneratorExpression node) {
      this.decompiler.addToken(87);
      int lineno = node.getLineno();
      Node expr = this.transform(node.getResult());
      List<GeneratorExpressionLoop> loops = node.getLoops();
      int numLoops = loops.size();
      Node[] iterators = new Node[numLoops];
      Node[] iteratedObjs = new Node[numLoops];

      for(int i = 0; i < numLoops; ++i) {
         GeneratorExpressionLoop acl = (GeneratorExpressionLoop)loops.get(i);
         this.decompiler.addName(" ");
         this.decompiler.addToken(119);
         this.decompiler.addToken(87);
         AstNode iter = acl.getIterator();
         String name = null;
         if (iter.getType() == 39) {
            name = iter.getString();
            this.decompiler.addName(name);
         } else {
            this.decompile(iter);
            name = this.currentScriptOrFn.getNextTempName();
            this.defineSymbol(87, name, false);
            expr = this.createBinary(89, this.createAssignment(90, iter, this.createName(name)), expr);
         }

         Node init = this.createName(name);
         this.defineSymbol(153, name, false);
         iterators[i] = init;
         this.decompiler.addToken(52);
         iteratedObjs[i] = this.transform(acl.getIteratedObject());
         this.decompiler.addToken(88);
      }

      Node yield = new Node(72, expr, node.getLineno());
      Node body = new Node(133, yield, lineno);
      if (node.getFilter() != null) {
         this.decompiler.addName(" ");
         this.decompiler.addToken(112);
         this.decompiler.addToken(87);
         body = this.createIf(this.transform(node.getFilter()), body, (Node)null, lineno);
         this.decompiler.addToken(88);
      }

      int pushed = 0;
      boolean var17 = false;

      try {
         var17 = true;

         for(int var23 = numLoops - 1; var23 >= 0; --var23) {
            GeneratorExpressionLoop acl = (GeneratorExpressionLoop)loops.get(var23);
            Scope loop = this.createLoopNode((Node)null, acl.getLineno());
            this.pushScope(loop);
            ++pushed;
            body = this.createForIn(153, loop, iterators[var23], iteratedObjs[var23], body, acl.isForEach());
         }

         var17 = false;
      } finally {
         if (var17) {
            for(int i = 0; i < pushed; ++i) {
               this.popScope();
            }

         }
      }

      for(int i = 0; i < pushed; ++i) {
         this.popScope();
      }

      this.decompiler.addToken(88);
      return body;
   }

   private Node transformIf(IfStatement n) {
      this.decompiler.addToken(112);
      this.decompiler.addToken(87);
      Node cond = this.transform(n.getCondition());
      this.decompiler.addToken(88);
      this.decompiler.addEOL(85);
      Node ifTrue = this.transform(n.getThenPart());
      Node ifFalse = null;
      if (n.getElsePart() != null) {
         this.decompiler.addToken(86);
         this.decompiler.addToken(113);
         this.decompiler.addEOL(85);
         ifFalse = this.transform(n.getElsePart());
      }

      this.decompiler.addEOL(86);
      return this.createIf(cond, ifTrue, ifFalse, n.getLineno());
   }

   private Node transformInfix(InfixExpression node) {
      Node left = this.transform(node.getLeft());
      this.decompiler.addToken(node.getType());
      Node right = this.transform(node.getRight());
      if (node instanceof XmlDotQuery) {
         this.decompiler.addToken(88);
      }

      return this.createBinary(node.getType(), left, right);
   }

   private Node transformLabeledStatement(LabeledStatement ls) {
      for(Label lb : ls.getLabels()) {
         this.decompiler.addName(lb.getName());
         this.decompiler.addEOL(103);
      }

      Label label = ls.getFirstLabel();
      Node statement = this.transform(ls.getStatement());
      Node breakTarget = Node.newTarget();
      Node block = new Node(129, label, statement, breakTarget);
      label.target = breakTarget;
      return block;
   }

   private Node transformLetNode(LetNode node) {
      this.pushScope(node);

      LetNode var4;
      try {
         this.decompiler.addToken(153);
         this.decompiler.addToken(87);
         Node vars = this.transformVariableInitializers(node.getVariables());
         this.decompiler.addToken(88);
         node.addChildToBack(vars);
         boolean letExpr = node.getType() == 158;
         if (node.getBody() != null) {
            if (letExpr) {
               this.decompiler.addName(" ");
            } else {
               this.decompiler.addEOL(85);
            }

            node.addChildToBack(this.transform(node.getBody()));
            if (!letExpr) {
               this.decompiler.addEOL(86);
            }
         }

         var4 = node;
      } finally {
         this.popScope();
      }

      return var4;
   }

   private Node transformLiteral(AstNode node) {
      this.decompiler.addToken(node.getType());
      return node;
   }

   private Node transformName(Name node) {
      this.decompiler.addName(node.getIdentifier());
      return node;
   }

   private Node transformNewExpr(NewExpression node) {
      this.decompiler.addToken(30);
      Node nx = this.createCallOrNew(30, this.transform(node.getTarget()));
      nx.setLineno(node.getLineno());
      List<AstNode> args = node.getArguments();
      this.decompiler.addToken(87);

      for(int i = 0; i < args.size(); ++i) {
         AstNode arg = (AstNode)args.get(i);
         nx.addChildToBack(this.transform(arg));
         if (i < args.size() - 1) {
            this.decompiler.addToken(89);
         }
      }

      this.decompiler.addToken(88);
      if (node.getInitializer() != null) {
         nx.addChildToBack(this.transformObjectLiteral(node.getInitializer()));
      }

      return nx;
   }

   private Node transformNumber(NumberLiteral node) {
      this.decompiler.addNumber(node.getNumber());
      return node;
   }

   private Node transformObjectLiteral(ObjectLiteral node) {
      if (node.isDestructuring()) {
         return node;
      } else {
         this.decompiler.addToken(85);
         List<ObjectProperty> elems = node.getElements();
         Node object = new Node(66);
         Object[] properties;
         if (elems.isEmpty()) {
            properties = ScriptRuntime.emptyArgs;
         } else {
            int size = elems.size();
            int i = 0;
            properties = new Object[size];

            for(ObjectProperty prop : elems) {
               if (prop.isGetter()) {
                  this.decompiler.addToken(151);
               } else if (prop.isSetter()) {
                  this.decompiler.addToken(152);
               }

               properties[i++] = this.getPropKey(prop.getLeft());
               if (!prop.isGetter() && !prop.isSetter()) {
                  this.decompiler.addToken(66);
               }

               Node right = this.transform(prop.getRight());
               if (prop.isGetter()) {
                  right = this.createUnary(151, right);
               } else if (prop.isSetter()) {
                  right = this.createUnary(152, right);
               }

               object.addChildToBack(right);
               if (i < size) {
                  this.decompiler.addToken(89);
               }
            }
         }

         this.decompiler.addToken(86);
         object.putProp(12, properties);
         return object;
      }
   }

   private Object getPropKey(Node id) {
      Object key;
      if (id instanceof Name) {
         String s = ((Name)id).getIdentifier();
         this.decompiler.addName(s);
         key = ScriptRuntime.getIndexObject(s);
      } else if (id instanceof StringLiteral) {
         String s = ((StringLiteral)id).getValue();
         this.decompiler.addString(s);
         key = ScriptRuntime.getIndexObject(s);
      } else {
         if (!(id instanceof NumberLiteral)) {
            throw Kit.codeBug();
         }

         double n = ((NumberLiteral)id).getNumber();
         this.decompiler.addNumber(n);
         key = ScriptRuntime.getIndexObject(n);
      }

      return key;
   }

   private Node transformParenExpr(ParenthesizedExpression node) {
      AstNode expr = node.getExpression();
      this.decompiler.addToken(87);

      int count;
      for(count = 1; expr instanceof ParenthesizedExpression; expr = ((ParenthesizedExpression)expr).getExpression()) {
         this.decompiler.addToken(87);
         ++count;
      }

      Node result = this.transform(expr);

      for(int i = 0; i < count; ++i) {
         this.decompiler.addToken(88);
      }

      result.putProp(19, Boolean.TRUE);
      return result;
   }

   private Node transformPropertyGet(PropertyGet node) {
      Node target = this.transform(node.getTarget());
      String name = node.getProperty().getIdentifier();
      this.decompiler.addToken(108);
      this.decompiler.addName(name);
      return this.createPropertyGet(target, (String)null, name, 0);
   }

   private Node transformRegExp(RegExpLiteral node) {
      this.decompiler.addRegexp(node.getValue(), node.getFlags());
      this.currentScriptOrFn.addRegExp(node);
      return node;
   }

   private Node transformReturn(ReturnStatement node) {
      boolean expClosure = Boolean.TRUE.equals(node.getProp(25));
      if (expClosure) {
         this.decompiler.addName(" ");
      } else {
         this.decompiler.addToken(4);
      }

      AstNode rv = node.getReturnValue();
      Node value = rv == null ? null : this.transform(rv);
      if (!expClosure) {
         this.decompiler.addEOL(82);
      }

      return rv == null ? new Node(4, node.getLineno()) : new Node(4, value, node.getLineno());
   }

   private Node transformScript(ScriptNode node) {
      this.decompiler.addToken(136);
      if (this.currentScope != null) {
         Kit.codeBug();
      }

      this.currentScope = node;
      Node body = new Node(129);

      for(Node kid : node) {
         body.addChildToBack(this.transform((AstNode)kid));
      }

      node.removeChildren();
      Node children = body.getFirstChild();
      if (children != null) {
         node.addChildrenToBack(children);
      }

      return node;
   }

   private Node transformString(StringLiteral node) {
      this.decompiler.addString(node.getValue());
      return Node.newString(node.getValue());
   }

   private Node transformSwitch(SwitchStatement node) {
      this.decompiler.addToken(114);
      this.decompiler.addToken(87);
      Node switchExpr = this.transform(node.getExpression());
      this.decompiler.addToken(88);
      node.addChildToBack(switchExpr);
      Node block = new Node(129, node, node.getLineno());
      this.decompiler.addEOL(85);

      for(SwitchCase sc : node.getCases()) {
         AstNode expr = sc.getExpression();
         Node caseExpr = null;
         if (expr != null) {
            this.decompiler.addToken(115);
            caseExpr = this.transform(expr);
         } else {
            this.decompiler.addToken(116);
         }

         this.decompiler.addEOL(103);
         List<AstNode> stmts = sc.getStatements();
         Node body = new Block();
         if (stmts != null) {
            for(AstNode kid : stmts) {
               body.addChildToBack(this.transform(kid));
            }
         }

         this.addSwitchCase(block, caseExpr, body);
      }

      this.decompiler.addEOL(86);
      this.closeSwitch(block);
      return block;
   }

   private Node transformThrow(ThrowStatement node) {
      this.decompiler.addToken(50);
      Node value = this.transform(node.getExpression());
      this.decompiler.addEOL(82);
      return new Node(50, value, node.getLineno());
   }

   private Node transformTry(TryStatement node) {
      this.decompiler.addToken(81);
      this.decompiler.addEOL(85);
      Node tryBlock = this.transform(node.getTryBlock());
      this.decompiler.addEOL(86);
      Node catchBlocks = new Block();

      for(CatchClause cc : node.getCatchClauses()) {
         this.decompiler.addToken(124);
         this.decompiler.addToken(87);
         String varName = cc.getVarName().getIdentifier();
         this.decompiler.addName(varName);
         Node catchCond = null;
         AstNode ccc = cc.getCatchCondition();
         if (ccc != null) {
            this.decompiler.addName(" ");
            this.decompiler.addToken(112);
            catchCond = this.transform(ccc);
         } else {
            catchCond = new EmptyExpression();
         }

         this.decompiler.addToken(88);
         this.decompiler.addEOL(85);
         Node body = this.transform(cc.getBody());
         this.decompiler.addEOL(86);
         catchBlocks.addChildToBack(this.createCatch(varName, catchCond, body, cc.getLineno()));
      }

      Node finallyBlock = null;
      if (node.getFinallyBlock() != null) {
         this.decompiler.addToken(125);
         this.decompiler.addEOL(85);
         finallyBlock = this.transform(node.getFinallyBlock());
         this.decompiler.addEOL(86);
      }

      return this.createTryCatchFinally(tryBlock, catchBlocks, finallyBlock, node.getLineno());
   }

   private Node transformUnary(UnaryExpression node) {
      int type = node.getType();
      if (type == 74) {
         return this.transformDefaultXmlNamepace(node);
      } else {
         if (node.isPrefix()) {
            this.decompiler.addToken(type);
         }

         Node child = this.transform(node.getOperand());
         if (node.isPostfix()) {
            this.decompiler.addToken(type);
         }

         return type != 106 && type != 107 ? this.createUnary(type, child) : this.createIncDec(type, node.isPostfix(), child);
      }
   }

   private Node transformVariables(VariableDeclaration node) {
      this.decompiler.addToken(node.getType());
      this.transformVariableInitializers(node);
      AstNode parent = node.getParent();
      if (!(parent instanceof Loop) && !(parent instanceof LetNode)) {
         this.decompiler.addEOL(82);
      }

      return node;
   }

   private Node transformVariableInitializers(VariableDeclaration node) {
      List<VariableInitializer> vars = node.getVariables();
      int size = vars.size();
      int i = 0;

      for(VariableInitializer var : vars) {
         AstNode target = var.getTarget();
         AstNode init = var.getInitializer();
         Node left = null;
         if (var.isDestructuring()) {
            this.decompile(target);
            left = target;
         } else {
            left = this.transform(target);
         }

         Node right = null;
         if (init != null) {
            this.decompiler.addToken(90);
            right = this.transform(init);
         }

         if (var.isDestructuring()) {
            if (right == null) {
               node.addChildToBack(left);
            } else {
               Node d = this.createDestructuringAssignment(node.getType(), left, right);
               node.addChildToBack(d);
            }
         } else {
            if (right != null) {
               left.addChildToBack(right);
            }

            node.addChildToBack(left);
         }

         if (i++ < size - 1) {
            this.decompiler.addToken(89);
         }
      }

      return node;
   }

   private Node transformWhileLoop(WhileLoop loop) {
      this.decompiler.addToken(117);
      loop.setType(132);
      this.pushScope(loop);

      Node var4;
      try {
         this.decompiler.addToken(87);
         Node cond = this.transform(loop.getCondition());
         this.decompiler.addToken(88);
         this.decompiler.addEOL(85);
         Node body = this.transform(loop.getBody());
         this.decompiler.addEOL(86);
         var4 = this.createLoop(loop, 1, body, cond, (Node)null, (Node)null);
      } finally {
         this.popScope();
      }

      return var4;
   }

   private Node transformWith(WithStatement node) {
      this.decompiler.addToken(123);
      this.decompiler.addToken(87);
      Node expr = this.transform(node.getExpression());
      this.decompiler.addToken(88);
      this.decompiler.addEOL(85);
      Node stmt = this.transform(node.getStatement());
      this.decompiler.addEOL(86);
      return this.createWith(expr, stmt, node.getLineno());
   }

   private Node transformYield(Yield node) {
      this.decompiler.addToken(72);
      Node kid = node.getValue() == null ? null : this.transform(node.getValue());
      return kid != null ? new Node(72, kid, node.getLineno()) : new Node(72, node.getLineno());
   }

   private Node transformXmlLiteral(XmlLiteral node) {
      Node pnXML = new Node(30, node.getLineno());
      List<XmlFragment> frags = node.getFragments();
      XmlString first = (XmlString)frags.get(0);
      boolean anon = first.getXml().trim().startsWith("<>");
      pnXML.addChildToBack(this.createName(anon ? "XMLList" : "XML"));
      Node pn = null;

      for(XmlFragment frag : frags) {
         if (frag instanceof XmlString) {
            String xml = ((XmlString)frag).getXml();
            this.decompiler.addName(xml);
            if (pn == null) {
               pn = this.createString(xml);
            } else {
               pn = this.createBinary(21, pn, this.createString(xml));
            }
         } else {
            XmlExpression xexpr = (XmlExpression)frag;
            boolean isXmlAttr = xexpr.isXmlAttribute();
            this.decompiler.addToken(85);
            Node expr;
            if (xexpr.getExpression() instanceof EmptyExpression) {
               expr = this.createString("");
            } else {
               expr = this.transform(xexpr.getExpression());
            }

            this.decompiler.addToken(86);
            if (isXmlAttr) {
               expr = this.createUnary(75, expr);
               Node prepend = this.createBinary(21, this.createString("\""), expr);
               expr = this.createBinary(21, prepend, this.createString("\""));
            } else {
               expr = this.createUnary(76, expr);
            }

            pn = this.createBinary(21, pn, expr);
         }
      }

      pnXML.addChildToBack(pn);
      return pnXML;
   }

   private Node transformXmlMemberGet(XmlMemberGet node) {
      XmlRef ref = node.getMemberRef();
      Node pn = this.transform(node.getLeft());
      int flags = ref.isAttributeAccess() ? 2 : 0;
      if (node.getType() == 143) {
         flags |= 4;
         this.decompiler.addToken(143);
      } else {
         this.decompiler.addToken(108);
      }

      return this.transformXmlRef(pn, ref, flags);
   }

   private Node transformXmlRef(XmlRef node) {
      int memberTypeFlags = node.isAttributeAccess() ? 2 : 0;
      return this.transformXmlRef((Node)null, node, memberTypeFlags);
   }

   private Node transformXmlRef(Node pn, XmlRef node, int memberTypeFlags) {
      if ((memberTypeFlags & 2) != 0) {
         this.decompiler.addToken(147);
      }

      Name namespace = node.getNamespace();
      String ns = namespace != null ? namespace.getIdentifier() : null;
      if (ns != null) {
         this.decompiler.addName(ns);
         this.decompiler.addToken(144);
      }

      if (node instanceof XmlPropRef) {
         String name = ((XmlPropRef)node).getPropName().getIdentifier();
         this.decompiler.addName(name);
         return this.createPropertyGet(pn, ns, name, memberTypeFlags);
      } else {
         this.decompiler.addToken(83);
         Node expr = this.transform(((XmlElemRef)node).getExpression());
         this.decompiler.addToken(84);
         return this.createElementGet(pn, ns, expr, memberTypeFlags);
      }
   }

   private Node transformDefaultXmlNamepace(UnaryExpression node) {
      this.decompiler.addToken(116);
      this.decompiler.addName(" xml");
      this.decompiler.addName(" namespace");
      this.decompiler.addToken(90);
      Node child = this.transform(node.getOperand());
      return this.createUnary(74, child);
   }

   private void addSwitchCase(Node switchBlock, Node caseExpression, Node statements) {
      if (switchBlock.getType() != 129) {
         throw Kit.codeBug();
      } else {
         Jump switchNode = (Jump)switchBlock.getFirstChild();
         if (switchNode.getType() != 114) {
            throw Kit.codeBug();
         } else {
            Node gotoTarget = Node.newTarget();
            if (caseExpression != null) {
               Jump caseNode = new Jump(115, caseExpression);
               caseNode.target = gotoTarget;
               switchNode.addChildToBack(caseNode);
            } else {
               switchNode.setDefault(gotoTarget);
            }

            switchBlock.addChildToBack(gotoTarget);
            switchBlock.addChildToBack(statements);
         }
      }
   }

   private void closeSwitch(Node switchBlock) {
      if (switchBlock.getType() != 129) {
         throw Kit.codeBug();
      } else {
         Jump switchNode = (Jump)switchBlock.getFirstChild();
         if (switchNode.getType() != 114) {
            throw Kit.codeBug();
         } else {
            Node switchBreakTarget = Node.newTarget();
            switchNode.target = switchBreakTarget;
            Node defaultTarget = switchNode.getDefault();
            if (defaultTarget == null) {
               defaultTarget = switchBreakTarget;
            }

            switchBlock.addChildAfter(this.makeJump(5, defaultTarget), switchNode);
            switchBlock.addChildToBack(switchBreakTarget);
         }
      }
   }

   private Node createExprStatementNoReturn(Node expr, int lineno) {
      return new Node(133, expr, lineno);
   }

   private Node createString(String string) {
      return Node.newString(string);
   }

   private Node createCatch(String varName, Node catchCond, Node stmts, int lineno) {
      if (catchCond == null) {
         catchCond = new Node(128);
      }

      return new Node(124, this.createName(varName), catchCond, stmts, lineno);
   }

   private Node initFunction(FunctionNode fnNode, int functionIndex, Node statements, int functionType) {
      fnNode.setFunctionType(functionType);
      fnNode.addChildToBack(statements);
      int functionCount = fnNode.getFunctionCount();
      if (functionCount != 0) {
         fnNode.setRequiresActivation();
      }

      if (functionType == 2) {
         Name name = fnNode.getFunctionName();
         if (name != null && name.length() != 0 && fnNode.getSymbol(name.getIdentifier()) == null) {
            fnNode.putSymbol(new Symbol(109, name.getIdentifier()));
            Node setFn = new Node(133, new Node(8, Node.newString(49, name.getIdentifier()), new Node(63)));
            statements.addChildrenToFront(setFn);
         }
      }

      Node lastStmt = statements.getLastChild();
      if (lastStmt == null || lastStmt.getType() != 4) {
         statements.addChildToBack(new Node(4));
      }

      Node result = Node.newString(109, fnNode.getName());
      result.putIntProp(1, functionIndex);
      return result;
   }

   private Scope createLoopNode(Node loopLabel, int lineno) {
      Scope result = this.createScopeNode(132, lineno);
      if (loopLabel != null) {
         ((Jump)loopLabel).setLoop(result);
      }

      return result;
   }

   private Node createFor(Scope loop, Node init, Node test, Node incr, Node body) {
      if (init.getType() == 153) {
         Scope let = Scope.splitScope(loop);
         let.setType(153);
         let.addChildrenToBack(init);
         let.addChildToBack(this.createLoop(loop, 2, body, test, new Node(128), incr));
         return let;
      } else {
         return this.createLoop(loop, 2, body, test, init, incr);
      }
   }

   private Node createLoop(Jump loop, int loopType, Node body, Node cond, Node init, Node incr) {
      Node bodyTarget = Node.newTarget();
      Node condTarget = Node.newTarget();
      if (loopType == 2 && cond.getType() == 128) {
         cond = new Node(45);
      }

      Jump IFEQ = new Jump(6, cond);
      IFEQ.target = bodyTarget;
      Node breakTarget = Node.newTarget();
      loop.addChildToBack(bodyTarget);
      loop.addChildrenToBack(body);
      if (loopType == 1 || loopType == 2) {
         loop.addChildrenToBack(new Node(128, loop.getLineno()));
      }

      loop.addChildToBack(condTarget);
      loop.addChildToBack(IFEQ);
      loop.addChildToBack(breakTarget);
      loop.target = breakTarget;
      Node continueTarget = condTarget;
      if (loopType == 1 || loopType == 2) {
         loop.addChildToFront(this.makeJump(5, condTarget));
         if (loopType == 2) {
            int initType = init.getType();
            if (initType != 128) {
               if (initType != 122 && initType != 153) {
                  init = new Node(133, init);
               }

               loop.addChildToFront(init);
            }

            Node incrTarget = Node.newTarget();
            loop.addChildAfter(incrTarget, body);
            if (incr.getType() != 128) {
               incr = new Node(133, incr);
               loop.addChildAfter(incr, incrTarget);
            }

            continueTarget = incrTarget;
         }
      }

      loop.setContinue(continueTarget);
      return loop;
   }

   private Node createForIn(int declType, Node loop, Node lhs, Node obj, Node body, boolean isForEach) {
      int destructuring = -1;
      int destructuringLen = 0;
      int type = lhs.getType();
      Node lvalue;
      if (type != 122 && type != 153) {
         if (type != 65 && type != 66) {
            lvalue = this.makeReference(lhs);
            if (lvalue == null) {
               this.reportError("msg.bad.for.in.lhs");
               return null;
            }
         } else {
            destructuring = type;
            lvalue = lhs;
            destructuringLen = 0;
            if (lhs instanceof ArrayLiteral) {
               destructuringLen = ((ArrayLiteral)lhs).getDestructuringLength();
            }
         }
      } else {
         Node kid = lhs.getLastChild();
         int kidType = kid.getType();
         if (kidType != 65 && kidType != 66) {
            if (kidType != 39) {
               this.reportError("msg.bad.for.in.lhs");
               return null;
            }

            lvalue = Node.newString(39, kid.getString());
         } else {
            destructuring = kidType;
            type = kidType;
            lvalue = kid;
            destructuringLen = 0;
            if (kid instanceof ArrayLiteral) {
               destructuringLen = ((ArrayLiteral)kid).getDestructuringLength();
            }
         }
      }

      Node localBlock = new Node(141);
      int initType = isForEach ? 59 : (destructuring != -1 ? 60 : 58);
      Node init = new Node(initType, obj);
      init.putProp(3, localBlock);
      Node cond = new Node(61);
      cond.putProp(3, localBlock);
      Node id = new Node(62);
      id.putProp(3, localBlock);
      Node newBody = new Node(129);
      Node assign;
      if (destructuring != -1) {
         assign = this.createDestructuringAssignment(declType, lvalue, id);
         if (!isForEach && (destructuring == 66 || destructuringLen != 2)) {
            this.reportError("msg.bad.for.in.destruct");
         }
      } else {
         assign = this.simpleAssignment(lvalue, id);
      }

      newBody.addChildToBack(new Node(133, assign));
      newBody.addChildToBack(body);
      loop = this.createLoop((Jump)loop, 1, newBody, cond, (Node)null, (Node)null);
      loop.addChildToFront(init);
      if (type == 122 || type == 153) {
         loop.addChildToFront(lhs);
      }

      localBlock.addChildToBack(loop);
      return localBlock;
   }

   private Node createTryCatchFinally(Node tryBlock, Node catchBlocks, Node finallyBlock, int lineno) {
      boolean hasFinally = finallyBlock != null && (finallyBlock.getType() != 129 || finallyBlock.hasChildren());
      if (tryBlock.getType() == 129 && !tryBlock.hasChildren() && !hasFinally) {
         return tryBlock;
      } else {
         boolean hasCatch = catchBlocks.hasChildren();
         if (!hasFinally && !hasCatch) {
            return tryBlock;
         } else {
            Node handlerBlock = new Node(141);
            Jump pn = new Jump(81, tryBlock, lineno);
            pn.putProp(3, handlerBlock);
            if (hasCatch) {
               Node endCatch = Node.newTarget();
               pn.addChildToBack(this.makeJump(5, endCatch));
               Node catchTarget = Node.newTarget();
               pn.target = catchTarget;
               pn.addChildToBack(catchTarget);
               Node catchScopeBlock = new Node(141);
               Node cb = catchBlocks.getFirstChild();
               boolean hasDefault = false;

               for(int scopeIndex = 0; cb != null; ++scopeIndex) {
                  int catchLineNo = cb.getLineno();
                  Node name = cb.getFirstChild();
                  Node cond = name.getNext();
                  Node catchStatement = cond.getNext();
                  cb.removeChild(name);
                  cb.removeChild(cond);
                  cb.removeChild(catchStatement);
                  catchStatement.addChildToBack(new Node(3));
                  catchStatement.addChildToBack(this.makeJump(5, endCatch));
                  Node condStmt;
                  if (cond.getType() == 128) {
                     condStmt = catchStatement;
                     hasDefault = true;
                  } else {
                     condStmt = this.createIf(cond, catchStatement, (Node)null, catchLineNo);
                  }

                  Node catchScope = new Node(57, name, this.createUseLocal(handlerBlock));
                  catchScope.putProp(3, catchScopeBlock);
                  catchScope.putIntProp(14, scopeIndex);
                  catchScopeBlock.addChildToBack(catchScope);
                  catchScopeBlock.addChildToBack(this.createWith(this.createUseLocal(catchScopeBlock), condStmt, catchLineNo));
                  cb = cb.getNext();
               }

               pn.addChildToBack(catchScopeBlock);
               if (!hasDefault) {
                  Node rethrow = new Node(51);
                  rethrow.putProp(3, handlerBlock);
                  pn.addChildToBack(rethrow);
               }

               pn.addChildToBack(endCatch);
            }

            if (hasFinally) {
               Node finallyTarget = Node.newTarget();
               pn.setFinally(finallyTarget);
               pn.addChildToBack(this.makeJump(135, finallyTarget));
               Node finallyEnd = Node.newTarget();
               pn.addChildToBack(this.makeJump(5, finallyEnd));
               pn.addChildToBack(finallyTarget);
               Node fBlock = new Node(125, finallyBlock);
               fBlock.putProp(3, handlerBlock);
               pn.addChildToBack(fBlock);
               pn.addChildToBack(finallyEnd);
            }

            handlerBlock.addChildToBack(pn);
            return handlerBlock;
         }
      }
   }

   private Node createWith(Node obj, Node body, int lineno) {
      this.setRequiresActivation();
      Node result = new Node(129, lineno);
      result.addChildToBack(new Node(2, obj));
      Node bodyNode = new Node(123, body, lineno);
      result.addChildrenToBack(bodyNode);
      result.addChildToBack(new Node(3));
      return result;
   }

   private Node createIf(Node cond, Node ifTrue, Node ifFalse, int lineno) {
      int condStatus = isAlwaysDefinedBoolean(cond);
      if (condStatus == 1) {
         return ifTrue;
      } else if (condStatus == -1) {
         return ifFalse != null ? ifFalse : new Node(129, lineno);
      } else {
         Node result = new Node(129, lineno);
         Node ifNotTarget = Node.newTarget();
         Jump IFNE = new Jump(7, cond);
         IFNE.target = ifNotTarget;
         result.addChildToBack(IFNE);
         result.addChildrenToBack(ifTrue);
         if (ifFalse != null) {
            Node endTarget = Node.newTarget();
            result.addChildToBack(this.makeJump(5, endTarget));
            result.addChildToBack(ifNotTarget);
            result.addChildrenToBack(ifFalse);
            result.addChildToBack(endTarget);
         } else {
            result.addChildToBack(ifNotTarget);
         }

         return result;
      }
   }

   private Node createCondExpr(Node cond, Node ifTrue, Node ifFalse) {
      int condStatus = isAlwaysDefinedBoolean(cond);
      if (condStatus == 1) {
         return ifTrue;
      } else {
         return condStatus == -1 ? ifFalse : new Node(102, cond, ifTrue, ifFalse);
      }
   }

   private Node createUnary(int nodeType, Node child) {
      int childType = child.getType();
      switch (nodeType) {
         case 26:
            int status = isAlwaysDefinedBoolean(child);
            if (status != 0) {
               int type;
               if (status == 1) {
                  type = 44;
               } else {
                  type = 45;
               }

               if (childType != 45 && childType != 44) {
                  return new Node(type);
               }

               child.setType(type);
               return child;
            }
            break;
         case 27:
            if (childType == 40) {
               int value = ScriptRuntime.toInt32(child.getDouble());
               child.setDouble((double)(~value));
               return child;
            }
         case 28:
         case 30:
         default:
            break;
         case 29:
            if (childType == 40) {
               child.setDouble(-child.getDouble());
               return child;
            }
            break;
         case 31:
            Node n;
            if (childType == 39) {
               child.setType(49);
               Node right = Node.newString(child.getString());
               n = new Node(nodeType, child, right);
            } else if (childType != 33 && childType != 36) {
               if (childType == 67) {
                  Node ref = child.getFirstChild();
                  child.removeChild(ref);
                  n = new Node(69, ref);
               } else {
                  n = new Node(nodeType, new Node(45), child);
               }
            } else {
               Node left = child.getFirstChild();
               Node right = child.getLastChild();
               child.removeChild(left);
               child.removeChild(right);
               n = new Node(nodeType, left, right);
            }

            return n;
         case 32:
            if (childType == 39) {
               child.setType(137);
               return child;
            }
      }

      return new Node(nodeType, child);
   }

   private Node createCallOrNew(int nodeType, Node child) {
      int type = 0;
      if (child.getType() == 39) {
         String name = child.getString();
         if (name.equals("eval")) {
            type = 1;
         } else if (name.equals("With")) {
            type = 2;
         }
      } else if (child.getType() == 33) {
         String name = child.getLastChild().getString();
         if (name.equals("eval")) {
            type = 1;
         }
      }

      Node node = new Node(nodeType, child);
      if (type != 0) {
         this.setRequiresActivation();
         node.putIntProp(10, type);
      }

      return node;
   }

   private Node createIncDec(int nodeType, boolean post, Node child) {
      child = this.makeReference(child);
      int childType = child.getType();
      switch (childType) {
         case 33:
         case 36:
         case 39:
         case 67:
            Node n = new Node(nodeType, child);
            int incrDecrMask = 0;
            if (nodeType == 107) {
               incrDecrMask |= 1;
            }

            if (post) {
               incrDecrMask |= 2;
            }

            n.putIntProp(13, incrDecrMask);
            return n;
         default:
            throw Kit.codeBug();
      }
   }

   private Node createPropertyGet(Node target, String namespace, String name, int memberTypeFlags) {
      if (namespace == null && memberTypeFlags == 0) {
         if (target == null) {
            return this.createName(name);
         } else {
            this.checkActivationName(name, 33);
            if (ScriptRuntime.isSpecialProperty(name)) {
               Node ref = new Node(71, target);
               ref.putProp(17, name);
               return new Node(67, ref);
            } else {
               return new Node(33, target, Node.newString(name));
            }
         }
      } else {
         Node elem = Node.newString(name);
         memberTypeFlags |= 1;
         return this.createMemberRefGet(target, namespace, elem, memberTypeFlags);
      }
   }

   private Node createElementGet(Node target, String namespace, Node elem, int memberTypeFlags) {
      if (namespace == null && memberTypeFlags == 0) {
         if (target == null) {
            throw Kit.codeBug();
         } else {
            return new Node(36, target, elem);
         }
      } else {
         return this.createMemberRefGet(target, namespace, elem, memberTypeFlags);
      }
   }

   private Node createMemberRefGet(Node target, String namespace, Node elem, int memberTypeFlags) {
      Node nsNode = null;
      if (namespace != null) {
         if (namespace.equals("*")) {
            nsNode = new Node(42);
         } else {
            nsNode = this.createName(namespace);
         }
      }

      Node ref;
      if (target == null) {
         if (namespace == null) {
            ref = new Node(79, elem);
         } else {
            ref = new Node(80, nsNode, elem);
         }
      } else if (namespace == null) {
         ref = new Node(77, target, elem);
      } else {
         ref = new Node(78, target, nsNode, elem);
      }

      if (memberTypeFlags != 0) {
         ref.putIntProp(16, memberTypeFlags);
      }

      return new Node(67, ref);
   }

   private Node createBinary(int nodeType, Node left, Node right) {
      String s2;
      label83: {
         switch (nodeType) {
            case 21:
               if (left.type == 41) {
                  if (right.type == 41) {
                     s2 = right.getString();
                     break label83;
                  }

                  if (right.type == 40) {
                     s2 = ScriptRuntime.numberToString(right.getDouble(), 10);
                     break label83;
                  }
               } else if (left.type == 40) {
                  if (right.type == 40) {
                     left.setDouble(left.getDouble() + right.getDouble());
                     return left;
                  }

                  if (right.type == 41) {
                     s2 = ScriptRuntime.numberToString(left.getDouble(), 10);
                     String s2 = right.getString();
                     right.setString(s2.concat(s2));
                     return right;
                  }
               }
               break;
            case 22:
               if (left.type == 40) {
                  double ld = left.getDouble();
                  if (right.type == 40) {
                     left.setDouble(ld - right.getDouble());
                     return left;
                  }

                  if (ld == (double)0.0F) {
                     return new Node(29, right);
                  }
               } else if (right.type == 40 && right.getDouble() == (double)0.0F) {
                  return new Node(28, left);
               }
               break;
            case 23:
               if (left.type == 40) {
                  double ld = left.getDouble();
                  if (right.type == 40) {
                     left.setDouble(ld * right.getDouble());
                     return left;
                  }

                  if (ld == (double)1.0F) {
                     return new Node(28, right);
                  }
               } else if (right.type == 40 && right.getDouble() == (double)1.0F) {
                  return new Node(28, left);
               }
               break;
            case 24:
               if (right.type == 40) {
                  double rd = right.getDouble();
                  if (left.type == 40) {
                     left.setDouble(left.getDouble() / rd);
                     return left;
                  }

                  if (rd == (double)1.0F) {
                     return new Node(28, left);
                  }
               }
               break;
            case 104:
               int leftStatus = isAlwaysDefinedBoolean(left);
               if (leftStatus == 1) {
                  return left;
               }

               if (leftStatus == -1) {
                  return right;
               }
               break;
            case 105:
               int leftStatus = isAlwaysDefinedBoolean(left);
               if (leftStatus == -1) {
                  return left;
               }

               if (leftStatus == 1) {
                  return right;
               }
         }

         return new Node(nodeType, left, right);
      }

      String s1 = left.getString();
      left.setString(s1.concat(s2));
      return left;
   }

   private Node createAssignment(int assignType, Node left, Node right) {
      Node ref = this.makeReference(left);
      if (ref == null) {
         if (left.getType() != 65 && left.getType() != 66) {
            this.reportError("msg.bad.assign.left");
            return right;
         } else if (assignType != 90) {
            this.reportError("msg.bad.destruct.op");
            return right;
         } else {
            return this.createDestructuringAssignment(-1, left, right);
         }
      } else {
         int assignOp;
         switch (assignType) {
            case 90:
               return this.simpleAssignment(ref, right);
            case 91:
               assignOp = 9;
               break;
            case 92:
               assignOp = 10;
               break;
            case 93:
               assignOp = 11;
               break;
            case 94:
               assignOp = 18;
               break;
            case 95:
               assignOp = 19;
               break;
            case 96:
               assignOp = 20;
               break;
            case 97:
               assignOp = 21;
               break;
            case 98:
               assignOp = 22;
               break;
            case 99:
               assignOp = 23;
               break;
            case 100:
               assignOp = 24;
               break;
            case 101:
               assignOp = 25;
               break;
            default:
               throw Kit.codeBug();
         }

         int nodeType = ref.getType();
         switch (nodeType) {
            case 33:
            case 36:
               Node obj = ref.getFirstChild();
               Node id = ref.getLastChild();
               int type = nodeType == 33 ? 139 : 140;
               Node opLeft = new Node(138);
               Node op = new Node(assignOp, opLeft, right);
               return new Node(type, obj, id, op);
            case 39:
               Node op = new Node(assignOp, ref, right);
               Node lvalueLeft = Node.newString(49, ref.getString());
               return new Node(8, lvalueLeft, op);
            case 67:
               ref = ref.getFirstChild();
               this.checkMutableReference(ref);
               Node opLeft = new Node(138);
               Node op = new Node(assignOp, opLeft, right);
               return new Node(142, ref, op);
            default:
               throw Kit.codeBug();
         }
      }
   }

   private Node createUseLocal(Node localBlock) {
      if (141 != localBlock.getType()) {
         throw Kit.codeBug();
      } else {
         Node result = new Node(54);
         result.putProp(3, localBlock);
         return result;
      }
   }

   private Jump makeJump(int type, Node target) {
      Jump n = new Jump(type);
      n.target = target;
      return n;
   }

   private Node makeReference(Node node) {
      int type = node.getType();
      switch (type) {
         case 33:
         case 36:
         case 39:
         case 67:
            return node;
         case 38:
            node.setType(70);
            return new Node(67, node);
         default:
            return null;
      }
   }

   private static int isAlwaysDefinedBoolean(Node node) {
      switch (node.getType()) {
         case 40:
            double num = node.getDouble();
            if (num == num && num != (double)0.0F) {
               return 1;
            }

            return -1;
         case 41:
         case 43:
         default:
            return 0;
         case 42:
         case 44:
            return -1;
         case 45:
            return 1;
      }
   }

   boolean isDestructuring(Node n) {
      return n instanceof DestructuringForm && ((DestructuringForm)n).isDestructuring();
   }

   Node decompileFunctionHeader(FunctionNode fn) {
      Node mexpr = null;
      if (fn.getFunctionName() != null) {
         this.decompiler.addName(fn.getName());
      } else if (fn.getMemberExprNode() != null) {
         mexpr = this.transform(fn.getMemberExprNode());
      }

      this.decompiler.addToken(87);
      List<AstNode> params = fn.getParams();

      for(int i = 0; i < params.size(); ++i) {
         this.decompile((AstNode)params.get(i));
         if (i < params.size() - 1) {
            this.decompiler.addToken(89);
         }
      }

      this.decompiler.addToken(88);
      if (!fn.isExpressionClosure()) {
         this.decompiler.addEOL(85);
      }

      return mexpr;
   }

   void decompile(AstNode node) {
      switch (node.getType()) {
         case 33:
            this.decompilePropertyGet((PropertyGet)node);
            break;
         case 36:
            this.decompileElementGet((ElementGet)node);
            break;
         case 39:
            this.decompiler.addName(((Name)node).getIdentifier());
            break;
         case 40:
            this.decompiler.addNumber(((NumberLiteral)node).getNumber());
            break;
         case 41:
            this.decompiler.addString(((StringLiteral)node).getValue());
            break;
         case 43:
            this.decompiler.addToken(node.getType());
            break;
         case 65:
            this.decompileArrayLiteral((ArrayLiteral)node);
            break;
         case 66:
            this.decompileObjectLiteral((ObjectLiteral)node);
         case 128:
            break;
         default:
            Kit.codeBug("unexpected token: " + Token.typeToName(node.getType()));
      }

   }

   void decompileArrayLiteral(ArrayLiteral node) {
      this.decompiler.addToken(83);
      List<AstNode> elems = node.getElements();
      int size = elems.size();

      for(int i = 0; i < size; ++i) {
         AstNode elem = (AstNode)elems.get(i);
         this.decompile(elem);
         if (i < size - 1) {
            this.decompiler.addToken(89);
         }
      }

      this.decompiler.addToken(84);
   }

   void decompileObjectLiteral(ObjectLiteral node) {
      this.decompiler.addToken(85);
      List<ObjectProperty> props = node.getElements();
      int size = props.size();

      for(int i = 0; i < size; ++i) {
         ObjectProperty prop = (ObjectProperty)props.get(i);
         boolean destructuringShorthand = Boolean.TRUE.equals(prop.getProp(26));
         this.decompile(prop.getLeft());
         if (!destructuringShorthand) {
            this.decompiler.addToken(103);
            this.decompile(prop.getRight());
         }

         if (i < size - 1) {
            this.decompiler.addToken(89);
         }
      }

      this.decompiler.addToken(86);
   }

   void decompilePropertyGet(PropertyGet node) {
      this.decompile(node.getTarget());
      this.decompiler.addToken(108);
      this.decompile(node.getProperty());
   }

   void decompileElementGet(ElementGet node) {
      this.decompile(node.getTarget());
      this.decompiler.addToken(83);
      this.decompile(node.getElement());
      this.decompiler.addToken(84);
   }
}
