/*
 * Copyright 2014 VectorPrint.
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Enables being notified about property changes
 * @see Observable
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ObservableProperties extends AbstractPropertiesDecorator implements Observable {

   private Set<Observer> observers = new HashSet<Observer>(1);

   public ObservableProperties(EnhancedMap properties) {
      super(properties);
   }

   @Override
   public void addObserver(Observer o) {
      if (!observers.add(o)) {
         throw new VectorPrintRuntimeException(String.format("could not add observer %s", o));
      }
   }

   @Override
   public void removeObserver(Observer o) {
      observers.remove(o);
   }

   @Override
   public void notifyObservers(Changes changes) {
      for (Observer o : observers) {
         o.update(this, changes);
      }
   }

   private boolean notified;

   @Override
   public String put(String key, String value) {
      boolean exists = containsKey(key);
      String v1 = value;
      String v2 = get(key);
      String s = super.put(key, value);
      if (exists) {
         if ((v1 == null && v2 != null) || (v1 != null && !v1.equals(v2))) {
            notifyObservers(new Changes(null, Changes.fromKeys(key), null));
         }
      } else {
         notifyObservers(new Changes(Changes.fromKeys(key), null, null));
      }
      return s;
   }

   @Override
   public void putAll(Map<? extends String, ? extends String> m) {
      List<String> added = new ArrayList<String>(m.size());
      List<String> changed = new ArrayList<String>(m.size());
      for (Map.Entry<? extends String, ? extends String> e : m.entrySet()) {
         if (containsKey(e.getKey())) {
            String v1 = e.getValue();
            String v2 = get(e.getKey());
            if ((v1 == null && v2 != null) || (v1 != null && !v1.equals(v2))) {
               changed.add(e.getKey());
            }
         } else {
            added.add(e.getKey());
         }
      }
      super.putAll(m);
      notifyObservers(new Changes(added, changed, null));
   }

   @Override
   public String remove(Object key) {
      String s = super.remove(key);
      notifyObservers(new Changes(null, null, Changes.fromKeys((String) key)));
      return s;
   }

   @Override
   public void clear() {
      List<String> gone = new ArrayList<String>(keySet());
      super.clear();
      notifyObservers(new Changes(null, null, gone));
   }

   @Override
   public EnhancedMap clone() {
      ObservableProperties observableProperties = new ObservableProperties(getEmbeddedProperties().clone());
      observableProperties.observers.addAll(this.observers);
      return observableProperties;
   }

}
