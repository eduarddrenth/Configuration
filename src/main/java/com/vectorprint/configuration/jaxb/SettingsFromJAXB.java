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
package com.vectorprint.configuration.jaxb;

import com.vectorprint.ArrayHelper;
import com.vectorprint.VectorPrintException;
import com.vectorprint.config.jaxb.Featuretype;
import com.vectorprint.config.jaxb.Preprocessortype;
import com.vectorprint.config.jaxb.Settingstype;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.Settings;
import static com.vectorprint.configuration.annotation.SettingsAnnotationProcessorImpl.findConstructor;
import com.vectorprint.configuration.binding.BindingHelper;
import com.vectorprint.configuration.binding.BindingHelperImpl;
import com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactoryImpl;
import com.vectorprint.configuration.binding.settings.EnhancedMapParser;
import com.vectorprint.configuration.binding.settings.EnhancedMapSerializer;
import com.vectorprint.configuration.decoration.AbstractPropertiesDecorator;
import com.vectorprint.configuration.decoration.CachingProperties;
import com.vectorprint.configuration.decoration.ObservableProperties;
import com.vectorprint.configuration.decoration.ParsingProperties;
import com.vectorprint.configuration.decoration.PreparingProperties;
import com.vectorprint.configuration.decoration.ReadonlyProperties;
import com.vectorprint.configuration.decoration.visiting.PreparingVisitor;
import com.vectorprint.configuration.preparing.AbstractPrepareKeyValue;
import com.vectorprint.configuration.parser.PropertiesParser;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import javax.xml.bind.JAXBException;

/**
 * Threadsafe mapper for translating xml into application settings with features
 * @author Eduard Drenth at VectorPrint.nl
 */
public class SettingsFromJAXB {

   public EnhancedMap fromJaxb(Reader xml) throws VectorPrintException, JAXBException {
      return fromJaxb(SettingsXMLHelper.fromXML(xml));
   }

   public EnhancedMap fromJaxb(Settingstype settingstype) throws VectorPrintException {
      EnhancedMap settings = new Settings();
      try {
         if (!settingstype.getPreprocessor().isEmpty()) {
            settings = new PreparingProperties(settings);
            for (Preprocessortype p : settingstype.getPreprocessor()) {
               Class<? extends AbstractPrepareKeyValue> c = (Class<? extends AbstractPrepareKeyValue>) Class.forName(p.getPreprocessorclassname());
               AbstractPrepareKeyValue apv = c.newInstance();
               if (!p.getKeysToSkip().isEmpty()) {
                  apv.addKeysToSkip(ArrayHelper.toArray(p.getKeysToSkip()));
               }
               ((PreparingProperties) settings).accept(new PreparingVisitor(apv));
            }
         }
         if (settingstype.isObservable()) {
            settings = new ObservableProperties(settings);
         }
         if (!settingstype.getUrl().isEmpty()) {
            settings = new ParsingProperties(settings, EnhancedMapBindingFactoryImpl.getDefaultFactory().getBindingHelper().
                convert(ArrayHelper.toArray(settingstype.getUrl()), URL[].class));
         }
         if (settingstype.isReadonly()) {
            settings = new ReadonlyProperties(settings);
         }
         if (settingstype.isCache()) {
            settings = new CachingProperties(settings);
         }
         if (!settingstype.getFeature().isEmpty()) {
            for (Featuretype f : settingstype.getFeature()) {
               Class<? extends AbstractPropertiesDecorator> forName = (Class<? extends AbstractPropertiesDecorator>) Class.forName(f.getClassname());
               if (!f.getUrl().isEmpty()) {
                  URL[] urls = EnhancedMapBindingFactoryImpl.getDefaultFactory().getBindingHelper().convert(ArrayHelper.toArray(f.getUrl()), URL[].class);
                  Constructor<? extends AbstractPropertiesDecorator> constructor = findConstructor(forName, EnhancedMap.class, URL[].class);
                  if (ParsingProperties.class.isAssignableFrom(forName)) {
                     if (!PropertiesParser.class.getName().equals(f.getParserclassname())
                         || !PropertiesParser.class.getName().equals(f.getSerializerclassname())
                         || !BindingHelperImpl.class.getName().equals(f.getHelperclassname())) {
                        Class<? extends EnhancedMapParser> pc = (Class<? extends EnhancedMapParser>) Class.forName(f.getParserclassname());
                        Class<? extends EnhancedMapSerializer> sc = (Class<? extends EnhancedMapSerializer>) Class.forName(f.getSerializerclassname());
                        Class<? extends BindingHelper> bh = (Class<? extends BindingHelper>) Class.forName(f.getHelperclassname());
                        ParsingProperties.setFactory(
                            EnhancedMapBindingFactoryImpl.getFactory(pc, sc, bh.newInstance(), false));
                     }
                  }
                  settings = constructor.newInstance(settings, urls);
               } else {
                  Constructor<? extends AbstractPropertiesDecorator> constructor = findConstructor(forName, EnhancedMap.class);
                  settings = constructor.newInstance(settings);
               }
            }
         }
      } catch (ClassNotFoundException classNotFoundException) {
         throw new VectorPrintException(classNotFoundException);
      } catch (InstantiationException instantiationException) {
         throw new VectorPrintException(instantiationException);
      } catch (IllegalAccessException illegalAccessException) {
         throw new VectorPrintException(illegalAccessException);
      } catch (IOException iOException) {
         throw new VectorPrintException(iOException);
      } catch (IllegalArgumentException illegalArgumentException) {
         throw new VectorPrintException(illegalArgumentException);
      } catch (InvocationTargetException invocationTargetException) {
         throw new VectorPrintException(invocationTargetException);
      }
      return settings;
   }

}
