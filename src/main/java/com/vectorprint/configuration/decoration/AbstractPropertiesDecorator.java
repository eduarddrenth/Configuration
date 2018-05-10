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
import com.vectorprint.configuration.PropertyHelp;
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessorImpl;
import com.vectorprint.configuration.decoration.visiting.DecoratorVisitor;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Base class for all decorators that add functionality to {@link EnhancedMap}. All implemented methods just call the
 * embedded {@link EnhancedMap}.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public abstract class AbstractPropertiesDecorator implements EnhancedMap, DecorationAware {

   private EnhancedMap settings;
   private AbstractPropertiesDecorator outermostDecorator;
   private final List<Class<? extends AbstractPropertiesDecorator>> decorators = new ArrayList<>(2);
   protected static final Logger log = Logger.getLogger(AbstractPropertiesDecorator.class.getName());

   /**
    * Will call {@link DecorationAware#addDecorator(java.lang.Class) } and
    * {@link DecorationAware#setOutermostDecorator(com.vectorprint.configuration.decoration.AbstractPropertiesDecorator) }
    *
    * @param settings may not be null
    * @throws VectorPrintRuntimeException when a decorator of this type is already there or when this decorator
    * {@link HiddenBy hides} another decorator.
    */
   public AbstractPropertiesDecorator(EnhancedMap settings) {
      if (settings == null) {
         throw new VectorPrintRuntimeException("settings may not be null");
      }
      if (hasProperties(settings.getClass())) {
         throw new VectorPrintRuntimeException(String.format("%s already in the stack", settings.getClass().getName()));
      }
      this.settings = settings;
      accept(new Hiding(this));
      accept(new DecoratorOveriew(getDecorationAwares()));
   }

   /**
    *
    * @param defaultValue the value of defaultValue
    * @param keys
    * @return the boolean
    */
   @Override
   public boolean getBooleanProperty(Boolean defaultValue, String... keys) {
      return settings.getBooleanProperty(defaultValue, keys);
   }

   @Override
   public Color getColorProperty(Color defaultValue, String... keys) {
      return settings.getColorProperty(defaultValue, keys);
   }

   @Override
   public double getDoubleProperty(Double defaultValue, String... keys) {
      return settings.getDoubleProperty(defaultValue, keys);
   }

   @Override
   public float getFloatProperty(Float defaultValue, String... keys) {
      return settings.getFloatProperty(defaultValue, keys);
   }

   @Override
   public int getIntegerProperty(Integer defaultValue, String... keys) {
      return settings.getIntegerProperty(defaultValue, keys);
   }

   @Override
   public long getLongProperty(Long defaultValue, String... keys) {
      return settings.getLongProperty(defaultValue, keys);
   }

   @Override
   public String getPropertyNoDefault(String key) {
      return settings.getPropertyNoDefault(key);
   }

   @Override
   public String getProperty(String defaultValue, String... keys) {
      return settings.getProperty(defaultValue, keys);
   }

   @Override
   public String[] getStringProperties(String[] defaultValue, String... keys) {
      return settings.getStringProperties(defaultValue, keys);
   }

   @Override
   public float[] getFloatProperties(float[] defaultValue, String... keys) {
      return settings.getFloatProperties(defaultValue, keys);
   }

   @Override
   public double[] getDoubleProperties(double[] defaultValue, String... keys) {
      return settings.getDoubleProperties(defaultValue, keys);
   }

   @Override
   public int[] getIntegerProperties(int[] defaultValue, String... keys) {
      return settings.getIntegerProperties(defaultValue, keys);
   }

   @Override
   public long[] getLongProperties(long[] defaultValue, String... keys) {
      return settings.getLongProperties(defaultValue, keys);
   }

   @Override
   public boolean[] getBooleanProperties(boolean[] defaultValue, String... keys) {
      return settings.getBooleanProperties(defaultValue, keys);
   }

   @Override
   public Color[] getColorProperties(Color[] defaultValue, String... keys) {
      return settings.getColorProperties(defaultValue, keys);
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
   public String[] get(Object key) {
      return settings.get(key);
   }

   @Override
   public String[] put(String key, String[] value) {
      return settings.put(key, value);
   }

   @Override
   public String[] remove(Object key) {
      return settings.remove(key);
   }

   @Override
   public void putAll(Map<? extends String, ? extends String[]> m) {
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
   public Set<Entry<String, String[]>> entrySet() {
      return settings.entrySet();
   }

   /**
    * clone this decorator and its contained EnhancedMap.
    *
    * @return
    */
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
         if (clazz.isInstance(inner)) {
            return true;
         } else if (inner instanceof AbstractPropertiesDecorator) {
            inner = ((AbstractPropertiesDecorator) inner).settings;
         } else {
            inner = null;
         }
      }
      return false;
   }

   private final DecorationAware[] getDecorationAwares() {
      EnhancedMap inner = settings;
      List<DecorationAware> l = new ArrayList<>(1);
      while (inner != null) {
         if (inner instanceof DecorationAware) {
            l.add((DecorationAware) inner);
         }
         if (inner instanceof AbstractPropertiesDecorator) {
            inner = ((AbstractPropertiesDecorator) inner).settings;
         } else {
            inner = null;
         }
      }
      return l.toArray(new DecorationAware[l.size()]);
   }

   /**
    * traverse the stack of settings decorators and visit all that are instances of {@link DecoratorVisitor#getClazzToVisit()
    * }. {@link DecoratorVisitor#visit(com.vectorprint.configuration.EnhancedMap) } will be called.
    *
    * @param dv
    * @see SettingsAnnotationProcessorImpl
    */
   public final void accept(DecoratorVisitor dv) {
      EnhancedMap inner = this;
      while (inner != null) {
         if (dv.shouldVisit(inner)) {
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
   public URL getURLProperty(URL defaultValue, String... keys) throws MalformedURLException {
      return settings.getURLProperty(defaultValue, keys);
   }

   @Override
   public URL[] getURLProperties(URL[] defaultValue, String... keys) throws MalformedURLException {
      return settings.getURLProperties(defaultValue, keys);
   }

   @Override
   public <T> T getGenericProperty(T defaultValue, Class<T> clazz, String... keys) {
      return settings.getGenericProperty(defaultValue, clazz, keys);
   }

   @Override
   public short getShortProperty(Short defaultValue, String... keys) {
      return settings.getShortProperty(defaultValue, keys);
   }

   @Override
   public char getCharProperty(Character defaultValue, String... keys) {
      return settings.getCharProperty(defaultValue, keys);
   }

   @Override
   public byte getByteProperty(Byte defaultValue, String... keys) {
      return settings.getByteProperty(defaultValue, keys);
   }

   @Override
   public short[] getShortProperties(short[] defaultValue, String... keys) {
      return settings.getShortProperties(defaultValue, keys);
   }

   @Override
   public char[] getCharProperties(char[] defaultValue, String... keys) {
      return settings.getCharProperties(defaultValue, keys);

   }

   @Override
   public byte[] getByteProperties(byte[] defaultValue, String... keys) {
      return settings.getByteProperties(defaultValue, keys);

   }

   @Override
   public Date getDateProperty(Date defaultValue, String... keys) {
      return settings.getDateProperty(defaultValue, keys);
   }

   @Override
   public Date[] getDateProperties(Date[] defaultValue, String... keys) {
      return settings.getDateProperties(defaultValue, keys);
   }

   @Override
   public Collection<String> getUnusedKeys() {
      return settings.getUnusedKeys();
   }

   @Override
   public Collection<String> getKeysNotPresent() {
      return settings.getKeysNotPresent();
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
      return settings.getClassProperty(defaultValue, keys);
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
      return settings.getClassProperties(defaultValue, keys);
   }

   @Override
   public Pattern getRegexProperty(Pattern defaultValue, String... keys) {
      return settings.getRegexProperty(defaultValue, keys);
   }

   @Override
   public Pattern[] getRegexProperties(Pattern[] defaultValue, String... keys) {
      return settings.getRegexProperties(defaultValue, keys);
   }

   @Override
   public File getFileProperty(File defaultValue, String... keys) {
      return settings.getFileProperty(defaultValue, keys);
   }

   @Override
   public File[] getFileProperties(File[] defaultValue, String... keys) {
      return settings.getFileProperties(defaultValue, keys);
   }

   @Override
   public void setId(String id) {
      settings.setId(id);
   }

   @Override
   public String[] put(String key, String value) {
      return settings.put(key, value);
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
    * for serialization, writes the embedded settings to the stream, call this in subclasses after you have done your
    * own serialization
    *
    * @param s
    * @throws java.io.IOException
    */
   protected void writeEmbeddedSettings(java.io.ObjectOutputStream s) throws IOException {
      s.writeObject(settings);
   }

   @Override
   public List<Class<? extends AbstractPropertiesDecorator>> getDecorators() {
      return decorators;
   }

   @Override
   public void addDecorator(Class<? extends AbstractPropertiesDecorator> clazz) {
      decorators.add(clazz);
   }

   @Override
   public AbstractPropertiesDecorator getOutermostDecorator() {
      return outermostDecorator;
   }

   @Override
   public void setOutermostDecorator(AbstractPropertiesDecorator outermostDecorator) {
      this.outermostDecorator = outermostDecorator;
      log.warning(String.format("NB! %s decorated by %s, you should use this instead of %s", getClass().getName(), outermostDecorator.getClass().getName(),
          getClass().getName()));
   }

   private static class DecoratorOveriew implements DecoratorVisitor<AbstractPropertiesDecorator> {

      private final DecorationAware[] das;
      private boolean visit = true;

      public DecoratorOveriew(DecorationAware... das) {
         this.das = das;
      }

      @Override
      public boolean shouldVisit(EnhancedMap e) {
         return visit && e instanceof AbstractPropertiesDecorator;
      }

      @Override
      public void visit(AbstractPropertiesDecorator e) {
         for (DecorationAware da : das) {
            if (e.equals(da)) {
               // this AbstractPropertiesDecorator and the ones to come (from accept) do not wrap da
               // other DecorationAwares in the list have already been visited, so this construct
               // doesn't cause loss of information
               visit = false;
               break;
            }
            if (!da.getDecorators().contains(e.getClass())) {
               da.addDecorator(e.getClass());
               da.setOutermostDecorator(e);
            }
         }
      }

   }

   private static class Hiding implements DecoratorVisitor<HiddenBy> {

      private final AbstractPropertiesDecorator settings;

      public Hiding(AbstractPropertiesDecorator settings) {
         this.settings = settings;
      }

      @Override
      public void visit(HiddenBy e) {
         if (e.hiddenBy(settings.getClass())) {
            throw new VectorPrintRuntimeException(String.format("%s hides %s",
                settings.getClass().getName(), e.getClass().getName()));
         }
      }

      @Override
      public boolean shouldVisit(EnhancedMap e) {
         return e instanceof HiddenBy;
      }

   }
}
