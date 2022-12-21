package org.dreamcat.injection.test.spring.hello.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.dreamcat.common.util.MapUtil;
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
    public Map<String, Object> addAndGetBook(String name) {
        addBook(name);
        Map<String, Object> map = new HashMap<>();
        map.putAll(getBook(name));
        map.putAll(getExtra(name));
        return map;
    }

    @Override
    public Map<String, Object> getBook(String name) {
        return Collections.singletonMap("name", name);
    }

    private Map<String, Object> getExtra(String name) {
        return MapUtil.of("desc", name);
    }
}
