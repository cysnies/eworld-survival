package org.mozilla.javascript.ast;

public class Name extends AstNode {
   private String identifier;
   private Scope scope;

   public Name() {
      super();
      this.type = 39;
   }

   public Name(int pos) {
      super(pos);
      this.type = 39;
   }

   public Name(int pos, int len) {
      super(pos, len);
      this.type = 39;
   }

   public Name(int pos, int len, String name) {
      super(pos, len);
      this.type = 39;
      this.setIdentifier(name);
   }

   public Name(int pos, String name) {
      super(pos);
      this.type = 39;
      this.setIdentifier(name);
      this.setLength(name.length());
   }

   public String getIdentifier() {
      return this.identifier;
   }

   public void setIdentifier(String identifier) {
      this.assertNotNull(identifier);
      this.identifier = identifier;
      this.setLength(identifier.length());
   }

   public void setScope(Scope s) {
      this.scope = s;
   }

   public Scope getScope() {
      return this.scope;
   }

   public Scope getDefiningScope() {
      Scope enclosing = this.getEnclosingScope();
      String name = this.getIdentifier();
      return enclosing == null ? null : enclosing.getDefiningScope(name);
   }

   public boolean isLocalName() {
      Scope scope = this.getDefiningScope();
      return scope != null && scope.getParentScope() != null;
   }

   public int length() {
      return this.identifier == null ? 0 : this.identifier.length();
   }

   public String toSource(int depth) {
      return this.makeIndent(depth) + (this.identifier == null ? "<null>" : this.identifier);
   }

   public void visit(NodeVisitor v) {
      v.visit(this);
   }
}
