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

import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactory;
import com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactoryImpl;
import com.vectorprint.configuration.decoration.AbstractPropertiesDecorator;
import com.vectorprint.configuration.decoration.HelpSupportedProperties;
import com.vectorprint.configuration.decoration.ParsingProperties;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * a feature to use in {@link SettingsField}
 * @author Eduard Drenth at VectorPrint.nl
 */
@Documented
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Feature {
   /**
    * A feature is an implementation of the decorator pattern for adding features to properties. This
    * requires a constructor with {@link EnhancedMap} as argument.
    * @return 
    */
   public Class<? extends AbstractPropertiesDecorator> clazz();
   /**
    * decorators may need input for their functionality, a URL to retrieve help info for example.
    * setting this argument requires a constructor with {@link EnhancedMap} and URL[].
    * @see ParsingProperties
    * @see HelpSupportedProperties
    * @return 
    */
   public String[] urls() default {};
   
   /**
    * @see ParsingProperties
    * @return 
    */
   public Class<? extends EnhancedMapBindingFactory> factoryClass() default EnhancedMapBindingFactoryImpl.class;
   
}
