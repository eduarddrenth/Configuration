package com.vectorprint.configuration.cdi;

import jakarta.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

@Qualifier
@Retention(RUNTIME)
@Target({ElementType.TYPE,ElementType.FIELD})
public @interface AppTestBean {
}
