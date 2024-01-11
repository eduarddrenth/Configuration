
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


import com.vectorprint.configuration.binding.BindingHelper;
import com.vectorprint.configuration.parameters.Parameter;

import java.io.Serializable;

public interface ParamBindingHelper extends BindingHelper {

   /**
    * call this from {@link ParameterizableSerializer} to give applications a chance to manipulate values before
    * serialization.
    *
    * @param <TYPE>
    * @param p
    * @param useDefault when true call {@link Parameter#getDefault() }, otherwise {@link Parameter#getValue() } 
    * @return
    */
   <TYPE extends Serializable> TYPE getValueToSerialize(Parameter<TYPE> p, boolean useDefault);

   /**
    *
    * Call this from {@link ParameterizableParser#initParameter(com.vectorprint.configuration.parameters.Parameter, java.lang.Object)
    * } and when a default is found to give applications a chance to manipulate values before setting it in a Parameter.
    *
    * @param <TYPE>
    * @param parameter
    * @param value
    * @param setDefault
    */
   <TYPE extends Serializable> void setValueOrDefault(Parameter<TYPE> parameter, TYPE value, boolean setDefault);

}
