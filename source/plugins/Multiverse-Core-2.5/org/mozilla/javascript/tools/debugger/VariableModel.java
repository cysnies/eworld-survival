package org.mozilla.javascript.tools.debugger;

import java.util.Arrays;
import java.util.Comparator;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import org.mozilla.javascript.tools.debugger.treetable.TreeTableModel;

class VariableModel implements TreeTableModel {
   private static final String[] cNames = new String[]{" Name", " Value"};
   private static final Class[] cTypes = new Class[]{TreeTableModel.class, String.class};
   private static final VariableNode[] CHILDLESS = new VariableNode[0];
   private Dim debugger;
   private VariableNode root;

   public VariableModel() {
      super();
   }

   public VariableModel(Dim debugger, Object scope) {
      super();
      this.debugger = debugger;
      this.root = new VariableNode(scope, "this");
   }

   public Object getRoot() {
      return this.debugger == null ? null : this.root;
   }

   public int getChildCount(Object nodeObj) {
      if (this.debugger == null) {
         return 0;
      } else {
         VariableNode node = (VariableNode)nodeObj;
         return this.children(node).length;
      }
   }

   public Object getChild(Object nodeObj, int i) {
      if (this.debugger == null) {
         return null;
      } else {
         VariableNode node = (VariableNode)nodeObj;
         return this.children(node)[i];
      }
   }

   public boolean isLeaf(Object nodeObj) {
      if (this.debugger == null) {
         return true;
      } else {
         VariableNode node = (VariableNode)nodeObj;
         return this.children(node).length == 0;
      }
   }

   public int getIndexOfChild(Object parentObj, Object childObj) {
      if (this.debugger == null) {
         return -1;
      } else {
         VariableNode parent = (VariableNode)parentObj;
         VariableNode child = (VariableNode)childObj;
         VariableNode[] children = this.children(parent);

         for(int i = 0; i != children.length; ++i) {
            if (children[i] == child) {
               return i;
            }
         }

         return -1;
      }
   }

   public boolean isCellEditable(Object node, int column) {
      return column == 0;
   }

   public void setValueAt(Object value, Object node, int column) {
   }

   public void addTreeModelListener(TreeModelListener l) {
   }

   public void removeTreeModelListener(TreeModelListener l) {
   }

   public void valueForPathChanged(TreePath path, Object newValue) {
   }

   public int getColumnCount() {
      return cNames.length;
   }

   public String getColumnName(int column) {
      return cNames[column];
   }

   public Class getColumnClass(int column) {
      return cTypes[column];
   }

   public Object getValueAt(Object nodeObj, int column) {
      if (this.debugger == null) {
         return null;
      } else {
         VariableNode node = (VariableNode)nodeObj;
         switch (column) {
            case 0:
               return node.toString();
            case 1:
               String result;
               try {
                  result = this.debugger.objectToString(this.getValue(node));
               } catch (RuntimeException exc) {
                  result = exc.getMessage();
               }

               StringBuffer buf = new StringBuffer();
               int len = result.length();

               for(int i = 0; i < len; ++i) {
                  char ch = result.charAt(i);
                  if (Character.isISOControl(ch)) {
                     ch = ' ';
                  }

                  buf.append(ch);
               }

               return buf.toString();
            default:
               return null;
         }
      }
   }

   private VariableNode[] children(VariableNode node) {
      if (node.children != null) {
         return node.children;
      } else {
         Object value = this.getValue(node);
         Object[] ids = this.debugger.getObjectIds(value);
         VariableNode[] children;
         if (ids != null && ids.length != 0) {
            Arrays.sort(ids, new Comparator() {
               public int compare(Object l, Object r) {
                  if (l instanceof String) {
                     return r instanceof Integer ? -1 : ((String)l).compareToIgnoreCase((String)r);
                  } else if (r instanceof String) {
                     return 1;
                  } else {
                     int lint = (Integer)l;
                     int rint = (Integer)r;
                     return lint - rint;
                  }
               }
            });
            children = new VariableNode[ids.length];

            for(int i = 0; i != ids.length; ++i) {
               children[i] = new VariableNode(value, ids[i]);
            }
         } else {
            children = CHILDLESS;
         }

         node.children = children;
         return children;
      }
   }

   public Object getValue(VariableNode node) {
      try {
         return this.debugger.getObjectProperty(node.object, node.id);
      } catch (Exception var3) {
         return "undefined";
      }
   }

   private static class VariableNode {
      private Object object;
      private Object id;
      private VariableNode[] children;

      public VariableNode(Object object, Object id) {
         super();
         this.object = object;
         this.id = id;
      }

      public String toString() {
         return this.id instanceof String ? (String)this.id : "[" + (Integer)this.id + "]";
      }
   }
}
