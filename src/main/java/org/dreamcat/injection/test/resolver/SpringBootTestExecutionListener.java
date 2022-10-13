package org.dreamcat.injection.test.resolver;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.dreamcat.common.di.InjectionFactory;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.util.ReflectUtil;
import org.dreamcat.injection.test.context.TestContext;
import org.dreamcat.injection.test.context.TestExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Jerry Will
 * @version 2022-10-13
 */
public class SpringBootTestExecutionListener implements TestExecutionListener {

    private final Set<String> basePackageSet = new HashSet<>();
    private final InjectionFactory di = new InjectionFactory();

    public static TestExecutionListener resolve(Class<?> testClass) {
        SpringBootApplication sba = ReflectUtil.retrieveAnnotation(testClass, SpringBootApplication.class);
        if (sba == null) return null;
        return new SpringBootTestExecutionListener(sba, testClass);
    }

    private SpringBootTestExecutionListener(SpringBootApplication sba, Class<?> testClass) {
        String[] basePackages = sba.scanBasePackages();
        Class<?>[] basePackageClasses = sba.scanBasePackageClasses();
        if (ObjectUtil.isNotEmpty(basePackages)) {
            Collections.addAll(this.basePackageSet, basePackages);
        } else if (ObjectUtil.isNotEmpty(basePackageClasses)) {
            for (Class<?> basePackageClass : basePackageClasses) {
                this.basePackageSet.add(basePackageClass.getName());
            }
        } else {
            String name = testClass.getName();
            Package pkg = testClass.getPackage();
            if (pkg != null) {
                this.basePackageSet.add(pkg.getName());
            } else if (name.contains(".")) {
                this.basePackageSet.add(name.substring(0, name.lastIndexOf('.')));
            } else {
                this.basePackageSet.add("");
            }
        }
    }

    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        Class<?> testClass = testContext.getTestClass();
        Object testInstance = testContext.getTestInstance();
        for (Field field : ReflectUtil.retrieveBeanFields(testClass)) {
            if (!findResourceAnnotation(field)) continue;
            Object fieldValue = di.getBean(field.getType());
            ReflectUtil.setValue(testInstance, field, fieldValue);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean findResourceAnnotation(Field field) {
        if (field.getAnnotation(Autowired.class) != null) return true;
        try {
            Class res = Class.forName("javax.annotation.Resource");
            if (field.getAnnotation(res) != null) return true;
        } catch (ClassNotFoundException ignore) {}

        return false;
    }
}
