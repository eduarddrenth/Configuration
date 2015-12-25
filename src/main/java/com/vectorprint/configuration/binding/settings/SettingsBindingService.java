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

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton provider of {@link EnhancedMapBindingFactory} instances. This class uses spi ({@link ServiceLoader#load(java.lang.Class)
 * }) to find instances of {@link EnhancedMapBindingFactory} and of {@link SettingsFactoryValidator}. Every factory found is validated by all validators found,
 * the first valid factory is returned.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class SettingsBindingService {
   
   private static final Logger LOGGER = Logger.getLogger(SettingsBindingService.class.getName());

   private final ServiceLoader<EnhancedMapBindingFactory> factories;
   private final ServiceLoader<SettingsFactoryValidator> validators;

   private SettingsBindingService() {
      factories = ServiceLoader.load(EnhancedMapBindingFactory.class);
      validators = ServiceLoader.load(SettingsFactoryValidator.class);
   }

   private static final SettingsBindingService instance = new SettingsBindingService();

   public static SettingsBindingService getInstance() {
      return instance;
   }

   /**
    * Return the first implementation of {@link EnhancedMapBindingFactory} found that is valid according to all
    * {@link SettingsFactoryValidator}s, or return null. When no validator is published return the first {@link EnhancedMapBindingFactory} found.
    *
    * @return
    */
   public EnhancedMapBindingFactory getFactory() {
      for (EnhancedMapBindingFactory f : factories) {
         boolean ok = true;
         boolean noValidatorFound = true;
         for (SettingsFactoryValidator validator : validators) {
            noValidatorFound = false;
            if (!validator.isValid(f)) {
               if (LOGGER.isLoggable(Level.FINE)) {
                  LOGGER.fine(String.format("%s does not pass validation by %s", f.getClass().getName(), validator.getClass().getName()));
               }
               ok = false;
               break;
            }
         }
         if (ok||noValidatorFound) return f;
      }
      return null;
   }

   /**
    * 
    * @return a list of factories found through SPI
    */
   public List<Class<? extends EnhancedMapBindingFactory>> getFactoriesKnown() {
      List<Class<? extends EnhancedMapBindingFactory>> l = new ArrayList<>();
      for (EnhancedMapBindingFactory f : factories) {
         l.add(f.getClass());
      }
      return l;
   }

   /**
    * 
    * @return a list of validators found through SPI
    */
   public List<Class<? extends SettingsFactoryValidator>> getValidatorsKnown() {
      List<Class<? extends SettingsFactoryValidator>> l = new ArrayList<>();
      for (SettingsFactoryValidator f : validators) {
         l.add(f.getClass());
      }
      return l;
   }
}
