package org.dreamcat.injection.test.spring.hello.service.impl;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dreamcat.injection.test.spring.hello.service.HealthService;
import org.springframework.stereotype.Component;
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

    @Getter
    @Component
    @RequiredArgsConstructor
    public static class Jerry {

        private final Tom tom;
    }

    @Getter
    @Component
    @RequiredArgsConstructor
    public static class Tom {

        private final Harry harry;
    }

    @Data
    @Component
    public static class Harry {

        private Jerry jerry;

        // circular reference
        // public Harry(@Lazy Jerry jerry) {
        //     this.jerry = jerry;
        // }
    }
}
