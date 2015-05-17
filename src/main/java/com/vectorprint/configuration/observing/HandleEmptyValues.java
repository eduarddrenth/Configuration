/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.configuration.observing;

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
import java.util.logging.Logger;

/**
 * Class for dealing with empty (null or empty) values, throws an exception when an empty value is found or logs a
 * warning, depending on {@link #HandleEmptyValues(boolean) the constructor argument}. A list of keys that never have
 * values is skipped in the check. empties
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class HandleEmptyValues extends AbstractPrepareKeyValue<String, String[]> {

   /**
    * allow settings to be empty
    */
   public static final String ALLOW_EMPTY_SETTINGS = "-allowemptyvalues";
   private boolean allowEmpty = false;
   private static final Logger log = Logger.getLogger(HandleEmptyValues.class.getName());

   /**
    *
    * @param allowEmpty when true log a warning for empties otherwise throw an exception
    */
   public HandleEmptyValues(boolean allowEmpty) {
      this.allowEmpty = allowEmpty;
      addKeyToSkip(ALLOW_EMPTY_SETTINGS);
   }

   @Override
   public void prepare(KeyValue<String, String[]> keyValue) {
      if (keyValue.getValue() == null || keyValue.getValue().length==0 || keyValue.getValue()[0] == null || keyValue.getValue()[0].isEmpty()) {
         if (allowEmpty) {
            log.warning("empty value for key: " + keyValue.getKey());
         } else {
            throw new VectorPrintRuntimeException("empty value not allowed for key: " + keyValue.getKey());
         }
      }
   }
}
