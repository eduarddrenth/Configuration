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
import com.vectorprint.configuration.ArgumentParser;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.PropertyHelp;
import com.vectorprint.configuration.observing.PrepareKeyValue;
import java.awt.Color;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Adds multiplicity to properties, approaching multiple {@link EnhancedMap}s when dealing with properties. Methods that
 * take a key argument lookup the first {@link EnhancedMap} containing that key and call that {@link EnhancedMap},
 * otherwise the default or null is returned. Informational methods (size()) etc. return a sum of the containing
 * {@link EnhancedMap}s. Setters call all the embedded {@link EnhancedMap}s, or the first one found.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class MultipleProperties extends AbstractPropertiesDecorator {

   private final Collection<EnhancedMap> allProperties = new HashSet<EnhancedMap>(6);

   public MultipleProperties(EnhancedMap properties) {
      allProperties.add(properties);
   }

   public MultipleProperties addProperties(EnhancedMap properties) {
      allProperties.add(properties);
      return this;
   }

   public boolean removeProperties(EnhancedMap properties) {
      return allProperties.remove(properties);
   }

   @Override
   public boolean isFromArguments(String key) {
      EnhancedMap em = find(key, "");
      return em != null && em.isFromArguments(key);
   }

   /**
    *
    * @param key the value of key
    * @param defaultVal the value of defaultVal
    */
   private EnhancedMap find(String key, Object defaultVal) {
      for (EnhancedMap em : allProperties) {
         if (em.containsKey(key)) {
            return em;
         }
      }
      if (defaultVal == null) {
         throw new VectorPrintRuntimeException(key + " not found in " +getId()+ " and default is null");
      }
      return null;
   }

   @Override
   public boolean getBooleanProperty(String key, Boolean defaultValue) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getBooleanProperty(key, defaultValue);
      }
   }

   @Override
   public Color getColorProperty(String key, Color defaultValue) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getColorProperty(key, defaultValue);
      }
   }

   @Override
   public double getDoubleProperty(String key, Double defaultValue) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getDoubleProperty(key, defaultValue);
      }
   }

   @Override
   public float getFloatProperty(String key, Float defaultValue) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getFloatProperty(key, defaultValue);
      }
   }

   @Override
   public int getIntegerProperty(String key, Integer defaultValue) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getIntegerProperty(key, defaultValue);
      }
   }

   @Override
   public long getLongProperty(String key, Long defaultValue) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getLongProperty(key, defaultValue);
      }
   }

   @Override
   public String getProperty(String key) {
      EnhancedMap em = find(key, null);
      if (em == null) {
         return null;
      } else {
         return em.getProperty(key);
      }
   }

   @Override
   public String getProperty(String key, String defaultValue) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getProperty(key, defaultValue);
      }
   }

   @Override
   public String[] getStringProperties(String key, String[] defaultValue) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getStringProperties(key, defaultValue);
      }
   }

   @Override
   public float[] getFloatProperties(String key, float[] defaultValue) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getFloatProperties(key, defaultValue);
      }
   }

   @Override
   public double[] getDoubleProperties(String key, double[] defaultValue) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getDoubleProperties(key, defaultValue);
      }
   }

   @Override
   public int[] getIntegerProperties(String key, int[] defaultValue) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getIntegerProperties(key, defaultValue);
      }
   }

   @Override
   public long[] getLongProperties(String key, long[] defaultValue) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getLongProperties(key, defaultValue);
      }
   }

   @Override
   public Color[] getColorProperties(String key, Color[] defaultValue) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getColorProperties(key, defaultValue);
      }
   }

   @Override
   public boolean[] getBooleanProperties(String key, boolean[] defaultValue) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getBooleanProperties(key, defaultValue);
      }
   }

   @Override
   public URL getURLProperty(String key, URL defaultValue) throws MalformedURLException {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getURLProperty(key, defaultValue);
      }
   }

   @Override
   public URL[] getURLProperties(String key, URL[] defaultValue) throws MalformedURLException {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getURLProperties(key, defaultValue);
      }
   }

   @Override
   public <T> T getGenericProperty(String key, T defaultValue, Class<T> clazz) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getGenericProperty(key, defaultValue, clazz);
      }
   }

   @Override
   public PropertyHelp getHelp(String key) {
      EnhancedMap em = find(key, "");
      if (em == null) {
         return null;
      } else {
         return em.getHelp(key);
      }
   }

   @Override
   public Map<String, PropertyHelp> getHelp() {
      Map<String, PropertyHelp> help = new HashMap<String, PropertyHelp>(100);
      for (EnhancedMap em : allProperties) {
         help.putAll(em.getHelp());
      }
      return help;
   }

   @Override
   public String printHelp() {
      StringBuilder sb = new StringBuilder();
      for (EnhancedMap em : allProperties) {
         sb.append(em.printHelp());
      }
      return sb.toString();
   }

   @Override
   public void listProperties(PrintStream ps) {
      for (EnhancedMap em : allProperties) {
         em.listProperties(ps);
      }
   }

   /**
    * Set help in the first {@link EnhancedMap} found.
    *
    * @param h
    */
   @Override
   public void setHelp(Map<String, PropertyHelp> h) {
      if (!allProperties.isEmpty()) {
         allProperties.iterator().next().setHelp(h);
      }
   }

   @Override
   public void addFromArguments(String[] args) {
      putAll((Map<String, String>) ArgumentParser.parseArgs(args));
   }

   @Override
   public void addObserver(PrepareKeyValue<String, String> observer) {
      for (EnhancedMap em : allProperties) {
         em.addObserver(observer);
      }
   }

   private MultipleProperties() {
   }

   @Override
   public EnhancedMap clone() {
      MultipleProperties mp = new MultipleProperties();
      for (EnhancedMap em : allProperties) {
         mp.allProperties.add(em.clone());
      }
      return mp;
   }

   @Override
   public int size() {
      int size = 0;
      for (EnhancedMap em : allProperties) {
         size += em.size();
      }
      return size;
   }

   @Override
   public boolean isEmpty() {
      return size() == 0;
   }

   @Override
   public boolean containsKey(Object key) {
      for (EnhancedMap em : allProperties) {
         if (em.containsKey(key)) {
            return true;
         }
      }
      return false;
   }

   @Override
   public boolean containsValue(Object value) {
      for (EnhancedMap em : allProperties) {
         if (em.containsValue(value)) {
            return true;
         }
      }
      return false;
   }

   @Override
   public String get(Object key) {
      EnhancedMap em = find(String.valueOf(key), "");
      if (em != null) {
         return em.get(key);
      }
      return null;
   }

   /**
    * Sets the value for a key in all {@link EnhancedMap}s where the key is found, when the key is not found put the
    * value in the first {@link EnhancedMap}.
    *
    * @param key
    * @param value
    * @return the first value for a key if it was found
    */
   @Override
   public String put(String key, String value) {
      String previous = null;
      EnhancedMap toPut = null;
      boolean mustPut = true;
      for (EnhancedMap em : allProperties) {
         toPut = em;
         if (em.containsKey(key)) {
            mustPut = false;
            String s = em.put(key, value);
            if (previous == null) {
               previous = s;
            }
         }
      }
      if (mustPut && toPut != null) {
         String s = toPut.put(key, value);
         if (previous == null) {
            previous = s;
         }
      }
      return previous;
   }

   @Override
   public String remove(Object key) {
      String previous = null;
      for (EnhancedMap em : allProperties) {
         if (em.containsKey(key)) {
            String s = em.remove(key);
            if (previous == null) {
               previous = s;
            }
         }
      }
      return previous;
   }

   @Override
   public void putAll(Map<? extends String, ? extends String> m) {
      for (EnhancedMap em : allProperties) {
         em.putAll(m);
      }
   }

   @Override
   public void clear() {
      for (EnhancedMap em : allProperties) {
         em.clear();
      }
   }

   @Override
   public Set<String> keySet() {
      return combineValues(new HashSet<String>(size()), (short) 0);
   }

   @Override
   public Collection<String> getUnusedKeys() {
      Collection<String> unused = new HashSet<String>(size());
      for (EnhancedMap em : allProperties) {
         unused.addAll(em.getUnusedKeys());
      }
      return unused;
   }

   @Override
   public Collection<String> getKeysNotPresent() {
      Collection<String> notPresent = new HashSet<String>(size());
      for (EnhancedMap em : allProperties) {
         notPresent.addAll(em.getKeysNotPresent());
      }
      return notPresent;
   }

   @Override
   public Collection values() {
      return combineValues(new HashSet(size()), (short) 1);
   }

   @Override
   public Set<Entry<String, String>> entrySet() {
      return combineValues(new HashSet<Entry<String, String>>(size()), (short) 2);
   }

   private <C extends Collection<T>, T> C combineValues(C values, short type) {
      for (Iterator<EnhancedMap> it = allProperties.iterator(); it.hasNext();) {
         EnhancedMap mp = it.next();
         switch (type) {
            case 0:
               values.addAll((C) mp.keySet());
               break;
            case 1:
               values.addAll((C) mp.values());
               break;
            case 2:
               values.addAll((C) mp.entrySet());
               break;
         }
      }
      return values;
   }

   /**
    * concatenation of all id's.
    *
    * @return
    */
   @Override
   public String getId() {
      StringBuilder id = new StringBuilder(allProperties.size() * 20);
      for (EnhancedMap em : allProperties) {
         id.append(em.getId()).append(',');
      }
      return id.substring(0, id.length());
   }

   /**
    * not supported
    *
    * @return
    */
   @Override
   protected EnhancedMap getEmbeddedProperties() {
      throw new VectorPrintRuntimeException("This class cannot support this method because it embeds multiple EnhancedMaps.");
   }

   @Override
   public Class[] getClassProperties(String key, Class[] defaultValue) throws ClassNotFoundException {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getClassProperties(key, defaultValue);
      }
   }

   @Override
   public Class getClassProperty(String key, Class defaultValue) throws ClassNotFoundException {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getClassProperty(key, defaultValue);
      }
   }

   @Override
   public Date[] getDateProperties(String key, Date[] defaultValue) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getDateProperties(key, defaultValue);
      }
   }

   @Override
   public Date getDateProperty(String key, Date defaultValue) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getDateProperty(key, defaultValue);
      }
   }

   @Override
   public byte[] getByteProperties(String key, byte[] defaultValue) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getByteProperties(key, defaultValue);
      }
   }

   @Override
   public char[] getCharProperties(String key, char[] defaultValue) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getCharProperties(key, defaultValue);
      }
   }

   @Override
   public short[] getShortProperties(String key, short[] defaultValue) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getShortProperties(key, defaultValue);
      }
   }

   @Override
   public byte getByteProperty(String key, Byte defaultValue) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getByteProperty(key, defaultValue);
      }
   }

   @Override
   public char getCharProperty(String key, Character defaultValue) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getCharProperty(key, defaultValue);
      }
   }

   @Override
   public short getShortProperty(String key, Short defaultValue) {
      EnhancedMap em = find(key, defaultValue);
      if (em == null) {
         return defaultValue;
      } else {
         return em.getShortProperty(key, defaultValue);
      }
   }

}
