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
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 *
 * @author eduard
 */
@CheckRequired @Interceptor
@Priority(Interceptor.Priority.APPLICATION)
class RequiredInterceptor {

   @AroundInvoke
   public Object checkRequired(InvocationContext ctx) throws Exception {
       // TODO this check uses the @Property annotation on the producer methods, not the one on the field or method
       if (ctx.getMethod().isAnnotationPresent(Property.class)) {
           final boolean required = ctx.getMethod().getAnnotation(Property.class).required();
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
