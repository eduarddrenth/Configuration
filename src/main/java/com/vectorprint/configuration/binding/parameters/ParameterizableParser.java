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
package com.vectorprint.configuration.binding.parameters;

import com.vectorprint.configuration.binding.StringConversion;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessor;
import com.vectorprint.configuration.parameters.Parameter;
import com.vectorprint.configuration.parameters.Parameterizable;
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessor;
import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface ParameterizableParser<T> {

   /**
    * a package name that may be used in {@link #parseParameterizable() }
    *
    * @return
    */
   ParameterizableParser setPackageName(String pkg);

   String getPackageName();

   /**
    * settings that may be used in {@link #parseParameterizable() }
    *
    * @param settings
    * @return
    */
   ParameterizableParser setSettings(EnhancedMap settings);

   EnhancedMap getSettings();

   /**
    * Suggestion for how a parser could initialize a Parameterizable (in this order):<br/>
    * <ul>
    * <li>{@link SettingsAnnotationProcessor#initSettings(java.lang.Object, com.vectorprint.configuration.EnhancedMap) }
    * on the Parameterizable class</li>
    * <li>create an instance of the Parameterizable class</li>
    * <li>{@link SettingsAnnotationProcessor#initSettings(java.lang.Object, com.vectorprint.configuration.EnhancedMap) }
    * on the Parameterizable instance</li>
    * <li>{@link ParamAnnotationProcessor#initParameters(com.vectorprint.configuration.parameters.Parameterizable) } on
    * the Parameterizable instance</li>
    * <li>{@link #initParameter(com.vectorprint.configuration.parameters.Parameter, java.lang.Object) } where Object holds the value(s) for the parameter.
    * In this method you can call {@link #setValueOrDefault(com.vectorprint.configuration.parameters.Parameter, java.lang.Object, boolean) } and use
    * {@link StringConversion#getStringConversion() }.
    * </li>
    * <li>finally you may want to set default values for parameters using {@link ParameterHelper#findDefaultKey(java.lang.String, java.lang.Class, com.vectorprint.configuration.EnhancedMap) }, {@link #parseAsParameterValue(java.lang.String, java.lang.String) } and {@link #setValueOrDefault(com.vectorprint.configuration.parameters.Parameter, java.lang.Object, boolean) }.</li>
    * </ul>
    *
    * @return
    */
   Parameterizable parseParameterizable();

   ParameterizableParser setStringConversion(StringConversion stringConversion);

   StringConversion getStringConversion();

   void initParameterizable(Parameterizable parameterizable);

   /**
    * 
    * @param parameter
    * @param value 
    */
   void initParameter(Parameter parameter, T value);
   
   T parseAsParameterValue(String valueToParse, String key);

   void setValueOrDefault(Parameter parameter, T values, boolean setDefault);
}
