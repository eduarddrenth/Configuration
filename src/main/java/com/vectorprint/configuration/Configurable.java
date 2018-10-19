package com.vectorprint.configuration;

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

import com.vectorprint.configuration.annotation.Setting;
import com.vectorprint.configuration.annotation.SettingsField;
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessor;
import com.vectorprint.configuration.decoration.FindableProperties;
import java.util.Map;

public interface Configurable<P extends Map> {

   P getSettings();

   /**
    * A settingsprovider can provide settings to a Configurable by calling this method.
    *
    * @param settings
    */
   void initSettings(P settings);

   /**
    * return a setting of a certain type
    *
    * @param keys
    * @see EnhancedMap#getGenericProperty(java.lang.Object, java.lang.Class, java.lang.String...)
    * @param defaultValue
    * @param clazz
    * @return
    */
   <TYPE> TYPE getSetting(TYPE defaultValue, Class<TYPE> clazz, Object... keys);
}
