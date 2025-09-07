package org.dreamcat.injection.test.resolver;


import org.dreamcat.common.util.ArrayUtil;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.util.ReflectUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Jerry Will
 * @version 2025-09-05
 */
public class SpringTestExecutionListener extends InjectionTestExecutionListener {

    public SpringTestExecutionListener(Class<?> testClass, Map<String, String> properties) {
        super(testClass, properties);
    }

    protected Set<String> resolveBasePackageFromApplicationClass(Class<?> applicationClass) {
        SpringBootApplication sba = ReflectUtil.retrieveAnnotation(
                applicationClass, SpringBootApplication.class);
        ComponentScan cs = ReflectUtil.retrieveAnnotation(
                applicationClass, ComponentScan.class);

        String[] basePackages;
        Class<?>[] basePackageClasses = null;
        if (sba != null) {
            basePackages = sba.scanBasePackages();
            basePackageClasses = sba.scanBasePackageClasses();
        } else if (cs != null) {
            basePackages = cs.basePackages();
            basePackageClasses = cs.basePackageClasses();
        } else {
            basePackages = ArrayUtil.EMPTY_STRING_ARRAY;
        }

        Set<String> basePackageSet = new HashSet<>();
        if (ObjectUtil.isNotEmpty(basePackages)) {
            Collections.addAll(basePackageSet, basePackages);
        } else if (ObjectUtil.isNotEmpty(basePackageClasses)) {
            for (Class<?> basePackageClass : basePackageClasses) {
                basePackageSet.add(basePackageClass.getPackage().getName());
            }
        } else {
            basePackageSet.add(applicationClass.getPackage().getName());
        }
        return basePackageSet;
    }

    @Override
    protected Function<String, Object> resolveExprValueProviderForConfig() {
        Environment env = createEnvironment();
        return env::getProperty;
    }

    /// parse expr vars
    private static final List<String> extensions = Arrays.asList(
            "yml", "yaml", "properties");

    public static Environment createEnvironment() {
        StandardEnvironment env = new StandardEnvironment();

        addSources(env, null);

        String activeProfile = env.getProperty("spring.profiles.active");
        if (activeProfile == null) return env;

        addSources(env, activeProfile);
        return env;
    }

    private static void addSources(StandardEnvironment env, String profile) {
        for (String ext : extensions) {
            ResourcePropertySource cfgSource = resourcePropertySource(ext, "config/", profile);
            if (cfgSource != null) {
                env.getPropertySources().addLast(cfgSource);
            }
            ResourcePropertySource source = resourcePropertySource(ext, "", profile);
            if (source != null) {
                env.getPropertySources().addLast(source);
            }
            if (cfgSource != null || source != null) break;
        }
    }

    private static ResourcePropertySource resourcePropertySource(
            String ext, String prefix, String profile) {
        try {
            return new ResourcePropertySource(String.format(
                    "classpath:%sapplication%s.%s", prefix, profile, ext));
        } catch (IOException e) {
            return null;
        }
    }
}
