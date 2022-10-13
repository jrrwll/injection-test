package org.dreamcat.injection.test.context;

import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jerry Will
 * @version 2022-10-13
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultTestContext implements TestContext {

    private final Class<?> testClass;
    private volatile Object testInstance;
    private volatile Method testMethod;
    private volatile Throwable testException;

    public final Class<?> getTestClass() {
        return this.testClass;
    }

    @Override
    public TestContext copy() {
        DefaultTestContext context = new DefaultTestContext(testClass);
        context.testInstance = testInstance;
        context.testMethod = testMethod;
        context.testException = testException;
        return context;
    }

    @Override
    public final Object getTestInstance() {
        Object testInstance = this.testInstance;
        if (testInstance == null) {
            throw new IllegalStateException("No test instance");
        }
        return testInstance;
    }

    @Override
    public final Method getTestMethod() {
        Method testMethod = this.testMethod;
        if (testMethod == null) {
            throw new IllegalStateException("No test method");
        }
        return testMethod;
    }

    @Override
    public final Throwable getTestException() {
        return this.testException;
    }

    @Override
    public void updateState(Object testInstance, Method testMethod, Throwable testException) {
        this.testInstance = testInstance;
        this.testMethod = testMethod;
        this.testException = testException;
    }
}
