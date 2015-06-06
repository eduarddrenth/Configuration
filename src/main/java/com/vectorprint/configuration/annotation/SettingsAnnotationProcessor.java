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

import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;

/**
 * Processes objects or classes looking for fields annotated with {@link Setting} or {@link SettingsField}, when both are present only {@link SettingsField} is processed.
 * When the field has no value a value from settings will be required, when none is found you will get a {@link VectorPrintRuntimeException}. It is suggested that
 * initSettings will not be executed when it was already called with the same object and the settings. 
 * @author Eduard Drenth at VectorPrint.nl
 * @see EnhancedMap#getGenericProperty(java.lang.Object, java.lang.Class, java.lang.String...) 
 * @see SettingsField
 * @see Setting
 */
public interface SettingsAnnotationProcessor {
   
   /**
    * initializes annotated fields for a class or object. When the object argument is a Class,
    * only static fields will be initialized, otherwise only instance fields.
    * @param o the Class or Object
    * @param settings the settings to use for initialization
    * @return true when initialization was executed
    */
   boolean initSettings(Object o, EnhancedMap settings);

}
