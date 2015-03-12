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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class TrimKeyValue extends AbstractPrepareKeyValue<String, String> {

        @Override
        public void prepare(KeyValue<String, String> keyValue) {
           if (keyValue.getValue()!=null) {
              keyValue.setValue(keyValue.getValue().trim());
           }
           if (keyValue.getKey()!=null) {
              keyValue.setKey(keyValue.getKey().trim());
           }
        }
}
