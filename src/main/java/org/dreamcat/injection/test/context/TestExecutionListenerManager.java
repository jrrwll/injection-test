package org.dreamcat.injection.test.context;

import lombok.extern.slf4j.Slf4j;
import org.dreamcat.injection.test.resolver.RitaTestExecutionListener;
import org.dreamcat.injection.test.resolver.SpringTestExecutionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Jerry Will
 * @version 2022-10-13
 */
@Slf4j
public class TestExecutionListenerManager {

    public static List<TestExecutionListener> resolveTestExecutionListeners(Class<?> testClass, Map<String, String> properties) {
        List<TestExecutionListener> listeners = new ArrayList<>();
        if (SpringTestExecutionListener.isEnable()) {
            listeners.add(new SpringTestExecutionListener(testClass, properties));
        }
        if (RitaTestExecutionListener.isEnable()) {
            listeners.add(new RitaTestExecutionListener(testClass, properties));
        }
        return listeners;
    }
}
