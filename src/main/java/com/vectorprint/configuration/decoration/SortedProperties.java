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

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class SortedProperties extends AbstractPropertiesDecorator {
   
   public SortedProperties(EnhancedMap settings) {
      super(settings);
   }
   
   private static final Comparator<Entry<String, String[]>> ECOMP = Comparator.comparing(Entry::getKey);
   
   @Override
   public Set<Entry<String, String[]>> entrySet() {
      TreeSet<Entry<String, String[]>> treeSet = new TreeSet<>(ECOMP);
      treeSet.addAll(super.entrySet());
      return treeSet;
   }
   
   @Override
   public Set<String> keySet() {
      return new TreeSet<>(super.keySet());
   }

    @Override
    public EnhancedMap clone() throws CloneNotSupportedException {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }
   
   
}
