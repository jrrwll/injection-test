package org.dreamcat.injection.test.resolver;

import static org.dreamcat.injection.test.resolver.SpringBootTestExecutionListener.EnvOrProperty.INJECTION_TEST_DISABLE_DYNAMIC_CLASS_FILTER;
import static org.dreamcat.injection.test.resolver.SpringBootTestExecutionListener.EnvOrProperty.INJECTION_TEST_IGNORE_CLASS_PATTERN;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.di.InjectionFactory;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.util.ReflectUtil;
import org.dreamcat.common.util.StringUtil;
import org.dreamcat.common.util.SystemUtil;
import org.dreamcat.injection.test.context.TestContext;
import org.dreamcat.injection.test.context.TestExecutionListener;
import org.dreamcat.injection.test.context.TestExecutionListenerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 * @author Jerry Will
 * @version 2022-10-13
 */
@Slf4j
@SuppressWarnings({"rawtypes", "unchecked"})
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SpringBootTestExecutionListener implements TestExecutionListener {

    private final InjectionFactory di;

    /**
     * maybe return null if the spring annotations aren't found
     *
     * @param testClass test class
     * @return the instance of {@link SpringBootTestExecutionListener}
     * @see TestExecutionListenerManager#resolveTestExecutionListeners(Class)
     */
    public static TestExecutionListener resolve(Class<?> testClass) throws Exception {
        SpringBootApplication sba = ReflectUtil.retrieveAnnotation(
                testClass, SpringBootApplication.class);
        ComponentScan cs = ReflectUtil.retrieveAnnotation(
                testClass, ComponentScan.class);
        if (sba == null && cs == null) {
            return null;
        }
        return new SpringBootTestExecutionListener(testClass, sba, cs);
    }

    private SpringBootTestExecutionListener(
            Class<?> testClass, SpringBootApplication sba, ComponentScan cs) throws Exception {
        String[] basePackages;
        Class<?>[] basePackageClasses;
        if (sba != null) {
            basePackages = sba.scanBasePackages();
            basePackageClasses = sba.scanBasePackageClasses();
        } else {
            basePackages = cs.basePackages();
            basePackageClasses = cs.basePackageClasses();
        }

        Set<String> basePackageSet = new HashSet<>();
        if (ObjectUtil.isNotEmpty(basePackages)) {
            Collections.addAll(basePackageSet, basePackages);
        } else if (ObjectUtil.isNotEmpty(basePackageClasses)) {
            for (Class<?> basePackageClass : basePackageClasses) {
                basePackageSet.add(basePackageClass.getPackage().getName());
            }
        } else {
            basePackageSet.add(testClass.getPackage().getName());
        }
        InjectionFactory.Builder builder = InjectionFactory.builder()
                .disableFailOnThrow()
                .basePackage(basePackageSet)
                .addResourceMapping(Component.class, Component::value)
                .addResourceMapping(Service.class, Service::value)
                .addResourceMapping(Configuration.class, Configuration::value)
                .addResourceMapping(Repository.class, Repository::value)
                .addResourceMapping(Controller.class, Controller::value)
                // Note that Spring @Bean is supported partially
                .addResourceMapping(Bean.class, it -> it.name().length > 0 ? it.name()[0] : "")
                .addResourceMapping(SpringBootApplication.class, (Function) emptyNameGetter)
                .addInjectMapping(Qualifier.class, Qualifier::value)
                .addInjectMapping(Autowired.class, (Function) emptyNameGetter);

        if (javaxResourceClass != null) {
            builder.addInjectMapping(javaxResourceClass, (Function) nameGetter);
            builder.addPostConstruct(javaxPostConstructClass);
        }
        if (springMockBeanClass != null && springSpyBeanClass != null &&
                mockitoClass != null) {
            builder.addMockInjectMapping(springMockBeanClass, nameGetter, this::mock);

            builder.addInjectMapping(springSpyBeanClass, (Function) nameGetter);
            builder.addSpyInjectMapping(springSpyBeanClass, this::spy);
        }
        if (mockitoClass != null) {
            builder.addMockInjectMapping(mockClass, nameGetter, this::mock);

            builder.addInjectMapping(spyClass, (Function) emptyNameGetter);
            builder.addSpyInjectMapping(spyClass, this::spy);
        }

        List<Predicate<Class<?>>> ignorePredicates = new ArrayList<>();
        String ignoreClassPattern = INJECTION_TEST_IGNORE_CLASS_PATTERN.get();
        if (StringUtil.isNotEmpty(ignoreClassPattern)) {
            ignorePredicates.add(clazz -> clazz.getName().matches(ignoreClassPattern));
        }
        String disableDynamicClassFilter = INJECTION_TEST_DISABLE_DYNAMIC_CLASS_FILTER.get();
        if (!StringUtil.isTrueString(disableDynamicClassFilter)) {
            ignorePredicates.add(this::isDynamicClass);
        }
        if (!ignorePredicates.isEmpty()) {
            builder.ignorePredicate(clazz -> {
                for (Predicate<Class<?>> ignorePredicate : ignorePredicates) {
                    if (ignorePredicate.test(clazz)) return true;
                }
                return false;
            });
        }
        this.di = builder.build();
    }

    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        Class<?> testClass = testContext.getTestClass();
        Object testInstance = testContext.getTestInstance();
        try {
            di.resolveMockBeans(testClass, testInstance);
            di.refresh();
            di.resolveFields(testClass, testInstance);
            di.resolveSpyBeans(testClass, testInstance);
        } catch (Exception e) {
            log.error("di failed for injection test: " + e.getMessage(), e);
        }
    }

    private static final Class javaxResourceClass =
            ReflectUtil.forNameOrNull("javax.annotation.Resource");
    private static final Class javaxPostConstructClass =
            ReflectUtil.forNameOrNull("javax.annotation.PostConstruct");
    private static final Class mockitoClass =
            ReflectUtil.forNameOrNull("org.mockito.Mockito");
    private static final Class mockClass =
            ReflectUtil.forNameOrNull("org.mockito.Mock");
    private static final Class spyClass =
            ReflectUtil.forNameOrNull("org.mockito.Spy");
    private static final Class springMockBeanClass =
            ReflectUtil.forNameOrNull("org.springframework.boot.test.mock.mockito.MockBean");
    private static final Class springSpyBeanClass =
            ReflectUtil.forNameOrNull("org.springframework.boot.test.mock.mockito.SpyBean");

    @SneakyThrows
    private Object mock(Class<?> clazz) {
        Method method = mockitoClass.getDeclaredMethod("mock", Class.class);
        return method.invoke(null, clazz);
    }

    @SneakyThrows
    private Object spy(Object value) {
        Method method = mockitoClass.getDeclaredMethod("spy", Object.class);
        return method.invoke(null, value);
    }

    private static final Function<Object, String> nameGetter =
            ann -> (String) ReflectUtil.invoke(ann, "name");
    private static final Function<Object, String> emptyNameGetter = ann -> "";

    private boolean isDynamicClass(Class<?> clazz) {
        if (Proxy.isProxyClass(clazz)) return true;
        String className = clazz.getSimpleName();
        if (generatedClassName.matcher(className).matches()) return true;
        return className.contains("$$"); // maybe cglib
    }

    private static final Pattern generatedClassName = Pattern.compile("^.*?\\$.*?\\$\\d+$");

    @Getter
    @RequiredArgsConstructor
    enum EnvOrProperty {
        // INJECTION_TEST_IGNORE_CLASS_PATTERN=^.*?Test$
        INJECTION_TEST_IGNORE_CLASS_PATTERN(
                "org.dreamcat.injection.test.ignore_class_pattern"),
        // INJECTION_TEST_DISABLE_DYNAMIC_CLASS_FILTER=1
        INJECTION_TEST_DISABLE_DYNAMIC_CLASS_FILTER(
                "org.dreamcat.injection.test.disable_dynamic_class_filter"),
        ;

        private final String propertyName;

        public String get() {
            String v = System.getenv(name());
            if (StringUtil.isNotEmpty(v)) return v;
            return System.getProperty(propertyName);
        }
    }
}
