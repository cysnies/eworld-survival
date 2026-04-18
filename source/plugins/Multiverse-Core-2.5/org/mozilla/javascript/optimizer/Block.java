package org.mozilla.javascript.optimizer;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ObjArray;
import org.mozilla.javascript.ObjToIntMap;
import org.mozilla.javascript.ast.Jump;

class Block {
   private Block[] itsSuccessors;
   private Block[] itsPredecessors;
   private int itsStartNodeIndex;
   private int itsEndNodeIndex;
   private int itsBlockID;
   private BitSet itsLiveOnEntrySet;
   private BitSet itsLiveOnExitSet;
   private BitSet itsUseBeforeDefSet;
   private BitSet itsNotDefSet;
   static final boolean DEBUG = false;
   private static int debug_blockCount;

   Block(int startNodeIndex, int endNodeIndex) {
      super();
      this.itsStartNodeIndex = startNodeIndex;
      this.itsEndNodeIndex = endNodeIndex;
   }

   static void runFlowAnalyzes(OptFunctionNode fn, Node[] statementNodes) {
      int paramCount = fn.fnode.getParamCount();
      int varCount = fn.fnode.getParamAndVarCount();
      int[] varTypes = new int[varCount];

      for(int i = 0; i != paramCount; ++i) {
         varTypes[i] = 3;
      }

      for(int i = paramCount; i != varCount; ++i) {
         varTypes[i] = 0;
      }

      Block[] theBlocks = buildBlocks(statementNodes);
      reachingDefDataFlow(fn, statementNodes, theBlocks, varTypes);
      typeFlow(fn, statementNodes, theBlocks, varTypes);

      for(int i = paramCount; i != varCount; ++i) {
         if (varTypes[i] == 1) {
            fn.setIsNumberVar(i);
         }
      }

   }

   private static Block[] buildBlocks(Node[] statementNodes) {
      Map<Node, FatBlock> theTargetBlocks = new HashMap();
      ObjArray theBlocks = new ObjArray();
      int beginNodeIndex = 0;

      for(int i = 0; i < statementNodes.length; ++i) {
         switch (statementNodes[i].getType()) {
            case 5:
            case 6:
            case 7:
               FatBlock fb = newFatBlock(beginNodeIndex, i);
               if (statementNodes[beginNodeIndex].getType() == 131) {
                  theTargetBlocks.put(statementNodes[beginNodeIndex], fb);
               }

               theBlocks.add(fb);
               beginNodeIndex = i + 1;
               break;
            case 131:
               if (i != beginNodeIndex) {
                  FatBlock fb = newFatBlock(beginNodeIndex, i - 1);
                  if (statementNodes[beginNodeIndex].getType() == 131) {
                     theTargetBlocks.put(statementNodes[beginNodeIndex], fb);
                  }

                  theBlocks.add(fb);
                  beginNodeIndex = i;
               }
         }
      }

      if (beginNodeIndex != statementNodes.length) {
         FatBlock fb = newFatBlock(beginNodeIndex, statementNodes.length - 1);
         if (statementNodes[beginNodeIndex].getType() == 131) {
            theTargetBlocks.put(statementNodes[beginNodeIndex], fb);
         }

         theBlocks.add(fb);
      }

      for(int i = 0; i < theBlocks.size(); ++i) {
         FatBlock fb = (FatBlock)theBlocks.get(i);
         Node blockEndNode = statementNodes[fb.realBlock.itsEndNodeIndex];
         int blockEndNodeType = blockEndNode.getType();
         if (blockEndNodeType != 5 && i < theBlocks.size() - 1) {
            FatBlock fallThruTarget = (FatBlock)theBlocks.get(i + 1);
            fb.addSuccessor(fallThruTarget);
            fallThruTarget.addPredecessor(fb);
         }

         if (blockEndNodeType == 7 || blockEndNodeType == 6 || blockEndNodeType == 5) {
            Node target = ((Jump)blockEndNode).target;
            FatBlock branchTargetBlock = (FatBlock)theTargetBlocks.get(target);
            target.putProp(6, branchTargetBlock.realBlock);
            fb.addSuccessor(branchTargetBlock);
            branchTargetBlock.addPredecessor(fb);
         }
      }

      Block[] result = new Block[theBlocks.size()];

      for(int i = 0; i < theBlocks.size(); ++i) {
         FatBlock fb = (FatBlock)theBlocks.get(i);
         Block b = fb.realBlock;
         b.itsSuccessors = fb.getSuccessors();
         b.itsPredecessors = fb.getPredecessors();
         b.itsBlockID = i;
         result[i] = b;
      }

      return result;
   }

   private static FatBlock newFatBlock(int startNodeIndex, int endNodeIndex) {
      FatBlock fb = new FatBlock();
      fb.realBlock = new Block(startNodeIndex, endNodeIndex);
      return fb;
   }

   private static String toString(Block[] blockList, Node[] statementNodes) {
      return null;
   }

   private static void reachingDefDataFlow(OptFunctionNode fn, Node[] statementNodes, Block[] theBlocks, int[] varTypes) {
      for(int i = 0; i < theBlocks.length; ++i) {
         theBlocks[i].initLiveOnEntrySets(fn, statementNodes);
      }

      boolean[] visit = new boolean[theBlocks.length];
      boolean[] doneOnce = new boolean[theBlocks.length];
      int vIndex = theBlocks.length - 1;
      boolean needRescan = false;
      visit[vIndex] = true;

      while(true) {
         if (visit[vIndex] || !doneOnce[vIndex]) {
            doneOnce[vIndex] = true;
            visit[vIndex] = false;
            if (theBlocks[vIndex].doReachedUseDataFlow()) {
               Block[] pred = theBlocks[vIndex].itsPredecessors;
               if (pred != null) {
                  for(int i = 0; i < pred.length; ++i) {
                     int index = pred[i].itsBlockID;
                     visit[index] = true;
                     needRescan |= index > vIndex;
                  }
               }
            }
         }

         if (vIndex == 0) {
            if (!needRescan) {
               theBlocks[0].markAnyTypeVariables(varTypes);
               return;
            }

            vIndex = theBlocks.length - 1;
            needRescan = false;
         } else {
            --vIndex;
         }
      }
   }

   private static void typeFlow(OptFunctionNode fn, Node[] statementNodes, Block[] theBlocks, int[] varTypes) {
      boolean[] visit = new boolean[theBlocks.length];
      boolean[] doneOnce = new boolean[theBlocks.length];
      int vIndex = 0;
      boolean needRescan = false;
      visit[vIndex] = true;

      while(true) {
         if (visit[vIndex] || !doneOnce[vIndex]) {
            doneOnce[vIndex] = true;
            visit[vIndex] = false;
            if (theBlocks[vIndex].doTypeFlow(fn, statementNodes, varTypes)) {
               Block[] succ = theBlocks[vIndex].itsSuccessors;
               if (succ != null) {
                  for(int i = 0; i < succ.length; ++i) {
                     int index = succ[i].itsBlockID;
                     visit[index] = true;
                     needRescan |= index < vIndex;
                  }
               }
            }
         }

         if (vIndex == theBlocks.length - 1) {
            if (!needRescan) {
               return;
            }

            vIndex = 0;
            needRescan = false;
         } else {
            ++vIndex;
         }
      }
   }

   private static boolean assignType(int[] varTypes, int index, int type) {
      int prev = varTypes[index];
      return prev != (varTypes[index] |= type);
   }

   private void markAnyTypeVariables(int[] varTypes) {
      for(int i = 0; i != varTypes.length; ++i) {
         if (this.itsLiveOnEntrySet.get(i)) {
            assignType(varTypes, i, 3);
         }
      }

   }

   private void lookForVariableAccess(OptFunctionNode fn, Node n) {
      switch (n.getType()) {
         case 55:
            int varIndex = fn.getVarIndex(n);
            if (!this.itsNotDefSet.get(varIndex)) {
               this.itsUseBeforeDefSet.set(varIndex);
            }
            break;
         case 56:
            Node lhs = n.getFirstChild();
            Node rhs = lhs.getNext();
            this.lookForVariableAccess(fn, rhs);
            this.itsNotDefSet.set(fn.getVarIndex(n));
            break;
         case 106:
         case 107:
            Node child = n.getFirstChild();
            if (child.getType() == 55) {
               int varIndex = fn.getVarIndex(child);
               if (!this.itsNotDefSet.get(varIndex)) {
                  this.itsUseBeforeDefSet.set(varIndex);
               }

               this.itsNotDefSet.set(varIndex);
            } else {
               this.lookForVariableAccess(fn, child);
            }
            break;
         case 137:
            int varIndex = fn.fnode.getIndexForNameNode(n);
            if (varIndex > -1 && !this.itsNotDefSet.get(varIndex)) {
               this.itsUseBeforeDefSet.set(varIndex);
            }
            break;
         default:
            for(Node child = n.getFirstChild(); child != null; child = child.getNext()) {
               this.lookForVariableAccess(fn, child);
            }
      }

   }

   private void initLiveOnEntrySets(OptFunctionNode fn, Node[] statementNodes) {
      int listLength = fn.getVarCount();
      this.itsUseBeforeDefSet = new BitSet(listLength);
      this.itsNotDefSet = new BitSet(listLength);
      this.itsLiveOnEntrySet = new BitSet(listLength);
      this.itsLiveOnExitSet = new BitSet(listLength);

      for(int i = this.itsStartNodeIndex; i <= this.itsEndNodeIndex; ++i) {
         Node n = statementNodes[i];
         this.lookForVariableAccess(fn, n);
      }

      this.itsNotDefSet.flip(0, listLength);
   }

   private boolean doReachedUseDataFlow() {
      this.itsLiveOnExitSet.clear();
      if (this.itsSuccessors != null) {
         for(int i = 0; i < this.itsSuccessors.length; ++i) {
            this.itsLiveOnExitSet.or(this.itsSuccessors[i].itsLiveOnEntrySet);
         }
      }

      return this.updateEntrySet(this.itsLiveOnEntrySet, this.itsLiveOnExitSet, this.itsUseBeforeDefSet, this.itsNotDefSet);
   }

   private boolean updateEntrySet(BitSet entrySet, BitSet exitSet, BitSet useBeforeDef, BitSet notDef) {
      int card = entrySet.cardinality();
      entrySet.or(exitSet);
      entrySet.and(notDef);
      entrySet.or(useBeforeDef);
      return entrySet.cardinality() != card;
   }

   private static int findExpressionType(OptFunctionNode fn, Node n, int[] varTypes) {
      switch (n.getType()) {
         case 8:
         case 35:
         case 37:
         case 56:
         case 89:
            return findExpressionType(fn, n.getLastChild(), varTypes);
         case 9:
         case 10:
         case 11:
         case 18:
         case 19:
         case 20:
         case 22:
         case 23:
         case 24:
         case 25:
         case 27:
         case 28:
         case 29:
         case 106:
         case 107:
            return 1;
         case 12:
         case 13:
         case 14:
         case 15:
         case 16:
         case 17:
         case 26:
         case 31:
         case 44:
         case 45:
         case 46:
         case 47:
         case 52:
         case 53:
         case 69:
            return 3;
         case 21:
            Node child = n.getFirstChild();
            int lType = findExpressionType(fn, child, varTypes);
            int rType = findExpressionType(fn, child.getNext(), varTypes);
            return lType | rType;
         case 30:
         case 38:
         case 70:
            return 3;
         case 32:
         case 41:
         case 137:
            return 3;
         case 33:
         case 36:
         case 39:
         case 43:
            return 3;
         case 34:
         case 49:
         case 50:
         case 51:
         case 54:
         case 57:
         case 58:
         case 59:
         case 60:
         case 61:
         case 62:
         case 63:
         case 64:
         case 67:
         case 68:
         case 71:
         case 72:
         case 73:
         case 74:
         case 75:
         case 76:
         case 77:
         case 78:
         case 79:
         case 80:
         case 81:
         case 82:
         case 83:
         case 84:
         case 85:
         case 86:
         case 87:
         case 88:
         case 90:
         case 91:
         case 92:
         case 93:
         case 94:
         case 95:
         case 96:
         case 97:
         case 98:
         case 99:
         case 100:
         case 101:
         case 103:
         case 108:
         case 109:
         case 110:
         case 111:
         case 112:
         case 113:
         case 114:
         case 115:
         case 116:
         case 117:
         case 118:
         case 119:
         case 120:
         case 121:
         case 122:
         case 123:
         case 124:
         case 125:
         case 127:
         case 128:
         case 129:
         case 130:
         case 131:
         case 132:
         case 133:
         case 134:
         case 135:
         case 136:
         case 138:
         case 139:
         case 140:
         case 141:
         case 142:
         case 143:
         case 144:
         case 145:
         case 146:
         case 147:
         case 148:
         case 149:
         case 150:
         case 151:
         case 152:
         case 153:
         case 154:
         case 155:
         case 156:
         default:
            return 3;
         case 40:
            return 1;
         case 42:
         case 48:
         case 65:
         case 66:
         case 157:
            return 3;
         case 55:
            return varTypes[fn.getVarIndex(n)];
         case 102:
            Node ifTrue = n.getFirstChild().getNext();
            Node ifFalse = ifTrue.getNext();
            int ifTrueType = findExpressionType(fn, ifTrue, varTypes);
            int ifFalseType = findExpressionType(fn, ifFalse, varTypes);
            return ifTrueType | ifFalseType;
         case 104:
         case 105:
            Node child = n.getFirstChild();
            int lType = findExpressionType(fn, child, varTypes);
            int rType = findExpressionType(fn, child.getNext(), varTypes);
            return lType | rType;
         case 126:
            return 3;
      }
   }

   private static boolean findDefPoints(OptFunctionNode fn, Node n, int[] varTypes) {
      boolean result = false;
      Node first = n.getFirstChild();

      for(Node next = first; next != null; next = next.getNext()) {
         result |= findDefPoints(fn, next, varTypes);
      }

      switch (n.getType()) {
         case 56:
            Node rValue = first.getNext();
            int theType = findExpressionType(fn, rValue, varTypes);
            int i = fn.getVarIndex(n);
            result |= assignType(varTypes, i, theType);
            break;
         case 106:
         case 107:
            if (first.getType() == 55) {
               int i = fn.getVarIndex(first);
               result |= assignType(varTypes, i, 1);
            }
      }

      return result;
   }

   private boolean doTypeFlow(OptFunctionNode fn, Node[] statementNodes, int[] varTypes) {
      boolean changed = false;

      for(int i = this.itsStartNodeIndex; i <= this.itsEndNodeIndex; ++i) {
         Node n = statementNodes[i];
         if (n != null) {
            changed |= findDefPoints(fn, n, varTypes);
         }
      }

      return changed;
   }

   private void printLiveOnEntrySet(OptFunctionNode fn) {
   }

   private static class FatBlock {
      private ObjToIntMap successors;
      private ObjToIntMap predecessors;
      Block realBlock;

      private FatBlock() {
         super();
         this.successors = new ObjToIntMap();
         this.predecessors = new ObjToIntMap();
      }

      private static Block[] reduceToArray(ObjToIntMap map) {
         Block[] result = null;
         if (!map.isEmpty()) {
            result = new Block[map.size()];
            int i = 0;
            ObjToIntMap.Iterator iter = map.newIterator();
            iter.start();

            while(!iter.done()) {
               FatBlock fb = (FatBlock)iter.getKey();
               result[i++] = fb.realBlock;
               iter.next();
            }
         }

         return result;
      }

      void addSuccessor(FatBlock b) {
         this.successors.put(b, 0);
      }

      void addPredecessor(FatBlock b) {
         this.predecessors.put(b, 0);
      }

      Block[] getSuccessors() {
         return reduceToArray(this.successors);
      }

      Block[] getPredecessors() {
         return reduceToArray(this.predecessors);
      }
   }
}
