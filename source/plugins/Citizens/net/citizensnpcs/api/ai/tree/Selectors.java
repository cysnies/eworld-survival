package net.citizensnpcs.api.ai.tree;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;

public class Selectors {
   private static final Comparator BEHAVIOR_COMPARATOR = new Comparator() {
      public int compare(Behavior o1, Behavior o2) {
         return ((Comparable)o1).compareTo(o2);
      }
   };

   private Selectors() {
      super();
   }

   public static Function prioritySelectionFunction() {
      return prioritySelectionFunction0(BEHAVIOR_COMPARATOR);
   }

   private static Function prioritySelectionFunction0(final Comparator comparator) {
      return new Function() {
         public Behavior apply(@Nullable List input) {
            Collections.sort(input, comparator);
            return (Behavior)input.get(input.size() - 1);
         }
      };
   }

   public static Selector.Builder prioritySelector(Comparator comparator, Behavior... behaviors) {
      return prioritySelector(comparator, (Collection)Arrays.asList(behaviors));
   }

   public static Selector.Builder prioritySelector(Comparator comparator, Collection behaviors) {
      Preconditions.checkArgument(behaviors.size() > 0, "must have at least one behavior for comparison");
      return Selector.selecting(behaviors).selectionFunction(prioritySelectionFunction0(comparator));
   }
}
