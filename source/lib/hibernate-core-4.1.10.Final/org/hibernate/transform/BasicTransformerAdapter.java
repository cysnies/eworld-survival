package org.hibernate.transform;

import java.util.List;

public abstract class BasicTransformerAdapter implements ResultTransformer {
   public BasicTransformerAdapter() {
      super();
   }

   public Object transformTuple(Object[] tuple, String[] aliases) {
      return tuple;
   }

   public List transformList(List list) {
      return list;
   }
}
