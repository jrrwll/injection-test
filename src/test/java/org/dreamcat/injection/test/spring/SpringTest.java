package org.dreamcat.injection.test.spring;

import org.dreamcat.injection.test.InjectionExtension;
import org.dreamcat.injection.test.spring.hello.HelloApp;
import org.dreamcat.injection.test.spring.hello.service.HelloService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Jerry Will
 * @version 2022-10-13
 */
@SpringBootApplication(scanBasePackageClasses = {HelloApp.class})
@ExtendWith(InjectionExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SpringTest {

    @Autowired
    HelloService helloService;

    @Test
    void test1() {
        System.out.println(helloService.say("jerry"));;
    }

    @Test
    void test2() {
        System.out.println(helloService.say("1314"));;
    }
}
