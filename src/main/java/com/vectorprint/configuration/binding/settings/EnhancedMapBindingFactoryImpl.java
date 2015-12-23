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

/*
 * #%L
 * Config
 * %%
 * Copyright (C) 2015 VectorPrint
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
import com.vectorprint.configuration.binding.BindingHelper;
import com.vectorprint.configuration.binding.BindingHelperImpl;
import com.vectorprint.configuration.generated.parser.PropertiesParser;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This implementation supports the fast built in syntax for settings.
 * @see SettingsBindingService
 * @author Eduard Drenth at VectorPrint.nl
 */
public class EnhancedMapBindingFactoryImpl implements EnhancedMapBindingFactory {

   private static final Class<? extends EnhancedMapParser> parserClass = PropertiesParser.class;
   private static Constructor<? extends EnhancedMapParser> constructor;
   private static final BindingHelper bindingHelper = new BindingHelperImpl();

   static {
      try {
         constructor = PropertiesParser.class.getConstructor(Reader.class);
      } catch (NoSuchMethodException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (SecurityException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }



   @Override
   public BindingHelper getBindingHelper() {
      return bindingHelper;
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

   private static final Reader r = new StringReader("");

   @Override
   public EnhancedMapSerializer getSerializer() {
      try {
         return (EnhancedMapSerializer) constructor.newInstance(r);
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
      return "EnhancedMapBindingFactoryImpl{" + "parserClass=" + parserClass + ", bindingHelper=" + getBindingHelper() + ", serializerClass=" + parserClass + '}';
   }
}
