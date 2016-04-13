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
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public abstract class AbstractPrepareKeyValue implements PrepareKeyValue<String, String[]> {

   private static final long serialVersionUID = 1;
   private Set<String> keysToSkip = new HashSet<>(3);

   /**
    * returns true when the key should not be skipped
    *
    * @see #addKeyToSkip(java.io.Serializable) 
    * @param keyValue 
    * @return
    */
   @Override
   public boolean shouldPrepare(KeyValue<String, String[]> keyValue) {
      return !keysToSkip.contains(keyValue.getKey());
   }

   /**
    * register a key that should not be prepared
    *
    * @param keys
    * @return
    */
   public AbstractPrepareKeyValue addKeysToSkip(String... keys) {
      for (String k : keys) {
         keysToSkip.add(k);
      }
      return this;
   }
}
