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
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessor;
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessorImpl;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ParameterizableImpl implements Parameterizable {

   private static final ParamAnnotationProcessor paramProcessor = new ParamAnnotationProcessorImpl();

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

   /**
    * initialize a styler from defaults or arguments. Defaults are searched in {@link EnhancedMap properties} using the
    * concatenation of {@link Class#getSimpleName() }, a "." and the {@link #getParameterInfo() name of a setting} for
    * the styler. Subclasses of {@link AbstractStyler} will be searched starting with the actual class, ending with the
    * direct subclass of {@link AbstractStyler}.
    *
    * @param args
    */
   @Override
   public void setup(Map<String, String> args, Map<String, String> settings) {
      if (args == null) {
         args = new HashMap<String, String>(5);
      }
      ParameterHelper.setup(this, args, settings);
   }

   public static final ParamAnnotationProcessor processor = new ParamAnnotationProcessorImpl();

   @Override
   public Parameterizable clone() {
      try {
         Constructor con = getClass().getConstructor();
         ParameterizableImpl pi = (ParameterizableImpl) con.newInstance();
         processor.initParameters(pi);
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

}
