package cus;

import java.util.ArrayList;
import java.util.List;
import land.Pos;

public class MonPoint {
   private long id;
   private Pos pos;
   private String type;
   private int monType;
   private int interval;
   private int chance;
   private int max;
   private List monList = new ArrayList();

   public MonPoint() {
      super();
   }

   public MonPoint(Pos pos) {
      super();
      this.pos = pos;
   }

   public long getId() {
      return this.id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public Pos getPos() {
      return this.pos;
   }

   public void setPos(Pos pos) {
      this.pos = pos;
   }

   public String getType() {
      return this.type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public int getInterval() {
      return this.interval;
   }

   public void setInterval(int interval) {
      this.interval = interval;
   }

   public int getChance() {
      return this.chance;
   }

   public void setChance(int chance) {
      this.chance = chance;
   }

   public int getMax() {
      return this.max;
   }

   public void setMax(int max) {
      this.max = max;
   }

   public List getMonList() {
      return this.monList;
   }

   public void setMonList(List monList) {
      this.monList = monList;
   }

   public int getMonType() {
      return this.monType;
   }

   public void setMonType(int monType) {
      this.monType = monType;
   }
}
