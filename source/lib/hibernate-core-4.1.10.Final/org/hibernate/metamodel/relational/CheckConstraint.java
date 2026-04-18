package org.hibernate.metamodel.relational;

public class CheckConstraint {
   private final Table table;
   private String name;
   private String condition;

   public CheckConstraint(Table table) {
      super();
      this.table = table;
   }

   public CheckConstraint(Table table, String name, String condition) {
      super();
      this.table = table;
      this.name = name;
      this.condition = condition;
   }

   public String getCondition() {
      return this.condition;
   }

   public void setCondition(String condition) {
      this.condition = condition;
   }

   public Table getTable() {
      return this.table;
   }

   public String getName() {
      return this.name;
   }
}
