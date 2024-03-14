package com.stip.mybatis.generator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: chenjunan
 * Date: 2019/12/20 10:23
 * Content: auto add updatime use aop
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface UpdateTime {
    String value() default "";
}
