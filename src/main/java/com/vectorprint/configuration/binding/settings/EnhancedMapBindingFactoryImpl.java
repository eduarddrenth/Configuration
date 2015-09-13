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
import com.vectorprint.configuration.binding.BindingHelperImpl;
import com.vectorprint.configuration.generated.parser.PropertiesParser;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This implementation gives you full control over the syntax to use in settings files.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class EnhancedMapBindingFactoryImpl implements EnhancedMapBindingFactory {

   /**
    * name of the system property (java -D...) in which you specify a parser Class
    */
   public static final String SETTINGSPARSER = "settingsparser";
   /**
    * name of the system property (java -D...) in which you specify a serializer Class
    */
   public static final String SETTINGSSERIALIZER = "settingsserializer";
   /**
    * name of the system property (java -D...) in which you specify a helper Class
    */
   public static final String SETTINGSHELPER = "settingshelper";
   public static final Class<? extends EnhancedMapParser> SETTINGSPARSERCLASS = PropertiesParser.class;
   public static final Class<? extends EnhancedMapSerializer> SETTINGSSERIALIZERCLASS = PropertiesParser.class;
   public static final Class<? extends BindingHelper> SETTINGSHELPERCLASS = BindingHelperImpl.class;

   private static <T> Class<T> findClass(String systemProperty, Class<T> clazz) throws ClassNotFoundException {
      if (System.getProperty(systemProperty) != null) {
         return (Class<T>) Class.forName(System.getProperty(systemProperty));
      } else {
         return clazz;
      }
   }

   private Class<? extends EnhancedMapParser> parserClass;
   private Constructor<? extends EnhancedMapParser> constructor;

   private EnhancedMapBindingFactoryImpl() {
   }

   private static EnhancedMapBindingFactory factory;

   static {
      try {
         EnhancedMapBindingFactoryImpl.getFactory(findClass(SETTINGSPARSER, SETTINGSPARSERCLASS),
             findClass(SETTINGSSERIALIZER, SETTINGSSERIALIZERCLASS),
             findClass(SETTINGSHELPER, SETTINGSHELPERCLASS).newInstance(), true);
      } catch (ClassNotFoundException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (InstantiationException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (IllegalAccessException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   /**
    * initializes parser class, serializer class, BindinHelper and optionally the default factory
    *
    * @param parserClass
    * @param serializerClass
    * @param bindingHelper the value of bindingHelper
    * @param setAsDefault when true {@link #getDefaultFactory() the default factory} will be set to the requested factory
    * @return the com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactory
    */
   public static EnhancedMapBindingFactory getFactory(Class<? extends EnhancedMapParser> parserClass, Class<? extends EnhancedMapSerializer> serializerClass, BindingHelper bindingHelper, boolean setAsDefault) {
      try {
         EnhancedMapBindingFactoryImpl factory = new EnhancedMapBindingFactoryImpl();
         factory.parserClass = parserClass;
         factory.constructor = parserClass.getConstructor(Reader.class);
         factory.serializerClass = serializerClass;
         if (!PropertiesParser.class.equals(serializerClass)) {
            // check no arg constructor
            serializerClass.getConstructor();
         }
         factory.bindingHelper = bindingHelper;
         if (setAsDefault) {
            synchronized (EnhancedMapBindingFactoryImpl.class) {
               EnhancedMapBindingFactoryImpl.factory = factory;
            }
         }
         return factory;
      } catch (NoSuchMethodException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (SecurityException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   /**
    * return the factory last requested by {@link #getFactory(java.lang.Class, java.lang.Class, com.vectorprint.configuration.binding.BindingHelper, boolean)  }
    * with true for setAsDefault. Never returns null, see static finals in this class for initial factory classes.
    *
    * @return
    */
   public static EnhancedMapBindingFactory getDefaultFactory() {
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

   @Override
   public String toString() {
      return "EnhancedMapBindingFactoryImpl{" + "parserClass=" + parserClass + ", bindingHelper=" + bindingHelper.getClass() + ", serializerClass=" + serializerClass + '}';
   }

}
