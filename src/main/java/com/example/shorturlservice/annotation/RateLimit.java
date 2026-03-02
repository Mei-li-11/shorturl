package com.example.shorturlservice.annotation;

import java.lang.annotation.*;

// 告诉 Java，这个标签是贴在方法上的
@Target(ElementType.METHOD)
// 告诉 Java，这个标签在程序运行的时候依然要保留，不能被丢掉
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    // 规定时间窗口，默认 60 秒
    int time() default 60;

    // 规定在时间窗口内最多允许访问的次数，默认 5 次
    int count() default 5;
}