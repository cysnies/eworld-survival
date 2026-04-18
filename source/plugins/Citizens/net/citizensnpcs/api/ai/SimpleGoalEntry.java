package net.citizensnpcs.api.ai;

public class SimpleGoalEntry implements GoalController.GoalEntry {
   final Goal goal;
   final int priority;

   public SimpleGoalEntry(Goal goal, int priority) {
      super();
      this.goal = goal;
      this.priority = priority;
   }

   public int compareTo(GoalController.GoalEntry o) {
      return o.getPriority() > this.priority ? 1 : (o.getPriority() < this.priority ? -1 : 0);
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         SimpleGoalEntry other = (SimpleGoalEntry)obj;
         if (this.goal == null) {
            if (other.goal != null) {
               return false;
            }
         } else if (!this.goal.equals(other.goal)) {
            return false;
         }

         return this.priority == other.priority;
      } else {
         return false;
      }
   }

   public Goal getGoal() {
      return this.goal;
   }

   public int getPriority() {
      return this.priority;
   }

   public int hashCode() {
      int prime = 31;
      return 31 * (31 + (this.goal == null ? 0 : this.goal.hashCode())) + this.priority;
   }
}
