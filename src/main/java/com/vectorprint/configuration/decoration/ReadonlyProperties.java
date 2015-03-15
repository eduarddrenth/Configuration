/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.configuration.decoration;

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
import com.vectorprint.configuration.EnhancedMap;
import java.util.Map;

/**
 * A Readonly {@link EnhancedMap}.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ReadonlyProperties extends AbstractPropertiesDecorator {


        public ReadonlyProperties(EnhancedMap properties) {
                super(properties);
        }

        @Override
        public String put(String key, String value) {
                throw new VectorPrintRuntimeException("Properties are readonly");
        }

        @Override
        public String remove(Object key) {
                throw new VectorPrintRuntimeException("Properties are readonly");
        }

        @Override
        public void putAll(Map<? extends String, ? extends String> m) {
                throw new VectorPrintRuntimeException("Properties are readonly");
        }

        @Override
        public void clear() {
                throw new VectorPrintRuntimeException("Properties are readonly");
        }

        @Override
        public EnhancedMap clone() {
                return new ReadonlyProperties(getEmbeddedProperties().clone());
        }

}
