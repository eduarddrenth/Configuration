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

import com.vectorprint.configuration.binding.BindingHelper;
import com.vectorprint.configuration.parameters.Parameter;
import com.vectorprint.configuration.parameters.Parameterizable;
import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface ParameterizableSerializer {

   /**
    * Build syntax for a certain Parameterizable
    * @param p
    * @param w
    * @throws IOException 
    */
   void serialize(Parameterizable p, Writer w) throws IOException;
   
   /**
    * Build syntax for a certain Parameter
    * @param p
    * @param w
    * @throws IOException 
    */
   void serialize(Parameter p, Writer w) throws IOException;

   ParameterizableSerializer setPrintOnlyNonDefault(boolean printOnlyNonDefault);

   boolean getPrintOnlyNonDefault();

}
