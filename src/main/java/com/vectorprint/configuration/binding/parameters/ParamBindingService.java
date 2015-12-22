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

import com.vectorprint.configuration.binding.parameters.json.ParameterizableBindingFactoryJson;
import java.util.ServiceLoader;

/**
 * Singleton provider of {@link ParameterizableBindingFactory} instances. This class uses spi ({@link ServiceLoader#load(java.lang.Class) }) 
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ParamBindingService {
   
   private final ServiceLoader<ParameterizableBindingFactory> loader;
   
   private ParamBindingService() {
      loader = ServiceLoader.load(ParameterizableBindingFactory.class);
   }
   
   private static final ParamBindingService instance = new ParamBindingService();
   
   private boolean json = false;
   
   public static ParamBindingService getInstance() {
      return instance;
   }

   /**
    * Return the first implementation of {@link ParameterizableBindingFactory} found that is not built in or return one of the built in factories
    * ({@link ParameterizableBindingFactoryImpl} and {@link ParameterizableBindingFactoryJson}).
    * @see #setJson(boolean) 
    * @return 
    */
   public ParameterizableBindingFactory getFactory() {
      ParameterizableBindingFactory factory = json ? new ParameterizableBindingFactoryJson() : new ParameterizableBindingFactoryImpl();
      for (ParameterizableBindingFactory  f : loader) {
         if (!ParameterizableBindingFactoryImpl.class.equals(f.getClass()) && !ParameterizableBindingFactoryJson.class.equals(f.getClass())) {
            factory = f;
            break;
         }
      }
      return factory;
   }

   /**
    * When true {@link ParameterizableBindingFactoryJson} is the preferred built in class otherwise {@link ParameterizableBindingFactoryImpl} is.
    * @param json 
    * @return
    */
   public ParamBindingService setJson(boolean json) {
      this.json = json;
      return this;
   }
      
}
