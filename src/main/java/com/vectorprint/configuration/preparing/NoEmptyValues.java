/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.configuration.preparing;

/*
 * #%L
 * VectorPrintConfig3.0
 * %%
 * Copyright (C) 2011 - 2013 VectorPrint
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

/**
 * Class for dealing with empty (null or empty) values, throws an exception when an empty value is found.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class NoEmptyValues extends AbstractPrepareKeyValue {

   @Override
   public void prepare(KeyValue<String, String[]> keyValue) {
      if (keyValue.getValue() == null || keyValue.getValue().length == 0 || keyValue.getValue()[0] == null || keyValue.getValue()[0].isEmpty()) {
         throw new VectorPrintRuntimeException("empty value not allowed for key: " + keyValue.getKey());
      }
   }
}