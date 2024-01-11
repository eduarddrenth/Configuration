
package com.vectorprint.configuration.parameters;

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
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.Settings;
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessor;
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessorImpl;
import com.vectorprint.configuration.annotation.SettingsField;
import com.vectorprint.configuration.binding.parameters.ParameterizableParser;
import com.vectorprint.configuration.decoration.CachingProperties;
import com.vectorprint.configuration.generated.parser.ParameterizableParserImpl;
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessor;
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ParameterizableImpl implements Parameterizable {

   public static final ParamAnnotationProcessor paramProcessor = new ParamAnnotationProcessorImpl();
   protected static final Logger logger = LoggerFactory.getLogger(ParameterizableImpl.class.getName());
   private static final SettingsAnnotationProcessor sap = new SettingsAnnotationProcessorImpl();

   private final Map<String, Parameter> parameters = new HashMap<String, Parameter>(5) {
      @Override
      public Parameter remove(Object key) {
         return null;
      }

      @Override
      public Parameter put(String key, Parameter value) {
         if (containsKey(key)) {
            throw new VectorPrintRuntimeException(String.format("parameter already known %s: %s", key, get(key)));
         }
         return super.put(key, value);
      }

      @Override
      public void clear() {
      }
   };

   /**
    * will be initialized from parsing {@link ParameterizableParserImpl}
    */
   @SettingsField
   private EnhancedMap settings = new CachingProperties(new Settings(0));

   /**
    * Adds the parameter to this Parameterizable and registers this Parameterizable with the Parameter as Observer.
    * Calls {@link SettingsAnnotationProcessor#initSettings(java.lang.Object, com.vectorprint.configuration.EnhancedMap)
    * }
    * on the parameter class and the parameter object. Sets the {@link Parameter#getDeclaringClass() declaring class}
    *
    * @param parameter
    */
   @Override
   public final void addParameter(Parameter parameter, Class<? extends Parameterizable> declaringClass) {
      sap.initSettings(parameter.getClass(), settings);
      sap.initSettings(parameter, settings);
      parameters.put(parameter.getKey(), parameter);
      parameter.addObserver(this);
      if (parameter instanceof ParameterImpl) {
         ((ParameterImpl) parameter).setDeclaringClass(declaringClass);
      }
   }

   @Override
   public <TYPE extends Serializable> Parameter<TYPE> getParameter(String key, Class<TYPE> T) {
      return parameters.get(key);
   }

   @Override
   public Map<String, Parameter> getParameters() {
      return parameters;
   }

   @Override
   public <TYPE extends Serializable> TYPE getValue(String key, Class<TYPE> T) {
      if (!parameters.containsKey(key)) {
         throw new VectorPrintRuntimeException(String.format("parameter %s not found in %s", key, getClass()));
      }
      return (TYPE) parameters.get(key).getValue();
   }

   @Override
   public <TYPE extends Serializable> void setValue(String key, TYPE value) {
      parameters.get(key).setValue(value);
   }

   /**
    *
    * @return
    */
   @Override
   public Parameterizable clone() throws CloneNotSupportedException {
       Parameterizable parameterizable = (Parameterizable) super.clone();
       try {
         Constructor con = getClass().getConstructor();
         ParameterizableImpl pi = (ParameterizableImpl) con.newInstance();
         paramProcessor.initParameters(pi);
         pi.parameters.values().forEach((p) -> p.setValue(getParameters().get(p.getKey()).getValue()));
         return pi;
      } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   @Override
   public boolean isParameterSet(String key) {
      return parameters.containsKey(key) && parameters.get(key).getValue() != null;
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 79 * hash + this.parameters.hashCode();
      return hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (obj == this) {
         return true;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final ParameterizableImpl other = (ParameterizableImpl) obj;
      return !(this.parameters != other.parameters && !this.parameters.equals(other.parameters));
   }

   @Override
   public String toString() {
      return getClass().getSimpleName() + "{" + "parameters=" + parameters + '}';
   }

   /**
    * Returns settings for this Parameterizable.
    *
    * @return
    */
   public EnhancedMap getSettings() {
      return settings;
   }

   /**
    * Calls {@link SettingsAnnotationProcessor#initSettings(java.lang.Object, com.vectorprint.configuration.EnhancedMap)
    * } (only when settings argument is not null) and
    * {@link ParamAnnotationProcessor#initParameters(com.vectorprint.configuration.parameters.Parameterizable) }. This
    * method is meant to be called when this object is not the result of parsing, see {@link ParameterizableParser#parseParameterizable()
    * }.
    *
    * @param settings
    * @throws NoSuchMethodException
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @throws InvocationTargetException
    */
   public void initialize(EnhancedMap settings) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
      if (settings != null) {
         this.settings = settings;
         sap.initSettings(this, settings);
      }
      paramProcessor.initParameters(this);
   }

   @Override
   public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

   }
}
