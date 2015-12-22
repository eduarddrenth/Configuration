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

/*
 * #%L
 * Config
 * %%
 * Copyright (C) 2015 VectorPrint
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
import com.vectorprint.configuration.decoration.AbstractPropertiesDecorator;
import com.vectorprint.configuration.decoration.CachingProperties;
import com.vectorprint.configuration.decoration.ObservableProperties;
import com.vectorprint.configuration.decoration.Observer;
import com.vectorprint.configuration.decoration.ParsingProperties;
import com.vectorprint.configuration.decoration.PreparingProperties;
import com.vectorprint.configuration.decoration.ReadonlyProperties;
import com.vectorprint.configuration.decoration.visiting.DecoratorVisitor;
import com.vectorprint.configuration.decoration.visiting.ObservableVisitor;
import com.vectorprint.configuration.decoration.visiting.PreparingVisitor;
import com.vectorprint.configuration.preparing.AbstractPrepareKeyValue;
import java.beans.Statement;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.vectorprint.ClassHelper.findConstructor;
import com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactory;
import com.vectorprint.configuration.binding.settings.SettingsBindingService;

/**
 * This implementation will try to call a setter for a field first when injecting a value from settings, when this fails
 * the value of the field will be set directly using {@link Field#set(java.lang.Object, java.lang.Object) }. This
 * implementation will traverse all fields (including non public ones) in the class of the Object argument, including
 * those in superclasses. {@link SettingsField} may trigger
 * {@link AbstractPropertiesDecorator#AbstractPropertiesDecorator(com.vectorprint.configuration.EnhancedMap) wrapping},
 * this instance will never wrap settings in the same decorator more than once. When settings are wrapped the outermost
 * wrapper should be used, otherwise functionality implemented by a decorator may not execute. When the object is a
 * {@link DecoratorVisitor} and settings are a subclass of {@link AbstractPropertiesDecorator}, call
    * {@link AbstractPropertiesDecorator#accept(com.vectorprint.configuration.decoration.visiting.DecoratorVisitor) }.
 * Classes will only be initialized once with the same settings.
 *
 * @see AbstractPropertiesDecorator#hasProperties(java.lang.Class)
 * @see SettingsField
 * @see Setting
 * @author Eduard Drenth at VectorPrint.nl
 */
public class SettingsAnnotationProcessorImpl implements SettingsAnnotationProcessor {

   private static final Logger LOGGER = Logger.getLogger(SettingsAnnotationProcessorImpl.class.getName());

   private EnhancedMap settingsUsed = null;

   private final Set objects = new HashSet();

   /**
    * Look for annotated fields in the Object class, use settings argument to apply settings. NOTE that the settings
    * argument may be wrapped, you should always use the {@link Settings#getOutermostWrapper() }
    * in that case, and not call the settings argument directly after initialization is performed. When the Object
    * argument is a class only static fields will be processed, otherwise only instance fields.
    *
    * @param o
    * @param settings when null create a new {@link Settings} instance.
    */
   @Override
   public boolean initSettings(Object o, EnhancedMap settings) {
      if (settingsUsed == null) {
         settingsUsed = settings;
      } else if (o instanceof Class && objects.contains(o) && settingsUsed.equals(settings)) {
         // only check for classes, because equals of objects may be very expensive
         if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("assuming, based on equals, settings for %s already initialized with %s", settings, o));
         }
         return false;
      }
      initSettings(o instanceof Class ? (Class) o : o.getClass(), o instanceof Class ? null : o, settings == null ? new Settings() : settings, settings != null);
      if (o instanceof Class) {
         objects.add(o);
      }
      return true;
   }

   private void initSettings(Class c, Object obj, EnhancedMap eh, boolean notifyWrapping) {
      Field[] declaredFields = c.getDeclaredFields();
      if (LOGGER.isLoggable(Level.FINE)) {
         LOGGER.fine(String.format("looking for %s fields to apply settings to", (obj == null ? "static" : "instance")));
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
         Annotation a = field.getAnnotation(Setting.class);
         Annotation se = field.getAnnotation(SettingsField.class);
         if (se != null) {
            if (a != null) {
               LOGGER.warning(String.format("Setting annotation is not processed because Settings is also present"));
            }
            if (!type.isAssignableFrom(EnhancedMap.class)) {
               throw new VectorPrintRuntimeException(String.format("%s is not an EnhancedMap, cannot assign settings", type.getName()));
            }
            SettingsField set = (SettingsField) se;
            try {
               if (set.preprocessors().length > 0) {
                  if (!hasProps(settings, PreparingProperties.class)) {
                     if (notifyWrapping) {
                        LOGGER.warning(String.format("wrapping %s in %s, you should use the wrapper", settings.getClass().getName(), PreparingProperties.class.getName()));
                     }
                     settings = new PreparingProperties(settings);
                  }
                  for (PreProcess pp : set.preprocessors()) {
                     AbstractPrepareKeyValue apkv = pp.preProcessorClass().newInstance().addKeysToSkip(pp.keysToSkip());
                     AbstractPropertiesDecorator apd = (AbstractPropertiesDecorator) settings;
                     apd.accept(new PreparingVisitor(apkv));
                  }
               }

               if (set.observable()) {
                  if (!hasProps(settings, ObservableProperties.class)) {
                     if (notifyWrapping) {
                        LOGGER.warning(String.format("wrapping %s in %s, you should use the wrapper", settings.getClass().getName(), ObservableProperties.class.getName()));
                     }
                     settings = new ObservableProperties(settings);
                  }
                  if (!isStatic && obj instanceof Observer) {
                     ((AbstractPropertiesDecorator) settings).accept(new ObservableVisitor((Observer) obj));
                  }
               }
               if (set.urls().length > 0) {
                  if (notifyWrapping) {
                     LOGGER.warning(String.format("wrapping %s in %s, you should use the wrapper", settings.getClass().getName(), ParsingProperties.class.getName()));
                  }
                  settings = new ParsingProperties(settings, set.urls());
               }
               if (set.readonly()) {
                  if (notifyWrapping) {
                     LOGGER.warning(String.format("wrapping %s in %s, you should use the wrapper", settings.getClass().getName(), ReadonlyProperties.class.getName()));
                  }
                  settings = new ReadonlyProperties(settings);
               }
               if (set.cache()) {
                  if (!hasProps(settings, CachingProperties.class)) {
                     if (notifyWrapping) {
                        LOGGER.warning(String.format("wrapping %s in %s, you should use the wrapper", settings.getClass().getName(), CachingProperties.class.getName()));
                     }
                     settings = new CachingProperties(settings);
                  }
               }
               for (Feature feat : set.features()) {
                  Class<? extends AbstractPropertiesDecorator> dec = feat.clazz();
                  if (feat.urls().length > 0) {
                     URL[] urls = SettingsBindingService.getInstance().getFactory().getBindingHelper().convert(feat.urls(), URL[].class);
                     Constructor<? extends AbstractPropertiesDecorator> constructor = findConstructor(dec, EnhancedMap.class, URL[].class);
                     if (!hasProps(settings, dec)) {
                        if (notifyWrapping) {
                           LOGGER.warning(String.format("wrapping %s in %s, you should use the wrapper", settings.getClass().getName(), dec.getName()));
                        }
                        if (ParsingProperties.class.isInstance(feat)) {
                           Class<? extends EnhancedMapBindingFactory> factoryClass = feat.factoryClass();
                              ParsingProperties.setFactory(SettingsBindingService.getInstance().setFactoryClass(factoryClass).getFactory());
                        }
                        settings = constructor.newInstance(settings, urls);
                     }
                  } else {
                     if (!hasProps(settings, dec)) {
                        Constructor<? extends AbstractPropertiesDecorator> constructor = findConstructor(dec, EnhancedMap.class);
                        if (notifyWrapping) {
                           LOGGER.warning(String.format("wrapping %s in %s, you should use the wrapper", settings.getClass().getName(), dec.getName()));
                        }
                        settings = constructor.newInstance(settings);
                     }
                  }
               }
               if (!isStatic && obj instanceof DecoratorVisitor && settings instanceof AbstractPropertiesDecorator) {
                  ((AbstractPropertiesDecorator) settings).accept((DecoratorVisitor) obj);
               }
            } catch (IOException ex) {
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
               if (!executeSetter(field, obj, settings, isStatic)) {
                  field.set(obj, settings);
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
               Object cur = field.get(isStatic ? null : obj);
               Object v = null;
               if (cur == null) {
                  if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.fine(String.format("requiring a value for %s in settings", Arrays.toString(s.keys())));
                  }
                  // don't catch exception, a setting is required
                  v = settings.getGenericProperty(null, type, s.keys());
               } else {
                  if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.fine(String.format("looking for a value for %s in settings, ", Arrays.toString(s.keys())));
                  }
                  // a setting is not required, only look for one if it is there
                  v = settings.getGenericProperty(cur, type, s.keys());
               }
               if (v != null) {
                  if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.fine(String.format("found %s for %s in settings, ", v, Arrays.toString(s.keys())));
                  }
                  if (!executeSetter(field, obj, v, isStatic)) {
                     field.set(obj, v);
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
         // when recursing we use the original settings argument, each settings annotation should use its own setup
         initSettings(c.getSuperclass(), obj, eh, notifyWrapping);
      }
   }

   private boolean executeSetter(Field f, Object o, Object value, boolean isStatic) {
      try {
         if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("trying to call %s with %s", "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1), value));
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
         LOGGER.log(Level.FINE, null, ex);
      } catch (SecurityException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (Exception ex) {
         throw new VectorPrintRuntimeException(ex);
      }
      return false;
   }

   private boolean hasProps(EnhancedMap settings, Class<? extends AbstractPropertiesDecorator> clazz) {
      return settings instanceof AbstractPropertiesDecorator && ((AbstractPropertiesDecorator) settings).hasProperties(clazz);
   }

}
