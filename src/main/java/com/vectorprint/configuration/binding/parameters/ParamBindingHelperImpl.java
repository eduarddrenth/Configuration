

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


import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.binding.BindingHelperImpl;
import com.vectorprint.configuration.parameters.Parameter;
import java.io.Serializable;

public class ParamBindingHelperImpl extends BindingHelperImpl implements ParamBindingHelper {

   /**
    *
    * Call this from {@link ParameterizableParser#initParameter(com.vectorprint.configuration.parameters.Parameter, java.lang.Object)
    * } and when a default is found.
    *
    * @param <TYPE>
    * @param parameter
    * @param value
    * @param setDefault
    */
   @Override
   public <TYPE extends Serializable> void setValueOrDefault(Parameter<TYPE> parameter, TYPE value, boolean setDefault) {
      if (!parameter.getValueClass().isAssignableFrom(value.getClass())) {
         throw new VectorPrintRuntimeException(String.format("%s is not a %s", value.getClass(), parameter.getValueClass()));
      }
      if (setDefault) {
         parameter.setDefault(value);
      } else {
         parameter.setValue(value);
      }
   }

   /**
    * @param <TYPE>
    * @param p
    * @param useDefault
    * @return
    */
   @Override
   public <TYPE extends Serializable> TYPE getValueToSerialize(Parameter<TYPE> p, boolean useDefault) {
      return useDefault ? p.getDefault() : p.getValue();
   }

}
