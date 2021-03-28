package com.vectorprint.configuration.cdi;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier
@Retention(RUNTIME)
@Target({ElementType.TYPE,ElementType.FIELD})
public @interface AppTestBean {
}
