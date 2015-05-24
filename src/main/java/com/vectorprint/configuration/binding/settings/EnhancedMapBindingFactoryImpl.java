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
import com.vectorprint.configuration.binding.BindingHelper;
import com.vectorprint.configuration.parser.PropertiesParser;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * This implementation gives you full control over the syntax to use in settings files.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class EnhancedMapBindingFactoryImpl implements EnhancedMapBindingFactory {

   private Class<? extends EnhancedMapParser> parserClass;
   private Constructor<? extends EnhancedMapParser> constructor;

   private EnhancedMapBindingFactoryImpl() {
   }

   private static final Map<CacheKey, EnhancedMapBindingFactory> cache = new HashMap<CacheKey, EnhancedMapBindingFactory>(2);
   private static EnhancedMapBindingFactory factory;

   /**
    * initializes parser class, serializer class and BindinHelper.
    * The next call to {@link #getFactory() } will return the same factory.
    * @param parserClass
    * @param serializerClass
    * @return 
    */
   public static synchronized EnhancedMapBindingFactory getFactory(Class<? extends EnhancedMapParser> parserClass,
       Class<? extends EnhancedMapSerializer> serializerClass,
       BindingHelper bindingHelper) {
      CacheKey ck = new CacheKey(parserClass, serializerClass);
      if (cache.containsKey(ck)) {
         return cache.get(ck);
      } else {
         try {
            EnhancedMapBindingFactoryImpl factory = new EnhancedMapBindingFactoryImpl();
            factory.parserClass = parserClass;
            factory.constructor = parserClass.getConstructor(Reader.class);
            factory.serializerClass = serializerClass;
            factory.bindingHelper = bindingHelper;
            cache.put(ck, factory);
            EnhancedMapBindingFactoryImpl.factory = factory;
            return factory;
         } catch (NoSuchMethodException ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (SecurityException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
      }
   }

   /**
    * return the factory last requested by {@link #getFactory(java.lang.Class, java.lang.Class) }
    * @return 
    */
   public static EnhancedMapBindingFactory getFactory() {
      return factory;
   }
   
   private BindingHelper bindingHelper;

   @Override
   public BindingHelper getBindingHelper() {
      return bindingHelper;
   }

   private Class<? extends EnhancedMapSerializer> serializerClass;

   @Override
   public EnhancedMapParser getParser(Reader input) {
      try {
         return constructor.newInstance(input);
      } catch (InstantiationException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (IllegalAccessException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (IllegalArgumentException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (InvocationTargetException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (SecurityException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   private static final Reader r = new StringReader("");

   @Override
   public EnhancedMapSerializer getSerializer() {
      try {
         if (PropertiesParser.class.equals(serializerClass) && PropertiesParser.class.equals(parserClass)) {
            return (EnhancedMapSerializer) constructor.newInstance(r);
         } else {
            return serializerClass.newInstance();
         }
      } catch (InstantiationException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (IllegalAccessException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (IllegalArgumentException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (InvocationTargetException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (SecurityException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

}
