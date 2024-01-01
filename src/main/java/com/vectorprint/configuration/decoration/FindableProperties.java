
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

import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.EnhancedMap;
import java.util.HashMap;
import java.util.Map;

public class FindableProperties extends AbstractPropertiesDecorator {

   private static final Map<String, EnhancedMap> findableProperties = new HashMap<>(6);

   /**
    * 
    * @param settings
    * @throws VectorPrintException when {@link EnhancedMap#getId() } is already statically known
    */
   public FindableProperties(EnhancedMap settings) throws VectorPrintException {
      super(settings);
      init(settings);
   }

   private void init(EnhancedMap settings) throws VectorPrintException {
      if (findableProperties.containsKey(settings.getId())) {
         throw new VectorPrintException("Already known: " + settings.getId());
      } else if (settings.getId() == null) {
         throw new VectorPrintException("Id is null");
      }
      findableProperties.put(settings.getId(), settings);
   }

   
   /**
    * looks up a set of properties by exact id.
    *
    * @param id
    * @return the embedded properties found
    */
   public static EnhancedMap findExact(String id) {
      return findableProperties.get(id);
   }

   /**
    * return the first properties whose id contains the argument id
    *
    * @param id
    * @return
    */
   public static EnhancedMap findContains(String id) {
      for (EnhancedMap em : findableProperties.values()) {
         if (em.getId().contains(id)) {
            return em;
         }
      }
      return null;
   }

   @Override
   public EnhancedMap clone() throws CloneNotSupportedException {
      clearStaticReferences();
      return super.clone();
   }

   /**
    * removes all static references to properties
    */
   public static void clearStaticReferences() {
      findableProperties.clear();
   }

   /**
    * sets a static reference
    *
    * @param em
    * @throws VectorPrintException when the reference exists
    */
   public static void setStaticReference(EnhancedMap em) throws VectorPrintException {
      if (findableProperties.containsKey(em.getId())) {
         throw new VectorPrintException("Already known: " + em.getId());
      }
      findableProperties.put(em.getId(), em);
   }

   /**
    * removes a static reference
    *
    * @param em
    * @return the previous reference when found
    */
   public static EnhancedMap removeStaticReference(EnhancedMap em) {
      return findableProperties.remove(em.getId());
   }
}
