package net.citizensnpcs.api.util.cuboid;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class BookmarkedResult implements Iterable {
   final QuadNode bookmark;
   private final List results;
   public static final BookmarkedResult EMPTY;

   BookmarkedResult(QuadNode node, List cuboids) {
      super();
      this.bookmark = node;
      this.results = Collections.unmodifiableList(cuboids);
   }

   public Collection getResults() {
      return this.results;
   }

   public Iterator iterator() {
      return this.results.iterator();
   }

   static {
      EMPTY = new BookmarkedResult((QuadNode)null, Collections.EMPTY_LIST);
   }
}
