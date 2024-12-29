
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class CachingProperties extends AbstractPropertiesDecorator {

   private static final Logger LOGGER = LoggerFactory.getLogger(CachingProperties.class.getName());

   public CachingProperties(EnhancedMap settings) {
      super(settings);
   }

   private final Map<String, Object> cache = new HashMap<>();

   @Override
   public LocalDate[] getLocalDateProperties(LocalDate[] defaultValue, String... keys) {
      return fromCache(defaultValue, LocalDate[].class, keys);
   }

   private <T> T fromCache(T defaultValue, Class<T> clazz, String... keys) {
      String key = keys.length == 1 ? keys[0] : String.join("", keys);
      Object value = cache.get(key);
      if (cache.containsKey(key) && (clazz.isInstance(value) || (clazz.isPrimitive() && checkPrimitive(clazz, value.getClass())))) {
         return (T) value;
      }
      if (!cache.containsKey(key)) {
         cache.put(key, super.getGenericProperty(defaultValue, clazz, keys));
      } else if (null != value) {
         if (clazz.isPrimitive()) {
            Class c = value.getClass();
            if (!checkPrimitive(clazz, c)) {
               cache.put(key, super.getGenericProperty(defaultValue, clazz, keys));
               if (LOGGER.isWarnEnabled()) {
                  LOGGER.warn(String.format("class for %s in cache is %s, this does not match requested class: %s. New type put in cache.",
                      key, c.getName(), clazz.getName()));
               }
            }
         } else if (!clazz.isInstance(value)) {
            cache.put(key, super.getGenericProperty(defaultValue, clazz, keys));
            if (LOGGER.isWarnEnabled()) {
               LOGGER.warn(String.format("class for %s in cache is %s, this does not match requested class: %s. New type put in cache.",
                   key, value.getClass().getName(), clazz.getName()));
            }
         }
      }
      return (T) cache.get(key);
   }

   private static boolean checkPrimitive(Class prim, Class wrapper) {
      return (prim.equals(int.class) && wrapper.equals(Integer.class))
          || (prim.equals(boolean.class) && wrapper.equals(Boolean.class))
          || (prim.equals(long.class) && wrapper.equals(Long.class))
          || (prim.equals(double.class) && wrapper.equals(Double.class))
          || (prim.equals(float.class) && wrapper.equals(Float.class))
          || (prim.equals(char.class) && wrapper.equals(Character.class))
          || (prim.equals(short.class) && wrapper.equals(Short.class))
          || (prim.equals(byte.class) && wrapper.equals(Byte.class));
   }

   @Override
   public LocalDate getLocalDateProperty(LocalDate defaultValue, String... keys) {
      return fromCache(defaultValue, LocalDate.class, keys);
   }

   @Override
   public byte[] getByteProperties(byte[] defaultValue, String... keys) {
      return fromCache(defaultValue, byte[].class, keys);
   }

   @Override
   public char[] getCharProperties(char[] defaultValue, String... keys) {
      return fromCache(defaultValue, char[].class, keys);
   }

   @Override
   public byte getByteProperty(Byte defaultValue, String... keys) {
      return fromCache(defaultValue, byte.class, keys);
   }

   @Override
   public char getCharProperty(Character defaultValue, String... keys) {
      return fromCache(defaultValue, char.class, keys);
   }

   @Override
   public String[] remove(Object key) {
       cache.entrySet().removeIf(next -> next.getKey().contains(String.valueOf(key)));
      return super.remove(key);
   }

   @Override
   public String[] put(String key, String value) {
      return put(key, new String[]{value});
   }

   @Override
   public String[] put(String key, String[] value) {
       cache.entrySet().removeIf(next -> next.getKey().contains(String.valueOf(key)));
      return super.put(key, value);
   }

   @Override
   public Color[] getColorProperties(Color[] defaultValue, String... keys) {
      return fromCache(defaultValue, Color[].class, keys);
   }

   @Override
   public boolean[] getBooleanProperties(boolean[] defaultValue, String... keys) {
      return fromCache(defaultValue, boolean[].class, keys);
   }

   @Override
   public double[] getDoubleProperties(double[] defaultValue, String... keys) {
      return fromCache(defaultValue, double[].class, keys);
   }

   @Override
   public float[] getFloatProperties(float[] defaultValue, String... keys) {
      return fromCache(defaultValue, float[].class, keys);
   }

   @Override
   public float getFloatProperty(Float defaultValue, String... keys) {
      return fromCache(defaultValue, float.class, keys);
   }

   @Override
   public double getDoubleProperty(Double defaultValue, String... keys) {
      return fromCache(defaultValue, double.class, keys);
   }

   @Override
   public Color getColorProperty(Color defaultValue, String... keys) {
      return fromCache(defaultValue, Color.class, keys);
   }

   /**
    *
    * @param defaultValue the value of defaultValue
    * @param keys
    * @return the boolean
    */
   @Override
   public boolean getBooleanProperty(Boolean defaultValue, String... keys) {
      return fromCache(defaultValue, boolean.class, keys);
   }

   /**
    *
    * @param defaultValue the value of defaultValue
    * @param keys the value of keys
    * @return
    */
   @Override
   public Class getClassProperty(Class defaultValue, String... keys) {
      return fromCache(defaultValue, Class.class, keys);
   }

   /**
    *
    * @param defaultValue the value of defaultValue
    * @param keys the value of keys
    * @return
    */
   @Override
   public Class[] getClassProperties(Class[] defaultValue, String... keys) {
      return fromCache(defaultValue, Class[].class, keys);
   }

   @Override
   public short[] getShortProperties(short[] defaultValue, String... keys) {
      return fromCache(defaultValue, short[].class, keys);
   }

   @Override
   public short getShortProperty(Short defaultValue, String... keys) {
      return fromCache(defaultValue, short.class, keys);
   }

   @Override
   public URL[] getURLProperties(URL[] defaultValue, String... keys) {
      return fromCache(defaultValue, URL[].class, keys);
   }

   @Override
   public URL getURLProperty(URL defaultValue, String... keys) {
      return fromCache(defaultValue, URL.class, keys);
   }

   @Override
   public File[] getFileProperties(File[] defaultValue, String... keys) {
      return fromCache(defaultValue, File[].class, keys);
   }

   @Override
   public File getFileProperty(File defaultValue, String... keys) {
      return fromCache(defaultValue, File.class, keys);
   }

   @Override
   public long[] getLongProperties(long[] defaultValue, String... keys) {
      return fromCache(defaultValue, long[].class, keys);
   }

   @Override
   public int[] getIntegerProperties(int[] defaultValue, String... keys) {
      return fromCache(defaultValue, int[].class, keys);
   }

   @Override
   public String[] getStringProperties(String[] defaultValue, String... keys) {
      return fromCache(defaultValue, String[].class, keys);
   }

   @Override
   public long getLongProperty(Long defaultValue, String... keys) {
      return fromCache(defaultValue, long.class, keys);
   }

   @Override
   public int getIntegerProperty(Integer defaultValue, String... keys) {
      return fromCache(defaultValue, int.class, keys);
   }

   @Override
   public Pattern getRegexProperty(Pattern defaultValue, String... keys) {
      return fromCache(defaultValue, Pattern.class, keys);
   }

   @Override
   public Pattern[] getRegexProperties(Pattern[] defaultValue, String... keys) {
      return fromCache(defaultValue, Pattern[].class, keys);
   }

   @Override
   public <T> T getGenericProperty(T defaultValue, Class<T> clazz, String... keys) {
      return fromCache(defaultValue, clazz, keys);
   }
   
   public void clearCache() {
       cache.clear();
   }

}
