package ch.openech.mj.db.model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Date {

	boolean partialAllowed() default false;
	
}
