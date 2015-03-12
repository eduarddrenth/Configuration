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
package com.vectorprint.configuration.annotation;

import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import java.beans.Statement;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class SettingsAnnotationProcessorImpl implements SettingsAnnotationProcessor {

   private static final Logger LOGGER = Logger.getLogger(SettingsAnnotationProcessorImpl.class.getName());
   
   /**
    * This implementation will try to call a setter for a field first when applying a value from settings, when this
    * fails the value of the field will be set directly using {@link Field#set(java.lang.Object, java.lang.Object) }.
    * This implementation will traverse all fields (including non public ones) in the class of the Object argument, including those in superclasses.
    * @param o
    * @param settings 
    */
   @Override
   public void initSettings(Object o, EnhancedMap settings) {
      initSettings(o.getClass(), o, settings);
   }

   private void initSettings(Class c, Object o, EnhancedMap settings) {
      Field[] declaredFields = c.getDeclaredFields();
      for (Field f : declaredFields) {
         f.setAccessible(true);
         Class type = f.getType();
         Annotation a = f.getAnnotation(Setting.class);
         Annotation se = f.getAnnotation(Settings.class);
         if (se != null) {
            if (a != null) {
               LOGGER.warning(String.format("Setting annotation is not processed because Settings is also present"));
            }
            if (!type.isAssignableFrom(EnhancedMap.class)) {
               throw new VectorPrintRuntimeException(String.format("%s is not an EnhancedMap, cannot assign settings", type.getName()));
            }
            try {
               if (!executeSetter(f, o, settings)) {
                  f.set(o, settings);
               }
               return;
            } catch (IllegalArgumentException ex) {
               throw new VectorPrintRuntimeException(ex);
            } catch (IllegalAccessException ex) {
               throw new VectorPrintRuntimeException(ex);
            }
         }
         if (a != null) {
            Setting s = (Setting) a;
            try {
               Object cur = f.get(o);
               Object v = null;
               if (cur == null) {
                  if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.fine(String.format("requiring a value for %s in settings", s.key()));
                  }
                  // don't catch exception, a setting is required
                  v = settings.getGenericProperty(s.key(), null, type);
               } else {
                  if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.fine(String.format("looking for a value for %s in settings, ", s.key()));
                  }
                  // a setting is not required, only look for one if it is there
                  if (settings.containsKey(s.key())) {
                     v = settings.getGenericProperty(s.key(), null, type);
                  }
               }
               if (v != null) {
                  if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.fine(String.format("found %s for %s in settings, ", v, s.key()));
                  }
                  if (!executeSetter(f, o, v)) {
                     f.set(o, v);
                  }
               }
            } catch (IllegalArgumentException ex) {
               throw new VectorPrintRuntimeException(ex);
            } catch (IllegalAccessException ex) {
               throw new VectorPrintRuntimeException(ex);
            }
         }
      }
      if (c.getSuperclass()!=null) {
         initSettings(c.getSuperclass(), o, settings);
      }
   }
   
   private boolean executeSetter(Field f, Object o, Object value) {
      try {
         if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("trying to call %s with %s", "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1), value));
         }
         new Statement(o, "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1), new Object[] { value }).execute();
         return true;
      } catch (Exception ex) {
         LOGGER.log(Level.WARNING, null, ex);
      }
      return false;
   }

}
