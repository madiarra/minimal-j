package ch.openech.mj.db.model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Int {

	/**
	 * 
	 * @return maximal size of integer (digits without a possible minus sign)
	 */
	int value();

	/**
	 * 
	 * @return true if negative values are allowed
	 */
	boolean negative() default false;
	
	boolean loong() default false;
}
