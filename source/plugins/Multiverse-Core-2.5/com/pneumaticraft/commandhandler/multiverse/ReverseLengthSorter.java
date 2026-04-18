package com.pneumaticraft.commandhandler.multiverse;

import java.util.Comparator;

public class ReverseLengthSorter implements Comparator {
   public ReverseLengthSorter() {
      super();
   }

   public int compare(CommandKey cmdA, CommandKey cmdB) {
      if (cmdA.getKey().length() > cmdB.getKey().length()) {
         return -1;
      } else {
         return cmdA.getKey().length() < cmdB.getKey().length() ? 1 : 0;
      }
   }
}
