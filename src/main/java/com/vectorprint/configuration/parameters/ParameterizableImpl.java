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
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessor;
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessorImpl;
import com.vectorprint.configuration.annotation.SettingsField;
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessor;
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessorImpl;
import com.vectorprint.configuration.parser.ObjectParser;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.logging.Logger;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ParameterizableImpl implements Parameterizable {

   public static final ParamAnnotationProcessor paramProcessor = new ParamAnnotationProcessorImpl();
   private static final Logger logger = Logger.getLogger(ParameterizableImpl.class.getName());
   
   @SettingsField
   private static EnhancedMap settings;

   /**
    * will call {@link ParamAnnotationProcessor#initParameters(com.vectorprint.configuration.parameters.Parameterizable) }
    */
   public ParameterizableImpl() {
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

   private Map<String, Parameter> parameters = new HashMap<String, Parameter>(5) {
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
    * Adds the parameter to this Parameterizable and registers this Parameterizable with the
    * Parameter as Observer.
    * @param parameter 
    */
   @Override
   public void addParameter(Parameter parameter, Class<? extends Parameterizable> declaringClass) {
      parameters.put(parameter.getKey(), parameter);
      parameter.addObserver(this);
      if (parameter instanceof ParameterImpl) {
         ((ParameterImpl)parameter).setDeclaringClass(declaringClass);
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

   @Override
   public void setup(Map<String, String> args, EnhancedMap settings) {
      if (args == null) {
         args = new HashMap<String, String>(5);
      }
      if (ParameterizableImpl.settings != null) {
         for (Parameter parameter : parameters.values()) {
            SettingsAnnotationProcessorImpl.SAP.initStaticSettings(parameter.getClass(), ParameterizableImpl.settings);
            SettingsAnnotationProcessorImpl.SAP.initSettings(parameter, ParameterizableImpl.settings);
         }
      } else {
         logger.warning("static settings not initialized");
      }
      ParameterHelper.setup(this, args, settings);
   }

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
    * When the argument Observable is a Parameter, this method updates the cache. Overriders must
    * call this method to keep cache up to date.
    * @param o
    * @param arg 
    */
   @Override
   public void update(Observable o, Object arg) {
      if (o instanceof Parameter) {
         Parameter p = (Parameter) o;
         cache.put(p.getKey(), p.getValue());
      }
   }

   /**
    * Called by {@link ObjectParser}, settings will be used in {@link #setup(java.util.Map, java.util.Map) } to initialize settings
    * for parameters.
    * @param settings 
    */
   public static void setSettings(EnhancedMap settings) {
      ParameterizableImpl.settings = settings;
   }

}
