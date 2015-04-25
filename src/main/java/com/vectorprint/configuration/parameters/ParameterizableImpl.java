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
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessorImpl;
import com.vectorprint.configuration.annotation.SettingsField;
import static com.vectorprint.configuration.parameters.ParameterHelper.findDefaultKey;
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessor;
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessorImpl;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ParameterizableImpl implements Parameterizable {

   public static final ParamAnnotationProcessor paramProcessor = new ParamAnnotationProcessorImpl();
   private static final Logger logger = Logger.getLogger(ParameterizableImpl.class.getName());

   /**
    * will call {@link ParamAnnotationProcessor#initParameters(com.vectorprint.configuration.parameters.Parameterizable)
    * }
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
   
   @SettingsField
   private static EnhancedMap settings;

   /**
    * Adds the parameter to this Parameterizable and registers this Parameterizable with the Parameter as Observer.
    *
    * @param parameter
    */
   @Override
   public void addParameter(Parameter parameter, Class<? extends Parameterizable> declaringClass) {
      SettingsAnnotationProcessorImpl.SAP.initSettings(parameter.getClass(), settings);
      SettingsAnnotationProcessorImpl.SAP.initSettings(parameter, settings);
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
    * @param settings the value of settings
    */
   @Override
   public void initDefaults(EnhancedMap settings) {
      if (settings != null) {
         for (Parameter parameter : parameters.values()) {
            String key = findDefaultKey(parameter.getKey(), getClass(), settings);
            if (key != null) {
               if (logger.isLoggable(Level.FINE)) {
                  logger.fine(String.format("found default %s for key %s and class %s", key, parameter.getKey(), getClass().getName()));
               }
               parameter.setDefault(parameter.convert(settings.get(key)));
            }
         }
      }
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
    * When the argument Observable is a Parameter, this method clears the cache for the parameter key. Overriders must call this method to
    * keep cache up to date.
    *
    * @param o
    * @param arg
    */
   @Override
   public void update(Observable o, Object arg) {
      if (o instanceof Parameter) {
         Parameter p = (Parameter) o;
         cache.remove(p.getKey());
      }
   }

}
