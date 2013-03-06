package com.test.batch.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to declare the context of a batch test.
 * 
 * @author mgue706
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface ContextConfig {
	String DEFAULT = "";
	
	/**
	 * @return batch name
	 */
	String batchName() default DEFAULT;

	/**
	 * @return batch step name
	 */
	String stepName() default DEFAULT;

	/**
	 * @return main contexts
	 */
	String[] values() default {};
}
