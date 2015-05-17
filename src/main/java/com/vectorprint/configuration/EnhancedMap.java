/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.configuration;

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
import com.vectorprint.configuration.decoration.AbstractPropertiesDecorator;
import com.vectorprint.configuration.binding.StringConversion;
import com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactory;
import java.awt.Color;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This interface is a suggested way to store values for settings as arrays of String and retrieve settings
 * in the type needed by applications.
 *
 * @see Settings
 * @see AbstractPropertiesDecorator
 * @see EnhancedMapBindingFactory
 * @see StringConversion
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface EnhancedMap extends Map<String, String[]>, Cloneable, Serializable {

   boolean getBooleanProperty(String key, Boolean defaultValue);

   Class getClassProperty(String key, Class defaultValue) throws ClassNotFoundException;
   
   Class[] getClassProperties(String key, Class[] defaultValue) throws ClassNotFoundException;

   Pattern getRegexProperty(String key, Pattern defaultValue);
   
   Pattern[] getRegexProperties(String key, Pattern[] defaultValue);

   Color getColorProperty(String key, Color defaultValue);

   double getDoubleProperty(String key, Double defaultValue);

   short getShortProperty(String key, Short defaultValue);

   char getCharProperty(String key, Character defaultValue);

   byte getByteProperty(String key, Byte defaultValue);

   short[] getShortProperties(String key, short[] defaultValue);

   char[] getCharProperties(String key, char[] defaultValue);

   byte[] getByteProperties(String key, byte[] defaultValue);

   float getFloatProperty(String key, Float defaultValue);

   int getIntegerProperty(String key, Integer defaultValue);

   long getLongProperty(String key, Long defaultValue);

   String getProperty(String key);

   String getProperty(String key, String defaultValue);

   URL getURLProperty(String key, URL defaultValue) throws MalformedURLException;

   String[] getStringProperties(String key, String[] defaultValue);

   URL[] getURLProperties(String key, URL[] defaultValue) throws MalformedURLException;

   float[] getFloatProperties(String key, float[] defaultValue);

   double[] getDoubleProperties(String key, double[] defaultValue);

   int[] getIntegerProperties(String key, int[] defaultValue);

   long[] getLongProperties(String key, long[] defaultValue);

   boolean[] getBooleanProperties(String key, boolean[] defaultValue);

   Color[] getColorProperties(String key, Color[] defaultValue);

   Date getDateProperty(String key, Date defaultValue);

   Date[] getDateProperties(String key, Date[] defaultValue);
   
   <T> T getGenericProperty(T defaultValue, Class<T> clazz, String... keys);

   PropertyHelp getHelp(String key);

   Map<String, PropertyHelp> getHelp();

   String printHelp();

   void listProperties(PrintStream ps);

   public void setHelp(Map<String, PropertyHelp> h);

   public EnhancedMap clone();

   /**
    * return a collection of keys not used sofar in the settings.
    *
    * @return
    */
   public Collection<String> getUnusedKeys();

   /**
    * return a collection of keys not present in the settings, for which defaults where used instead.
    *
    * @return
    */
   public Collection<String> getKeysNotPresent();

   /**
    * each set of properties is suggested to have a unique id
    *
    * @return
    */
   String getId();
   void setId(String id);

   String[] put(String key, String value);
}
