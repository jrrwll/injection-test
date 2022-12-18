package org.dreamcat.injection.test.spring;

import org.dreamcat.injection.test.InjectionExtension;
import org.dreamcat.injection.test.spring.hello.service.HelloService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * @author Jerry Will
 * @version 2022-10-13
 */
@SpringBootApplication
// @SpringBootApplication(scanBasePackageClasses = {HelloApp.class})
@ExtendWith(InjectionExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InjectionExtensionTest {

    @Autowired
    @Qualifier("helloServiceImpl")
    HelloService helloService;
    @MockBean
    HelloService helloService2;

    @Test
    void test1() {
        helloService.say("jerry");
    }

    @Test
    void test2() {
        helloService2.say("1314");
    }
}
