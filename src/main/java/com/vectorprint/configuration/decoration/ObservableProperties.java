
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
import com.vectorprint.configuration.decoration.visiting.ObservableVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class ObservableProperties extends AbstractPropertiesDecorator implements Observable {

   private final Set<Observer> observers = new HashSet<>(1);
   
   private static final Logger LOGGER = Logger.getLogger(ObservableProperties.class.getName());

   public ObservableProperties(EnhancedMap settings) {
      super(settings);
   }

   @Override
   public void addObserver(Observer o) {
      if (!observers.add(o)) {
         LOGGER.warning(String.format("observer %s already present", o));
      }
   }

   @Override
   public void removeObserver(Observer o) {
      observers.remove(o);
   }

   @Override
   public void notifyObservers(Changes changes) {
       observers.forEach((o) -> {
           o.update(this, changes);
       });
   }

   @Override
   public String[] put(String key, String value) {
      return put(key,new String[] {value});
   }
   
   

   @Override
   public String[] put(String key, String[] value) {
      boolean exists = containsKey(key);
      String[] v1 = value;
      String[] v2 = get(key);
      String[] s = super.put(key, value);
      if (exists) {
         if ((v1 == null && v2 != null) || (v1 != null && !Arrays.equals(v1, v2))) {
            notifyObservers(new Changes(null, Changes.fromKeys(key), null));
         }
      } else {
         notifyObservers(new Changes(Changes.fromKeys(key), null, null));
      }
      return s;
   }

   @Override
   public void putAll(Map<? extends String, ? extends String[]> m) {
      List<String> added = new ArrayList<>(m.size());
      List<String> changed = new ArrayList<>(m.size());
      m.entrySet().forEach((e) -> {
          if (containsKey(e.getKey())) {
              String[] v1 = e.getValue();
              String[] v2 = get(e.getKey());
              if ((v1 == null && v2 != null) || (v1 != null && !Arrays.equals(v1, v2))) {
                  changed.add(e.getKey());
              }
          } else {
              added.add(e.getKey());
          }
       });
      super.putAll(m);
      notifyObservers(new Changes(added, changed, null));
   }

   @Override
   public String[] remove(Object key) {
      String[] s = super.remove(key);
      notifyObservers(new Changes(null, null, Changes.fromKeys((String) key)));
      return s;
   }

   @Override
   public void clear() {
      List<String> gone = new ArrayList<>(keySet());
      super.clear();
      notifyObservers(new Changes(null, null, gone));
   }

   @Override
   public EnhancedMap clone() throws CloneNotSupportedException {
      ObservableProperties observableProperties = (ObservableProperties) super.clone();
      observableProperties.observers.addAll(this.observers);
      return observableProperties;
   }

}
