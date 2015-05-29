/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.configuration.parameters.annotation;

/*
 * #%L
 * VectorPrintConfig
 * %%
 * Copyright (C) 2011 - 2013 VectorPrint
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
import com.vectorprint.configuration.binding.parameters.ParameterizableBindingFactoryImpl;
import com.vectorprint.configuration.parameters.ParameterImpl;
import com.vectorprint.configuration.parameters.Parameterizable;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ParamAnnotationProcessorImpl implements ParamAnnotationProcessor {

   private static final Logger log = Logger.getLogger(ParamAnnotationProcessorImpl.class.getName());
   
   private final Set<Parameterizable> s = new HashSet<Parameterizable>(50);

   /**
    * looks for parameter annotations on each class in the hierarchy and adds a parameter to the parameterizable for
    * each annotation found. Skips parameters already present on the parameterizable. Call {@link ParameterImpl#setDeclaringClass(java.lang.Class)
    * } with the class that declared this parameter. This implementation assumes a two argument constructor like {@link ParameterImpl#ParameterImpl(java.lang.String, java.lang.String)
    * }.
    *
    * @param parameterizable
    * @throws NoSuchMethodException
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @throws InvocationTargetException
    */
   @Override
   public boolean initParameters(Parameterizable parameterizable) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
      if (s.contains(parameterizable)) {
         if (log.isLoggable(Level.FINE)) {
            log.fine(String.format("assuming, based on equals, parameterizable for %s already initialized", parameterizable));
         }
         return false;
      }
      Class c = parameterizable.getClass();
      while (Parameterizable.class.isAssignableFrom(c)) {
         if (log.isLoggable(Level.FINE)) {
            log.fine(String.format("looking for parameter annotations on %s", c.getName()));
         }
         process(c, parameterizable);
         c = c.getSuperclass();
      }
      s.add(parameterizable);
      return true;
   }

   private void process(Class<? extends Parameterizable> c, Parameterizable parameterizable) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
      Annotation annotation = c.getAnnotation(Parameters.class);
      if (annotation != null) {
         Parameters ps = (Parameters) annotation;
         for (Param p : ps.parameters()) {
            String key = p.key();
            if (parameterizable.getParameters().containsKey(key)) {
               if (log.isLoggable(Level.FINE)) {
                  log.fine(String.format("skipping parameter %s, already present on %s: %s", key, parameterizable.getClass().getName(), parameterizable.getParameters().get(key)));
               }
               continue;
            }
            String help = p.help();
            String def = (Param.NULL.equals(p.defaultValue())) ? null : p.defaultValue();
            String[] defArray = (Param.NULL.equals(p.defaultArray()[0])) ? null : p.defaultArray();
            Class<? extends ParameterImpl> pic = p.clazz();
            if (log.isLoggable(Level.FINE)) {
               log.fine(String.format("applying parameter %s with key %s and default %s on %s", pic.getName(), key, (def)==null?Arrays.toString(defArray):def, c.getName()));
            }
            Constructor con = pic.getConstructor(String.class, String.class);
            ParameterImpl pi = (ParameterImpl) con.newInstance(key, help);
            if (def != null) {
               pi.setDefault((Serializable) ParameterizableBindingFactoryImpl.getDefaultFactory().getBindingHelper().convert(def, pi.getValueClass()));
            } else if (defArray!=null) {
               pi.setDefault((Serializable) ParameterizableBindingFactoryImpl.getDefaultFactory().getBindingHelper().convert(defArray, pi.getValueClass()));
            }
            parameterizable.addParameter(pi,c);
         }
      }
   }
   
}
