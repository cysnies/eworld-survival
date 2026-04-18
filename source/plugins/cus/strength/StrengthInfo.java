package strength;

import lib.hashList.HashList;

public class StrengthInfo {
   private String type;
   private String inherit;
   private HashList data;
   private DataInfo dataInfo;

   public StrengthInfo(String type, String inherit, HashList data, DataInfo dataInfo) {
      super();
      this.type = type;
      this.inherit = inherit;
      this.data = data;
      this.dataInfo = dataInfo;
   }

   public String getType() {
      return this.type;
   }

   public Object getData(String name) {
      try {
         if (this.data.has(name)) {
            return this.dataInfo.getData(name);
         }

         if (this.inherit != null) {
            return StrengthManager.getStrengthInfo(this.inherit).getData(name);
         }
      } catch (Exception var3) {
      }

      return null;
   }

   public Object getDataExact(String name) {
      return this.data.has(name) ? this.dataInfo.getData(name) : null;
   }

   public void setData(String name, Object obj) {
      this.data.add(name);
      this.dataInfo.setData(name, obj);
   }
}
