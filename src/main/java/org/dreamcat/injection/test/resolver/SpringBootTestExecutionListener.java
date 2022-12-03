package org.dreamcat.injection.test.resolver;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.di.InjectionFactory;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.util.ReflectUtil;
import org.dreamcat.injection.test.context.TestContext;
import org.dreamcat.injection.test.context.TestExecutionListener;
import org.dreamcat.injection.test.context.TestExecutionListenerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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

    private final Class<?> testClass;
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
        this.testClass = testClass;
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
                .basePackage(basePackageSet)
                .addResourceMapping(Component.class, Component::value)
                .addResourceMapping(Service.class, Service::value)
                .addResourceMapping(Configuration.class, Configuration::value)
                .addResourceMapping(Repository.class, Repository::value)
                .addResourceMapping(Controller.class, Controller::value)
                .addResourceMapping(SpringBootApplication.class, it -> "")
                .addInjectMapping(Autowired.class, Qualifier.class, Qualifier::value)
                .disableFailOnNotFound();

        if (javaxResourceClass != null) {
            builder.addInjectMapping(javaxResourceClass,
                    ann -> (String) ReflectUtil.invoke(ann, "name"));
            builder.addPostConstruct(javaxPostConstructClass);
        }
        this.di = builder.build();
        try {
            this.di.refresh();
        } catch (Exception e) {
            log.error("di failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        Class<?> testClass = testContext.getTestClass();
        Object testInstance = testContext.getTestInstance();
        testContext.getTestInstance();
        di.injectFields(testClass, testInstance);
    }

    private static final Class javaxResourceClass =
            findClass("javax.annotation.Resource");
    private static final Class javaxPostConstructClass =
            findClass("javax.annotation.PostConstruct");

    private static Class findClass(String name) {
        try {
            return Class.forName(name);
        }catch (ClassNotFoundException ignore) {
            return null;
        }
    }
}
