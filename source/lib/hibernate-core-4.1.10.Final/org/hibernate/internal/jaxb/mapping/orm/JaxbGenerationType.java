package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "generation-type"
)
@XmlEnum
public enum JaxbGenerationType {
   TABLE,
   SEQUENCE,
   IDENTITY,
   AUTO;

   private JaxbGenerationType() {
   }

   public String value() {
      return this.name();
   }

   public static JaxbGenerationType fromValue(String v) {
      return valueOf(v);
   }
}
