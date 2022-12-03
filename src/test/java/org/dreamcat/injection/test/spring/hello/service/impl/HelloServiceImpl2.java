package org.dreamcat.injection.test.spring.hello.service.impl;

import org.dreamcat.injection.test.spring.hello.service.HelloService;
import org.springframework.stereotype.Service;

/**
 * @author Jerry Will
 * @version 2022-12-04
 */
@Service("HelloServiceImpl2")
public class HelloServiceImpl2 implements HelloService {

    @Override
    public void say(String name) {
        System.out.println("Hi " + name + ", How you doing?");
    }
}
