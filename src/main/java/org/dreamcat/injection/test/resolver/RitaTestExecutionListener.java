package org.dreamcat.injection.test.resolver;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Jerry Will
 * @version 2025-09-05
 */
public class RitaTestExecutionListener extends InjectionTestExecutionListener {

    public RitaTestExecutionListener(Class<?> testClass, Map<String, String> properties) {
        super(testClass, properties);
    }

    @Override
    protected Set<String> resolveBasePackageFromApplicationClass(Class<?> applicationClass) {
        throw new RuntimeException("no implement yet");
    }

    @Override
    protected Function<String, Object> resolveExprValueProviderForConfig() {
        ProfileExprValueRe
        solver exprValueResolver = new ProfileExprValueResolver();
        Map<String, Object> exprVars = exprValueResolver.parseExprVars();
        return exprVars::get;
    }
}
