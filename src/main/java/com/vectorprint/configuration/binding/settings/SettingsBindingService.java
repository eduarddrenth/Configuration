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
package com.vectorprint.configuration.binding.settings;

import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.annotation.Feature;
import com.vectorprint.configuration.jaxb.SettingsFromJAXB;
import java.util.ServiceLoader;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class SettingsBindingService {
   
   private ServiceLoader<EnhancedMapBindingFactory> loader;
   
   private SettingsBindingService() {
      loader = ServiceLoader.load(EnhancedMapBindingFactory.class);
   }
   
   private static final SettingsBindingService instance = new SettingsBindingService();
   
   public static SettingsBindingService getInstance() {
      return instance;
   }
   
   private Class<? extends EnhancedMapBindingFactory> factoryClass = null;

   /**
    * Return the first implementation of {@link EnhancedMapBindingFactory} found that is not built in or return the built in factory.
    * This Service class uses {@link ServiceLoader#load(java.lang.Class)}.
    * @return 
    */
   public EnhancedMapBindingFactory getFactory() {
      if (factoryClass!=null) {
         try {
            return factoryClass.newInstance();
         } catch (InstantiationException ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (IllegalAccessException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
      }
      EnhancedMapBindingFactory factory = new EnhancedMapBindingFactoryImpl();
      for (EnhancedMapBindingFactory  f : loader) {
         if (!factory.getClass().equals(f.getClass())) {
            factory = f;
            break;
         }
      }
      return factory;
   }

   /**
    * when a factory is set here it will be used instead of one found by ServiceLoader.
    * @see Feature
    * @see SettingsFromJAXB
    * @param factoryClass 
    */
   public SettingsBindingService setFactoryClass(Class<? extends EnhancedMapBindingFactory> factoryClass) {
      this.factoryClass = factoryClass;
      return this;
   }
   
}
