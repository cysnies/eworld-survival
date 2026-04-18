package net.citizensnpcs.api.ai;

public interface GoalController extends Runnable, Iterable {
   void addGoal(Goal var1, int var2);

   void addPrioritisableGoal(PrioritisableGoal var1);

   void cancelCurrentExecution();

   void clear();

   boolean isExecutingGoal();

   boolean isPaused();

   void removeGoal(Goal var1);

   void setPaused(boolean var1);

   public interface GoalEntry extends Comparable {
      Goal getGoal();

      int getPriority();
   }
}
