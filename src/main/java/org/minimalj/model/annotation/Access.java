package org.minimalj.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation specifies the needed role to access a field or
 * a transaction class.<p>
 * 
 * NOT YET IMPLEMENTED
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Access {

	// roles
	String[] value() default {};

}