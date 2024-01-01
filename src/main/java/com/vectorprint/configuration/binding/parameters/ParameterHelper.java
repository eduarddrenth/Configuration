
package com.vectorprint.configuration.binding.parameters;

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
import com.vectorprint.configuration.parameters.Parameterizable;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParameterHelper {

   private static final Logger log = LoggerFactory.getLogger(ParameterHelper.class.getName());


   public enum SUFFIX {set_default, set_value}
   
   /**
    * looks for a default value for a key based on the simpleName of a class suffixed by .key and .suffix. This method
    * will traverse all Parameterizable superclasses in search of a default.
    * @param key the key to find
    * @param clazz
    * @param settings
    * @param suffix 
    * @return the key pointing to default setting or null
    */
   public static String findKey(String key, Class<? extends Parameterizable> clazz, EnhancedMap settings, SUFFIX suffix) {
      String simpleName = clazz.getSimpleName() + "." + key + "." + suffix;
      while (!settings.containsKey(simpleName)) {
         Class c = clazz.getSuperclass();
         if (!Parameterizable.class.isAssignableFrom(c)) {
            return null;
         }
         clazz=c;
         simpleName = clazz.getSimpleName() + "." + key + "." + suffix;
      }
      if (log.isDebugEnabled()) {
         log.debug("found default " + simpleName + ": " + Arrays.toString(settings.get(simpleName)));
      }
      return simpleName;
   }

}
