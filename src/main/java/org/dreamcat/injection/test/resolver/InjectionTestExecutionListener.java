package org.dreamcat.injection.test.resolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.di.InjectionFactory;
import org.dreamcat.common.util.ReflectUtil;
import org.dreamcat.common.util.StringUtil;
import org.dreamcat.injection.test.InjectionExtension.Property;
import org.dreamcat.injection.test.context.TestContext;
import org.dreamcat.injection.test.context.TestExecutionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * supported frameworks: springboot, rita
 *
 * @author Jerry Will
 * @version 2022-10-13
 */
@Slf4j
@SuppressWarnings({"unchecked"})
@RequiredArgsConstructor
public abstract class InjectionTestExecutionListener implements TestExecutionListener {

    private static final String springClass = "org.springframework.boot.SpringApplication";
    private static final String ritaClass = "org.dreamcat.rita.boot.RitaApplication";

    public static List<TestExecutionListener> getEnabledListeners(Class<?> testClass, Map<String, String> properties) {
        List<TestExecutionListener> listeners = new ArrayList<>();
        if (ReflectUtil.forNameOrNull(springClass) != null) {
            listeners.add(new SpringTestExecutionListener(testClass, properties));
        }
        if (ReflectUtil.forNameOrNull(ritaClass) != null) {
            listeners.add(new RitaTestExecutionListener(testClass, properties));
        }
        return listeners;
    }

    private final Class<?> testClass;
    private final Map<String, String> properties;

    protected abstract Set<String> resolveBasePackageFromApplicationClass(Class<?> applicationClass);

    protected abstract Function<String, Object> resolveExprValueProviderForConfig();

    private Set<String> resolveBasePackage() {
        String basePackages = properties.get(Property.base_packages.name());
        if (basePackages != null) {
            Set<String> basePackageSet = Arrays.stream(basePackages.split(","))
                    .filter(StringUtil::isNotEmpty)
                    .collect(Collectors.toSet());
            if (basePackageSet.isEmpty()) {
                throw new IllegalArgumentException(
                        "property base_packages is invalid: " + basePackages);
            }
            return basePackageSet;
        }

        String applicationClassName = properties.get(Property.application_class.name());
        if (applicationClassName != null) {
            Class<?> applicationClass = ReflectUtil.forNameOrNull(applicationClassName);
            if (applicationClass != null) {
                return resolveBasePackageFromApplicationClass(applicationClass);
            }
        }
        return Collections.singleton(testClass.getPackage().getName());
    }

    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        InjectionFactory di = resolveInjectionFactory();

        Object testInstance = testContext.getTestInstance();
        try {
            di.preResolve();
            di.resolveMockBeans(testClass, testInstance);
            di.resolveConstruct();

            di.resolveFields(testClass, testInstance);
            di.resolveSpyBeans(testClass, testInstance);

            di.resolvePostConstruct();
        } catch (Exception e) {
            log.error("di failed for injection test: " + e.getMessage(), e);
        }
    }

    private InjectionFactory resolveInjectionFactory() {
        Set<String> basePackageSet = resolveBasePackage();

        InjectionFactory.Builder builder = InjectionFactory.builder()
                .failOnThrow(false)
                .addBasePackage(basePackageSet)
                .outOfBox();

        String ignoreClassPatterns = properties.get(Property.ignore_class_patterns.name());
        if (StringUtil.isNotEmpty(ignoreClassPatterns)) {
            List<Pattern> patterns = Arrays.stream(ignoreClassPatterns.split(","))
                    .filter(StringUtil::isNotEmpty)
                    .map(Pattern::compile).collect(Collectors.toList());
            builder.addIgnoreClassPattern(patterns);
        }
        builder.addIgnoreClassPattern(generatedClassName1);
        builder.addIgnoreClassPattern(generatedClassName2);

        // @Value
        Function<String, Object> exprValueProvider = null;
        String exprVarProviderClassName = properties.get(Property.expr_value_provider.name());
        if (exprVarProviderClassName != null) {
            Class<?> exprVarProviderClass = ReflectUtil.forNameOrNull(exprVarProviderClassName);
            if (exprVarProviderClass != null) {
                if (!ReflectUtil.isAssignable(Function.class, exprVarProviderClass)) {
                    throw new IllegalArgumentException(
                            "property expr_value_provider is not a Function: " + exprVarProviderClassName);
                } else {
                    exprValueProvider = (Function<String, Object>) ReflectUtil.newInstance(exprVarProviderClass);
                }
            }
        }

        if (exprValueProvider == null) {
            exprValueProvider = resolveExprValueProviderForConfig();
        }
        builder.exprValueProvider(exprValueProvider);

        return builder.build();
    }

    private static final Pattern generatedClassName1 = Pattern.compile("^.*?\\$.*?\\$\\d+$");
    private static final Pattern generatedClassName2 = Pattern.compile("^.*?\\$\\$.*?$");
}
