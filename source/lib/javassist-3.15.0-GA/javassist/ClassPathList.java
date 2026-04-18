package javassist;

final class ClassPathList {
   ClassPathList next;
   ClassPath path;

   ClassPathList(ClassPath p, ClassPathList n) {
      super();
      this.next = n;
      this.path = p;
   }
}
