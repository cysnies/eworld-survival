package javassist;

class FieldInitLink {
   FieldInitLink next = null;
   CtField field;
   CtField.Initializer init;

   FieldInitLink(CtField f, CtField.Initializer i) {
      super();
      this.field = f;
      this.init = i;
   }
}
