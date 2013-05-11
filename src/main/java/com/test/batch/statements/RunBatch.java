package com.test.batch.statements;

import com.test.batch.context.TestContext;
import com.test.batch.launcher.BatchLauncher;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Statement launching a batch before launching another statement.
 * 
 * @author mguennec
 * 
 */
public class RunBatch extends Statement {
	/** next statement. */
	private final Statement next;

	/** test method. */
	private final FrameworkMethod method;

    private final TestContext testContext;

    private final Object testObject;

	/**
	 * @param method
	 *            test method
	 * @param next
	 *            next statement
	 */
	public RunBatch(final FrameworkMethod method, final TestContext testContext, final Object testObject, final Statement next) {
		this.method = method;
		this.next = next;
        this.testContext = testContext;
        this.testObject = testObject;
	}

	@Override
	public void evaluate() throws Throwable {
        final ConfigurableApplicationContext context = testContext.getContext(method.getMethod());
        context.getAutowireCapableBeanFactory().autowireBeanProperties(testObject, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
		new BatchLauncher().run(method.getMethod(), context);
		next.evaluate();
	}

}
