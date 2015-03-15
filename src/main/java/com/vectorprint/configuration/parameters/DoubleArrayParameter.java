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

import com.vectorprint.ArrayHelper;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.parser.ParseException;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class DoubleArrayParameter extends ParameterImpl<Double[]>{
   
   public DoubleArrayParameter(String key, String help) {
      super(key, help);
   }

   /**
    *
    * @throws VectorPrintRuntimeException
    */
   @Override
   public Double[] convert(String value) throws VectorPrintRuntimeException {
      try {
         return ArrayHelper.toArray(MultipleValueParser.getParamInstance().parseDoubleValues(value));
      } catch (ParseException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

}
