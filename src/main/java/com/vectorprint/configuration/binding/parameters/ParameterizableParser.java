
package com.vectorprint.configuration.binding.parameters;

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
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessor;
import com.vectorprint.configuration.parameters.Parameter;
import com.vectorprint.configuration.parameters.Parameterizable;
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessor;
import java.io.Serializable;

public interface ParameterizableParser<T> {

   /**
    * a package name that may be used in {@link #parseParameterizable() }
    *
    * @param pkg
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
    * <li>extend {@link AbstractParameterizableBinding}</li>
    * <li>{@link SettingsAnnotationProcessor#initSettings(java.lang.Object, com.vectorprint.configuration.EnhancedMap) }
    * on the Parameterizable class and object</li>
    * <li>{@link ParamAnnotationProcessor#initParameters(com.vectorprint.configuration.parameters.Parameterizable) }
    * on the Parameterizable object</li>
    * <li>call {@link #initParameterizable(com.vectorprint.configuration.parameters.Parameterizable) } on the
    * abstract</li>
    * <li>call {@link #initParameter(com.vectorprint.configuration.parameters.Parameter, java.lang.Object) } for each
    * parameter found. In this method you can call {@link ParamBindingHelper#setValueOrDefault(com.vectorprint.configuration.parameters.Parameter, java.io.Serializable, boolean)
    * }
    * after turning the Object into the type needed by the Parameter.
    * </li>
    * <li>finally you may want to set (default) values for parameters using {@link ParameterHelper#findKey(java.lang.String, java.lang.Class, com.vectorprint.configuration.EnhancedMap, com.vectorprint.configuration.binding.parameters.ParameterHelper.SUFFIX) }, {@link #parseAsParameterValue(java.lang.String, com.vectorprint.configuration.parameters.Parameter)
    * } and {@link ParamBindingHelper#setValueOrDefault(com.vectorprint.configuration.parameters.Parameter, java.io.Serializable, boolean)
    * }.</li>
    * </ul>
    *
    * @return
    */
   Parameterizable parseParameterizable();

   /**
    * Call this when the parsing process instantiated a Parameterizable
    *
    * @param parameterizable
    */
   void initParameterizable(Parameterizable parameterizable);

   /**
    * Call this when the parsing process found and instantiated a Parameter
    *
    * @param parameter
    * @param value
    */
   void initParameter(Parameter parameter, T value);

   /**
    * try to parse a String into a value for a parameter
    *
    * @param <TYPE>
    * @param valueToParse
    * @param parameter
    * @return
    */
   <TYPE extends Serializable> TYPE parseAsParameterValue(String valueToParse, Parameter<TYPE> parameter);

   void setBindingHelper(ParamBindingHelper bindingHelper);
}
