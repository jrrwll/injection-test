package org.dreamcat.injection.test.resolver;

import static org.dreamcat.injection.test.resolver.SpringBootTestExecutionListener.EnvOrProperty.INJECTION_TEST_ENABLE_SIMPLE_CONVENTION;
import static org.dreamcat.injection.test.resolver.SpringBootTestExecutionListener.EnvOrProperty.INJECTION_TEST_IGNORE_CLASS_PATTERNS;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.di.InjectionFactory;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.util.ReflectUtil;
import org.dreamcat.common.util.StringUtil;
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
                // Note that Spring @Bean is supported partially
                .addResourceMapping(Bean.class, it -> it.name().length > 0 ? it.name()[0] : "")
                .addInjectMapping(Qualifier.class, Qualifier::value)
                .addInjectMapping(Autowired.class, (Function) emptyNameGetter);

        boolean enableSimpleConvention = StringUtil.isTrueString(
                INJECTION_TEST_ENABLE_SIMPLE_CONVENTION.get());
        if (!enableSimpleConvention) {
            builder.addResourceMapping(Configuration.class, Configuration::value)
                    .addResourceMapping(Repository.class, Repository::value)
                    .addResourceMapping(Controller.class, Controller::value)
                    .addResourceMapping(SpringBootApplication.class, (Function) emptyNameGetter);

            if (springRestControllerClass != null) {
                builder.addResourceMapping(springRestControllerClass, (Function)valueGetter);
            }
        }

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
        configIgnorePredicate(builder);

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
    private static final Class springRestControllerClass =
            ReflectUtil.forNameOrNull("org.springframework.web.bind.annotation.RestController");

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
    private static final Function<Object, String> valueGetter =
            ann -> (String) ReflectUtil.invoke(ann, "value");
    private static final Function<Object, String> emptyNameGetter = ann -> "";

    @Getter
    @RequiredArgsConstructor
    enum EnvOrProperty {
        // INJECTION_TEST_ENABLE_SIMPLE_CONVENTION=1
        INJECTION_TEST_ENABLE_SIMPLE_CONVENTION(
                "org.dreamcat.injection.test.enable_simple_convention"),
        // INJECTION_TEST_IGNORE_CLASS_PATTERNS=^.*?Test$
        INJECTION_TEST_IGNORE_CLASS_PATTERNS(
                "org.dreamcat.injection.test.ignore_class_patterns"),
        ;

        private final String propertyName;

        public String get() {
            String v = System.getenv(name());
            if (StringUtil.isNotEmpty(v)) return v;
            return System.getProperty(propertyName);
        }
    }

    private void configIgnorePredicate(InjectionFactory.Builder builder) {
        List<Predicate<Class<?>>> ignorePredicates = new ArrayList<>();
        String ignoreClassPatterns = INJECTION_TEST_IGNORE_CLASS_PATTERNS.get();
        List<Pattern> patterns = new ArrayList<>();
        if (StringUtil.isNotEmpty(ignoreClassPatterns)) {
            patterns.addAll(Arrays.stream(ignoreClassPatterns.split(","))
                    .filter(StringUtil::isNotEmpty)
                    .map(Pattern::compile).collect(Collectors.toList()));
        }
        String enableSimpleConvention = INJECTION_TEST_ENABLE_SIMPLE_CONVENTION.get();
        if (StringUtil.isTrueString(enableSimpleConvention)) {
            patterns.add(generatedClassName1);
            patterns.add(generatedClassName2);
        }
        if (!patterns.isEmpty()) {
            ignorePredicates.add(clazz -> {
                String name = clazz.getName();
                for (Pattern pattern : patterns) {
                    if (pattern.matcher(name).matches()) return true;
                }
                return false;
            });
        }
        if (!ignorePredicates.isEmpty()) {
            builder.ignorePredicate(clazz -> {
                for (Predicate<Class<?>> ignorePredicate : ignorePredicates) {
                    if (ignorePredicate.test(clazz)) return true;
                }
                return false;
            });
        }
    }

    private static final Pattern generatedClassName1 = Pattern.compile("^.*?\\$.*?\\$\\d+$");
    private static final Pattern generatedClassName2 = Pattern.compile("^.*?\\$\\$.*?$");
}
