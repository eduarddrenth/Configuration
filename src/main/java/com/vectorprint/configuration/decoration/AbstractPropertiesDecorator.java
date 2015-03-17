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
import java.awt.Color;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Base class for all decorators that add functionality to properties. All implemented methods just call the embedded
 * {@link EnhancedMap}.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public abstract class AbstractPropertiesDecorator implements EnhancedMap {
   
   private EnhancedMap settings;

   /**
    * 
    * @param settings may not be null an may not implement {@link DoNotWrap}
    */
   public AbstractPropertiesDecorator(EnhancedMap settings) {
      if (settings==null) {
         throw new VectorPrintRuntimeException("settings may not be null");
      } if (settings instanceof DoNotWrap) {
         throw new VectorPrintRuntimeException(String.format("settings may not be wrapped: %s",settings));
      }
      this.settings = settings;
   }

   @Override
   public boolean getBooleanProperty(String key, Boolean defaultValue) {
      return getEmbeddedProperties().getBooleanProperty(key, defaultValue);
   }

   @Override
   public Color getColorProperty(String key, Color defaultValue) {
      return getEmbeddedProperties().getColorProperty(key, defaultValue);
   }

   @Override
   public double getDoubleProperty(String key, Double defaultValue) {
      return getEmbeddedProperties().getDoubleProperty(key, defaultValue);
   }

   @Override
   public float getFloatProperty(String key, Float defaultValue) {
      return getEmbeddedProperties().getFloatProperty(key, defaultValue);
   }

   @Override
   public int getIntegerProperty(String key, Integer defaultValue) {
      return getEmbeddedProperties().getIntegerProperty(key, defaultValue);
   }

   @Override
   public long getLongProperty(String key, Long defaultValue) {
      return getEmbeddedProperties().getLongProperty(key, defaultValue);
   }

   @Override
   public String getProperty(String key) {
      return getEmbeddedProperties().getProperty(key);
   }

   @Override
   public String getProperty(String key, String defaultValue) {
      return getEmbeddedProperties().getProperty(key, defaultValue);
   }

   @Override
   public String[] getStringProperties(String key, String[] defaultValue) {
      return getEmbeddedProperties().getStringProperties(key, defaultValue);
   }

   @Override
   public float[] getFloatProperties(String key, float[] defaultValue) {
      return getEmbeddedProperties().getFloatProperties(key, defaultValue);
   }

   @Override
   public double[] getDoubleProperties(String key, double[] defaultValue) {
      return getEmbeddedProperties().getDoubleProperties(key, defaultValue);
   }

   @Override
   public int[] getIntegerProperties(String key, int[] defaultValue) {
      return getEmbeddedProperties().getIntegerProperties(key, defaultValue);
   }

   @Override
   public long[] getLongProperties(String key, long[] defaultValue) {
      return getEmbeddedProperties().getLongProperties(key, defaultValue);
   }

   @Override
   public boolean[] getBooleanProperties(String key, boolean[] defaultValue) {
      return getEmbeddedProperties().getBooleanProperties(key, defaultValue);
   }

   @Override
   public Color[] getColorProperties(String key, Color[] defaultValue) {
      return getEmbeddedProperties().getColorProperties(key, defaultValue);
   }

   @Override
   public PropertyHelp getHelp(String key) {
      return getEmbeddedProperties().getHelp(key);
   }

   @Override
   public Map<String, PropertyHelp> getHelp() {
      return getEmbeddedProperties().getHelp();
   }

   @Override
   public String printHelp() {
      return getEmbeddedProperties().printHelp();
   }

   @Override
   public void listProperties(PrintStream ps) {
      getEmbeddedProperties().listProperties(ps);
   }

   @Override
   public void setHelp(Map<String, PropertyHelp> h) {
      getEmbeddedProperties().setHelp(h);
   }

   @Override
   public void addFromArguments(String[] args) {
         Map<String, String> props = ArgumentParser.parseArgs(args);
         if (props != null) {
            putAll(props);
         }
   }

   @Override
   public int size() {
      return getEmbeddedProperties().size();
   }

   @Override
   public boolean isEmpty() {
      return getEmbeddedProperties().isEmpty();
   }

   @Override
   public boolean containsKey(Object key) {
      return getEmbeddedProperties().containsKey(key);
   }

   @Override
   public boolean containsValue(Object value) {
      return getEmbeddedProperties().containsValue(value);
   }

   @Override
   public String get(Object key) {
      return getEmbeddedProperties().get(key);
   }

   @Override
   public String put(String key, String value) {
      return getEmbeddedProperties().put(key, value);
   }

   @Override
   public String remove(Object key) {
      return getEmbeddedProperties().remove(key);
   }

   @Override
   public void putAll(Map<? extends String, ? extends String> m) {
      getEmbeddedProperties().putAll(m);
   }

   @Override
   public void clear() {
      getEmbeddedProperties().clear();
   }

   @Override
   public Set<String> keySet() {
      return getEmbeddedProperties().keySet();
   }

   @Override
   public Collection values() {
      return getEmbeddedProperties().values();
   }

   @Override
   public Set<Entry<String, String>> entrySet() {
      return getEmbeddedProperties().entrySet();
   }

   @Override
   public EnhancedMap clone() {
      throw new VectorPrintRuntimeException("clone should be implemented by: " + getClass().getName());
   }

   protected final EnhancedMap getEmbeddedProperties() {
      return settings;
   }

   /**
    * return the first EnhancedMap in the stack of decorators that is an implementation of the class argument.
    * @param clazz
    * @return 
    */
   public final <E extends EnhancedMap> E getEmbeddedProperties(Class<E> clazz) {
      EnhancedMap inner = settings;
      while (inner != null) {
         if (clazz.isAssignableFrom(inner.getClass())) {
            return (E) inner;
         } else if (inner instanceof AbstractPropertiesDecorator) {
            inner = ((AbstractPropertiesDecorator)inner).getEmbeddedProperties();
         } else {
            inner = null;
         }
      }
      return null;
   }
   
   @Override
   public String getId() {
      return getEmbeddedProperties().getId();
   }


   @Override
   public URL getURLProperty(String key, URL defaultValue) throws MalformedURLException {
      return getEmbeddedProperties().getURLProperty(key, defaultValue);
   }

   

   @Override
   public URL[] getURLProperties(String key, URL[] defaultValue) throws MalformedURLException {
      return getEmbeddedProperties().getURLProperties(key, defaultValue);
   }

   /**
    * Provides a a recursive listing of all decorators for application properties.
    *
    * @return a recursive listing of all decorators for application properties
    */
   public Collection<Class<? extends AbstractPropertiesDecorator>> listDecorators() {
      Collection<Class<? extends AbstractPropertiesDecorator>> decorators
          = new ArrayList<Class<? extends AbstractPropertiesDecorator>>(5);
      decorators.add(getClass());
      return decorators(decorators, getEmbeddedProperties());
   }

   private Collection<Class<? extends AbstractPropertiesDecorator>> decorators(Collection<Class<? extends AbstractPropertiesDecorator>> decorators, EnhancedMap nested) {
      if (nested instanceof AbstractPropertiesDecorator) {
         decorators.add(((AbstractPropertiesDecorator) nested).getClass());
         try {
            return decorators(decorators, ((AbstractPropertiesDecorator) nested).getEmbeddedProperties());
         } catch (VectorPrintRuntimeException e) {
            return decorators;
         }
      }
      return decorators;
   }

   @Override
   public <T> T getGenericProperty(String key, T defaultValue, Class<T> clazz) {
      return getEmbeddedProperties().getGenericProperty(key, defaultValue, clazz);
   }

   @Override
   public short getShortProperty(String key, Short defaultValue) {
      return getEmbeddedProperties().getShortProperty(key, defaultValue);
   }

   @Override
   public char getCharProperty(String key, Character defaultValue) {
      return getEmbeddedProperties().getCharProperty(key, defaultValue);
   }

   @Override
   public byte getByteProperty(String key, Byte defaultValue) {
      return getEmbeddedProperties().getByteProperty(key, defaultValue);
   }

   @Override
   public short[] getShortProperties(String key, short[] defaultValue) {
      return getEmbeddedProperties().getShortProperties(key, defaultValue);
   }

   @Override
   public char[] getCharProperties(String key, char[] defaultValue) {
      return getEmbeddedProperties().getCharProperties(key, defaultValue);

   }

   @Override
   public byte[] getByteProperties(String key, byte[] defaultValue) {
      return getEmbeddedProperties().getByteProperties(key, defaultValue);

   }

   @Override
   public Date getDateProperty(String key, Date defaultValue) {
      return getEmbeddedProperties().getDateProperty(key, defaultValue);
   }

   @Override
   public Date[] getDateProperties(String key, Date[] defaultValue) {
      return getEmbeddedProperties().getDateProperties(key, defaultValue);
   }

   @Override
   public Collection<String> getUnusedKeys() {
      return getEmbeddedProperties().getUnusedKeys();
   }

   @Override
   public Collection<String> getKeysNotPresent() {
      return getEmbeddedProperties().getKeysNotPresent();
   }

   @Override
   public Class getClassProperty(String key, Class defaultValue) throws ClassNotFoundException {
      return getEmbeddedProperties().getClassProperty(key, defaultValue);
   }

   @Override
   public Class[] getClassProperties(String key, Class[] defaultValue) throws ClassNotFoundException {
      return getEmbeddedProperties().getClassProperties(key, defaultValue);
   }

   @Override
   public boolean isFromArguments(String key) {
      return getEmbeddedProperties().isFromArguments(key);
   }

   @Override
   public void setId(String id) {
      getEmbeddedProperties().setId(id);
   }

   /**
    * checks for equality of the embedded properties in the decorator
    * @param obj
    * @return 
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (!(obj instanceof AbstractPropertiesDecorator)) {
         return false;
      }
      AbstractPropertiesDecorator apd = (AbstractPropertiesDecorator) obj;
      if (!apd.getEmbeddedProperties().equals(getEmbeddedProperties())) {
         return false;
      }
      return true;
   }

   
}
