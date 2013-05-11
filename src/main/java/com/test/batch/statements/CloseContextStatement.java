package com.test.batch.statements;

import com.test.batch.context.TestContext;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Statement used to close a spring context after executing another statement.
 * 
 * @author mguennec
 * 
 */
public class CloseContextStatement extends Statement {

	/** previous statement. */
	private final Statement previous;

	/** Spring context to close. */
	private final TestContext context;
    private final FrameworkMethod method;

    public CloseContextStatement(FrameworkMethod method, TestContext ctxt, Statement previous) {
        this.previous = previous;
        this.context = ctxt;
        this.method = method;
    }

    @Override
	public void evaluate() throws Throwable {
		try {
			previous.evaluate();
		} finally {
			if (context != null) {
				context.close(method.getMethod());
			}
		}
	}

}
