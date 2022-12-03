package org.dreamcat.injection.test.spring.hello.config;

import org.dreamcat.injection.test.spring.hello.dao.HelloDao;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jerry Will
 * @version 2022-12-04
 */
@Configuration
public class DataSourceConfig {

    @Bean
    @ConditionalOnMissingBean
    public HelloDao helloDao() {
        return new HelloDao() {
            @Override
            public String findName(String name) {
                return null;
            }

            @Override
            public int findRule() {
                return 1;
            }
        };
    }
}
