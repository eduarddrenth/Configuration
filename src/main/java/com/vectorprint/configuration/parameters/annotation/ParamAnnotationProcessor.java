

package com.vectorprint.configuration.parameters.annotation;

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


import com.vectorprint.configuration.parameters.Parameterizable;
import com.vectorprint.configuration.parameters.ParameterizableImpl;
import java.lang.reflect.InvocationTargetException;

public interface ParamAnnotationProcessor {
   /**
    * you can safely use this, also from different threads
    */
   public static final ParamAnnotationProcessor PAP = new ParamAnnotationProcessorImpl();
   
   /**
    * Looks for {@link Parameters} and {@link Param} annotations to add parameters to the Parameterizable. Each Parameterizable should only be
    * initialized once. Preferably extend or use {@link ParameterizableImpl}.
    * @param parameterizable
    * @return false when the parameterizable was already initialized
    * @throws NoSuchMethodException
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @throws InvocationTargetException 
    */
   boolean initParameters(Parameterizable parameterizable) throws NoSuchMethodException,InstantiationException,IllegalAccessException, InvocationTargetException;

}
