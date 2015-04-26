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

package com.vectorprint.configuration.parameters.parsing;

import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessor;
import com.vectorprint.configuration.parameters.Parameter;
import com.vectorprint.configuration.parameters.Parameterizable;
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessor;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface Parser {
   
   /**
    * a package name that may be used in {@link #parse() }
    * @return 
    */
   Parser setPackageName(String pkg);
   
   /**
    * settings that may be used in {@link #parse() }
    * @param settings
    * @return 
    */
   Parser setSettings(EnhancedMap settings);

   /**
    * Suggestion for how a parser could initialize a Parameterizable (in this order):<br/>
    * <ul>
    * <li>{@link SettingsAnnotationProcessor#initSettings(java.lang.Object, com.vectorprint.configuration.EnhancedMap) } on the Parameterizable class</li>
    * <li>create an instance of the Parameterizable class</li>
    * <li>{@link SettingsAnnotationProcessor#initSettings(java.lang.Object, com.vectorprint.configuration.EnhancedMap) } on the Parameterizable instance</li>
    * <li>{@link ParamAnnotationProcessor#initParameters(com.vectorprint.configuration.parameters.Parameterizable)  } on the Parameterizable instance</li>
    * <li>{@link Parameter#setValue(java.io.Serializable) } with {@link Parameter#convert(java.lang.String) } as argument for key/values found</li>
    * <li>{@link Parameterizable#initDefaults(com.vectorprint.configuration.EnhancedMap) }</li>
    * </ul>
    * @return 
    */
   Parameterizable parse();
}
