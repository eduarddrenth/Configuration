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

import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.EnhancedMap;
import java.io.Serializable;

/**
 * {@link EnhancedMap}s can be Observables enabling the manipulation of key value pairs before they are added.
 *
 * @author Eduard Drenth at VectorPrint.nl
 * @param <K>
 * @param <V>
 */
public interface KeyValueObservable<K extends Serializable, V extends Serializable> extends Serializable {

        /**
         * add a key value pair manipulator
         *
         * @param observer
         */
        void addObserver(PrepareKeyValue<K, V> observer);

        /**
         * remove a key value pair manipulator
         *
         * @param <PKV>
         * @param observer
         * @return
         */
        <PKV extends PrepareKeyValue<K, V>> PKV removeObserver(PKV observer);
        
        /**
         * suggested method where the {@link PrepareKeyValue} will be called.
         *
         * @param keyValue
         * @throws VectorPrintException
         */
        void prepareKeyValue(KeyValue<K, V> keyValue) throws VectorPrintException;
}
