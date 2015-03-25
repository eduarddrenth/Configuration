/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.configuration.decoration;

/*
 * #%L
 * VectorPrintConfig3.0
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
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.PropertyHelp;
import com.vectorprint.configuration.PropertyHelpImpl;
import com.vectorprint.configuration.parser.HelpParser;
import com.vectorprint.configuration.parser.ParseException;
import com.vectorprint.configuration.parser.TokenMgrError;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class HelpSupportedProperties extends AbstractPropertiesDecorator {

   /**
    * default name of the file containing help for settings
    */
   public static final String HELPFILE = "help.properties";
   public static final String MISSINGHELP = "no help configured, provide help file " + HELPFILE + " using format <property>=<type>;<description>";
   private static final Logger log = Logger.getLogger(HelpSupportedProperties.class.getName());
   private URL help;

   public HelpSupportedProperties(EnhancedMap properties, URL help) {
      super(properties);
      initHelp(help);
      this.help = help;
   }
   
   /**
    * Uses {@link HelpParser}, calls {@link #setHelp(java.util.Map) }
    * @param url 
    */
   protected void initHelp(URL url) {
      try {
         Map<String, PropertyHelp> h = new HashMap<String, PropertyHelp>(150);

         new HelpParser(url.openStream()).parse(h);

         super.setHelp(h);
      } catch (TokenMgrError iOException) {
         super.getHelp().put("nohelp", new PropertyHelpImpl(MISSINGHELP));
         log.log(Level.WARNING, MISSINGHELP, iOException);
      } catch (ParseException iOException) {
         super.getHelp().put("nohelp", new PropertyHelpImpl(MISSINGHELP));
         log.log(Level.WARNING, MISSINGHELP, iOException);
      } catch (IOException iOException) {
         super.getHelp().put("nohelp", new PropertyHelpImpl(MISSINGHELP));
         log.log(Level.WARNING, MISSINGHELP, iOException);
      }
   }

   @Override
   public EnhancedMap clone() {
      HelpSupportedProperties h = (HelpSupportedProperties) super.clone();
      h.help = help;
      return h;
   }

}
