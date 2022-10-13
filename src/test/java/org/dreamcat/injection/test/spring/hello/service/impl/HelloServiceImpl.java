package org.dreamcat.injection.test.spring.hello.service.impl;

import lombok.RequiredArgsConstructor;
import org.dreamcat.injection.test.spring.hello.dao.HelloDao;
import org.dreamcat.injection.test.spring.hello.service.HelloService;
import org.dreamcat.injection.test.spring.hello.support.ParamChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Jerry Will
 * @version 2022-10-13
 */
@Service
@RequiredArgsConstructor
public class HelloServiceImpl implements HelloService {

    @Autowired
    HelloDao helloDao;

    final ParamChecker paramChecker;

    @Override
    public String say(String name) {
        return "Hello " + name;
    }
}
