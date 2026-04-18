package net.citizensnpcs.api.ai;

public interface GoalSelector {
   void finish();

   void finishAndRemove();

   void select(Goal var1);

   void selectAdditional(Goal... var1);
}
