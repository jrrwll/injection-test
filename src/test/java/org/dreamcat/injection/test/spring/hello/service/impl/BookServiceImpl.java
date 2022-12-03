package org.dreamcat.injection.test.spring.hello.service.impl;

import java.util.Collections;
import java.util.Map;
import org.dreamcat.injection.test.spring.hello.service.BookService;
import org.dreamcat.injection.test.spring.hello.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

/**
 * @author Jerry Will
 * @version 2022-10-13
 */
@Repository
public class BookServiceImpl implements BookService {

    @Autowired
    @Qualifier("helloServiceImpl")
    HelloService helloService;

    @Override
    public void addBook(String name) {
        helloService.say(name);
    }

    @Override
    public Map<String, Object> getBook(String name) {
        return Collections.singletonMap("name", name);
    }
}
