package net.citizensnpcs.api.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Persist {
   Class collectionType() default Collection.class;

   boolean required() default false;

   String value() default "UNINITIALISED";
}
