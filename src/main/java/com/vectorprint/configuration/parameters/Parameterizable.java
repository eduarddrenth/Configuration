
package com.vectorprint.configuration.parameters;

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

//~--- JDK imports ------------------------------------------------------------

import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessor;
import java.io.Serializable;
import java.util.Map;
import java.util.Observer;

public interface Parameterizable extends Cloneable, Observer {

   /**
    * contains parameters
    *
    * @return
    */
   Map<String, Parameter> getParameters();

   /**
    * get hold of a parameter
    *
    * @param <TYPE>
    * @param key
    * @param T
    * @return
    */
   <TYPE extends Serializable> Parameter<TYPE> getParameter(String key, Class<TYPE> T);

   /**
    * get a value from a parameter.
    *
    * @param <TYPE>
    * @param key
    * @param T
    * @return
    */
   <TYPE extends Serializable> TYPE getValue(String key, Class<TYPE> T);

   /**
    * set a value of a parameter
    *
    * @param <TYPE>
    * @param key
    * @param value
    */
   <TYPE extends Serializable> void setValue(String key, TYPE value);

   /**
    * addParameter a Parameter to this Parameterizable
    *
    * @param declaringClass the class in which the parameter was declared
    * @see ParamAnnotationProcessor
    * @param parameter
    */
   void addParameter(Parameter parameter, Class<? extends Parameterizable> declaringClass);

   Parameterizable clone() throws CloneNotSupportedException;

   /**
    * return true when {@link Parameter#getValue() } is not null.
    *
    * @param key
    * @return
    */
   boolean isParameterSet(String key);

   EnhancedMap getSettings();
}
