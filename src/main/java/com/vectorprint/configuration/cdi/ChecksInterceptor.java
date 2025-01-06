/*
 * Copyright 2018 Fryske Akademy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vectorprint.configuration.cdi;

import com.vectorprint.configuration.NoValueException;
import jakarta.annotation.Priority;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 *
 * @author eduard
 */
@CheckInjection
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
class ChecksInterceptor {
    private final static Logger log = LoggerFactory.getLogger(ChecksInterceptor.class);

   @AroundInvoke
   public Object check(InvocationContext ctx) throws Exception {
       InjectionPoint ip = (InjectionPoint) ctx.getParameters()[0];
       boolean ok = ip.getMember() instanceof Field || (ip.getMember() instanceof Method m && m.getParameterCount() == 1);
       if (!ok) {
           throw new IllegalStateException("Method %s#%s must have one argument".formatted(ip.getMember().getDeclaringClass(),ip.getMember().getName()));
       }
       final Property property = CDIProperties.fromIp(ip);
       if (property != null) {
           final boolean required = property.required();
           try {
               return ctx.proceed();
           } catch (NoValueException ex) { // thrown by Settings#handleNoValue
               if (required) throw ex;
               else return null;
           }
       } else {
           return null;
       }
   }    
}
