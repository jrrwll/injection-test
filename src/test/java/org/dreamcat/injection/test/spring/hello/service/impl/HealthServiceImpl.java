package org.dreamcat.injection.test.spring.hello.service.impl;

import org.dreamcat.injection.test.spring.hello.service.HealthService;
import org.springframework.stereotype.Service;

/**
 * @author Jerry Will
 * @version 2022-12-16
 */
@Service
public class HealthServiceImpl implements HealthService {

    @Override
    public void ping() {
        System.out.println("PONG");
    }
}
