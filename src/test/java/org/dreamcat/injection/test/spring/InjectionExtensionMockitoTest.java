package org.dreamcat.injection.test.spring;

import static org.mockito.ArgumentMatchers.anyString;

import java.util.Collections;
import java.util.Map;
import org.dreamcat.injection.test.InjectionExtension;
import org.dreamcat.injection.test.spring.hello.service.HelloService;
import org.dreamcat.injection.test.spring.hello.service.impl.BookServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

/**
 * @author Jerry Will
 * @version 2022-10-13
 */
@SpringBootApplication
@ExtendWith(InjectionExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InjectionExtensionMockitoTest {

    @Mock
    @MockBean
    HelloService helloService;
    @Spy
    @SpyBean
    BookServiceImpl bookService;

    @Test
    void test1() {
        helloService.say("jerry");
    }

    @Test
    void test2() {
        Map<String, Object> map = bookService.addAndGetBook("1314");
        System.out.println(map);
        Assertions.assertTrue(map.containsKey("name") && map.containsKey("desc"));

        Mockito.doReturn(Collections.emptyMap()).when(bookService).getBook(anyString());
        map = bookService.addAndGetBook("1314");
        System.out.println(map);
        Assertions.assertTrue(!map.containsKey("name") && map.containsKey("desc"));
    }
}
