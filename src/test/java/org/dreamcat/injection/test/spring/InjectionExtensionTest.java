package org.dreamcat.injection.test.spring;

import org.dreamcat.injection.test.InjectionExtension;
import org.dreamcat.injection.test.InjectionExtension.Property;
import org.dreamcat.injection.test.spring.hello.HelloApp;
import org.dreamcat.injection.test.spring.hello.service.HelloService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.stereotype.Component;

/**
 * @author Jerry Will
 * @version 2022-10-13
 */
@Component
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InjectionExtensionTest {

    @Autowired
    @Qualifier("helloServiceImpl")
    HelloService helloService;
    @MockBean
    HelloService helloService2;

    @RegisterExtension
    static InjectionExtension extension = InjectionExtension.builder()
            .property(Property.application_class, HelloApp.class.getName())
            .build();

    @Test
    void test1() {
        helloService.say("jerry");
    }

    @Test
    void test2() {
        helloService2.say("1314");
    }
}
