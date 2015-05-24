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

import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.binding.BindingHelper;
import com.vectorprint.configuration.parser.ParameterizableParserImpl;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * This implementation gives you full control over the syntax used to get to and from Parameterizables.
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ParameterizableBindingFactoryImpl implements ParameterizableBindingFactory {

   private BindingHelper bindingHelper;

   private ParameterizableBindingFactoryImpl() {
   }

   private static final Map<CacheKey, ParameterizableBindingFactoryImpl> cache = new HashMap<CacheKey, ParameterizableBindingFactoryImpl>(2);

   /**
    * initializes parser class, serializer class and bindingHelper.
    * The next call to {@link #getFactory() } will return the same factory.
    *
    * @param parserClass
    * @param serializerClass
    * @return
    */
   public static synchronized ParameterizableBindingFactory getFactory(Class<? extends ParameterizableParser> parserClass,
       Class<? extends ParameterizableSerializer> serializerClass, BindingHelper bindingHelper) {
      CacheKey ck = new CacheKey(parserClass, parserClass);
      if (cache.containsKey(ck)) {
         ParameterizableBindingFactoryImpl.factory=cache.get(ck);
         cache.get(ck).bindingHelper = bindingHelper;
         return cache.get(ck);
      } else {
         ParameterizableBindingFactoryImpl factory = new ParameterizableBindingFactoryImpl();
         try {
            factory.parserClass = parserClass;
            factory.constructor = parserClass.getConstructor(Reader.class);
            factory.serializerClass = serializerClass;
            if (!ParameterizableParserImpl.class.equals(serializerClass)) {
               // check no arg constructor
               serializerClass.getConstructor();
            }
         } catch (NoSuchMethodException ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (SecurityException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
         ParameterizableBindingFactoryImpl.factory=factory;
         factory.bindingHelper = bindingHelper;
         cache.put(ck, factory);
         return factory;
      }
   }

   /**
    * return the factory last requested by {@link #getFactory(java.lang.Class, java.lang.Class, com.vectorprint.configuration.binding.BindingHelper) }
    * @return 
    */
   public static ParameterizableBindingFactory getFactory() {
      return factory;
   }

   private Class<? extends ParameterizableParser> parserClass;
   private Constructor<? extends ParameterizableParser> constructor;

   private Class<? extends ParameterizableSerializer> serializerClass;

   private static ParameterizableBindingFactory factory = null;

   /**
    * instantiate parser, call {@link ParameterizableParser#setBindingHelper(com.vectorprint.configuration.binding.BindingHelper) } and return the parser.
    * @param input
    * @return 
    */
   @Override
   public ParameterizableParser getParser(Reader input) {
      try {
         ParameterizableParser newInstance = constructor.newInstance(input);
         newInstance.setBindingHelper(bindingHelper);
         return newInstance;
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

   /**
    * instantiate serializer, call {@link ParameterizableSerializer#setBindingHelper(com.vectorprint.configuration.binding.BindingHelper) } and return the serializer.
    * @return 
    */
   @Override
   public ParameterizableSerializer getSerializer() {
      try {
         ParameterizableSerializer ps = null;
         if (ParameterizableParserImpl.class.equals(serializerClass) && ParameterizableParserImpl.class.equals(parserClass)) {
            ps = (ParameterizableSerializer) constructor.newInstance(r);
         } else {
            ps = serializerClass.newInstance();
         }
         ps.setBindingHelper(bindingHelper);
         return ps;
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

   @Override
   public BindingHelper getBindingHelper() {
      return bindingHelper;
   }

}
