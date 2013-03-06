package com.test.batch.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to declare batch parameters
 * 
 * @author mguennec
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BatchParameters {
	/**
	 * @return keys array
	 */
	String[] keys() default {};

	/**
	 * @return values array
	 */
	String[] values() default {};
}
