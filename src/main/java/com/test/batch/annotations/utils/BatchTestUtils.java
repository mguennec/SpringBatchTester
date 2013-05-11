package com.test.batch.annotations.utils;

import com.test.batch.annotations.BatchParameters;
import com.test.batch.annotations.BatchTest;
import com.test.batch.annotations.ContextConfig;
import com.test.batch.annotations.DatabaseInit;
import org.apache.commons.lang3.ArrayUtils;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.springframework.batch.core.BatchStatus;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class using annotations to launch batch tests.
 * 
 * @author mguennec
 * 
 */
public final class BatchTestUtils {

	private BatchTestUtils() {
		// Ne fait rien
	}

	/**
	 * Gets the job parameters on a test method.
	 * 
	 * @param method
	 *            method
	 * @return the parameters
	 */
	public static Map<String, String> getJobParameters(final Method method) {
		final Map<String, String> params = new HashMap<>();
		if (method.isAnnotationPresent(BatchParameters.class)) {
			final BatchParameters batchParameters = method.getAnnotation(BatchParameters.class);
			if (batchParameters.keys().length == 0 || batchParameters.keys().length != batchParameters.values().length) {
				throw new IllegalArgumentException("BatchParameters invalid on " + method.getName() + " method from class " + method.getDeclaringClass().getName());
			}
			for (int i = 0; i < batchParameters.keys().length; i++) {
				final String key = batchParameters.keys()[i];
				final String value = batchParameters.values()[i];
				params.put(key, value);
			}
		}

		return params;
	}

	/**
	 * Gets an array containing the context path used for database initialization.
	 * 
	 * @param method
	 *            method
	 * @return the array
	 */
	public static String[] getInitDatabasePath(final Method method) {
		final DatabaseInit init = method.getAnnotation(DatabaseInit.class);
		return init == null ? new String[0] : init.value();
	}

	/**
	 * Gets an array containing the main context path.
	 * 
	 * @param method
	 *            method
	 * @return the array
	 */
	public static String[] getContext(final Method method) {
		ContextConfig config = method.getAnnotation(ContextConfig.class);
		if (config == null || config.values().length == 0) {
			config = method.getDeclaringClass().getAnnotation(ContextConfig.class);
		}
		return !isBatchTest(method) || config == null ? new String[0] : config.values();
	}

    public static ConfigurableApplicationContext getApplicationContext(Method method) {
        // Spring Context
        String[] path = BatchTestUtils.getContext(method);
        Assert.assertThat("Context must be specified.", path.length, IsNot.not(IsEqual.equalTo(0)));

        // Database initialization context
        path = ArrayUtils.addAll(path, BatchTestUtils.getInitDatabasePath(method));

        // Context creation
        return new ClassPathXmlApplicationContext(path);
    }

	/**
	 * 
	 * @param method
	 *            the method
	 * @return true if a method is a batch test
	 */
	public static boolean isBatchTest(final Method method) {
		return method.isAnnotationPresent(BatchTest.class);
	}

	/**
	 * Gets the expected status at the end of the batch test execution
	 * 
	 * @param method
	 *            the method
	 * @return expected status
	 */
	public static BatchStatus getExpectedStatus(final Method method) {
		final BatchTest annotation = method.getAnnotation(BatchTest.class);
		return annotation == null ? null : annotation.expectedStatus();
	}

	/**
	 * Gets the expected return code at the end of the batch test execution.
	 * 
	 * @param method
	 *            the method
	 * @return expected return code
	 */
	public static int getExpectedReturnValue(final Method method) {
		final BatchTest annotation = method.getAnnotation(BatchTest.class);
		return annotation == null ? BatchTest.DEFAULT : annotation.expectedReturn();
	}

	/**
	 * Gets the batch name of a batch test (first from the method, then from the class).
	 * 
	 * @param method
	 *            the method
	 * @return the batch name
	 */
	public static String getBatchName(final Method method) {
		ContextConfig config = method.getAnnotation(ContextConfig.class);
		if (config == null || !StringUtils.hasLength(config.batchName())) {
			config = method.getDeclaringClass().getAnnotation(ContextConfig.class);
		}
		return !isBatchTest(method) || config == null ? null : config.batchName();
	}

	/**
	 * Gets the batch step name of a batch test (first from the method, then from the class).
	 * 
	 * @param method
	 *            the method
	 * @return the batch step name
	 */
	public static String getStepName(final Method method) {
		ContextConfig config = method.getAnnotation(ContextConfig.class);
		if (config == null || !StringUtils.hasLength(config.stepName())) {
			config = method.getDeclaringClass().getAnnotation(ContextConfig.class);
		}
		return !isBatchTest(method) || config == null ? null : config.stepName();
	}

}
