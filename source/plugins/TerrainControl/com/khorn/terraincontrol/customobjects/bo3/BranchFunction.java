package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.customobjects.Branch;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.CustomObjectCoordinate;
import com.khorn.terraincontrol.customobjects.Rotation;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BranchFunction extends BO3Function implements Branch {
   public int x;
   public int y;
   public int z;
   public List branches;
   public List branchChances;
   public List branchRotations;

   public BranchFunction() {
      super();
   }

   public BranchFunction rotate() {
      BranchFunction rotatedBranch = new BranchFunction();
      rotatedBranch.x = this.z;
      rotatedBranch.y = this.y;
      rotatedBranch.z = -this.x;
      rotatedBranch.branches = this.branches;
      rotatedBranch.branchChances = this.branchChances;
      rotatedBranch.branchRotations = new ArrayList();

      for(Rotation rotation : this.branchRotations) {
         rotatedBranch.branchRotations.add(Rotation.next(rotation));
      }

      return rotatedBranch;
   }

   public void load(List args) throws InvalidConfigException {
      this.assureSize(6, args);
      this.x = this.readInt((String)args.get(0), -32, 32);
      this.y = this.readInt((String)args.get(1), -64, 64);
      this.z = this.readInt((String)args.get(2), -32, 32);
      this.branches = new ArrayList();
      this.branchRotations = new ArrayList();
      this.branchChances = new ArrayList();

      for(int i = 3; i < args.size() - 2; i += 3) {
         CustomObject object = (CustomObject)((BO3Config)this.getHolder()).otherObjectsInDirectory.get(((String)args.get(i)).toLowerCase());
         if (object == null) {
            throw new InvalidConfigException("The branch " + (String)args.get(i) + " was not found. Make sure to place it in the same directory.");
         }

         this.branches.add(object);
         this.branchRotations.add(Rotation.getRotation((String)args.get(i + 1)));
         this.branchChances.add(this.readInt((String)args.get(i + 2), 1, 100));
      }

   }

   public String makeString() {
      String output = "Branch(" + this.x + "," + this.y + "," + this.z;

      for(int i = 0; i < this.branches.size(); ++i) {
         output = output + "," + ((CustomObject)this.branches.get(i)).getName() + "," + this.branchRotations.get(i) + "," + this.branchChances.get(i);
      }

      return output + ")";
   }

   public CustomObjectCoordinate toCustomObjectCoordinate(LocalWorld world, Random random, int x, int y, int z) {
      for(int branchNumber = 0; branchNumber < this.branches.size(); ++branchNumber) {
         if (random.nextInt(100) < (Integer)this.branchChances.get(branchNumber)) {
            return new CustomObjectCoordinate((CustomObject)this.branches.get(branchNumber), (Rotation)this.branchRotations.get(branchNumber), x + this.x, y + this.y, z + this.z);
         }
      }

      return null;
   }
}
