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
import java.util.Map;

/**
 * A Caching {@link EnhancedMap}.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class CachingProperties extends AbstractPropertiesDecorator {


   public CachingProperties(EnhancedMap properties) {
      super(properties);
   }

   @Override
   public EnhancedMap clone() {
      return new CachingProperties(getEmbeddedProperties().clone());
   }

   private Map<String, Object> cache = new HashMap<String, Object>();

   @Override
   public Date[] getDateProperties(String key, Date[] defaultValue) {
      return fromCache(key, defaultValue, Date[].class);
   }

   private <T> T fromCache(String key, T defaultValue, Class<T> clazz) {
      if (!cache.containsKey(key)) {
         cache.put(key, super.getGenericProperty(key, defaultValue, clazz));
      } else if (null != cache.get(key)) {
         Class c = cache.get(key).getClass();
         if (clazz.isPrimitive()) {
            if (!checkPrimitive(clazz, c)) {
               cache.remove(key);
               throw new VectorPrintRuntimeException(String.format("class for %s in cache is %s, this does not match requested class: %s. Removed from cache.",
                   key, c.getName(), clazz.getName()));
            }
         } else if (!clazz.isAssignableFrom(cache.get(key).getClass())) {
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
   public Date getDateProperty(String key, Date defaultValue) {
      return fromCache(key, defaultValue, Date.class);
   }

   @Override
   public byte[] getByteProperties(String key, byte[] defaultValue) {
      return fromCache(key, defaultValue, byte[].class);
   }

   @Override
   public char[] getCharProperties(String key, char[] defaultValue) {
      return fromCache(key, defaultValue, char[].class);
   }

   @Override
   public byte getByteProperty(String key, Byte defaultValue) {
      return fromCache(key, defaultValue, byte.class);
   }

   @Override
   public char getCharProperty(String key, Character defaultValue) {
      return fromCache(key, defaultValue, char.class);
   }

   @Override
   public <T> T getGenericProperty(String key, T defaultValue, Class<T> clazz) {
      return fromCache(key, defaultValue, clazz);
   }

   @Override
   public String remove(Object key) {
      cache.remove(key);
      return super.remove(key);
   }

   @Override
   public String put(String key, String value) {
      cache.remove(key);
      return super.put(key, value);
   }

   @Override
   public Color[] getColorProperties(String key, Color[] defaultValue) {
      return fromCache(key, defaultValue, Color[].class);
   }

   @Override
   public boolean[] getBooleanProperties(String key, boolean[] defaultValue) {
      return fromCache(key, defaultValue, boolean[].class);
   }

   @Override
   public double[] getDoubleProperties(String key, double[] defaultValue) {
      return fromCache(key, defaultValue, double[].class);
   }

   @Override
   public float[] getFloatProperties(String key, float[] defaultValue) {
      return fromCache(key, defaultValue, float[].class);
   }

   @Override
   public float getFloatProperty(String key, Float defaultValue) {
      return fromCache(key, defaultValue, float.class);
   }

   @Override
   public double getDoubleProperty(String key, Double defaultValue) {
      return fromCache(key, defaultValue, double.class);
   }

   @Override
   public Color getColorProperty(String key, Color defaultValue) {
      return fromCache(key, defaultValue, Color.class);
   }

   @Override
   public boolean getBooleanProperty(String key, Boolean defaultValue) {
      return fromCache(key, defaultValue, boolean.class);
   }

   @Override
   public Class getClassProperty(String key, Class defaultValue) throws ClassNotFoundException {
      return fromCache(key, defaultValue, Class.class);
   }

   @Override
   public Class[] getClassProperties(String key, Class[] defaultValue) throws ClassNotFoundException {
      return fromCache(key, defaultValue, Class[].class);
   }

   @Override
   public short[] getShortProperties(String key, short[] defaultValue) {
      return fromCache(key, defaultValue, short[].class);
   }

   @Override
   public short getShortProperty(String key, Short defaultValue) {
      return fromCache(key, defaultValue, short.class);
   }

   @Override
   public URL[] getURLProperties(String key, URL[] defaultValue) throws MalformedURLException {
      return fromCache(key, defaultValue, URL[].class);
   }

   @Override
   public URL getURLProperty(String key, URL defaultValue) throws MalformedURLException {
      return fromCache(key, defaultValue, URL.class);
   }

   @Override
   public long[] getLongProperties(String key, long[] defaultValue) {
      return fromCache(key, defaultValue, long[].class);
   }

   @Override
   public int[] getIntegerProperties(String key, int[] defaultValue) {
      return fromCache(key, defaultValue, int[].class);
   }

   @Override
   public String[] getStringProperties(String key, String[] defaultValue) {
      return fromCache(key, defaultValue, String[].class);
   }

   @Override
   public String getProperty(String key) {
      return fromCache(key, null, String.class);
   }

   @Override
   public long getLongProperty(String key, Long defaultValue) {
      return fromCache(key, defaultValue, long.class);
   }

   @Override
   public int getIntegerProperty(String key, Integer defaultValue) {
      return fromCache(key, defaultValue, int.class);
   }

}
