package org.mozilla.javascript.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.mozilla.javascript.Node;

public class Scope extends Jump {
   protected Map symbolTable;
   protected Scope parentScope;
   protected ScriptNode top;
   private List childScopes;

   public Scope() {
      super();
      this.type = 129;
   }

   public Scope(int pos) {
      super();
      this.type = 129;
      this.position = pos;
   }

   public Scope(int pos, int len) {
      this(pos);
      this.length = len;
   }

   public Scope getParentScope() {
      return this.parentScope;
   }

   public void setParentScope(Scope parentScope) {
      this.parentScope = parentScope;
      this.top = parentScope == null ? (ScriptNode)this : parentScope.top;
   }

   public void clearParentScope() {
      this.parentScope = null;
   }

   public List getChildScopes() {
      return this.childScopes;
   }

   public void addChildScope(Scope child) {
      if (this.childScopes == null) {
         this.childScopes = new ArrayList();
      }

      this.childScopes.add(child);
      child.setParentScope(this);
   }

   public void replaceWith(Scope newScope) {
      if (this.childScopes != null) {
         for(Scope kid : this.childScopes) {
            newScope.addChildScope(kid);
         }

         this.childScopes.clear();
         this.childScopes = null;
      }

      if (this.symbolTable != null && !this.symbolTable.isEmpty()) {
         joinScopes(this, newScope);
      }

   }

   public ScriptNode getTop() {
      return this.top;
   }

   public void setTop(ScriptNode top) {
      this.top = top;
   }

   public static Scope splitScope(Scope scope) {
      Scope result = new Scope(scope.getType());
      result.symbolTable = scope.symbolTable;
      scope.symbolTable = null;
      result.parent = scope.parent;
      result.setParentScope(scope.getParentScope());
      result.setParentScope(result);
      scope.parent = result;
      result.top = scope.top;
      return result;
   }

   public static void joinScopes(Scope source, Scope dest) {
      Map<String, Symbol> src = source.ensureSymbolTable();
      Map<String, Symbol> dst = dest.ensureSymbolTable();
      if (!Collections.disjoint(src.keySet(), dst.keySet())) {
         codeBug();
      }

      for(Map.Entry entry : src.entrySet()) {
         Symbol sym = (Symbol)entry.getValue();
         sym.setContainingTable(dest);
         dst.put(entry.getKey(), sym);
      }

   }

   public Scope getDefiningScope(String name) {
      for(Scope s = this; s != null; s = s.parentScope) {
         Map<String, Symbol> symbolTable = s.getSymbolTable();
         if (symbolTable != null && symbolTable.containsKey(name)) {
            return s;
         }
      }

      return null;
   }

   public Symbol getSymbol(String name) {
      return this.symbolTable == null ? null : (Symbol)this.symbolTable.get(name);
   }

   public void putSymbol(Symbol symbol) {
      if (symbol.getName() == null) {
         throw new IllegalArgumentException("null symbol name");
      } else {
         this.ensureSymbolTable();
         this.symbolTable.put(symbol.getName(), symbol);
         symbol.setContainingTable(this);
         this.top.addSymbol(symbol);
      }
   }

   public Map getSymbolTable() {
      return this.symbolTable;
   }

   public void setSymbolTable(Map table) {
      this.symbolTable = table;
   }

   private Map ensureSymbolTable() {
      if (this.symbolTable == null) {
         this.symbolTable = new LinkedHashMap(5);
      }

      return this.symbolTable;
   }

   public List getStatements() {
      List<AstNode> stmts = new ArrayList();

      for(Node n = this.getFirstChild(); n != null; n = n.getNext()) {
         stmts.add((AstNode)n);
      }

      return stmts;
   }

   public String toSource(int depth) {
      StringBuilder sb = new StringBuilder();
      sb.append(this.makeIndent(depth));
      sb.append("{\n");

      for(Node kid : this) {
         sb.append(((AstNode)kid).toSource(depth + 1));
      }

      sb.append(this.makeIndent(depth));
      sb.append("}\n");
      return sb.toString();
   }

   public void visit(NodeVisitor v) {
      if (v.visit(this)) {
         for(Node kid : this) {
            ((AstNode)kid).visit(v);
         }
      }

   }
}
