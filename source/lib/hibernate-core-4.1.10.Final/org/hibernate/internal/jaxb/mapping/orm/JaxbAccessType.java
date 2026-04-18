package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "access-type"
)
@XmlEnum
public enum JaxbAccessType {
   PROPERTY,
   FIELD;

   private JaxbAccessType() {
   }

   public String value() {
      return this.name();
   }

   public static JaxbAccessType fromValue(String v) {
      return valueOf(v);
   }
}
