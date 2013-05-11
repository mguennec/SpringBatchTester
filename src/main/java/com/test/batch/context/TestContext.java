package com.test.batch.context;

import com.test.batch.annotations.utils.BatchTestUtils;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Context of a test.
 * User: Maxime Guennec
 * Date: 11/05/13
 * Time: 16:52
 */
public class TestContext {

    private Map<Method, ConfigurableApplicationContext> contexts = new HashMap<>();

    public ConfigurableApplicationContext getContext(final Method method) {
        if (!contexts.containsKey(method)) {
            contexts.put(method, BatchTestUtils.getApplicationContext(method));
        }
        return contexts.get(method);
    }

    public void close(final Method method) {
        if (contexts.containsKey(method)) {
            final ConfigurableApplicationContext context = contexts.get(method);
            context.close();
            contexts.remove(method);
        }
    }
}
