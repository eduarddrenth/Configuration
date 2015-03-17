/*
 * Copyright 2015 VectorPrint.
 *
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
 */
package com.vectorprint.configuration.decoration;

import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.observing.KeyValue;
import com.vectorprint.configuration.observing.KeyValueObservable;
import com.vectorprint.configuration.observing.PrepareKeyValue;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class PreparingProperties extends AbstractPropertiesDecorator implements KeyValueObservable<String, String> {


   public PreparingProperties(EnhancedMap properties) {
      super(properties);
   }

   public PreparingProperties(EnhancedMap properties, List<PrepareKeyValue<String, String>> observers) {
      super(properties);
      this.observers.addAll(observers);
   }

   @Override
   public void addObserver(PrepareKeyValue<String, String> observer) {
      observers.add(observer);
   }

   @Override
   public <PKV extends PrepareKeyValue<String, String>> PKV removeObserver(PKV observer) {
      return (observers.remove(observer)) ? observer : null;
   }

   @Override
   public void prepareKeyValue(KeyValue<String, String> kv) {
      for (PrepareKeyValue<String, String> pkv : observers) {
         if (pkv.shouldPrepare(kv)) {
            pkv.prepare(kv);
         }
      }
   }
   private final List<PrepareKeyValue<String, String>> observers = new LinkedList<PrepareKeyValue<String, String>>();

   @Override
   public String put(String key, String value) {
      KeyValue<String, String> prepared = new KeyValue<String, String>(key, value);
      prepareKeyValue(prepared);
      return super.put(prepared.getKey(), prepared.getValue());
   }

   @Override
   public void putAll(Map<? extends String, ? extends String> m) {
      for (Map.Entry<? extends String, ? extends String> e : m.entrySet()) {
         put(e.getKey(), e.getValue());
      }
   }

   @Override
   public EnhancedMap clone() {
      PreparingProperties preparingProperties = new PreparingProperties(getEmbeddedProperties().clone());
      preparingProperties.observers.addAll(observers);
      return preparingProperties;
   }

   @Override
   public void clear() {
      super.clear();
      observers.clear();
   }
   
   
}
