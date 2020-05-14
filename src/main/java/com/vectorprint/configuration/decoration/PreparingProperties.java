
package com.vectorprint.configuration.decoration;

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


import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.preparing.KeyValue;
import com.vectorprint.configuration.preparing.KeyValueObservable;
import com.vectorprint.configuration.preparing.PrepareKeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PreparingProperties extends AbstractPropertiesDecorator implements KeyValueObservable<String, String[]> {
   private static final Logger log = LoggerFactory.getLogger(PreparingProperties.class.getName());

   public PreparingProperties(EnhancedMap properties) {
      super(properties);
   }

   public PreparingProperties(EnhancedMap properties, List<PrepareKeyValue<String, String[]>> observers) {
      super(properties);
      this.observers.addAll(observers);
   }

   /**
    * adds the observer if none of the same class is already registered.
    * @param observer 
    */
   @Override
   public void addObserver(PrepareKeyValue<String, String[]> observer) {
      for (PrepareKeyValue pkv : observers) {
         if (pkv.getClass().equals(observer.getClass())) {
            log.warn(String.format("not adding %s, an observer of this class is already present", observer));
            return;
         }
      }
      observers.add(observer);
   }

   @Override
   public <PKV extends PrepareKeyValue<String, String[]>> PKV removeObserver(PKV observer) {
      return (observers.remove(observer)) ? observer : null;
   }

   @Override
   public void prepareKeyValue(KeyValue<String, String[]> kv) {
       observers.stream().filter((pkv) -> (pkv.shouldPrepare(kv))).forEachOrdered((pkv) -> pkv.prepare(kv));
   }
   private final List<PrepareKeyValue<String, String[]>> observers = new LinkedList<>();

   @Override
   public String[] put(String key, String[] value) {
      KeyValue<String, String[]> prepared = new KeyValue<>(key, value);
      prepareKeyValue(prepared);
      return super.put(prepared.getKey(), prepared.getValue());
   }

   @Override
   public void putAll(Map<? extends String, ? extends String[]> m) {
       m.forEach(this::put);
   }

   @Override
   public EnhancedMap clone() throws CloneNotSupportedException {
      PreparingProperties preparingProperties = (PreparingProperties) super.clone();
      preparingProperties.observers.addAll(observers);
      return preparingProperties;
   }

   @Override
   public void clear() {
      super.clear();
      observers.clear();
   }
   
   
}
