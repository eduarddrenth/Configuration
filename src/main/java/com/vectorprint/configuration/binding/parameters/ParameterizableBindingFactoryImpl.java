
package com.vectorprint.configuration.binding.parameters;

/*-
 * #%L
 * Config
 * %%
 * Copyright (C) 2015 - 2018 VectorPrint
 * %%
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
 * #L%
 */


import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.generated.parser.ParameterizableParserImpl;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ParameterizableBindingFactoryImpl implements ParameterizableBindingFactory {

   private static final ParamBindingHelper bindingHelper = new EscapingBindingHelper();
   private static Constructor<? extends ParameterizableParser> constructor;
   
   static {
      try {
         constructor = ParameterizableParserImpl.class.getConstructor(Reader.class);
      } catch (NoSuchMethodException | SecurityException ex) {
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
         newInstance.setBindingHelper(getBindingHelper());
         return newInstance;
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException ex) {
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
         ParameterizableSerializer ps = (ParameterizableSerializer) constructor.newInstance(r);
         ps.setBindingHelper(getBindingHelper());
         return ps;
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   @Override
   public ParamBindingHelper getBindingHelper() {
      return bindingHelper;
   }

   @Override
   public String toString() {
      return "ParameterizableBindingFactoryImpl{" + "bindingHelper=" + getBindingHelper().getClass() + ", parserClass=" + ParameterizableParserImpl.class + ", serializerClass=" + ParameterizableParserImpl.class + '}';
   }

}
