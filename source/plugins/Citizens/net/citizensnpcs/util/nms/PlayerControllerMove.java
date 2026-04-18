package net.citizensnpcs.util.nms;

import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_6_R2.AttributeInstance;
import net.minecraft.server.v1_6_R2.GenericAttributes;
import net.minecraft.server.v1_6_R2.MathHelper;

public class PlayerControllerMove {
   private final EntityHumanNPC a;
   private double b;
   private double c;
   private double d;
   private double e;
   private boolean f;

   public PlayerControllerMove(EntityHumanNPC entityinsentient) {
      super();
      this.a = entityinsentient;
      this.b = entityinsentient.locX;
      this.c = entityinsentient.locY;
      this.d = entityinsentient.locZ;
   }

   public boolean a() {
      return this.f;
   }

   public void a(double d0, double d1, double d2, double d3) {
      this.b = d0;
      this.c = d1;
      this.d = d2;
      this.e = d3;
      this.f = true;
   }

   private float a(float f, float f1, float f2) {
      float f3 = MathHelper.g(f1 - f);
      if (f3 > f2) {
         f3 = f2;
      }

      if (f3 < -f2) {
         f3 = -f2;
      }

      return f + f3;
   }

   public double b() {
      return this.e;
   }

   public void c() {
      this.a.bf = 0.0F;
      if (this.f) {
         this.f = false;
         int i = MathHelper.floor(this.a.boundingBox.b + (double)0.5F);
         double d0 = this.b - this.a.locX;
         double d1 = this.d - this.a.locZ;
         double d2 = this.c - (double)i;
         double d3 = d0 * d0 + d2 * d2 + d1 * d1;
         if (d3 >= (double)2.5000003E-7F) {
            float f = (float)(Math.atan2(d1, d0) * (double)180.0F / (double)(float)Math.PI) - 90.0F;
            this.a.yaw = this.a(this.a.yaw, f, 30.0F);
            NMS.setHeadYaw(this.a, this.a.yaw);
            AttributeInstance speed = this.a.getAttributeInstance(GenericAttributes.d);
            speed.setValue(0.1 * this.e);
            float movement = (float)(this.e * speed.getValue()) * 10.0F;
            this.a.i(movement);
            this.a.bf = movement;
            if (d2 > (double)0.0F && d0 * d0 + d1 * d1 < (double)1.0F) {
               this.a.getControllerJump().a();
            }
         }
      }

   }
}
