package fr.neatmonster.nocheatplus.components.order;

import java.util.Comparator;

public class Order {
   public static Comparator cmpSetupOrder = new Comparator() {
      public int compare(Object obj1, Object obj2) {
         int prio1 = 0;
         int prio2 = 0;
         SetupOrder order1 = (SetupOrder)obj1.getClass().getAnnotation(SetupOrder.class);
         if (order1 != null) {
            prio1 = order1.priority();
         }

         SetupOrder order2 = (SetupOrder)obj2.getClass().getAnnotation(SetupOrder.class);
         if (order2 != null) {
            prio2 = order2.priority();
         }

         if (prio1 < prio2) {
            return -1;
         } else {
            return prio1 == prio2 ? 0 : 1;
         }
      }
   };

   public Order() {
      super();
   }
}
