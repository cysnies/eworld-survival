package javax.mail.search;

public abstract class IntegerComparisonTerm extends ComparisonTerm {
   protected int number;
   private static final long serialVersionUID = -6963571240154302484L;

   protected IntegerComparisonTerm(int comparison, int number) {
      super();
      this.comparison = comparison;
      this.number = number;
   }

   public int getNumber() {
      return this.number;
   }

   public int getComparison() {
      return this.comparison;
   }

   protected boolean match(int i) {
      switch (this.comparison) {
         case 1:
            if (i <= this.number) {
               return true;
            }

            return false;
         case 2:
            if (i < this.number) {
               return true;
            }

            return false;
         case 3:
            if (i == this.number) {
               return true;
            }

            return false;
         case 4:
            if (i != this.number) {
               return true;
            }

            return false;
         case 5:
            if (i > this.number) {
               return true;
            }

            return false;
         case 6:
            if (i >= this.number) {
               return true;
            }

            return false;
         default:
            return false;
      }
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof IntegerComparisonTerm)) {
         return false;
      } else {
         IntegerComparisonTerm ict = (IntegerComparisonTerm)obj;
         return ict.number == this.number && super.equals(obj);
      }
   }

   public int hashCode() {
      return this.number + super.hashCode();
   }
}
