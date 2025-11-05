package org.dreamcat.injection.test.resolver;

import org.dreamcat.common.Pair;
import org.dreamcat.common.di.InjectionFactory;

import java.util.Arrays;
import java.util.List;
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
        ProfileExprValueResolver exprValueResolver = new ProfileExprValueResolver();
        Map<String, Object> exprVars = exprValueResolver.parseExprVars();
        return exprVars::get;
    }

    private InjectionFactory.Builder createInjectionFactoryBuilder() {
        InjectionFactory.Builder builder = InjectionFactory.builder();
        builder.addResourceMappingPossibly("org.dreamcat.rita.annotation.RitaBootApplication", null);
        builder.addResourceMappingPossibly("org.dreamcat.rita.annotation.Route", null);
        return builder;
    }
}
