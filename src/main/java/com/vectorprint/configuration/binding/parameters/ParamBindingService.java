
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class ParamBindingService {

   private static final Logger LOGGER = LoggerFactory.getLogger(ParamBindingService.class.getName());

   private final ServiceLoader<ParameterizableBindingFactory> factories;
   private final ServiceLoader<ParamFactoryValidator> validators;

   private ParamBindingService() {
      factories = ServiceLoader.load(ParameterizableBindingFactory.class);
      validators = ServiceLoader.load(ParamFactoryValidator.class);
   }

   private static final ParamBindingService instance = new ParamBindingService();

   public static ParamBindingService getInstance() {
      return instance;
   }

   /**
    * Return the first implementation of {@link ParameterizableBindingFactory} found that is valid according to all
    * {@link ParamFactoryValidator}s, or return null. When no validator is published return the first {@link ParameterizableBindingFactory} found.
    *
    * @return
    */
   public ParameterizableBindingFactory getFactory() {
      for (ParameterizableBindingFactory f : factories) {
         if (isValid(f)) {
            return f;
         }
      }
      return null;
   }

   /**
    * 
    * @return a list of factories found through SPI
    */
   public List<Class<? extends ParameterizableBindingFactory>> getFactoriesKnown() {
      List<Class<? extends ParameterizableBindingFactory>> l = new ArrayList<>();
      for (ParameterizableBindingFactory f : factories) {
         l.add(f.getClass());
      }
      return l;
   }
   
   public boolean isValid(ParameterizableBindingFactory f ) {
         boolean ok = true;
         for (ParamFactoryValidator validator : validators) {
            if (!validator.isValid(f)) {
               if (LOGGER.isDebugEnabled()) {
                  LOGGER.debug(String.format("%s does not pass validation by %s", f.getClass().getName(), validator.getClass().getName()));
               }
               ok = false;
               break;
            }
         }
         return ok;
   }

   /**
    * 
    * @return a list of valid factories found through SPI
    */
   public List<Class<? extends ParameterizableBindingFactory>> getValidFactories() {
      List<Class<? extends ParameterizableBindingFactory>> l = new ArrayList<>();
      for (ParameterizableBindingFactory f : factories) {
         if (isValid(f)) {
            l.add(f.getClass());
         }
      }
      return l;
   }

   /**
    * 
    * @return a list of validators found through SPI
    */
   public List<Class<? extends ParamFactoryValidator>> getValidatorsKnown() {
      List<Class<? extends ParamFactoryValidator>> l = new ArrayList<>();
      for (ParamFactoryValidator f : validators) {
         l.add(f.getClass());
      }
      return l;
   }

}
