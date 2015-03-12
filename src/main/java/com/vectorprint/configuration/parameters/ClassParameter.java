/*
 * Copyright 2014 VectorPrint.
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
package com.vectorprint.configuration.parameters;

import com.vectorprint.VectorPrintRuntimeException;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ClassParameter extends ParameterImpl<Class> {

   public ClassParameter(String key, String help) {
      super(key, help);
   }

   /**
    * uses static cache
    *
    * @param value
    * @return
    * @throws VectorPrintRuntimeException
    */
   @Override
   public Class convert(String value) throws VectorPrintRuntimeException {
      try {
         return MultipleValueParser.classFromKey(value);
      } catch (ClassNotFoundException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   @Override
   protected String valueToString(Object value) {
      return ((Class) value).getName();
   }

}
