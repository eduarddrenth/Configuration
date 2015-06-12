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
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A Caching {@link EnhancedMap}.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class CachingProperties extends AbstractPropertiesDecorator {

   public CachingProperties(EnhancedMap settings) {
      super(settings);
   }

   @Override
   public EnhancedMap clone() {
      return super.clone();
   }

   private Map<String, Object> cache = new HashMap<String, Object>();

   @Override
   public Date[] getDateProperties(Date[] defaultValue, String... keys) {
      return fromCache( defaultValue, Date[].class, keys);
   }
   
   private String cacheKey(String... keys) {
      String s = keys[0];
      for (int i = 1; i < keys.length; i++) {
         s += keys[i];
      }
      return s;
   }

   private <T> T fromCache(T defaultValue, Class<T> clazz, String... keys) {
      String key = cacheKey(keys);
      if (!cache.containsKey(key)) {
         cache.put(key, super.getGenericProperty(defaultValue, clazz, key));
      } else if (null != cache.get(key)) {
         if (clazz.isPrimitive()) {
            Class c = cache.get(key).getClass();
            if (!checkPrimitive(clazz, c)) {
               cache.remove(key);
               throw new VectorPrintRuntimeException(String.format("class for %s in cache is %s, this does not match requested class: %s. Removed from cache.",
                   key, c.getName(), clazz.getName()));
            }
         } else if (!clazz.isAssignableFrom(cache.get(key).getClass())) {
            Class c = cache.get(key).getClass();
            cache.remove(key);
            throw new VectorPrintRuntimeException(String.format("class for %s in cache is %s, this does not match requested class: %s. Removed from cache.",
                key, c.getName(), clazz.getName()));
         }
      }
      return (T) cache.get(key);
   }

   private static boolean checkPrimitive(Class prim, Class wrapper) {
      return (prim.equals(short.class) && wrapper.equals(Short.class))
          || (prim.equals(int.class) && wrapper.equals(Integer.class))
          || (prim.equals(long.class) && wrapper.equals(Long.class))
          || (prim.equals(float.class) && wrapper.equals(Float.class))
          || (prim.equals(double.class) && wrapper.equals(Double.class))
          || (prim.equals(boolean.class) && wrapper.equals(Boolean.class))
          || (prim.equals(char.class) && wrapper.equals(Character.class))
          || (prim.equals(byte.class) && wrapper.equals(Byte.class));
   }

   @Override
   public Date getDateProperty(Date defaultValue, String... keys) {
      return fromCache( defaultValue, Date.class, keys);
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
      for (Iterator<Entry<String, Object>> it = cache.entrySet().iterator(); it.hasNext();) {
         Entry<String, Object> next = it.next();
         if (next.getKey().contains(String.valueOf(key))) {
            it.remove();
         }
      }
      return super.remove(key);
   }

   @Override
   public String[] put(String key, String value) {
      return put(key,new String[] {value});
   }

   @Override
   public String[] put(String key, String[] value) {
      for (Iterator<Entry<String, Object>> it = cache.entrySet().iterator(); it.hasNext();) {
         Entry<String, Object> next = it.next();
         if (next.getKey().contains(String.valueOf(key))) {
            it.remove();
         }
      }
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
    * @throws ClassNotFoundException
    */
   @Override
   public Class getClassProperty(Class defaultValue, String... keys) throws ClassNotFoundException {
      return fromCache(defaultValue, Class.class, keys);
   }

   /**
    *
    * @param defaultValue the value of defaultValue
    * @param keys the value of keys
    * @return 
    * @throws ClassNotFoundException
    */
   @Override
   public Class[] getClassProperties(Class[] defaultValue, String... keys) throws ClassNotFoundException {
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
   public URL[] getURLProperties(URL[] defaultValue, String... keys) throws MalformedURLException {
      return fromCache(defaultValue, URL[].class, keys);
   }

   @Override
   public URL getURLProperty(URL defaultValue, String... keys) throws MalformedURLException {
      return fromCache(defaultValue, URL.class, keys);
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
   public String getPropertyNoDefault(String... keys) {
      return fromCache(null, String.class, keys);
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

}
