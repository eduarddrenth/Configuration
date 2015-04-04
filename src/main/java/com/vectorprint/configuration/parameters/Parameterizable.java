
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.configuration.parameters;

/*
 * #%L
 * VectorPrintReport4.0
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

//~--- JDK imports ------------------------------------------------------------
import com.vectorprint.configuration.ArgumentParser;
import com.vectorprint.configuration.EnhancedMap;
import java.io.Serializable;
import java.util.Map;
import java.util.Observer;

/**
 * Provides a way to use parameters in objects, parameters do not hold their values themselves, they
 * do provide a method to convert a String value to the type of value we actually need.
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface Parameterizable extends Cloneable, Observer {

   /**
    * contains parameters
    *
    * @return
    */
   Map<String, Parameter> getParameters();
   
   /**
    * get hold of a parameter
    * @param <TYPE>
    * @param key
    * @param T
    * @return 
    */
   <TYPE extends Serializable> Parameter<TYPE> getParameter(String key, Class<TYPE> T);
   
   /**
    * method to get the type of value needed, using the intelligence of a parameter.
    *
    * @param key
    * @param T
    * @return 
    */
   <TYPE extends Serializable> TYPE getValue(String key, Class<TYPE> T);
   
   <TYPE extends Serializable> void setValue(String key, TYPE value);
   /**
    * A Parameterizable can be initialized using a map of key/value pairs and a map holding application settings. A default value for a
    * certain Parameter in a Parameterizable can be specified in the form SimpleClassName.ParameterKey=value. This default will be used
    * when no key/value pair is present in the arguments of the Parameterizable or when the default is specified on the commandline.
    * 
    *
    * @param args the arguments to this parameter, see {@link ArgumentParser}
    * @param settings settings for the application, possibly holding default values
    * @see EnhancedMap#isFromArguments(java.lang.String) 
    * @see ParameterHelper#setup(com.vectorprint.configuration.parameters.Parameterizable, java.util.Map, java.util.Map)
    * @see ParameterHelper#findDefaultKey(java.lang.String, java.lang.Class, java.util.Map) 
    */
   void setup(Map<String, String> args, Map<String, String> settings);
   
   /**
    * addParameter a Parameter to this Parameterizable which value may be unknown at this time
    * @param declaringClass the class in which the parameter was declared
    * @see #setup(java.util.Map) 
    * @param parameter 
    */
   void addParameter(Parameter parameter, Class<? extends Parameterizable> declaringClass);
   
   Parameterizable clone();
   
   boolean isParameterSet(String key);
}
