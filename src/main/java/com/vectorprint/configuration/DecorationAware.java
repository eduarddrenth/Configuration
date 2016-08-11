/*
 * Copyright 2016 VectorPrint.
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
package com.vectorprint.configuration;

import com.vectorprint.configuration.annotation.SettingsField;
import com.vectorprint.configuration.decoration.AbstractPropertiesDecorator;
import java.util.List;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface DecorationAware {

   /**
    * Called by {@link AbstractPropertiesDecorator#AbstractPropertiesDecorator(com.vectorprint.configuration.EnhancedMap)
    * }
    * to build a list of decorators for settings. When decorated your code should call methods on the outermost
    * decorator only, if you don't, functionality of decorators will not be called. The preferred way to achieve this is
    * to use the {@link SettingsField} annotation in conjunction with a call to {@link SettingsAnnotationProcessor#initSettings(java.lang.Object, com.vectorprint.configuration.EnhancedMap) }
    * }.
    *
    * @param clazz
    */
   void addDecorator(Class<? extends AbstractPropertiesDecorator> clazz);

   /**
    * @see AbstractPropertiesDecorator#AbstractPropertiesDecorator(com.vectorprint.configuration.EnhancedMap)
    * @return a list of decorators that wrap these settings
    */
   List<Class<? extends AbstractPropertiesDecorator>> getDecorators();

   /**
    * Set by {@link AbstractPropertiesDecorator#AbstractPropertiesDecorator(com.vectorprint.configuration.EnhancedMap)
    * }, if it is not null use it instead of these settings.
    *
    * @see SettingsAnnotationProcessor
    * @return
    */
   AbstractPropertiesDecorator getOutermostDecorator();

   /**
    * Called by {@link AbstractPropertiesDecorator#AbstractPropertiesDecorator(com.vectorprint.configuration.EnhancedMap)
    * }.
    *
    * @see SettingsAnnotationProcessor
    * @param outermostDecorator
    */
   void setOutermostDecorator(AbstractPropertiesDecorator outermostDecorator);

}
