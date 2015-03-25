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
import com.vectorprint.configuration.decoration.visiting.DecoratorVisitor;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.ArgumentParser;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.PropertyHelp;
import com.vectorprint.configuration.Settings;
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessorImpl;
import java.awt.Color;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
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
    * Will call {@link ApplicationSettings#addDecorator(java.lang.Class) } and 
    * {@link ApplicationSettings#setOutermostWrapper(com.vectorprint.configuration.decoration.AbstractPropertiesDecorator) }.
    * 
    * @param settings may not be null
    * @throws VectorPrintRuntimeException when a decorator of this type is already there or when this decorator {@link HiddenBy hides}
    * another decorator or when the argument is not an instance of {@link Settings} or {@link AbstractPropertiesDecorator}.
    */
   public AbstractPropertiesDecorator(EnhancedMap settings) {
      if (settings == null) {
         throw new VectorPrintRuntimeException("settings may not be null");
      }
      if (hasProperties(settings.getClass())) {
         throw new VectorPrintRuntimeException(String.format("settings already in the stack: %s", settings.getClass().getName()));
      }
      if (!(settings instanceof Settings || settings instanceof AbstractPropertiesDecorator)) {
         throw new VectorPrintRuntimeException(String.format("%s is not an instance of %s or %s", 
             settings.getClass().getName(),
             Settings.class.getName(),
             AbstractPropertiesDecorator.class.getName()));
      }
      this.settings = settings;
      accept(new Hiding(this));
      accept(new WrapperOveriew(getApplicationSettings()));
   }

   @Override
   public boolean getBooleanProperty(String key, Boolean defaultValue) {
      return settings.getBooleanProperty(key, defaultValue);
   }

   @Override
   public Color getColorProperty(String key, Color defaultValue) {
      return settings.getColorProperty(key, defaultValue);
   }

   @Override
   public double getDoubleProperty(String key, Double defaultValue) {
      return settings.getDoubleProperty(key, defaultValue);
   }

   @Override
   public float getFloatProperty(String key, Float defaultValue) {
      return settings.getFloatProperty(key, defaultValue);
   }

   @Override
   public int getIntegerProperty(String key, Integer defaultValue) {
      return settings.getIntegerProperty(key, defaultValue);
   }

   @Override
   public long getLongProperty(String key, Long defaultValue) {
      return settings.getLongProperty(key, defaultValue);
   }

   @Override
   public String getProperty(String key) {
      return settings.getProperty(key);
   }

   @Override
   public String getProperty(String key, String defaultValue) {
      return settings.getProperty(key, defaultValue);
   }

   @Override
   public String[] getStringProperties(String key, String[] defaultValue) {
      return settings.getStringProperties(key, defaultValue);
   }

   @Override
   public float[] getFloatProperties(String key, float[] defaultValue) {
      return settings.getFloatProperties(key, defaultValue);
   }

   @Override
   public double[] getDoubleProperties(String key, double[] defaultValue) {
      return settings.getDoubleProperties(key, defaultValue);
   }

   @Override
   public int[] getIntegerProperties(String key, int[] defaultValue) {
      return settings.getIntegerProperties(key, defaultValue);
   }

   @Override
   public long[] getLongProperties(String key, long[] defaultValue) {
      return settings.getLongProperties(key, defaultValue);
   }

   @Override
   public boolean[] getBooleanProperties(String key, boolean[] defaultValue) {
      return settings.getBooleanProperties(key, defaultValue);
   }

   @Override
   public Color[] getColorProperties(String key, Color[] defaultValue) {
      return settings.getColorProperties(key, defaultValue);
   }

   @Override
   public PropertyHelp getHelp(String key) {
      return settings.getHelp(key);
   }

   @Override
   public Map<String, PropertyHelp> getHelp() {
      return settings.getHelp();
   }

   @Override
   public String printHelp() {
      return settings.printHelp();
   }

   @Override
   public void listProperties(PrintStream ps) {
      settings.listProperties(ps);
   }

   @Override
   public void setHelp(Map<String, PropertyHelp> h) {
      settings.setHelp(h);
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
      return settings.size();
   }

   @Override
   public boolean isEmpty() {
      return settings.isEmpty();
   }

   @Override
   public boolean containsKey(Object key) {
      return settings.containsKey(key);
   }

   @Override
   public boolean containsValue(Object value) {
      return settings.containsValue(value);
   }

   @Override
   public String get(Object key) {
      return settings.get(key);
   }

   @Override
   public String put(String key, String value) {
      return settings.put(key, value);
   }

   @Override
   public String remove(Object key) {
      return settings.remove(key);
   }

   @Override
   public void putAll(Map<? extends String, ? extends String> m) {
      settings.putAll(m);
   }

   @Override
   public void clear() {
      settings.clear();
   }

   @Override
   public Set<String> keySet() {
      return settings.keySet();
   }

   @Override
   public Collection values() {
      return settings.values();
   }

   @Override
   public Set<Entry<String, String>> entrySet() {
      return settings.entrySet();
   }

   @Override
   public EnhancedMap clone() {
      try {
         AbstractPropertiesDecorator clone = (AbstractPropertiesDecorator) super.clone();
         clone.settings = settings.clone();
         return clone;
      } catch (CloneNotSupportedException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   /**
    * returns true if an EnhancedMap is present in the stack of decorators that is an implementation of the class
    * argument.
    *
    * @param clazz
    * @return
    */
   public final boolean hasProperties(Class<? extends EnhancedMap> clazz) {
      EnhancedMap inner = this;
      while (inner != null) {
         if (clazz.isAssignableFrom(inner.getClass())) {
            return true;
         } else if (inner instanceof AbstractPropertiesDecorator) {
            inner = ((AbstractPropertiesDecorator) inner).settings;
         } else {
            inner = null;
         }
      }
      return false;
   }

   private final Settings getApplicationSettings() {
      EnhancedMap inner = settings;
      while (inner != null) {
         if (inner instanceof Settings) {
            return (Settings) inner;
         }
         if (inner instanceof AbstractPropertiesDecorator) {
            inner = ((AbstractPropertiesDecorator) inner).settings;
         } else {
            inner = null;
         }
      }
      throw new VectorPrintRuntimeException(String.format("no %s found", Settings.class.getName()));
   }

   /**
    * traverse the stack of settings decorators and visit all that are instances of {@link DecoratorVisitor#getClazzToVisit()
    * }. {@link DecoratorVisitor#visit(com.vectorprint.configuration.EnhancedMap, java.lang.Object) } will be called
    *
    * @param dv
    * @see SettingsAnnotationProcessorImpl
    */
   public final void accept(DecoratorVisitor dv) {
      EnhancedMap inner = this;
      while (inner != null) {
         if (dv.getClazzToVisit().isAssignableFrom(inner.getClass())) {
            dv.visit(inner);
         }
         if (inner instanceof AbstractPropertiesDecorator) {
            inner = ((AbstractPropertiesDecorator) inner).settings;
         } else {
            inner = null;
         }
      }
   }

   @Override
   public String getId() {
      return settings.getId();
   }

   @Override
   public URL getURLProperty(String key, URL defaultValue) throws MalformedURLException {
      return settings.getURLProperty(key, defaultValue);
   }

   @Override
   public URL[] getURLProperties(String key, URL[] defaultValue) throws MalformedURLException {
      return settings.getURLProperties(key, defaultValue);
   }

   @Override
   public <T> T getGenericProperty(T defaultValue, Class<T> clazz, String... keys) {
      return settings.getGenericProperty(defaultValue, clazz, keys);
   }

   @Override
   public short getShortProperty(String key, Short defaultValue) {
      return settings.getShortProperty(key, defaultValue);
   }

   @Override
   public char getCharProperty(String key, Character defaultValue) {
      return settings.getCharProperty(key, defaultValue);
   }

   @Override
   public byte getByteProperty(String key, Byte defaultValue) {
      return settings.getByteProperty(key, defaultValue);
   }

   @Override
   public short[] getShortProperties(String key, short[] defaultValue) {
      return settings.getShortProperties(key, defaultValue);
   }

   @Override
   public char[] getCharProperties(String key, char[] defaultValue) {
      return settings.getCharProperties(key, defaultValue);

   }

   @Override
   public byte[] getByteProperties(String key, byte[] defaultValue) {
      return settings.getByteProperties(key, defaultValue);

   }

   @Override
   public Date getDateProperty(String key, Date defaultValue) {
      return settings.getDateProperty(key, defaultValue);
   }

   @Override
   public Date[] getDateProperties(String key, Date[] defaultValue) {
      return settings.getDateProperties(key, defaultValue);
   }

   @Override
   public Collection<String> getUnusedKeys() {
      return settings.getUnusedKeys();
   }

   @Override
   public Collection<String> getKeysNotPresent() {
      return settings.getKeysNotPresent();
   }

   @Override
   public Class getClassProperty(String key, Class defaultValue) throws ClassNotFoundException {
      return settings.getClassProperty(key, defaultValue);
   }

   @Override
   public Class[] getClassProperties(String key, Class[] defaultValue) throws ClassNotFoundException {
      return settings.getClassProperties(key, defaultValue);
   }

   @Override
   public boolean isFromArguments(String key) {
      return settings.isFromArguments(key);
   }

   @Override
   public void setId(String id) {
      settings.setId(id);
   }

   /**
    * checks for equality of the embedded properties in the decorator
    *
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
      if (!apd.settings.equals(settings)) {
         return false;
      }
      return true;
   }

   /**
    * for serialization, writes the embedded settings to the stream, call this in subclasses after you have doen your
    * own serialization
    *
    * @param s
    */
   protected void writeEmbeddedSettings(java.io.ObjectOutputStream s) throws IOException {
      s.writeObject(settings);
   }

   private static class WrapperOveriew implements DecoratorVisitor<AbstractPropertiesDecorator> {

      private final Settings vp;

      public WrapperOveriew(Settings vp) {
         this.vp = vp;
      }

      @Override
      public Class<AbstractPropertiesDecorator> getClazzToVisit() {
         return AbstractPropertiesDecorator.class;
      }

      @Override
      public void visit(AbstractPropertiesDecorator e) {
         if (!vp.getDecorators().contains(e.getClass())) {
            vp.addDecorator(e.getClass());
            vp.setOutermostWrapper(e);
         }
      }

   }
   private static class Hiding implements DecoratorVisitor<EnhancedMap> {

      private final AbstractPropertiesDecorator settings;

      public Hiding(AbstractPropertiesDecorator settings) {
         this.settings = settings;
      }

      @Override
      public Class<EnhancedMap> getClazzToVisit() {
         return EnhancedMap.class;
      }

      @Override
      public void visit(EnhancedMap e) {
         if (e instanceof HiddenBy) {
            if (((HiddenBy)e).hiddenBy(settings.getClass())) {
               throw new VectorPrintRuntimeException(String.format("%s hides %s",
                   settings.getClass().getName(),e.getClass().getName()));
            }
         }
      }

   }
}
