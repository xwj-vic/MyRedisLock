package com.xwj;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@ComponentScan("com.xwj")
public class SpringConfig {

    @Bean
    public JedisPool jedisPool(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(101);
        jedisPoolConfig.setMaxIdle(1000);
        jedisPoolConfig.setMinIdle(1);
        jedisPoolConfig.setMaxWaitMillis(2000);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setTestOnReturn(true);
        JedisPool jedisPool = new JedisPool(jedisPoolConfig,"192.168.204.188",6379);
        return jedisPool;
    }

}
