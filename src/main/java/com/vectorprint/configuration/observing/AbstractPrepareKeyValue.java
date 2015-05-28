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
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 * @param <K>
 * @param <V>
 */
public abstract class AbstractPrepareKeyValue<K extends Serializable, V extends Serializable> implements PrepareKeyValue<K, V> {

   private static final long serialVersionUID = 1;
   private Set<K> keysToSkip = new HashSet<K>(3);

   /**
    * returns true when the key should not be skipped
    *
    * @see #addKeyToSkip(java.io.Serializable) 
    * @param keyValue 
    * @return
    */
   @Override
   public boolean shouldPrepare(KeyValue<K, V> keyValue) {
      return !keysToSkip.contains(keyValue.getKey());
   }

   /**
    * register a key that should not be prepared
    *
    * @param key
    * @return
    */
   public AbstractPrepareKeyValue<K, V> addKeyToSkip(K key) {
      keysToSkip.add(key);
      return this;
   }
}
