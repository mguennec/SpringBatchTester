package com.test.batch.annotations;

import org.springframework.batch.core.BatchStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to declare a batch test method.
 *
 * @author mguennec
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BatchTest {

    int DEFAULT = Integer.MAX_VALUE;

    /**
     * @return expected status
     */
    BatchStatus expectedStatus() default BatchStatus.UNKNOWN;

    /**
     * @return expected return code
     */
    int expectedReturn() default DEFAULT;
}
