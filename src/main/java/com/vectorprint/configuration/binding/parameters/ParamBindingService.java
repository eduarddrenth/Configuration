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
import com.vectorprint.VectorPrintRuntimeException;
import java.util.ServiceLoader;

/**
 * Singleton provider of {@link ParameterizableBindingFactory} instances. This class uses spi ({@link ServiceLoader#load(java.lang.Class)
 * })
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ParamBindingService {

   private final ServiceLoader<ParameterizableBindingFactory> loader;

   private ParamBindingService() {
      loader = ServiceLoader.load(ParameterizableBindingFactory.class);
   }

   private static final ParamBindingService instance = new ParamBindingService();

   public static ParamBindingService getInstance() {
      return instance;
   }

   /**
    * When {@link #setFactoryClass(java.lang.Class) a custom factory is set} return a new instance of this class,
    * otherwise return the first external implementation of {@link ParameterizableBindingFactory} found that is not built in or
    * return {@link ParameterizableBindingFactoryImpl}.
    *
    * @see #setFactoryClass(java.lang.Class)
    * @return
    */
   public ParameterizableBindingFactory getFactory() {
      if (factoryClass != null) {
         try {
            return factoryClass.newInstance();
         } catch (InstantiationException ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (IllegalAccessException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
      }
      ParameterizableBindingFactory factory = new ParameterizableBindingFactoryImpl();
      for (ParameterizableBindingFactory f : loader) {
         return f;
      }
      return factory;
   }
   private Class<? extends ParameterizableBindingFactory> factoryClass = null;

   public ParamBindingService setFactoryClass(Class<? extends ParameterizableBindingFactory> factoryClass) {
      this.factoryClass = factoryClass;
      return this;
   }


}
