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
import com.vectorprint.configuration.parameters.annotation.Param;
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessorImpl;
import com.vectorprint.configuration.binding.parameters.ParameterizableBindingFactory;
import java.io.Serializable;

/**
 * Parameters for a {@link Parameterizable} provide help, a key to identify the parameter.
 *
 * @see ParamAnnotationProcessorImpl
 * @see ParameterizableBindingFactory
 * @author Eduard Drenth at VectorPrint.nl
 * @param <TYPE>
 */
public interface Parameter<TYPE extends Serializable> extends Cloneable, Serializable, Observable {

   /**
    *
    * @return the class of the parameter value
    */
   Class<TYPE> getValueClass();

   /**
    * the identifier for this parameter
    *
    * @return
    */
   String getKey();

   /**
    * provide some information about the parameter
    *
    * @return
    */
   String getHelp();


   /**
    *
    * @param value the new value
    * @return 
    */
   Parameter<TYPE> setValue(TYPE value);

   /**
    *
    * @param value the new default value
    * @return 
    */
   Parameter<TYPE> setDefault(TYPE value);

   /**
    * return the value for this parameter or, when it is null, the default value
    *
    * @return the value or the default
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
    *
    * @see Param
    * @see ParamAnnotationProcessorImpl#initParameters(com.vectorprint.configuration.parameters.Parameterizable)
    * @see Parameterizable#addParameter(com.vectorprint.configuration.parameters.Parameter, java.lang.Class)
    * @return
    */
   Class<? extends Parameterizable> getDeclaringClass();
}
