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
import com.vectorprint.configuration.Configurable;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessor;
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessorImpl;
import com.vectorprint.configuration.parameters.Parameterizable;
import com.vectorprint.configuration.parameters.ParameterizableImpl;
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessor;
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessorImpl;
import com.vectorprint.configuration.binding.StringConversion;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

/**
 * Syntax independent base class for Parsers.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public abstract class AbstractParameterizableParser<T> implements ParameterizableParser<T>, ParameterizableSerializer {

   private boolean printOnlyNonDefault = false;
   private static final Logger logger = Logger.getLogger(AbstractParameterizableParser.class.getName());
   private String packageName;
   private EnhancedMap settings;
   private StringConversion stringConversion = StringConversion.getStringConversion();

   @Override
   public ParameterizableSerializer setPrintOnlyNonDefault(boolean printOnlyNonDefault) {
      this.printOnlyNonDefault = printOnlyNonDefault;
      return this;
   }

   public final void checkKey(Parameterizable st, String key) {
      if (!st.getParameters().containsKey(key)) {
         throw new VectorPrintRuntimeException(String.format("wrong paramter(s) for %s: %s; parameters allowed are: %s",
             st.getClass().getSimpleName(), key, st.getParameters()));
      }
   }

   /**
    * Calls {@link SettingsAnnotationProcessor#initSettings(java.lang.Object, com.vectorprint.configuration.EnhancedMap) },
    * {@link ParamAnnotationProcessor#initParameters(com.vectorprint.configuration.parameters.Parameterizable) } and if
    * applicable {@link Configurable#initSettings(java.util.Map) }
    *
    * @param settings the value of settings
    */
   @Override
   public void initParameterizable(Parameterizable parameterizable) {
      SettingsAnnotationProcessorImpl.SAP.initSettings(parameterizable, settings);
      if (!(parameterizable instanceof ParameterizableImpl)) {
         try {
            ParamAnnotationProcessorImpl.PAP.initParameters(parameterizable);
         } catch (NoSuchMethodException ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (InstantiationException ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (IllegalAccessException ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (InvocationTargetException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
      }
      if (parameterizable instanceof Configurable) {
         ((Configurable) parameterizable).initSettings(settings);
      }
   }

   @Override
   public boolean getPrintOnlyNonDefault() {
      return printOnlyNonDefault;
   }

   @Override
   public EnhancedMap getSettings() {
      return settings;
   }

   @Override
   public ParameterizableParser setSettings(EnhancedMap settings) {
      this.settings = settings;
      return this;
   }

   @Override
   public StringConversion getStringConversion() {
      return stringConversion;
   }

   @Override
   public ParameterizableParser setStringConversion(StringConversion stringConversion) {
      this.stringConversion = stringConversion;
      return this;
   }

   @Override
   public String getPackageName() {
      return packageName;
   }

   @Override
   public ParameterizableParser setPackageName(String packageName) {
      this.packageName = packageName;
      return this;
   }

}
