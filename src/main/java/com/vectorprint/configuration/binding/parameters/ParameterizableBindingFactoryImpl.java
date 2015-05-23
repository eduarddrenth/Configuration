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
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ParameterizableBindingFactoryImpl implements ParameterizableBindingFactory {

   private BindingHelper bindingHelper;

   private ParameterizableBindingFactoryImpl() {
   }

   private static final Map<CacheKey, ParameterizableBindingFactoryImpl> cache = new HashMap<CacheKey, ParameterizableBindingFactoryImpl>(2);

   /**
    * initializes parser, serializer and bindingHelper.
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

   /**
    * The default parser returned by instances of this factory.
    */
   public static final Class<? extends ParameterizableParser> DEFAULTPARSER = ParameterizableParserImpl.class;
   private Class<? extends ParameterizableParser> parserClass = DEFAULTPARSER;
   private Constructor<? extends ParameterizableParser> constructor;

   /**
    * The default serializer returned by instances of this factory.
    */
   public static final Class<? extends ParameterizableSerializer> DEFAULTSERIALIZER = ParameterizableParserImpl.class;

   private Class<? extends ParameterizableSerializer> serializerClass = DEFAULTSERIALIZER;

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

   private static class CacheKey {

      final Class a, b;

      public CacheKey(Class a, Class b) {
         this.a = a;
         this.b = b;
      }

      @Override
      public int hashCode() {
         int hash = 5;
         return hash;
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == null) {
            return false;
         }
         if (getClass() != obj.getClass()) {
            return false;
         }
         final CacheKey other = (CacheKey) obj;
         if (this.a != other.a && (this.a == null || !this.a.equals(other.a))) {
            return false;
         }
         if (this.b != other.b && (this.b == null || !this.b.equals(other.b))) {
            return false;
         }
         return true;
      }

   }
}
