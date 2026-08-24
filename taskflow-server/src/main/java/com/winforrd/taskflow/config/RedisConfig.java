package com.winforrd.taskflow.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import org.springframework.data.redis.connection.RedisConnectionFactory;

import org.springframework.data.redis.core.RedisTemplate;


import org.springframework.data.redis.serializer.StringRedisSerializer;



@Configuration
public class RedisConfig {



    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory){
        // 创建RedisTemplate对象
        RedisTemplate<String,Object> template = new RedisTemplate<>();
        // 设置连接工厂
        template.setConnectionFactory(factory);
        // 设置键的序列化器
        template.setKeySerializer(new StringRedisSerializer());
        // 设置值的序列化器
        template.setValueSerializer(new StringRedisSerializer());
        // 初始化RedisTemplate
        template.afterPropertiesSet();
        return template;

    }


}