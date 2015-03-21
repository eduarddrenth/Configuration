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
import com.vectorprint.configuration.VectorPrintProperties;
import com.vectorprint.configuration.decoration.AbstractPropertiesDecorator;
import com.vectorprint.configuration.decoration.CachingProperties;
import com.vectorprint.configuration.decoration.HelpSupportedProperties;
import com.vectorprint.configuration.decoration.ObservableProperties;
import com.vectorprint.configuration.decoration.Observer;
import com.vectorprint.configuration.decoration.ParsingProperties;
import com.vectorprint.configuration.decoration.ReadonlyProperties;
import com.vectorprint.configuration.decoration.visiting.DecoratorVisitor;
import com.vectorprint.configuration.decoration.visiting.ObservableVisitor;
import com.vectorprint.configuration.parser.ParseException;
import java.beans.Statement;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
    * This implementation will try to call a setter for a field first when injecting a value from settings, when this
    * fails the value of the field will be set directly using {@link Field#set(java.lang.Object, java.lang.Object) }.
    * This implementation will traverse all fields (including non public ones) in the class of the Object argument,
    * including those in superclasses. When the object is a {@link DecoratorVisitor} and settings are a subclass of
    * {@link AbstractPropertiesDecorator}, call {@link AbstractPropertiesDecorator#accept(com.vectorprint.configuration.decoration.visiting.DecoratorVisitor, java.lang.Object) }
    *
    * @see Settings
    * @see Setting
 * @author Eduard Drenth at VectorPrint.nl
 */
public class SettingsAnnotationProcessorImpl implements SettingsAnnotationProcessor {

   private static final Logger LOGGER = Logger.getLogger(SettingsAnnotationProcessorImpl.class.getName());

   /**
    * Look for annotation in the object, use settings argument to inject settings.
    * @param o
    * @param settings
    */
   @Override
   public void initSettings(Object o, EnhancedMap settings) {
      initSettings(o.getClass(), o, settings);
   }

   /**
    * Call {@link #initSettings(java.lang.Object, com.vectorprint.configuration.EnhancedMap) } with 
    * a new {@link VectorPrintProperties#VectorPrintProperties() }.
    * @param o 
    */
   @Override
   public void initSettings(Object o) {
      initSettings(o, new VectorPrintProperties());
   }

   private void initSettings(Class c, Object o, EnhancedMap eh) {
      EnhancedMap settings = eh;
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
            Settings set = (Settings) se;
            try {
               if (set.observable()) {
                  if (!hasProps(settings, ObservableProperties.class)) {
                     settings = new ObservableProperties(settings);
                  }
                  if (o instanceof Observer) {
                     ((AbstractPropertiesDecorator)settings).accept(new ObservableVisitor((Observer) o));
                  }
               }
               if (set.urls().length>0) {
                  settings = new ParsingProperties(settings, set.urls());
               }
               if (set.readonly()) {
                  settings = new ReadonlyProperties(settings);
               }
               if (set.cache()) {
                  if (!hasProps(settings, CachingProperties.class)) {
                     settings = new CachingProperties(settings);
                  }
               }
               for (Feature feat : set.features()) {
                  Class<? extends AbstractPropertiesDecorator> dec = feat.clazz();
                  String extraSource = feat.url();
                  if (HelpSupportedProperties.class.isAssignableFrom(dec) || ParsingProperties.class.isAssignableFrom(dec)) {
                     if (!hasProps(settings, dec)) {
                        Constructor<? extends AbstractPropertiesDecorator> constructor = dec.getConstructor(EnhancedMap.class, URL.class);
                        URL u = new URL(extraSource);
                        settings = constructor.newInstance(settings, u);
                     }
                  } else {
                     if (!hasProps(settings, dec)) {
                        Constructor<? extends AbstractPropertiesDecorator> constructor = dec.getConstructor(EnhancedMap.class);
                        settings = constructor.newInstance(settings);
                     }
                  }
               }
               if (o instanceof DecoratorVisitor && settings instanceof AbstractPropertiesDecorator) {
                  ((AbstractPropertiesDecorator)settings).accept((DecoratorVisitor) o);
               }
            } catch (IOException ex) {
               throw new VectorPrintRuntimeException(ex);
            } catch (ParseException ex) {
               throw new VectorPrintRuntimeException(ex);
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
                  v = settings.getGenericProperty( null, type, s.key());
               } else {
                  if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.fine(String.format("looking for a value for %s in settings, ", s.key()));
                  }
                  // a setting is not required, only look for one if it is there
                  v = settings.getGenericProperty(cur, type, s.key());
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
      if (c.getSuperclass() != null) {
         initSettings(c.getSuperclass(), o, settings);
      }
   }

   private boolean executeSetter(Field f, Object o, Object value) {
      try {
         if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("trying to call %s with %s", "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1), value));
         }
         new Statement(o, "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1), new Object[]{value}).execute();
         return true;
      } catch (Exception ex) {
         LOGGER.log(Level.WARNING, null, ex);
      }
      return false;
   }

   private boolean hasProps(EnhancedMap settings, Class<? extends AbstractPropertiesDecorator> clazz) {
      return settings instanceof AbstractPropertiesDecorator && ((AbstractPropertiesDecorator) settings).hasProperties(clazz);
   }

}
