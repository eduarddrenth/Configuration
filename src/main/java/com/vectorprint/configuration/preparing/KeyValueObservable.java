
package com.vectorprint.configuration.preparing;

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


import com.vectorprint.VectorPrintException;
import java.io.Serializable;

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
         */
        void prepareKeyValue(KeyValue<K, V> keyValue);
}
