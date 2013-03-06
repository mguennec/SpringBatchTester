package com.test.batch.annotations.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.springframework.batch.core.BatchStatus;
import org.springframework.util.StringUtils;

import com.test.batch.annotations.BatchParameters;
import com.test.batch.annotations.BatchTest;
import com.test.batch.annotations.ContextConfig;
import com.test.batch.annotations.DatabaseInit;

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
	 * Looks for potential test methods.
	 * 
	 * @param clazz
	 *            class to look up
	 * @return the methods
	 */
	public static List<Method> getBatchTestMethods(final Class<?> clazz) {
		final List<Method> methods = new ArrayList<Method>();

		for (final Method method : clazz.getMethods()) {
			if (method.isAnnotationPresent(BatchTest.class) && !method.isAnnotationPresent(Ignore.class)) {
				methods.add(method);
			}
		}

		return methods;
	}

	/**
	 * Gets the job parameters on a test method.
	 * 
	 * @param method
	 *            method
	 * @return the parameters
	 */
	public static Map<String, String> getJobParameters(final Method method) {
		final Map<String, String> params = new HashMap<String, String>();
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
	 * Gets an array containing the main context path.
	 * 
	 * @param clazz
	 *            a class
	 * @return the array
	 */
	public static String[] getContext(final Class<?> clazz) {
		final ContextConfig config = clazz.getAnnotation(ContextConfig.class);
		return config == null ? new String[0] : config.values();
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
