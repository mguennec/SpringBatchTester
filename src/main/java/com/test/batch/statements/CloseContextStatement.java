package com.test.batch.statements;

import org.junit.runners.model.Statement;
import org.springframework.context.ConfigurableApplicationContext;

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
	private final ConfigurableApplicationContext context;

	/**
	 * @param context
	 *            Spring context to close
	 * @param previous
	 *            previous statement
	 */
	public CloseContextStatement(final ConfigurableApplicationContext context, final Statement previous) {
		this.context = context;
		this.previous = previous;
	}

	@Override
	public void evaluate() throws Throwable {
		try {
			previous.evaluate();
		} finally {
			if (context != null) {
				context.close();
			}
		}
	}

}
