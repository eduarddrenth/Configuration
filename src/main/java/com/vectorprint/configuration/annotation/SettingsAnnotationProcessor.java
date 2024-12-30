

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


import com.vectorprint.configuration.EnhancedMap;

public interface SettingsAnnotationProcessor {
   
   /**
    * initializes annotated fields for a class or object. When the object argument is a Class,
    * only static fields will be initialized, otherwise only instance fields.
    * @param o the Class or Object
    * @param settings the settings to use for initialization
    * @return the (wrapped) settings when initialization was executed, null otherwise
    */
   EnhancedMap initSettings(Object o, EnhancedMap settings);

   /**
    * Calls {@link #initSettings(Object, EnhancedMap)} with empty Settings.
    * @param o
    * @return
    */
   EnhancedMap initSettings(Object o);

}
