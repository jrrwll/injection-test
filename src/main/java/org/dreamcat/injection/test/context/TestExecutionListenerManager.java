package org.dreamcat.injection.test.context;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.util.ExceptionUtil;
import org.dreamcat.injection.test.resolver.SpringBootTestExecutionListener;

/**
 * @author Jerry Will
 * @version 2022-10-13
 */
@Slf4j
public class TestExecutionListenerManager {

    private static final Class<? extends TestExecutionListener> springBootListenerClass =
            findResolverClass("org.springframework.boot.autoconfigure.SpringBootApplication");

    private static Class<? extends TestExecutionListener> findResolverClass(
            String annotationClassName) {
        try {
            Class.forName(annotationClassName);
            return SpringBootTestExecutionListener.class;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static List<TestExecutionListener> resolveTestExecutionListeners(Class<?> testClass) {
        List<TestExecutionListener> listeners = new ArrayList<>();
        resolveTestExecutionListener(springBootListenerClass, testClass, listeners);
        return listeners;
    }

    private static void resolveTestExecutionListener(
            Class<? extends TestExecutionListener> resolverClass,
            Class<?> testClass, List<TestExecutionListener> listeners) {
        if (resolverClass == null) return;
        try {
            Method method = resolverClass.getDeclaredMethod("resolve", Class.class);
            TestExecutionListener listener = (TestExecutionListener) method.invoke(null, testClass);
            if (listener != null) listeners.add(listener);
        } catch (Throwable e) {
            if (log.isWarnEnabled()) {
                log.error(String.format("Caught exception while resolve '%s' on for test class [%s]",
                        resolverClass.getName(), testClass), e);
            }
            ExceptionUtil.rethrowRuntimeException(e);
        }
    }
}
