
package com.vectorprint.configuration.annotation;

/*-
 * #%L
 * Config
 * %%
 * Copyright (C) 2015 - 2018 VectorPrint
 * %%
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
 * #L%
 */


import com.vectorprint.configuration.binding.settings.SettingsBindingService;
import com.vectorprint.configuration.decoration.CachingProperties;
import com.vectorprint.configuration.decoration.ObservableProperties;
import com.vectorprint.configuration.decoration.Observer;
import com.vectorprint.configuration.decoration.ParsingProperties;
import com.vectorprint.configuration.decoration.ReadonlyProperties;
import com.vectorprint.configuration.decoration.visiting.ObservableVisitor;
import com.vectorprint.configuration.preparing.AbstractPrepareKeyValue;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SettingsField {

   /**
    * by default settings will be {@link ReadonlyProperties read / write}.
    *
    * @return
    */
   boolean readonly() default false;

   /**
    * by default settings will be {@link CachingProperties cached}.
    *
    * @return
    */
   boolean cache() default true;

   /**
    * by default settings will not be {@link com.vectorprint.configuration.decoration.ReloadableProperties reloadable}.
    *
    * @return
    */
   boolean autoreload() default false;

   /**
    * by default settings will not be {@link ObservableProperties observable}. If you want the object containing the
    * settings to be added automatically as observer, just implement {@link Observer}.
    *
    * @see SettingsAnnotationProcessorImpl
    * @see ObservableVisitor
    * @return
    */
   boolean observable() default false;

   /**
    * When urls are supplied properties will be loaded from it, using {@link SettingsBindingService#getFactory() }.
    *
    * @see ParsingProperties
    * @return
    */
   String[] urls() default {};

   /**
    * You can add your own features to settings, these will be applied at the end of the decoration stack.
    *
    * @return
    */
   Feature[] features() default {};

   /**
    * declare your preprocessors for key / value pairs
    * @see AbstractPrepareKeyValue
    * @return 
    */
   PreProcess[] preprocessors() default {};

}
