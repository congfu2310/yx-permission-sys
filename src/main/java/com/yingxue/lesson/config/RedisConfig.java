package com.yingxue.lesson.config;

import com.yingxue.lesson.serializer.MyStringRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
public class RedisConfig {

    //@Bean这个注解告诉Spring容器，方法redisTemplate()将会产生一个Bean，可以被其他组件引用或注入
    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        //创建一个RedisTemplate实例
        RedisTemplate<String,Object> redisTemplate=new RedisTemplate<>();
        //设置Redis连接工厂，以便RedisTemplate可以管理与Redis的连接
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //创建一个StringRedisSerializer对象，用于序列化Redis的键（key）
        StringRedisSerializer stringRedisSerializer=new StringRedisSerializer();
        MyStringRedisSerializer myStringRedisSerializer=new MyStringRedisSerializer();
        //设置键的序列化器为StringRedisSerializer
        redisTemplate.setKeySerializer(stringRedisSerializer);
        //设置哈希键的序列化器为StringRedisSerializer
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(myStringRedisSerializer);
        //设置值的序列化器为自定义的MyStringRedisSerializer
        redisTemplate.setValueSerializer(myStringRedisSerializer);
        return redisTemplate;
    }
}
