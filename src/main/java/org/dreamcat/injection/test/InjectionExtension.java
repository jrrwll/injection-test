package org.dreamcat.injection.test;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.dreamcat.injection.test.context.TestContextManager;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

/**
 * @author Jerry Will
 * @version 2022-10-13
 */
public class InjectionExtension implements
        BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor,
        BeforeEachCallback, AfterEachCallback,
        BeforeTestExecutionCallback, AfterTestExecutionCallback, ParameterResolver {

    private final Map<String, String> properties;

    public InjectionExtension() {
        this(Collections.emptyMap());
    }

    public InjectionExtension(Map<String, String> properties) {
        // copy properties and freeze it
        this.properties = Collections.unmodifiableMap(new HashMap<>(properties));
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        getTestContextManager(context).beforeTestClass();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        try {
            getTestContextManager(context).afterTestClass();
        } finally {
            getStore(context).remove(context.getRequiredTestClass());
        }
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        getTestContextManager(context).prepareTestInstance(testInstance);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Object testInstance = context.getRequiredTestInstance();
        Method testMethod = context.getRequiredTestMethod();
        getTestContextManager(context).beforeTestMethod(testInstance, testMethod);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Object testInstance = context.getRequiredTestInstance();
        Method testMethod = context.getRequiredTestMethod();
        Throwable testException = context.getExecutionException().orElse(null);
        getTestContextManager(context).afterTestMethod(testInstance, testMethod, testException);
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        Object testInstance = context.getRequiredTestInstance();
        Method testMethod = context.getRequiredTestMethod();
        getTestContextManager(context).beforeTestExecution(testInstance, testMethod);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        Object testInstance = context.getRequiredTestInstance();
        Method testMethod = context.getRequiredTestMethod();
        Throwable testException = context.getExecutionException().orElse(null);
        getTestContextManager(context).afterTestExecution(testInstance, testMethod, testException);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        Parameter parameter = parameterContext.getParameter();
        Executable executable = parameter.getDeclaringExecutable();
        Class<?> testClass = extensionContext.getRequiredTestClass();
        return false;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        Parameter parameter = parameterContext.getParameter();
        int index = parameterContext.getIndex();
        Class<?> testClass = extensionContext.getRequiredTestClass();
        return null;
    }

    private TestContextManager getTestContextManager(ExtensionContext context) {
        Objects.requireNonNull(context, "ExtensionContext must not be null");
        Class<?> testClass = context.getRequiredTestClass();
        ExtensionContext.Store store = getStore(context);
        return store.getOrComputeIfAbsent(testClass, k -> new TestContextManager(k, properties),
                TestContextManager.class);
    }

    private static ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getRoot().getStore(TEST_CONTEXT_MANAGER_NAMESPACE);
    }

    private static final ExtensionContext.Namespace TEST_CONTEXT_MANAGER_NAMESPACE =
            Namespace.create(InjectionExtension.class);

    /**
     * @return {@link InjectionExtension.Builder}
     * @see org.junit.jupiter.api.extension.RegisterExtension
     */
    public static Builder builder() {
        return new Builder();
    }

    /*
    @RegisterExtension
    static InjectionExtension extension = InjectionExtension.builder()
        .property("", "")
        .build();
     */
    public static class Builder {
        private final Map<String, String> properties = new HashMap<>();

        public Builder property(String name, String value) {
            properties.put(name, value);
            return this;
        }

        public InjectionExtension build() {
            return new InjectionExtension(properties);
        }
    }
}
