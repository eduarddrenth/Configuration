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
import com.vectorprint.ArrayHelper;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.generated.jaxb.Featuretype;
import com.vectorprint.configuration.generated.jaxb.Preprocessortype;
import com.vectorprint.configuration.generated.jaxb.Settingstype;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.Settings;
import static com.vectorprint.ClassHelper.findConstructor;
import com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactory;
import com.vectorprint.configuration.binding.settings.SettingsBindingService;
import com.vectorprint.configuration.binding.settings.SpecificClassValidator;
import com.vectorprint.configuration.decoration.AbstractPropertiesDecorator;
import com.vectorprint.configuration.decoration.CachingProperties;
import com.vectorprint.configuration.decoration.ObservableProperties;
import com.vectorprint.configuration.decoration.ParsingProperties;
import com.vectorprint.configuration.decoration.PreparingProperties;
import com.vectorprint.configuration.decoration.ReadonlyProperties;
import com.vectorprint.configuration.decoration.visiting.PreparingVisitor;
import com.vectorprint.configuration.preparing.AbstractPrepareKeyValue;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import javax.xml.bind.JAXBException;

/**
 * Threadsafe mapper for translating xml into application settings with features
 *
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
               AbstractPrepareKeyValue apv = c.newInstance().setOptIn(p.isOptIn());
               if (!p.getKeys().isEmpty()) {
                  apv.addKeys(ArrayHelper.toArray(p.getKeys()));
               }
               ((PreparingProperties) settings).accept(new PreparingVisitor(apv));
            }
         }
         if (settingstype.isObservable()) {
            settings = new ObservableProperties(settings);
         }
         if (!settingstype.getUrl().isEmpty()) {
            settings = new ParsingProperties(settings, SettingsBindingService.getInstance().getFactory().getBindingHelper().
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
                  URL[] urls = SettingsBindingService.getInstance().getFactory().getBindingHelper().convert(ArrayHelper.toArray(f.getUrl()), URL[].class);
                  Constructor<? extends AbstractPropertiesDecorator> constructor = findConstructor(forName, EnhancedMap.class, URL[].class);
                  if (ParsingProperties.class.isAssignableFrom(forName)) {
                     SpecificClassValidator.setClazz((Class<? extends EnhancedMapBindingFactory>) Class.forName(f.getFactoryClass()));
                  }
                  settings = constructor.newInstance(settings, urls);
               } else {
                  Constructor<? extends AbstractPropertiesDecorator> constructor = findConstructor(forName, EnhancedMap.class);
                  settings = constructor.newInstance(settings);
               }
            }
         }
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException | IllegalArgumentException | InvocationTargetException classNotFoundException) {
         throw new VectorPrintException(classNotFoundException);
      }
      return settings;
   }

}
