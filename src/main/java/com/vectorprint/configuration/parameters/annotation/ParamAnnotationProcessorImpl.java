
package com.vectorprint.configuration.parameters.annotation;

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

import com.vectorprint.configuration.binding.parameters.ParamBindingService;
import com.vectorprint.configuration.parameters.ParameterImpl;
import com.vectorprint.configuration.parameters.Parameterizable;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParamAnnotationProcessorImpl implements ParamAnnotationProcessor {

   private static final Logger log = LoggerFactory.getLogger(ParamAnnotationProcessorImpl.class.getName());

   /**
    * Looks for parameter annotations on each class in the hierarchy and
    * {@link Parameterizable#addParameter(com.vectorprint.configuration.parameters.Parameter, java.lang.Class) adds a parameter to the parameterizable}
    * for each annotation found. Skips parameters already present on the parameterizable. This implementation assumes a
    * two argument constructor like {@link ParameterImpl#ParameterImpl(String, String, Class)}.
    *
    * @param parameterizable
    * @throws NoSuchMethodException
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @throws InvocationTargetException
    */
   @Override
   public boolean initParameters(Parameterizable parameterizable) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
      if (parameterizable.getSettings()==null||parameterizable.getSettings().isEmpty()) {
         log.warn(String.format("Initializing parameters of %s without settings, you may want to initialize settings of %s before initializing parameters",
             parameterizable.getClass().getName(),parameterizable.getClass().getName()));
      }
      Class c = parameterizable.getClass();
      while (Parameterizable.class.isAssignableFrom(c)) {
         if (log.isDebugEnabled()) {
            log.debug(String.format("looking for parameter annotations on %s", c.getName()));
         }
         process(c, parameterizable);
         c = c.getSuperclass();
      }
      return true;
   }

   private void process(Class<? extends Parameterizable> c, Parameterizable parameterizable) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
      Parameters annotation = c.getAnnotation(Parameters.class);
      if (annotation != null) {
         Parameters ps = annotation;
         for (Param p : ps.parameters()) {
            String key = p.key();
            if (parameterizable.getParameters().containsKey(key)) {
               if (log.isDebugEnabled()) {
                  log.debug(String.format("skipping parameter %s, already present on %s: %s", key, parameterizable.getClass().getName(), parameterizable.getParameters().get(key)));
               }
               continue;
            }
            String help = p.help();
            String def = (Param.NULL.equals(p.defaultValue())) ? null : p.defaultValue();
            String[] defArray = (Param.NULL.equals(p.defaultArray()[0])) ? null : p.defaultArray();
            Class<? extends ParameterImpl> pic = p.clazz();
            if (log.isDebugEnabled()) {
               log.debug(String.format("applying parameter %s with key %s and default %s on %s", pic.getName(), key, (def) == null ? Arrays.toString(defArray) : def, c.getName()));
            }
            Constructor con = pic.getConstructor(String.class, String.class);
            ParameterImpl pi = (ParameterImpl) con.newInstance(key, help);
            if (def != null) {
               pi.setDefault((Serializable) ParamBindingService.getInstance().getFactory().getBindingHelper().convert(def, pi.getValueClass()));
            } else if (defArray != null) {
               pi.setDefault((Serializable) ParamBindingService.getInstance().getFactory().getBindingHelper().convert(defArray, pi.getValueClass()));
            }
            parameterizable.addParameter(pi, c);
         }
      }
   }

}
