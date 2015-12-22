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
package com.vectorprint.configuration.binding.parameters.json;

import com.vectorprint.configuration.binding.parameters.*;
import com.vectorprint.VectorPrintRuntimeException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This implementation uses Json syntax for parameters.
 * @see ParamBindingService
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ParameterizableBindingFactoryJson implements ParameterizableBindingFactory {

   private static final ParamBindingHelper bindingHelper = new JSONBindingHelper();
   private static Constructor<? extends ParameterizableParser> constructor;
   private static final Class<? extends ParameterizableSerializer> serializerClass = JSONSupport.class;

   static {
      try {
         constructor = JSONSupport.class.getConstructor(Reader.class);
      } catch (NoSuchMethodException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (SecurityException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }


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
         ParameterizableSerializer ps = serializerClass.newInstance();
         ps.setBindingHelper(bindingHelper);
         return ps;
      } catch (InstantiationException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (IllegalAccessException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (IllegalArgumentException ex) {
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
      return "ParameterizableBindingFactoryJson{" + "bindingHelper=" + bindingHelper + ", parserClass=" + serializerClass + ", serializerClass=" + serializerClass + '}';
   }


}
