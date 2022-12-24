package org.dreamcat.injection.test.context;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.util.ExceptionUtil;

/**
 * @author Jerry Will
 * @version 2022-10-13
 */
@Slf4j
public class TestContextManager {

    private final TestContext testContext;
    private final ThreadLocal<TestContext> testContextHolder;
    private final List<TestExecutionListener> testExecutionListeners;

    public TestContextManager(Class<?> testClass, Map<String, String> properties) {
        this.testContext = new DefaultTestContext(testClass);
        this.testContextHolder = ThreadLocal.withInitial(
                TestContextManager.this.testContext::copy);
        this.testExecutionListeners = new ArrayList<>();
        this.registerTestExecutionListeners(
                TestExecutionListenerManager.resolveTestExecutionListeners(testClass, properties));
    }

    public void registerTestExecutionListeners(List<TestExecutionListener> testExecutionListeners) {
        for (TestExecutionListener listener : testExecutionListeners) {
            if (log.isTraceEnabled()) {
                log.trace("Registering TestExecutionListener: " + listener);
            }
            this.testExecutionListeners.add(listener);
        }
    }

    public void registerTestExecutionListeners(TestExecutionListener... testExecutionListeners) {
        this.registerTestExecutionListeners(Arrays.asList(testExecutionListeners));
    }

    public final TestContext getTestContext() {
        return this.testContextHolder.get();
    }

    public final List<TestExecutionListener> getTestExecutionListeners() {
        return this.testExecutionListeners;
    }

    private List<TestExecutionListener> getReversedTestExecutionListeners() {
        List<TestExecutionListener> listenersReversed = new ArrayList<>(this.getTestExecutionListeners());
        Collections.reverse(listenersReversed);
        return listenersReversed;
    }

    public void beforeTestClass() throws Exception {
        Class<?> testClass = this.getTestContext().getTestClass();
        if (log.isTraceEnabled()) {
            log.trace("beforeTestClass(): class [" + testClass.getName() + "]");
        }
        this.getTestContext().updateState(null, null, null);
        List<TestExecutionListener> listeners = this.getTestExecutionListeners();
        for (TestExecutionListener listener : listeners) {
            try {
                listener.beforeTestClass(this.getTestContext());
            } catch (Throwable e) {
                logException(e, "beforeTestClass", listener, testClass);
                ExceptionUtil.rethrowException(e);
            }
        }
    }

    public void prepareTestInstance(Object testInstance) throws Exception {
        updateState("prepareTestInstance", testInstance, null, null);
        List<TestExecutionListener> listeners = this.getTestExecutionListeners();
        for (TestExecutionListener listener : listeners) {
            try {
                listener.prepareTestInstance(this.getTestContext());
            } catch (Throwable e) {
                if (log.isErrorEnabled()) {
                    log.error("Caught exception while allowing TestExecutionListener [" +
                            listener + "] to prepare test instance [" + testInstance + "]", e);
                }
                throw e;
            }
        }
    }

    public void beforeTestMethod(Object testInstance, Method testMethod) throws Exception {
        updateState("beforeTestMethod", testInstance, testMethod, null);
        List<TestExecutionListener> listeners = this.getTestExecutionListeners();
        for (TestExecutionListener listener : listeners) {
            try {
                listener.beforeTestMethod(this.getTestContext());
            } catch (Throwable e) {
                logException(e, "beforeTestMethod",
                        listener, this.getTestContext().getTestClass());
                ExceptionUtil.rethrowException(e);
            }
        }
    }

    public void beforeTestExecution(Object testInstance, Method testMethod) throws Exception {
        updateState("beforeTestExecution", testInstance, testMethod, null);
        List<TestExecutionListener> listeners = this.getTestExecutionListeners();
        for (TestExecutionListener listener : listeners) {
            try {
                listener.beforeTestExecution(this.getTestContext());
            } catch (Throwable e) {
                logException(e, "beforeTestExecution",
                        listener, this.getTestContext().getTestClass());
                ExceptionUtil.rethrowException(e);
            }
        }
    }

    public void afterTestExecution(Object testInstance, Method testMethod, Throwable exception) throws Exception {
        updateState("afterTestExecution", testInstance, testMethod, exception);
        Throwable ex = null;
        List<TestExecutionListener> listeners = this.getReversedTestExecutionListeners();
        for (TestExecutionListener listener : listeners) {
            try {
                listener.afterTestExecution(this.getTestContext());
            } catch (Throwable e) {
                logException(e, "afterTestExecution",
                        listener, this.getTestContext().getTestClass());
                if (ex == null) {
                    ex = e;
                } else {
                    ex.addSuppressed(e);
                }
            }
        }
        if (ex != null) {
            ExceptionUtil.rethrowException(ex);
        }
    }

    public void afterTestMethod(Object testInstance, Method testMethod, Throwable exception) throws Exception {
        updateState("afterTestMethod", testInstance, testMethod, null);
        Throwable ex = null;
        List<TestExecutionListener> listeners = this.getReversedTestExecutionListeners();
        for (TestExecutionListener listener : listeners) {
            try {
                listener.afterTestMethod(this.getTestContext());
            } catch (Throwable e) {
                logException(e, "afterTestMethod",
                        listener, this.getTestContext().getTestClass());
                if (ex == null) {
                    ex = e;
                } else {
                    ex.addSuppressed(e);
                }
            }
        }
        if (ex != null) {
            ExceptionUtil.rethrowException(ex);
        }
    }

    public void afterTestClass() throws Exception {
        Class<?> testClass = this.getTestContext().getTestClass();
        if (log.isTraceEnabled()) {
            log.trace("afterTestClass(): class [" + testClass.getName() + "]");
        }
        this.getTestContext().updateState(null, null, null);
        Throwable ex = null;
        List<TestExecutionListener> listeners = this.getReversedTestExecutionListeners();
        for (TestExecutionListener listener : listeners) {
            try {
                listener.afterTestClass(this.getTestContext());
            } catch (Throwable e) {
                logException(e, "afterTestClass",
                        listener, this.getTestContext().getTestClass());
                if (ex == null) {
                    ex = e;
                } else {
                    ex.addSuppressed(e);
                }
            }
        }
        this.testContextHolder.remove();
        if (ex != null) {
            ExceptionUtil.rethrowException(ex);
        }
    }

    private void updateState(String callbackName, Object testInstance, Method testMethod, Throwable testException) {
        if (log.isTraceEnabled()) {
            log.trace("{}(): instance [{}], method [{}], exception [{}]",
                    callbackName, testInstance, testMethod, testException);
        }
        this.getTestContext().updateState(testInstance, testMethod, testException);
    }

    private void logException(Throwable ex, String callbackName, TestExecutionListener testExecutionListener,
            Class<?> testClass) throws Exception {
        if (log.isWarnEnabled()) {
            log.warn(String.format(
                    "Caught exception while invoking '%s' callback on TestExecutionListener [%s] for test class [%s]",
                    callbackName, testExecutionListener, testClass), ex);
        }
    }
}
