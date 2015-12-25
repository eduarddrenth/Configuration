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
package com.vectorprint.configuration.binding.parameters;

/*
 * #%L
 * Config
 * %%
 * Copyright (C) 2015 VectorPrint
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
import java.util.ServiceLoader;

/**
 * Singleton provider of {@link ParameterizableBindingFactory} instances. This class uses spi ({@link ServiceLoader#load(java.lang.Class)
 * }) to find instances of {@link ParameterizableBindingFactory} and of {@link ParamFactoryValidator}.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ParamBindingService {

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
         boolean ok = true;
         boolean noValidatorFound = true;
         for (ParamFactoryValidator validator : validators) {
            noValidatorFound = false;
            if (!validator.isValid(f)) {
               ok = false;
               break;
            }
         }
         if (ok||noValidatorFound) return f;
      }
      return null;
   }


}
