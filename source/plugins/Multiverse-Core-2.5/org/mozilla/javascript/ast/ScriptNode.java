package org.mozilla.javascript.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.mozilla.javascript.Node;

public class ScriptNode extends Scope {
   private int encodedSourceStart = -1;
   private int encodedSourceEnd = -1;
   private String sourceName;
   private String encodedSource;
   private int endLineno = -1;
   private List functions;
   private List regexps;
   private List EMPTY_LIST = Collections.emptyList();
   private List symbols = new ArrayList(4);
   private int paramCount = 0;
   private String[] variableNames;
   private boolean[] isConsts;
   private Object compilerData;
   private int tempNumber = 0;

   public ScriptNode() {
      super();
      this.top = this;
      this.type = 136;
   }

   public ScriptNode(int pos) {
      super(pos);
      this.top = this;
      this.type = 136;
   }

   public String getSourceName() {
      return this.sourceName;
   }

   public void setSourceName(String sourceName) {
      this.sourceName = sourceName;
   }

   public int getEncodedSourceStart() {
      return this.encodedSourceStart;
   }

   public void setEncodedSourceStart(int start) {
      this.encodedSourceStart = start;
   }

   public int getEncodedSourceEnd() {
      return this.encodedSourceEnd;
   }

   public void setEncodedSourceEnd(int end) {
      this.encodedSourceEnd = end;
   }

   public void setEncodedSourceBounds(int start, int end) {
      this.encodedSourceStart = start;
      this.encodedSourceEnd = end;
   }

   public void setEncodedSource(String encodedSource) {
      this.encodedSource = encodedSource;
   }

   public String getEncodedSource() {
      return this.encodedSource;
   }

   public int getBaseLineno() {
      return this.lineno;
   }

   public void setBaseLineno(int lineno) {
      if (lineno < 0 || this.lineno >= 0) {
         codeBug();
      }

      this.lineno = lineno;
   }

   public int getEndLineno() {
      return this.endLineno;
   }

   public void setEndLineno(int lineno) {
      if (lineno < 0 || this.endLineno >= 0) {
         codeBug();
      }

      this.endLineno = lineno;
   }

   public int getFunctionCount() {
      return this.functions == null ? 0 : this.functions.size();
   }

   public FunctionNode getFunctionNode(int i) {
      return (FunctionNode)this.functions.get(i);
   }

   public List getFunctions() {
      return this.functions == null ? this.EMPTY_LIST : this.functions;
   }

   public int addFunction(FunctionNode fnNode) {
      if (fnNode == null) {
         codeBug();
      }

      if (this.functions == null) {
         this.functions = new ArrayList();
      }

      this.functions.add(fnNode);
      return this.functions.size() - 1;
   }

   public int getRegexpCount() {
      return this.regexps == null ? 0 : this.regexps.size();
   }

   public String getRegexpString(int index) {
      return ((RegExpLiteral)this.regexps.get(index)).getValue();
   }

   public String getRegexpFlags(int index) {
      return ((RegExpLiteral)this.regexps.get(index)).getFlags();
   }

   public void addRegExp(RegExpLiteral re) {
      if (re == null) {
         codeBug();
      }

      if (this.regexps == null) {
         this.regexps = new ArrayList();
      }

      this.regexps.add(re);
      re.putIntProp(4, this.regexps.size() - 1);
   }

   public int getIndexForNameNode(Node nameNode) {
      if (this.variableNames == null) {
         codeBug();
      }

      Scope node = nameNode.getScope();
      Symbol symbol = node == null ? null : node.getSymbol(((Name)nameNode).getIdentifier());
      return symbol == null ? -1 : symbol.getIndex();
   }

   public String getParamOrVarName(int index) {
      if (this.variableNames == null) {
         codeBug();
      }

      return this.variableNames[index];
   }

   public int getParamCount() {
      return this.paramCount;
   }

   public int getParamAndVarCount() {
      if (this.variableNames == null) {
         codeBug();
      }

      return this.symbols.size();
   }

   public String[] getParamAndVarNames() {
      if (this.variableNames == null) {
         codeBug();
      }

      return this.variableNames;
   }

   public boolean[] getParamAndVarConst() {
      if (this.variableNames == null) {
         codeBug();
      }

      return this.isConsts;
   }

   void addSymbol(Symbol symbol) {
      if (this.variableNames != null) {
         codeBug();
      }

      if (symbol.getDeclType() == 87) {
         ++this.paramCount;
      }

      this.symbols.add(symbol);
   }

   public List getSymbols() {
      return this.symbols;
   }

   public void setSymbols(List symbols) {
      this.symbols = symbols;
   }

   public void flattenSymbolTable(boolean flattenAllTables) {
      if (!flattenAllTables) {
         List<Symbol> newSymbols = new ArrayList();
         if (this.symbolTable != null) {
            for(int i = 0; i < this.symbols.size(); ++i) {
               Symbol symbol = (Symbol)this.symbols.get(i);
               if (symbol.getContainingTable() == this) {
                  newSymbols.add(symbol);
               }
            }
         }

         this.symbols = newSymbols;
      }

      this.variableNames = new String[this.symbols.size()];
      this.isConsts = new boolean[this.symbols.size()];

      for(int i = 0; i < this.symbols.size(); ++i) {
         Symbol symbol = (Symbol)this.symbols.get(i);
         this.variableNames[i] = symbol.getName();
         this.isConsts[i] = symbol.getDeclType() == 154;
         symbol.setIndex(i);
      }

   }

   public Object getCompilerData() {
      return this.compilerData;
   }

   public void setCompilerData(Object data) {
      this.assertNotNull(data);
      if (this.compilerData != null) {
         throw new IllegalStateException();
      } else {
         this.compilerData = data;
      }
   }

   public String getNextTempName() {
      return "$" + this.tempNumber++;
   }

   public void visit(NodeVisitor v) {
      if (v.visit(this)) {
         for(Node kid : this) {
            ((AstNode)kid).visit(v);
         }
      }

   }
}
