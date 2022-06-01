package com.example.simplenettyrpc.util.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPC service注解
 * 使用自定义的 @EnableRpcService 注解可以标记可以哪些方法是服务端提供的服务
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface EnableRpcService {
    Class<?> value();

    String version() default "";
}
