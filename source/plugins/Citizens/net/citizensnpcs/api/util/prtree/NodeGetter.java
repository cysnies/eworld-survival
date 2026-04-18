package net.citizensnpcs.api.util.prtree;

import java.util.List;

interface NodeGetter {
   Object getNextNode(int var1);

   boolean hasMoreData();

   boolean hasMoreNodes();

   List split(int var1, int var2);
}
