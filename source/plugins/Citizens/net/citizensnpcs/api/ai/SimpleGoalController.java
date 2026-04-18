package net.citizensnpcs.api.ai;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

public class SimpleGoalController implements GoalController {
   private final List executingGoals = Lists.newArrayList();
   private int executingPriority = -1;
   private Goal executingRootGoal;
   private boolean hasPrioritisableGoal;
   private volatile boolean paused;
   private final List possibleGoals = Lists.newArrayList();
   private final GoalSelector selector = new SimpleGoalSelector();

   public SimpleGoalController() {
      super();
   }

   public void addGoal(Goal goal, int priority) {
      Preconditions.checkNotNull(goal, "goal cannot be null");
      Preconditions.checkState(priority > 0 && priority < Integer.MAX_VALUE, "priority must be greater than 0");
      SimpleGoalEntry entry = new SimpleGoalEntry(goal, priority);
      if (!this.possibleGoals.contains(entry)) {
         this.possibleGoals.add(entry);
         Collections.sort(this.possibleGoals);
      }
   }

   private void addGoalToExecution(Goal goal) {
      if (CitizensAPI.hasImplementation()) {
         Bukkit.getPluginManager().registerEvents(goal, CitizensAPI.getPlugin());
      }

      this.executingGoals.add(goal);
      goal.run(this.selector);
   }

   public void addPrioritisableGoal(final PrioritisableGoal goal) {
      Preconditions.checkNotNull(goal, "goal cannot be null");
      this.possibleGoals.add(new GoalController.GoalEntry() {
         public int compareTo(GoalController.GoalEntry o) {
            int priority = this.getPriority();
            return o.getPriority() > priority ? 1 : (o.getPriority() < priority ? -1 : 0);
         }

         public Goal getGoal() {
            return goal;
         }

         public int getPriority() {
            return goal.getPriority();
         }
      });
      this.hasPrioritisableGoal = true;
   }

   public void cancelCurrentExecution() {
      this.finishCurrentGoalExecution();
   }

   public void clear() {
      this.finishCurrentGoalExecution();
      this.possibleGoals.clear();
   }

   private void finishCurrentGoalExecution() {
      if (this.executingRootGoal != null) {
         this.resetGoalList();
         this.executingPriority = -1;
         HandlerList.unregisterAll(this.executingRootGoal);
         this.executingRootGoal = null;
      }
   }

   public boolean isExecutingGoal() {
      return this.executingRootGoal != null;
   }

   public boolean isPaused() {
      return this.paused;
   }

   public Iterator iterator() {
      final Iterator<GoalController.GoalEntry> itr = this.possibleGoals.iterator();
      return new Iterator() {
         GoalController.GoalEntry cur;

         public boolean hasNext() {
            return itr.hasNext();
         }

         public GoalController.GoalEntry next() {
            return this.cur = (GoalController.GoalEntry)itr.next();
         }

         public void remove() {
            itr.remove();
            if (this.cur.getGoal() == SimpleGoalController.this.executingRootGoal) {
               SimpleGoalController.this.finishCurrentGoalExecution();
            }

         }
      };
   }

   public void removeGoal(Goal goal) {
      Preconditions.checkNotNull(goal, "goal cannot be null");

      for(int j = 0; j < this.possibleGoals.size(); ++j) {
         Goal test = ((GoalController.GoalEntry)this.possibleGoals.get(j)).getGoal();
         if (test.equals(goal)) {
            this.possibleGoals.remove(j--);
            if (test == this.executingRootGoal) {
               this.finishCurrentGoalExecution();
            }
         }
      }

      if (goal instanceof PrioritisableGoal) {
         boolean foundOther = false;

         for(GoalController.GoalEntry test : this.possibleGoals) {
            if (test.getGoal() instanceof PrioritisableGoal) {
               foundOther = true;
               break;
            }
         }

         if (!foundOther) {
            this.hasPrioritisableGoal = false;
         }
      }

   }

   private void resetGoalList() {
      for(int i = 0; i < this.executingGoals.size(); ++i) {
         Goal goal = (Goal)this.executingGoals.remove(i--);
         goal.reset();
         HandlerList.unregisterAll(goal);
      }

   }

   public void run() {
      if (!this.possibleGoals.isEmpty() && !this.paused) {
         this.trySelectGoal();

         for(int i = 0; i < this.executingGoals.size(); ++i) {
            Goal goal = (Goal)this.executingGoals.get(i);
            goal.run(this.selector);
         }

      }
   }

   public void setPaused(boolean paused) {
      this.paused = paused;
   }

   private void setupExecution(GoalController.GoalEntry entry) {
      this.finishCurrentGoalExecution();
      this.executingPriority = entry.getPriority();
      this.executingRootGoal = entry.getGoal();
      this.addGoalToExecution(entry.getGoal());
   }

   private void trySelectGoal() {
      int searchPriority = Math.max(this.executingPriority, 1);
      if (this.hasPrioritisableGoal) {
         Collections.sort(this.possibleGoals);
      }

      for(int i = this.possibleGoals.size() - 1; i >= 0; --i) {
         GoalController.GoalEntry entry = (GoalController.GoalEntry)this.possibleGoals.get(i);
         if (searchPriority > entry.getPriority()) {
            return;
         }

         if (entry.getGoal() != this.executingRootGoal && entry.getGoal().shouldExecute(this.selector)) {
            if (i == 0) {
               this.setupExecution(entry);
               return;
            }

            for(int j = i - 1; j >= 0; --j) {
               GoalController.GoalEntry next = (GoalController.GoalEntry)this.possibleGoals.get(j);
               boolean unequalPriorities = next.getPriority() != entry.getPriority();
               if (unequalPriorities || j == 0) {
                  if (unequalPriorities) {
                     ++j;
                  }

                  int ran = (int)Math.floor(Math.random() * (double)(i - j + 1) + (double)j);
                  if (ran < this.possibleGoals.size() && ran >= 0) {
                     GoalController.GoalEntry selected = (GoalController.GoalEntry)this.possibleGoals.get(ran);
                     if (selected.getPriority() != entry.getPriority()) {
                        this.setupExecution(entry);
                     } else {
                        this.setupExecution(selected);
                     }
                     break;
                  }

                  this.setupExecution(entry);
                  break;
               }
            }

            return;
         }
      }

   }

   public class SimpleGoalSelector implements GoalSelector {
      public SimpleGoalSelector() {
         super();
      }

      public void finish() {
         SimpleGoalController.this.finishCurrentGoalExecution();
      }

      public void finishAndRemove() {
         Goal toRemove = SimpleGoalController.this.executingRootGoal;
         this.finish();
         if (toRemove != null) {
            SimpleGoalController.this.removeGoal(toRemove);
         }

      }

      public void select(Goal goal) {
         SimpleGoalController.this.resetGoalList();
         SimpleGoalController.this.addGoalToExecution(goal);
      }

      public void selectAdditional(Goal... goals) {
         for(Goal goal : goals) {
            SimpleGoalController.this.addGoalToExecution(goal);
         }

      }
   }
}
