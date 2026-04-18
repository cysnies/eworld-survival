package javax.mail;

public class Header {
   protected String name;
   protected String value;

   public Header(String name, String value) {
      super();
      this.name = name;
      this.value = value;
   }

   public String getName() {
      return this.name;
   }

   public String getValue() {
      return this.value;
   }
}
