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
import com.vectorprint.configuration.parameters.ParameterImpl;
import com.vectorprint.configuration.parameters.Parameterizable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ParamAnnotationProcessorImpl implements ParamAnnotationProcessor {

   private static final Logger log = Logger.getLogger(ParamAnnotationProcessorImpl.class.getName());
   /**
    * you can safely use this, also from different threads
    */
   public static final ParamAnnotationProcessor PAP = new ParamAnnotationProcessorImpl();

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
   public void initParameters(Parameterizable parameterizable) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
      Class c = parameterizable.getClass();
      while (Parameterizable.class.isAssignableFrom(c)) {
         if (log.isLoggable(Level.FINE)) {
            log.fine(String.format("looking for parameter annotations on %s", c.getName()));
         }
         process(c, parameterizable);
         c = c.getSuperclass();
      }
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
            Class<? extends ParameterImpl> pic = p.clazz();
            if (log.isLoggable(Level.FINE)) {
               log.fine(String.format("applying parameter %s with key %s and default %s on %s", pic.getName(), key, def, c.getName()));
            }
            Constructor con = pic.getConstructor(String.class, String.class);
            ParameterImpl pi = (ParameterImpl) con.newInstance(key, help);
            if (def != null) {
               pi.setDefault(pi.unMarshall(def));
            }
            parameterizable.addParameter(pi,c);
         }
      }
   }

}
