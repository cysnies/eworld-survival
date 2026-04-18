package me.main__.util.multiverse.SerializationConfig;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
   Class serializor() default DefaultSerializor.class;

   Class validator() default Validator.class;

   Class virtualType() default Object.class;

   boolean persistVirtual() default false;

   String description() default "<no description set>";
}
