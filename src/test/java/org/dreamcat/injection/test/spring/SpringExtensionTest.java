package org.dreamcat.injection.test.spring;

import org.dreamcat.injection.test.spring.hello.HelloApp;
import org.dreamcat.injection.test.spring.hello.service.HelloService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Jerry Will
 * @version 2022-10-13
 */
@SpringBootTest(classes = {HelloApp.class})
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SpringExtensionTest {

    @Autowired
    @Qualifier("helloServiceImpl")
    HelloService helloService1;
    @SpyBean(name = "helloServiceImpl")
    HelloService helloService2;
    @MockBean(name = "HelloServiceImpl2")
    HelloService helloService3;
    @Autowired
    HelloApp helloApp;

    @Test
    void test1() {
        helloService1.say("jerry");
    }

    @Test
    void test2() {
        helloService2.say("1314");
    }

    @Test
    void test3() {
        helloService3.say("qq");
    }

    @Test
    void test4() {
        helloApp.run();
    }
}
