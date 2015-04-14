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
 * Processes objects or classes looking for fields annotated with {@link Setting} or {@link Settings}, when both are present only {@link Settings} is processed.
 * When the field has no value a value from settings will be required, when none is found you will get a {@link VectorPrintRuntimeException}.
 * @author Eduard Drenth at VectorPrint.nl
 * @see EnhancedMap#getGenericProperty(java.lang.String, java.lang.Object, java.lang.Class) 
 * @see SettingsField
 * @see Setting
 */
public interface SettingsAnnotationProcessor {
   
   /**
    * initializes non static fields
    * @param o
    * @param settings 
    */
   void initSettings(Object o, EnhancedMap settings);
   /**
    * initializes non static fields
    * @param o 
    */
   void initSettings(Object o);
   /**
    * initializes static fields
    * @param c
    * @param settings 
    */
   void initStaticSettings(Class c, EnhancedMap settings);
   /**
    * initializes static fields
    * @param c 
    */
   void initStaticSettings(Class c);

}
