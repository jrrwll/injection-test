package org.dreamcat.injection.test.context;

import java.lang.reflect.Method;

/**
 * @author Jerry Will
 * @version 2022-10-13
 */
public interface TestContext {

    TestContext copy();

    Class<?> getTestClass();

    Object getTestInstance();

    Method getTestMethod();

    Throwable getTestException();

    void updateState(Object testInstance, Method testMethod, Throwable testException);
}
