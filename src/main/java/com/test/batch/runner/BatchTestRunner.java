package com.test.batch.runner;

import com.test.batch.annotations.BatchTest;
import com.test.batch.context.TestContext;
import com.test.batch.statements.CloseContextStatement;
import com.test.batch.statements.RunBatch;
import org.junit.Test;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * Runner to launch a batch test :
 * - Context init
 * - Launch batch
 * - Launch test
 * 
 * @author mguennec
 * 
 */
public class BatchTestRunner extends BlockJUnit4ClassRunner {

	/** Runner Spring context. */
	private final TestContext ctxt;

	/**
	 * @param klass
	 *            test class
	 * @throws InitializationError
	 */
	public BatchTestRunner(final Class<?> klass) throws InitializationError {
		super(klass);
		ctxt = new TestContext();
	}

	@Override
	protected List<FrameworkMethod> computeTestMethods() {
		final List<FrameworkMethod> methods = new ArrayList<>(getTestClass().getAnnotatedMethods(BatchTest.class));
		methods.addAll(getTestClass().getAnnotatedMethods(Test.class));
		return methods;
	}

	@Override
	protected Statement methodInvoker(final FrameworkMethod method, final Object test) {
		return new CloseContextStatement(method, ctxt, new RunBatch(method, ctxt, test, new InvokeMethod(method, test)));
	}
}
