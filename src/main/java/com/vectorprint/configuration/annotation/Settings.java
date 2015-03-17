/*
 * Copyright 2015 VectorPrint.
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
package com.vectorprint.configuration.annotation;

import com.vectorprint.configuration.decoration.CachingProperties;
import com.vectorprint.configuration.decoration.ObservableProperties;
import com.vectorprint.configuration.decoration.Observer;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @see SettingsAnnotationProcessor
 * @author Eduard Drenth at VectorPrint.nl
 */
@Documented
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Settings {
   /**
    * by default settings will be {@link CachingProperties cached}.
    * @return 
    */
   boolean cache() default true;
   /**
    * by default settings will not be {@link ObservableProperties observable}.
    * @return 
    */
   boolean observable() default false;
   /**
    * by default the object that owns the settings field will be added as {@link Observer} to the
    * {@link ObservableProperties} when {@link #observable() } is true.
    * @return 
    */
   boolean objectShouldObserve() default true;
   /**
    * You can add your own features to settings, these will be applied at the end of the decoration stack.
    * @return 
    */
   Feature[] features() default {};
   
}
