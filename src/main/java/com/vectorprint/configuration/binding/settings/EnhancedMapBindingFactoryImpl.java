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
import com.vectorprint.configuration.parser.PropertiesParser;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This implementation uses two java system properties when this class is loaded to figure out
 * which parser class and which serializer class will be used. This way you have control over the syntax to use in
 * settings files.
 * @author Eduard Drenth at VectorPrint.nl
 */
public class EnhancedMapBindingFactoryImpl implements EnhancedMapBindingFactory {
   
   /**
    * The default parser returned by instances of this factory.
    */
   public static final Class<? extends EnhancedMapParser> DEFAULTPARSER = PropertiesParser.class;
   /**
    * name of the java property (-D) setting holding the classname of the parser to be used
    */
   public static final String PARSERCLASSNAME = EnhancedMapParser.class.getName();
   
   private static Class<? extends EnhancedMapParser> parserClass = DEFAULTPARSER;
   private static Constructor<? extends EnhancedMapParser> constructor;
   
   /**
    * The default serializer returned by instances of this factory.
    */
   public static final Class<? extends EnhancedMapSerializer> DEFAULTSERIALIZER = PropertiesParser.class;
   /**
    * name of the java property (-D) setting holding the classname of the serializer to be used
    */
   public static final String SERIALIZERCLASSNAME = EnhancedMapSerializer.class.getName();
   
   private static Class<? extends EnhancedMapSerializer> serializerClass = DEFAULTSERIALIZER;

   static {
      try {
         String className = System.getProperty(PARSERCLASSNAME);
         if (className!=null) {
            setParserClass(className);
         } else {
            constructor = parserClass.getConstructor(Reader.class);
         }
         className = System.getProperty(SERIALIZERCLASSNAME);
         if (className!=null) {
            setSerializerClass(className);
         }
      } catch (NoSuchMethodException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (SecurityException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (ClassNotFoundException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }
   
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
   
   public static void setParserClass(String className) throws ClassNotFoundException, NoSuchMethodException {
      setParserClass((Class<? extends EnhancedMapParser>) Class.forName(className));
   }
   
   public static void setParserClass(Class<? extends EnhancedMapParser> clazz) throws NoSuchMethodException {
      synchronized(parserClass) {
         constructor = clazz.getConstructor(Reader.class);
         parserClass = clazz;
      }
   }
   
   public static void setSerializerClass(String serializerClass) throws ClassNotFoundException {
      setSerializerClass((Class<? extends EnhancedMapSerializer>) Class.forName(serializerClass));
   }
   public static void setSerializerClass(Class<? extends EnhancedMapSerializer> serializerClass) {
      EnhancedMapBindingFactoryImpl.serializerClass = serializerClass;
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
