
package com.vectorprint.configuration.decoration;

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

import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.PropertyHelp;
import com.vectorprint.configuration.PropertyHelpImpl;
import com.vectorprint.configuration.generated.parser.HelpParser;
import com.vectorprint.configuration.generated.parser.ParseException;
import com.vectorprint.configuration.generated.parser.TokenMgrException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HelpSupportedProperties extends AbstractPropertiesDecorator {

   public static final String HELPFORMAT = "help format: <property>=<type>;<description>";
   private static final Logger log = LoggerFactory.getLogger(HelpSupportedProperties.class.getName());

   public HelpSupportedProperties(EnhancedMap properties, URL... help) {
      super(properties);
      initHelp(help);
   }

   /**
    * Uses {@link HelpParser}, calls {@link #setHelp(java.util.Map) }
    *
    * @param urls
    */
   protected void initHelp(URL... urls) {
      Map<String, PropertyHelp> h = new HashMap<>(150);
      for (URL url : urls) {
         try {
            new HelpParser(url.openStream(), StandardCharsets.UTF_8.name()).parse(h);
         } catch (TokenMgrException | ParseException | IOException iOException) {
            super.getHelp().put("nohelp", new PropertyHelpImpl(HELPFORMAT));
            log.warn(HELPFORMAT, iOException);
         }
      }
      super.setHelp(h);
   }

}
