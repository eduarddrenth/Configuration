
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class ObservableProperties extends AbstractPropertiesDecorator {

   private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

   private static final Logger LOGGER = LoggerFactory.getLogger(ObservableProperties.class.getName());

   public ObservableProperties(EnhancedMap settings) {
      super(settings);
   }

   public void addObserver(PropertyChangeListener o) {
      propertyChangeSupport.addPropertyChangeListener(o);
   }

   public void removeObserver(PropertyChangeListener o) {
      propertyChangeSupport.removePropertyChangeListener(o);
   }

   @Override
   public String[] put(String key, String value) {
      return put(key,new String[] {value});
   }
   
   

   @Override
   public String[] put(String key, String[] value) {
      String[] s = super.put(key, value);
      if (!Objects.deepEquals(s,value)) {
         propertyChangeSupport.firePropertyChange(key, s, value);
      }
      return s;
   }

   @Override
   public void putAll(Map<? extends String, ? extends String[]> m) {
      m.forEach((k,v) -> put(k,v));
   }

   @Override
   public String[] remove(Object key) {
      String[] s = super.remove(key);
      if (s!=null) {
         propertyChangeSupport.firePropertyChange(String.valueOf(key),s,null);
      }
      return s;
   }

   @Override
   public void clear() {
      if (!isEmpty()) {
          try {
              EnhancedMap old = clone();
             super.clear();
             old.forEach((k,v) -> propertyChangeSupport.firePropertyChange(k,v,null));
          } catch (CloneNotSupportedException e) {
              throw new RuntimeException(e);
          }
      }
   }

   @Override
   public EnhancedMap clone() throws CloneNotSupportedException {
      ObservableProperties observableProperties = (ObservableProperties) super.clone();
      Arrays.stream(propertyChangeSupport.getPropertyChangeListeners()).forEach(l ->
              observableProperties.propertyChangeSupport.addPropertyChangeListener(l));
      return observableProperties;
   }

}
