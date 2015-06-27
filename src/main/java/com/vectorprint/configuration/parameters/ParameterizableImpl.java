/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.configuration.parameters;

/*
 * #%L
 * VectorPrintConfig
 * %%
 * Copyright (C) 2011 - 2014 VectorPrint
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
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessor;
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessorImpl;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.logging.Logger;

/**
 * This implementation contains static {@link EnhancedMap settings}, which will be initialized in {@link ParameterizableParser#parseParameterizable() } and used in {@link #addParameter(com.vectorprint.configuration.parameters.Parameter, java.lang.Class) }.
 * If 
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ParameterizableImpl implements Parameterizable {

   public static final ParamAnnotationProcessor paramProcessor = new ParamAnnotationProcessorImpl();
   protected static final Logger logger = Logger.getLogger(ParameterizableImpl.class.getName());
   private final SettingsAnnotationProcessor sap = new SettingsAnnotationProcessorImpl();

   /**
    * will call {@link ParamAnnotationProcessor#initParameters(com.vectorprint.configuration.parameters.Parameterizable)
    * } and {@link SettingsAnnotationProcessor#initSettings(java.lang.Object, com.vectorprint.configuration.EnhancedMap) }.
    */
   public ParameterizableImpl() {
      sap.initSettings(this, settings);
      try {
         paramProcessor.initParameters(this);
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
   
   @SettingsField
   private static EnhancedMap settings = new CachingProperties(new Settings(0));

   /**
    * Adds the parameter to this Parameterizable and registers this Parameterizable with the Parameter as Observer.
    * Calls {@link SettingsAnnotationProcessor#initSettings(java.lang.Object, com.vectorprint.configuration.EnhancedMap) }
    * on the parameter class and the parameter object. Sets the {@link Parameter#getDeclaringClass() declaring class}
    * @param parameter
    */
   @Override
   public void addParameter(Parameter parameter, Class<? extends Parameterizable> declaringClass) {
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

   private final Map<String, Object> cache = new HashMap<String, Object>(10);

   @Override
   public <TYPE extends Serializable> TYPE getValue(String key, Class<TYPE> T) {
      if (!cache.containsKey(key)) {
         cache.put(key, parameters.get(key).getValue());
      }
      return (TYPE) cache.get(key);
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
   public Parameterizable clone() {
      try {
         Constructor con = getClass().getConstructor();
         ParameterizableImpl pi = (ParameterizableImpl) con.newInstance();
         paramProcessor.initParameters(pi);
         for (Parameter p : pi.parameters.values()) {
            p.setValue(getParameters().get(p.getKey()).getValue());
         }
         return pi;
      } catch (NoSuchMethodException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (SecurityException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (InstantiationException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (IllegalAccessException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (IllegalArgumentException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (InvocationTargetException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   @Override
   public boolean isParameterSet(String key) {
      return parameters.containsKey(key) && parameters.get(key).getValue() != null;
   }

   /**
    * When the argument Observable is a Parameter, this method clears the cache for the parameter key. Calls 
    * {@link #parameterChanged(com.vectorprint.configuration.parameters.Parameter) }.
    *
    * @param o
    * @param arg
    */
   @Override
   public final void update(Observable o, Object arg) {
      if (o instanceof Parameter) {
         Parameter p = (Parameter) o;
         cache.remove(p.getKey());
         parameterChanged(p);
      }
   }
   
   /**
    * Called when a Parameter changed (value or default changed), does nothing
    * @param o 
    */
   protected void parameterChanged(Parameter o) {
      
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 79 * hash + (this.parameters != null ? this.parameters.hashCode() : 0);
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
      if (this.parameters != other.parameters && (this.parameters == null || !this.parameters.equals(other.parameters))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return "ParameterizableImpl{" + "parameters=" + parameters + '}';
   }

   public EnhancedMap getSettings() {
      return settings;
   }

   public static void clearStaticSettings() {
      settings = null;
   }
   
}
