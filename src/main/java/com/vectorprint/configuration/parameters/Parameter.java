/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vectorprint.configuration.parameters;

/*
 * #%L
 * VectorPrintReport
 * %%
 * Copyright (C) 2012 - 2013 VectorPrint
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
import com.vectorprint.configuration.parameters.annotation.Param;
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessorImpl;
import java.io.Serializable;

/**
 * Parameters for a {@link Parametrizable} provide help, a key to identify the parameter and the intelligence
 * to convert a String into the value type for the parameter.
 * @author Eduard Drenth at VectorPrint.nl
 * @param <TYPE> 
 */
public interface Parameter<TYPE extends Serializable> extends Cloneable, Serializable, Observable {
   
   /**
    * the identifier for this parameter
    * @return 
    */
   String getKey();
   /**
    * provide some information about the parameter
    * @return 
    */
   String getHelp();
   /**
    *
    * @param value a String to be converted to a value of the required type
    * @return the TYPE
    * @throws VectorPrintRuntimeException can be thrown when conversion fails
    */
   TYPE convert(String value) throws VectorPrintRuntimeException;
   
   /**
    *
    */
   String serializeValue(TYPE value);
   /**
    *
    * @param value the new value
    */
   Parameter<TYPE> setValue(TYPE value);
   /**
    *
    * @param value the new default value
    */
   Parameter<TYPE> setDefault(TYPE value);
   /**
    * return the value for this parameter or, when it is null, the default value
    * @return the value
    */
   TYPE getValue();
   /**
    * 
    * @return the default value
    */
   TYPE getDefault();

   Parameter<TYPE> clone();
   
   /**
    * You may want to know which class declared this Parameter
    * @see Param
    * @see ParamAnnotationProcessorImpl#initParameters(com.vectorprint.configuration.parameters.Parameterizable) 
    * @see Parameterizable#addParameter(com.vectorprint.configuration.parameters.Parameter, java.lang.Class) 
    * @return 
    */
   Class<? extends Parameterizable> getDeclaringClass();
}
