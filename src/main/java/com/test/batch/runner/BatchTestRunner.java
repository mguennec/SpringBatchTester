package com.test.batch.runner;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.test.batch.annotations.BatchTest;
import com.test.batch.annotations.utils.BatchTestUtils;
import com.test.batch.statements.CloseContextStatement;
import com.test.batch.statements.RunBatch;

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
	private final ConfigurableApplicationContext ctxt;

	/**
	 * @param klass
	 *            test class
	 * @throws InitializationError
	 */
	public BatchTestRunner(final Class<?> klass) throws InitializationError {
		super(klass);
		final String[] path = BatchTestUtils.getContext(klass);
		if (path.length > 0) {
			ctxt = new ClassPathXmlApplicationContext(path);
		} else {
			ctxt = null;
		}
	}

	@Override
	protected List<FrameworkMethod> computeTestMethods() {
		final List<FrameworkMethod> methods = new ArrayList<FrameworkMethod>(getTestClass().getAnnotatedMethods(BatchTest.class));
		methods.addAll(getTestClass().getAnnotatedMethods(Test.class));
		return methods;
	}

	@Override
	protected Statement methodInvoker(final FrameworkMethod method, final Object test) {
		return new RunBatch(method, new InvokeMethod(method, test));
	}

	@Override
	protected Object createTest() throws Exception {
		final Object newInstance = super.createTest();
		ctxt.getAutowireCapableBeanFactory().autowireBeanProperties(newInstance, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
		return newInstance;
	}

	@Override
	protected Statement classBlock(final RunNotifier notifier) {
		return new CloseContextStatement(ctxt, super.classBlock(notifier));
	}
}
