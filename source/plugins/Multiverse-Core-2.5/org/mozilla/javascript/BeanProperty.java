package org.mozilla.javascript;

class BeanProperty {
   MemberBox getter;
   MemberBox setter;
   NativeJavaMethod setters;

   BeanProperty(MemberBox getter, MemberBox setter, NativeJavaMethod setters) {
      super();
      this.getter = getter;
      this.setter = setter;
      this.setters = setters;
   }
}
