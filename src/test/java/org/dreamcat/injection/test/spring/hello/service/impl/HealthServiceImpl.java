package org.dreamcat.injection.test.spring.hello.service.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dreamcat.injection.test.spring.hello.service.HealthService;
import org.springframework.context.annotation.Lazy;
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

    @Getter
    @Component
    public static class Harry {
        private final Jerry jerry;


        public Harry(
                @Lazy Jerry jerry) {
            this.jerry = jerry;
        }
    }
}
