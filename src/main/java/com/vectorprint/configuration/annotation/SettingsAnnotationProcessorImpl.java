
package com.vectorprint.configuration.annotation;

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

import static com.vectorprint.ClassHelper.findConstructor;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.Settings;
import com.vectorprint.configuration.binding.settings.SettingsBindingService;
import com.vectorprint.configuration.decoration.AbstractPropertiesDecorator;
import com.vectorprint.configuration.decoration.CachingProperties;
import com.vectorprint.configuration.decoration.ObservableProperties;
import com.vectorprint.configuration.decoration.Observer;
import com.vectorprint.configuration.decoration.ParsingProperties;
import com.vectorprint.configuration.decoration.PreparingProperties;
import com.vectorprint.configuration.decoration.ReadonlyProperties;
import com.vectorprint.configuration.decoration.ReloadableProperties;
import com.vectorprint.configuration.decoration.visiting.DecoratorVisitor;
import com.vectorprint.configuration.decoration.visiting.ObservableVisitor;
import com.vectorprint.configuration.decoration.visiting.PreparingVisitor;
import com.vectorprint.configuration.preparing.AbstractPrepareKeyValue;
import java.beans.Statement;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettingsAnnotationProcessorImpl implements SettingsAnnotationProcessor {

   private static final Logger LOGGER = LoggerFactory.getLogger(SettingsAnnotationProcessorImpl.class.getName());

   private EnhancedMap settingsUsed = null;

   private final Set objects = new HashSet();

   /**
    * Look for annotated fields in the Object class, use settings argument to apply settings. NOTE that the settings
    * argument may be wrapped, you should always use the {@link Settings#getOutermostDecorator() }
    * in that case, and not call the settings argument directly after initialization is performed. When the Object
    * argument is a class only static fields will be processed, otherwise only instance fields.
    *
    * @param o
    * @param settings when null create a new {@link Settings} instance.
    */
   @Override
   public boolean initSettings(Object o, EnhancedMap settings) {
      if (settings == null || settings.isEmpty()) {
         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("settings null or empty, no initialization");
         }
         return false;
      }
      if (settingsUsed == null) {
         settingsUsed = settings;
      } else if (o instanceof Class && objects.contains(o) && settingsUsed.equals(settings)) {
         // only check for classes, because equals of objects may be very expensive
         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("assuming, based on equals, settings for %s already initialized with %s", o, settings));
         }
         return false;
      }
      initSettings(o instanceof Class ? (Class) o : o.getClass(), o instanceof Class ? null : o, settings, true);
      if (o instanceof Class) {
         objects.add(o);
      }
      return true;
   }

   private void initSettings(Class c, Object obj, EnhancedMap eh, boolean notifyWrapping) {
      Field[] declaredFields = c.getDeclaredFields();
      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug(String.format("looking for %s fields to apply settings to", (obj == null ? "static" : "instance")));
      }
      for (Field field : declaredFields) {
         // when looping use the original settings argument, each settings annotation should use its own setup
         boolean isStatic = Modifier.isStatic(field.getModifiers());
         if (isStatic && obj != null) {
            continue;
         } else if (!isStatic && obj == null) {
            continue;
         }
         EnhancedMap settings = eh;
         field.setAccessible(true);
         Class type = field.getType();
         Setting a = field.getAnnotation(Setting.class);
         SettingsField se = field.getAnnotation(SettingsField.class);
         if (se != null) {
            if (a != null) {
               LOGGER.warn("Setting annotation is not processed because Settings is also present");
            }
            if (!type.isAssignableFrom(EnhancedMap.class)) {
               throw new VectorPrintRuntimeException(String.format("%s is not an EnhancedMap, cannot assign settings", type.getName()));
            }
             try {
               if (se.preprocessors().length > 0) {
                  if (!hasProps(settings, PreparingProperties.class)) {
                     if (notifyWrapping) {
                        LOGGER.warn(String.format("wrapping %s in %s, you should use the wrapper", settings.getClass().getName(), PreparingProperties.class.getName()));
                     }
                     settings = new PreparingProperties(settings);
                  }
                  for (PreProcess pp : se.preprocessors()) {
                     AbstractPrepareKeyValue apkv = pp.preProcessorClass().getConstructor().newInstance().addKeys(pp.keys()).setOptIn(pp.optIn());
                     AbstractPropertiesDecorator apd = (AbstractPropertiesDecorator) settings;
                     apd.accept(new PreparingVisitor(apkv));
                  }
               }

               if (se.observable()) {
                  if (!hasProps(settings, ObservableProperties.class)) {
                     if (notifyWrapping) {
                        LOGGER.warn(String.format("wrapping %s in %s, you should use the wrapper", settings.getClass().getName(), ObservableProperties.class.getName()));
                     }
                     settings = new ObservableProperties(settings);
                  }
                  if (!isStatic && obj instanceof Observer) {
                     ((AbstractPropertiesDecorator) settings).accept(new ObservableVisitor((Observer) obj));
                  }
               }
               if (se.urls().length > 0) {
                  if (notifyWrapping) {
                     LOGGER.warn(String.format("wrapping %s in %s, you should use the wrapper", settings.getClass().getName(), ParsingProperties.class.getName()));
                  }
                  if (se.autoreload()) {
                     settings = new ReloadableProperties(settings, se.pollInterval(), se.urls());
                  } else {
                     settings = new ParsingProperties(settings, se.urls());
                  }
               } else if (se.autoreload()) {
                  throw new IllegalArgumentException("autoreload needs file urls");
               }
               if (se.readonly()) {
                  if (notifyWrapping) {
                     LOGGER.warn(String.format("wrapping %s in %s, you should use the wrapper", settings.getClass().getName(), ReadonlyProperties.class.getName()));
                  }
                  settings = new ReadonlyProperties(settings);
               }
               if (se.cache()) {
                  if (!hasProps(settings, CachingProperties.class)) {
                     if (notifyWrapping) {
                        LOGGER.warn(String.format("wrapping %s in %s, you should use the wrapper", settings.getClass().getName(), CachingProperties.class.getName()));
                     }
                     settings = new CachingProperties(settings);
                  }
               }
               for (Feature feat : se.features()) {
                  Class<? extends AbstractPropertiesDecorator> dec = feat.clazz();
                  if (feat.urls().length > 0) {
                     URL[] urls = SettingsBindingService.getInstance().getFactory().getBindingHelper().convert(feat.urls(), URL[].class);
                     Constructor<? extends AbstractPropertiesDecorator> constructor = findConstructor(dec, EnhancedMap.class, URL[].class);
                     if (!hasProps(settings, dec)) {
                        if (notifyWrapping) {
                           LOGGER.warn(String.format("wrapping %s in %s, you should use the wrapper", settings.getClass().getName(), dec.getName()));
                        }
                        settings = constructor.newInstance(settings, urls);
                     }
                  } else if (!hasProps(settings, dec)) {
                     Constructor<? extends AbstractPropertiesDecorator> constructor = findConstructor(dec, EnhancedMap.class);
                     if (notifyWrapping) {
                        LOGGER.warn(String.format("wrapping %s in %s, you should use the wrapper", settings.getClass().getName(), dec.getName()));
                     }
                     settings = constructor.newInstance(settings);
                  }
               }
               if (!isStatic && obj instanceof DecoratorVisitor && settings instanceof AbstractPropertiesDecorator) {
                  ((AbstractPropertiesDecorator) settings).accept((DecoratorVisitor) obj);
               }
            } catch (IOException | SecurityException | InstantiationException | IllegalAccessException |
                     IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex) {
               throw new VectorPrintRuntimeException(ex);
            }
            try {
               if (!executeSetter(field, obj, settings, isStatic)) {
                  field.set(obj, settings);
               }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
               throw new VectorPrintRuntimeException(ex);
            }
         }
         if (a != null) {
             try {
               Object cur = field.get(isStatic ? null : obj);
               Object v;
               if (cur == null) {
                  if (LOGGER.isDebugEnabled()) {
                     LOGGER.debug(String.format("requiring a value for %s in settings", Arrays.toString(a.keys())));
                  }
                  // don't catch exception, a setting is required
                  v = settings.getGenericProperty(null, type, a.keys());
               } else {
                  if (LOGGER.isDebugEnabled()) {
                     LOGGER.debug(String.format("looking for a value for %s in settings, ", Arrays.toString(a.keys())));
                  }
                  // a setting is not required, only look for one if it is there
                  v = settings.getGenericProperty(cur, type, a.keys());
               }
               if (v != null) {
                  if (LOGGER.isDebugEnabled()) {
                     LOGGER.debug(String.format("found %s for %s in settings, ", v, Arrays.toString(a.keys())));
                  }
                  if (!executeSetter(field, obj, v, isStatic)) {
                     field.set(obj, v);
                  }
               }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
               throw new VectorPrintRuntimeException(ex);
            }
         }
      }
      if (c.getSuperclass() != null) {
         // when recursing we use the original settings argument, each settings annotation should use its own setup
         initSettings(c.getSuperclass(), obj, eh, notifyWrapping);
      }
   }

   private boolean executeSetter(Field f, Object o, Object value, boolean isStatic) {
      try {
         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("trying to call %s with %s", "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1), value));
         }
         if (!isStatic) {
            new Statement(o, "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1), new Object[]{value}).execute();
         } else {
            Method method = null;
            for (Method m : f.getDeclaringClass().getMethods()) {
               if (m.getName().equals("set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1))) {
                  method = m;
                  break;
               }
            }
            if (method != null && Modifier.isStatic(method.getModifiers())) {
               method.invoke(null, value);
            } else {
               return false;
            }
         }
         return true;
      } catch (NoSuchMethodException ex) {
         // no problem, we'll try to set the field value directly
         LOGGER.debug( "no problem, we'll try to set the field value directly", ex);
      } catch (Exception ex) {
         throw new VectorPrintRuntimeException(ex);
      }
      return false;
   }

   private boolean hasProps(EnhancedMap settings, Class<? extends AbstractPropertiesDecorator> clazz) {
      return settings instanceof AbstractPropertiesDecorator && ((AbstractPropertiesDecorator) settings).hasProperties(clazz);
   }

}
