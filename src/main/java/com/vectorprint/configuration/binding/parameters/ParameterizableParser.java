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

import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessor;
import com.vectorprint.configuration.binding.BindingHelper;
import com.vectorprint.configuration.parameters.Parameter;
import com.vectorprint.configuration.parameters.Parameterizable;
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessor;
import java.io.Serializable;

/**
 * Interface describing how to get from a syntax to a Parameterizable and vise versa. You can for example generate a (javacc) parser that
 * implements this interface, or implement this interface and use a (javacc) parser under the hood. You are recommended to
 * extend {@link AbstractParameterizableParser} and use a {@link BindingHelper}. The BindingHelper will be responsible for
 converting Strings into (atomic) values and vise versa, for setting values and defaults of parameters during or after parsing
 and for escaping meaningful characters for a certain syntax.
 * @see BindingHelper
 * @see ParameterizableBindingFactory
 * @see ParameterizableBindingFactoryImpl
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
    * In this method you can call {@link BindingHelper#setValueOrDefault(com.vectorprint.configuration.parameters.Parameter, java.io.Serializable, boolean) }
    * after turning the Object into the type needed by the Parameter.
    * </li>
    * <li>finally you may want to set default values for parameters using {@link ParameterHelper#findDefaultKey(java.lang.String, java.lang.Class, com.vectorprint.configuration.EnhancedMap) }, {@link #parseAsParameterValue(java.lang.String, java.lang.String) } and {@link BindingHelper#setValueOrDefault(com.vectorprint.configuration.parameters.Parameter, java.io.Serializable, boolean) }.</li>
    * </ul>
    *
    * @return
    */
   Parameterizable parseParameterizable();

   /**
    * Call this when the parsing process instantiated a Parameterizable
    * @param parameterizable 
    */
   void initParameterizable(Parameterizable parameterizable);

   /**
    * Call this when the parsing process found and instantiated a Parameter
    * @param parameter
    * @param value 
    */
   void initParameter(Parameter parameter, T value);
   
   /**
    * Useful when looking for {@link ParameterHelper#findDefaultKey(java.lang.String, java.lang.Class, com.vectorprint.configuration.EnhancedMap) default settings} for a parameter. With the result you can call {@link BindingHelper#setValueOrDefault(com.vectorprint.configuration.parameters.Parameter, java.io.Serializable, boolean) } 
    * @param valueToParse
    * @param parameter
    * @return 
    */
   <TYPE extends Serializable> TYPE parseAsParameterValue(String valueToParse, Parameter<TYPE> parameter);

   /**
    * instantiate syntax specific bindingHelper and call {@link ParameterizableBindingFactory#setBindingHelper(com.vectorprint.configuration.binding.BindingHelper) }
    * @param bindingFactory 
    */
   void initBindingHelper(ParameterizableBindingFactory bindingFactory);
}
