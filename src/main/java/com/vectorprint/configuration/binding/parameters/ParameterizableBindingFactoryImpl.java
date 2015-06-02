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
import com.vectorprint.configuration.parser.ParameterizableParserImpl;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This implementation gives you full control over the syntax used to get to and from Parameterizables.
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ParameterizableBindingFactoryImpl implements ParameterizableBindingFactory {
   /**
    * name of the system property (java -D...) in which you specify a parser Class
    */
   public static final String PARAMPARSER = "paramparser";
   /**
    * name of the system property (java -D...) in which you specify a serializer Class
    */
   public static final String PARAMSERIALIZER = "paramserializer";
   /**
    * name of the system property (java -D...) in which you specify a helper Class
    */
   public static final String PARAMHELPER = "paramhelper";
   public static final Class<? extends ParameterizableParser> PARAMPARSERCLASS = ParameterizableParserImpl.class;
   public static final Class<? extends ParameterizableSerializer> PARAMSERIALIZERCLASS = ParameterizableParserImpl.class;
   public static final Class<? extends ParamBindingHelper> PARAMHELPERCLASS = EscapingBindingHelper.class;

   private ParamBindingHelper bindingHelper;

   /**
    * return a Class found in the system property argument or the Class argument
    * @param <T>
    * @param systemProperty
    * @param clazz
    * @return
    * @throws ClassNotFoundException 
    */
   private static <T> Class<T> findClass(String systemProperty, Class<T> clazz) throws ClassNotFoundException {
      if (System.getProperty(systemProperty) != null) {
         return (Class<T>) Class.forName(System.getProperty(systemProperty));
      } else {
         return clazz;
      }
   }


   private static ParameterizableBindingFactory factory = null;

   static {
      try {
         getFactory(findClass(PARAMPARSER, PARAMPARSERCLASS),
             findClass(PARAMSERIALIZER, PARAMSERIALIZERCLASS),
             findClass(PARAMHELPER, PARAMHELPERCLASS).newInstance(), true);
      } catch (ClassNotFoundException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (InstantiationException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (IllegalAccessException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   private ParameterizableBindingFactoryImpl() {
   }

   /**
    * initializes parser class, serializer class and bindingHelper and optionally the default factory.
    *
    * @param parserClass
    * @param serializerClass
    * @param bindingHelper the value of bindingHelper will be used in the return factory
    * @param setAsDefault the value of setAsDefault
    * @return the com.vectorprint.configuration.binding.parameters.ParameterizableBindingFactory
    */
   public static ParameterizableBindingFactory getFactory(Class<? extends ParameterizableParser> parserClass, Class<? extends ParameterizableSerializer> serializerClass, ParamBindingHelper bindingHelper, boolean setAsDefault) {
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
      if (setAsDefault) {
         ParameterizableBindingFactoryImpl.factory=factory;
      }
      factory.bindingHelper = bindingHelper;
      return factory;
   }

   /**
    * return the factory last requested by {@link #getFactory(java.lang.Class, java.lang.Class, com.vectorprint.configuration.binding.parameters.ParamBindingHelper, boolean) }
    * with true for setAsDefault. Never returns null, see static finals in this class for initial factory classes.
    * @return 
    */
   public static ParameterizableBindingFactory getDefaultFactory() {
      return factory;
   }

   private Class<? extends ParameterizableParser> parserClass;
   private Constructor<? extends ParameterizableParser> constructor;

   private Class<? extends ParameterizableSerializer> serializerClass;

   /**
    * instantiate parser, call {@link ParameterizableParser#setBindingHelper(com.vectorprint.configuration.binding.parameters.ParamBindingHelper) } and return the parser.
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
    * instantiate serializer, call {@link ParameterizableSerializer#setBindingHelper(com.vectorprint.configuration.binding.parameters.ParamBindingHelper)  } and return the serializer.
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
   public ParamBindingHelper getBindingHelper() {
      return bindingHelper;
   }

   @Override
   public String toString() {
      return "ParameterizableBindingFactoryImpl{" + "bindingHelper=" + bindingHelper.getClass() + ", parserClass=" + parserClass + ", serializerClass=" + serializerClass + '}';
   }

}
