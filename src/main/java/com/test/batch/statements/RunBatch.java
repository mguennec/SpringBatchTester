package com.test.batch.statements;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.test.batch.launcher.BatchLauncher;

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

	/**
	 * @param method
	 *            test method
	 * @param next
	 *            next statement
	 */
	public RunBatch(final FrameworkMethod method, final Statement next) {
		this.method = method;
		this.next = next;
	}

	@Override
	public void evaluate() throws Throwable {
		new BatchLauncher().run(method.getMethod());
		next.evaluate();
	}

}
