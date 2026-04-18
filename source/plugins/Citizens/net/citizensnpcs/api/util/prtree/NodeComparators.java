package net.citizensnpcs.api.util.prtree;

import java.util.Comparator;

interface NodeComparators {
   Comparator getMaxComparator(int var1);

   Comparator getMinComparator(int var1);
}
