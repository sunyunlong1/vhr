package org.sang;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonClinetConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config=new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379")
                .setConnectionPoolSize(500)
                .setIdleConnectionTimeout(10000)
                .setConnectTimeout(30000)
                .setTimeout(3000);

        RedissonClient redisson=Redisson.create(config);
        return redisson;
    }

}
