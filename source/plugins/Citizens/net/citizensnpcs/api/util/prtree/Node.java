package net.citizensnpcs.api.util.prtree;

import java.util.List;
import java.util.PriorityQueue;

interface Node {
   void expand(MBR var1, MBRConverter var2, List var3, List var4);

   void find(MBR var1, MBRConverter var2, List var3);

   MBR getMBR(MBRConverter var1);

   void nnExpand(DistanceCalculator var1, NodeFilter var2, List var3, int var4, PriorityQueue var5, MinDistComparator var6);

   int size();
}
