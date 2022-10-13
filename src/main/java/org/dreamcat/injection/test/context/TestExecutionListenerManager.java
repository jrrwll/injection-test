package org.dreamcat.injection.test.context;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.util.ExceptionUtil;

/**
 * @author Jerry Will
 * @version 2022-10-13
 */
@Slf4j
public class TestExecutionListenerManager {

    private static final Class<?> springBootListenerClass = findResolverClass(
            "org.springframework.boot.autoconfigure.SpringBootApplication",
            "org.dreamcat.jwrap.test.resolver.SpringBootTestExecutionListener");

    private static Class<?> findResolverClass(
            String annotationClassName, String resolverClassName) {
        try {
            Class.forName(annotationClassName);
            return Class.forName(resolverClassName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static List<TestExecutionListener> resolveTestExecutionListeners(Class<?> testClass) {
        List<TestExecutionListener> listeners = new ArrayList<>();
        // TestExecutionListener resolve(Class<?> testClass)
        resolveTestExecutionListener(springBootListenerClass, testClass, listeners);
        return listeners;
    }

    private static void resolveTestExecutionListener(
            Class<?> resolverClass, Class<?> testClass, List<TestExecutionListener> listeners) {
        if (resolverClass == null) return;
        try {
            Method method = resolverClass.getDeclaredMethod("resolve", Class.class);
            TestExecutionListener listener = (TestExecutionListener) method.invoke(null, testClass);
            listeners.add(listener);
        } catch (Throwable e) {
            // nop
            if (log.isErrorEnabled()) {
                log.error(String.format("Caught exception while resolve '%s' on for test class [%s]",
                        resolverClass.getName(), testClass), e);
            }
            ExceptionUtil.rethrowRuntimeException(e);
        }
    }
}
