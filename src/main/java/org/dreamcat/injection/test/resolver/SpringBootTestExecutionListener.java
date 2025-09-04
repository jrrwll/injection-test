package org.dreamcat.injection.test.resolver;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.di.InjectionFactory;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.util.ReflectUtil;
import org.dreamcat.common.util.StringUtil;
import org.dreamcat.injection.test.InjectionExtension.Property;
import org.dreamcat.injection.test.context.TestContext;
import org.dreamcat.injection.test.context.TestExecutionListener;
import org.dreamcat.injection.test.context.TestExecutionListenerManager;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Jerry Will
 * @version 2022-10-13
 */
@Slf4j
@SuppressWarnings({"unchecked"})
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SpringBootTestExecutionListener implements TestExecutionListener {

    private final InjectionFactory di;

    /**
     * maybe return null if the spring annotations aren't found
     *
     * @param testClass test class
     * @return the instance of {@link SpringBootTestExecutionListener}
     * @see TestExecutionListenerManager#resolveTestExecutionListeners(Class, Map)
     */
    public static TestExecutionListener resolve(
            Class<?> testClass, Map<String, String> properties) {
        SpringBootApplication sba = ReflectUtil.retrieveAnnotation(
                testClass, SpringBootApplication.class);
        ComponentScan cs = ReflectUtil.retrieveAnnotation(
                testClass, ComponentScan.class);
        String[] defaultBasePackages = null;
        if (sba == null && cs == null) {
            defaultBasePackages = new String[]{testClass.getPackage().getName()};
        }
        return new SpringBootTestExecutionListener(
                testClass, properties, sba, cs, defaultBasePackages);
    }

    private SpringBootTestExecutionListener(
            Class<?> testClass, Map<String, String> properties, SpringBootApplication sba, ComponentScan cs,
            String[] defaultBasePackages) {
        String[] basePackages;
        Class<?>[] basePackageClasses = null;
        if (sba != null) {
            basePackages = sba.scanBasePackages();
            basePackageClasses = sba.scanBasePackageClasses();
        } else if (cs != null) {
            basePackages = cs.basePackages();
            basePackageClasses = cs.basePackageClasses();
        } else {
            basePackages = defaultBasePackages;
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
                .failOnThrow(false)
                .addBasePackage(basePackageSet)
                .outOfBox();

        configWithProperties(builder, properties);
        this.di = builder.build();
    }

    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        Class<?> testClass = testContext.getTestClass();
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

    private void configWithProperties(
            InjectionFactory.Builder builder, Map<String, String> properties) {
        String ignoreClassPatterns = Property.ignore_class_patterns.get(properties);;
        if (StringUtil.isNotEmpty(ignoreClassPatterns)) {
            List<Pattern> patterns = Arrays.stream(ignoreClassPatterns.split(","))
                    .filter(StringUtil::isNotEmpty)
                    .map(Pattern::compile).collect(Collectors.toList());
            builder.addIgnoreClassPattern( patterns);
        }
        builder.addIgnoreClassPattern(generatedClassName1);
        builder.addIgnoreClassPattern(generatedClassName2);

        // @Value
        String exprVarProviderClassName = Property.expr_value_provider.get(properties);
        if (exprVarProviderClassName != null) {
            Class<?> exprVarProviderClass = ReflectUtil.forNameOrNull(exprVarProviderClassName);

            Function<String, Object> exprValueProvider = null;
            if (exprVarProviderClass != null) {
                if (!ReflectUtil.isAssignable(Function.class, exprVarProviderClass)) {
                    log.error("build TestExecutionListener failed: {} is invalid", exprVarProviderClassName);
                } else {
                    exprValueProvider = (Function<String, Object>) ReflectUtil.newInstance(exprVarProviderClass);
                }
            } else {
                SpringConfigParser configParser = new SpringConfigParser();
                Map<String, Object> exprVars = configParser.parseExprVars();
                exprValueProvider = exprVars::get;
            }
            if (exprValueProvider != null) {
                builder.exprValueProvider(exprValueProvider);
            }
        }
    }

    private static final Pattern generatedClassName1 = Pattern.compile("^.*?\\$.*?\\$\\d+$");
    private static final Pattern generatedClassName2 = Pattern.compile("^.*?\\$\\$.*?$");
}
