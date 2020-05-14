
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

import java.awt.*;
import java.io.File;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

public interface EnhancedMap extends Map<String, String[]>, Cloneable, Serializable {

   boolean getBooleanProperty(Boolean defaultValue, String... keys);

   Class getClassProperty(Class defaultValue, String... keys) throws ClassNotFoundException;
   
   Class[] getClassProperties(Class[] defaultValue, String... keys) throws ClassNotFoundException;

   Pattern getRegexProperty(Pattern defaultValue, String... keys);
   
   Pattern[] getRegexProperties(Pattern[] defaultValue, String... keys);

   Color getColorProperty(Color defaultValue, String... keys);

   double getDoubleProperty(Double defaultValue, String... keys);

   short getShortProperty(Short defaultValue, String... keys);

   char getCharProperty(Character defaultValue, String... keys);

   byte getByteProperty(Byte defaultValue, String... keys);

   short[] getShortProperties(short[] defaultValue, String... keys);

   char[] getCharProperties(char[] defaultValue, String... keys);

   byte[] getByteProperties(byte[] defaultValue, String... keys);

   float getFloatProperty(Float defaultValue, String... keys);

   int getIntegerProperty(Integer defaultValue, String... keys);

   long getLongProperty(Long defaultValue, String... keys);

   String getProperty(String defaultValue, String... keys);

   URL getURLProperty(URL defaultValue, String... keys) throws MalformedURLException;
   
   File getFileProperty(File defaultValue, String... keys);

   File[] getFileProperties(File[] defaultValue, String... keys);

   String[] getStringProperties(String[] defaultValue, String... keys);

   URL[] getURLProperties(URL[] defaultValue, String... keys) throws MalformedURLException;

   float[] getFloatProperties(float[] defaultValue, String... keys);

   double[] getDoubleProperties(double[] defaultValue, String... keys);

   int[] getIntegerProperties(int[] defaultValue, String... keys);

   long[] getLongProperties(long[] defaultValue, String... keys);

   boolean[] getBooleanProperties(boolean[] defaultValue, String... keys);

   Color[] getColorProperties(Color[] defaultValue, String... keys);

   Date getDateProperty(Date defaultValue, String... keys);

   Date[] getDateProperties(Date[] defaultValue, String... keys);
   
   /**
    * look for a setting, if one of the keys is found return its value.
    * @param <T>
    * @param defaultValue
    * @param clazz
    * @param keys
    * @return 
    */
   <T> T getGenericProperty(T defaultValue, Class<T> clazz, String... keys);

   PropertyHelp getHelp(String key);

   Map<String, PropertyHelp> getHelp();

   String printHelp();

   void listProperties(PrintStream ps);

   public void setHelp(Map<String, PropertyHelp> h);

   public EnhancedMap clone() throws CloneNotSupportedException;

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
   
   void put(Map<String, String> m);
}
