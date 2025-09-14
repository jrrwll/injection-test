package org.dreamcat.injection.test.resolver;

import org.dreamcat.common.Pair;
import org.dreamcat.common.function.TriFunction;
import org.dreamcat.common.util.ArrayUtil;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.util.ReflectUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
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
@SuppressWarnings({"unchecked", "rawtypes"})
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
    static TriFunction<String, String, String, List<PropertySource<?>>> yamlFn = (ext, prefix, profile) -> {
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

        try {
            String path = String.format("%sapplication%s.%s", prefix, profile, ext);
            return loader.load(
                    "application", new ClassPathResource(path));
        } catch (IOException e) {
            return Collections.emptyList();
        }
    };

    static TriFunction<String, String, String, List<PropertySource<?>>> propsFn = (ext, prefix, profile) -> {
        String location = String.format("classpath:%sapplication%s.%s", prefix, profile, ext);
        try {
            ResourcePropertySource propertySource = new ResourcePropertySource(location);
            return Collections.singletonList(propertySource);
        } catch (IOException e) {
            return Collections.emptyList();
        }
    };

    private static final List<Pair<List<String>, TriFunction<
            String, String, String, List<PropertySource<?>>>>> extensions = Arrays.asList(
            Pair.of(Arrays.asList("yml", "yaml"), yamlFn),
            Pair.of(Collections.singletonList("properties"), propsFn)
    );

    public static Environment createEnvironment() {
        StandardEnvironment env = new StandardEnvironment();

        addSources(env, null);

        String activeProfile = env.getProperty("spring.profiles.active");
        if (activeProfile == null) return env;

        addSources(env, activeProfile);
        return env;
    }

    private static void addSources(StandardEnvironment env, String profile) {
        for (Pair pair : extensions) {
            List<String> extList = (List<String>) pair.first();
            TriFunction<String, String, String, List<PropertySource<?>>> fn = (TriFunction) pair.second();

            for (String ext : extList) {
                List<PropertySource<?>> cfgSources = fn.apply(ext, "config/", profile);
                if (ObjectUtil.isNotEmpty(cfgSources)) {
                    cfgSources.forEach(ps -> env.getPropertySources().addLast(ps));
                }

                List<PropertySource<?>> sources = fn.apply(ext, "", profile);
                if (ObjectUtil.isNotEmpty(sources)) {
                    sources.forEach(ps -> env.getPropertySources().addLast(ps));
                }
                if (ObjectUtil.isNotEmpty(cfgSources) || ObjectUtil.isNotEmpty(sources)) break;
            }
        }
    }
}
