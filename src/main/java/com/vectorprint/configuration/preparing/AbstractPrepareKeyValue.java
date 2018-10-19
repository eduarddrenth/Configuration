
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractPrepareKeyValue implements PrepareKeyValue<String, String[]> {

   private static final long serialVersionUID = 1;
   private final Set<String> keys = new HashSet<>(3);
   private boolean optIn = true;

   /**
    * returns true when optIn is true (the default) and the key is registered or when optIn is false and the key isn't registered
    *
    * @see #addKeys(java.io.Serializable)
    * @see #setOptIn(boolean) 
    * @param keyValue 
    * @return
    */
   @Override
   public boolean shouldPrepare(KeyValue<String, String[]> keyValue) {
      return (optIn && keys.contains(keyValue.getKey())) || (!optIn && !keys.contains(keyValue.getKey()));
   }

   /**
    * register a key that should not be prepared
    *
    * @param keys
    * @return
    */
   public AbstractPrepareKeyValue addKeys(String... keys) {
       this.keys.addAll(Arrays.asList(keys));
      return this;
   }

    public boolean isOptIn() {
        return optIn;
    }

    public AbstractPrepareKeyValue setOptIn(boolean optIn) {
        this.optIn = optIn;
        return this;
    }
   
   
}
