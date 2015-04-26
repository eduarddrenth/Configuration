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

package com.vectorprint.configuration.parameters.parsing;

import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.parser.ObjectParser;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class DefaultParserFactory implements ParserFactory {
   
   /**
    * The default parser returned by instances of this factory.
    */
   public static final Class<? extends Parser> DEFAULTPARSER = ObjectParser.class;
   /**
    * name of the java property (-D) setting holding the classname of the parser to be used
    */
   public static final String PARSERCLASSNAME = Parser.class.getName();
   
   private static Class<? extends Parser> parserClass = DEFAULTPARSER;
   private static Constructor<? extends Parser> constructor;
   
   static {
      try {
         String className = System.getProperty(PARSERCLASSNAME);
         if (className!=null) {
            setParserClass(className);
         } else {
            constructor = parserClass.getConstructor(Reader.class);
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
   public Parser getParser(Reader input) {
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
      setParserClass((Class<? extends Parser>) Class.forName(className));
   }
   
   public static void setParserClass(Class<? extends Parser> clazz) throws NoSuchMethodException {
      synchronized(parserClass) {
         Constructor<? extends Parser> constructor = clazz.getConstructor(Reader.class);
         parserClass = clazz;
      }
   }
   

}
