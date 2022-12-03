package org.dreamcat.injection.test.spring.hello;

import org.dreamcat.injection.test.spring.hello.dao.HelloDao;
import org.dreamcat.injection.test.spring.hello.service.BookService;
import org.dreamcat.injection.test.spring.hello.service.HelloService;
import org.dreamcat.injection.test.spring.hello.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author Jerry Will
 * @version 2022-10-13
 */
@SpringBootApplication
public class HelloApp {

    @Autowired
    BookService bookService;

    HelloService helloService;
    OrderService orderService;

    public HelloApp(
            @Autowired
            @Qualifier("HelloServiceImpl2")
            HelloService helloService,
            @Autowired
            @Qualifier("orderServiceImpl")
            OrderService orderService) {
        this.helloService = helloService;
        this.orderService = orderService;
    }

    public void run() {
        helloService.say("Rachel");
        bookService.addBook("What Is Life");
        orderService.borrow("Jerry", "Think Fast And Slow");
    }
}
