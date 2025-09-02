package org.dreamcat.injection.test.resolver;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dreamcat.common.util.StringUtil;

import java.util.Map;

/**
 * @author Jerry Will
 * @version 2025-09-03
 */
@Getter
@RequiredArgsConstructor
enum EnvOrProperty {
    // INJECTION_TEST_BASE_PACKAGES=org.myorg.a,org.myorg.b
    INJECTION_TEST_BASE_PACKAGES(
            "org.dreamcat.injection.test.base_packages"),
    // INJECTION_TEST_ENABLE_SIMPLE_CONVENTION=1
    INJECTION_TEST_ENABLE_SIMPLE_CONVENTION(
            "org.dreamcat.injection.test.enable_simple_convention"),
    // INJECTION_TEST_IGNORE_CLASS_PATTERNS=^.*?Test$
    INJECTION_TEST_IGNORE_CLASS_PATTERNS(
            "org.dreamcat.injection.test.ignore_class_patterns"),
    //
    INJECTION_TEST_EXPR_VALUE_PROVIDER("org.dreamcat.injection.test.expr_value_provider"),
    ;

    private final String propertyName;

    public String get(Map<String, String> properties) {
        String v = properties.get(name());
        if (StringUtil.isNotEmpty(v)) return v;
        v = properties.get(propertyName);
        if (StringUtil.isNotEmpty(v)) return v;
        v = System.getenv(name());
        if (StringUtil.isNotEmpty(v)) return v;
        return System.getProperty(propertyName);
    }
}
