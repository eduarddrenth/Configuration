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
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.annotation.Setting;
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessor;
import com.vectorprint.configuration.annotation.SettingsField;
import com.vectorprint.configuration.binding.AbstractBindingHelperDecorator;
import com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactory;
import com.vectorprint.configuration.binding.settings.SettingsBindingService;
import com.vectorprint.configuration.decoration.AbstractPropertiesDecorator;
import com.vectorprint.configuration.decoration.DecorationAware;
import java.awt.Color;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Enhances Java Map with support for data types, debugging info, working with default values in code. Internally a
 * backing Map is used which is a HashMap by default. You can provide your own backing map in {@link #Settings(java.util.Map)
 * }, this may not be an EnhancedMap implementation. You cannot subclass this class, instead subclass
 * {@link AbstractPropertiesDecorator} and wrap an instance of this class.
 *
 * @see EnhancedMapBindingFactory
 * @see com.vectorprint.configuration.decoration
 * @see SettingsAnnotationProcessor
 * @see SettingsField
 * @see Setting
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public final class Settings implements EnhancedMap, DecorationAware {

   private static final long serialVersionUID = 1;
   private static final Logger log = Logger.getLogger(Settings.class.getName());
   private final Map<String, String[]> backingMap;

   @Override
   public void listProperties(PrintStream ps) {
      ps.println("settings with id " + getId() + ":");
      ps.println();
      for (Map.Entry<String, String[]> entry : backingMap.entrySet()) {
         ps.println(entry.getKey() + "=" + (entry.getValue() != null ? Arrays.asList(entry.getValue()) : ""));
      }
      ps.println("");
      ps.println("settings wrapped by " + decorators.toString());
   }
   private String id;
   private final Map<String, PropertyHelp> help = new HashMap<>(50);
   private final List<Class<? extends AbstractPropertiesDecorator>> decorators
       = new ArrayList<>(3);
   private AbstractPropertiesDecorator outermostDecorator;

   /**
    * Creates a backing map {@link HashMap#HashMap() ) }.
    *
    */
   public Settings() {
      backingMap = new HashMap<>();
   }

   /**
    * Creates a backing map {@link HashMap#HashMap(int, float) }.
    *
    * @param initialCapacity
    * @param loadFactor
    */
   public Settings(int initialCapacity, float loadFactor) {
      backingMap = new HashMap<>(initialCapacity, loadFactor);
   }

   /**
    * Creates a backing map {@link HashMap#HashMap(int) }.
    *
    * @param initialCapacity
    */
   public Settings(int initialCapacity) {
      backingMap = new HashMap<>(initialCapacity);
   }

   /**
    * Uses the provided Map as backing map, throws an IllegalArgumentException if the map is an instance of EnhancedMap.
    *
    * @param map
    */
   public Settings(Map<String, String[]> map) {
      Objects.requireNonNull(map);
      if (map instanceof EnhancedMap) {
         throw new IllegalArgumentException("instance of " + EnhancedMap.class.getName() + " not allowed");
      }
      backingMap = map;
   }

   private void debug(Object val, String... keys) {
      debug(val, true, keys);
   }

   private void debug(Object val, boolean defaultVal, String... keys) {
      if (log.isLoggable(Level.FINE)) {
         StringBuilder s = new StringBuilder(String.valueOf(val));
         if (val != null && val.getClass().isArray()) {
            s = new StringBuilder("");
            Class compType = val.getClass().getComponentType();
            if (!compType.isPrimitive()) {
               for (Object o : (Object[]) val) {
                  s.append(String.valueOf(o)).append("; ");
               }
            } else if (compType.equals(boolean.class)) {
               for (boolean o : (boolean[]) val) {
                  s.append(String.valueOf(o)).append("; ");
               }
            } else if (compType.equals(char.class)) {
               for (char o : (char[]) val) {
                  s.append(String.valueOf(o)).append("; ");
               }
            } else if (compType.equals(byte.class)) {
               for (byte o : (byte[]) val) {
                  s.append(String.valueOf(o)).append("; ");
               }
            } else if (compType.equals(short.class)) {
               for (short o : (short[]) val) {
                  s.append(String.valueOf(o)).append("; ");
               }
            } else if (compType.equals(int.class)) {
               for (int o : (int[]) val) {
                  s.append(String.valueOf(o)).append("; ");
               }
            } else if (compType.equals(long.class)) {
               for (long o : (long[]) val) {
                  s.append(String.valueOf(o)).append("; ");
               }
            } else if (compType.equals(float.class)) {
               for (float o : (float[]) val) {
                  s.append(String.valueOf(o)).append("; ");
               }
            } else if (compType.equals(double.class)) {
               for (double o : (double[]) val) {
                  s.append(String.valueOf(o)).append("; ");
               }
            }
         }
         log.fine(String.format("looking for property %s in %s, using value %s", Arrays.asList(keys), getId(), s.append((defaultVal) ? " (default)" : "").toString()));
      }
   }

   @Override
   public final String[] get(Object key) {
      unused.remove(key);
      return backingMap.get(key);
   }

   @Override
   public String getPropertyNoDefault(String... keys) {
      String key = getFirstKeyPresent(keys);
      if (log.isLoggable(Level.FINE)) {
         debug((key != null) ? backingMap.get(key) : null, false, key);
      }
      return getFirst(key);
   }

   private String getFirstKeyPresent(String... keys) {
      if (keys == null || keys.length == 0 || keys[0] == null) {
         throw new VectorPrintRuntimeException("You should provide at least one key");
      }
      if (keys.length == 1) {
         if (containsKey(keys[0])) {
            if (log.isLoggable(Level.FINE)) {
               log.fine(String.format("Returning key \"%s\" from %s, it was first found in settings", keys[0], Arrays.asList(keys)));
            }
            return keys[0];
         } else {
            if (!notPresent.contains(keys[0])) {
               notPresent.add(keys[0]);
            }
            return null;
         }
      }
      for (String k : keys) {
         if (containsKey(k)) {
            if (log.isLoggable(Level.FINE)) {
               log.fine(String.format("Returning key \"%s\" from %s, it was first found in settings", k, Arrays.asList(keys)));
            }
            return k;
         } else if (!notPresent.contains(k)) {
            notPresent.add(k);
         }

      }
      if (log.isLoggable(Level.FINE)) {
         log.fine(String.format("None of %s found in settings", Arrays.asList(keys)));
      }
      return null;
   }

   private String getFirst(String key) {
      if (key == null) {
         return null;
      }
      String[] l = get(key);
      if (l != null) {
         if (l.length > 1) {
            throw new VectorPrintRuntimeException(String.format("more then one value (%s) for %s, expected one",
                getFactory().getBindingHelper().serializeValue(l), key));
         }
         return l.length == 0 ? null : l[0];
      } else {
         return null;
      }

   }

   @Override
   public String getProperty(String defaultValue, String... keys) {
      if (defaultValue != null && (keys == null || keys.length == 0)) {
         // assume defaultValue is key
         return getPropertyNoDefault(defaultValue);
      }
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         shouldUseDefault(defaultValue, keys);
         return defaultValue;
      }
      return getPropertyNoDefault(key);
   }

   @Override
   public URL getURLProperty(URL defaultValue, String... keys) throws MalformedURLException {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return getFactory().getBindingHelper().convert(getPropertyNoDefault(key), URL.class);
   }

   @Override
   public File getFileProperty(File defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return getFactory().getBindingHelper().convert(getPropertyNoDefault(key), File.class);
   }

   @Override
   public float getFloatProperty(Float defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return getFactory().getBindingHelper().convert(getPropertyNoDefault(keys), Float.class);
   }

   @Override
   public boolean getBooleanProperty(Boolean defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return getFactory().getBindingHelper().convert(getPropertyNoDefault(key), Boolean.class);
   }

   /**
    * determine if the default value should be used, check if it is set.
    *
    * @param defaultVal the value of defaultVal
    * @param keys the keys to look for, the first one found will be used
    * @throws VectorPrintRuntimeException when defaultVal should be used and is null
    * @return the key to be used or null to use a default value
    */
   private String shouldUseDefault(Object defaultVal, String... keys) throws VectorPrintRuntimeException {
      String key = getFirstKeyPresent(keys);
      if (key == null) {
         if (defaultVal == null) {
            throw new VectorPrintRuntimeException(Arrays.asList(keys) + " not found and default is null");
         } else {
            debug(defaultVal, key);
            return null;
         }
      } else {
         return key;
      }
   }

   @Override
   public double getDoubleProperty(Double defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return getFactory().getBindingHelper().convert(getPropertyNoDefault(key), Double.class);
   }

   @Override
   public int getIntegerProperty(Integer defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return getFactory().getBindingHelper().convert(getPropertyNoDefault(key), Integer.class);
   }

   @Override
   public short getShortProperty(Short defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return getFactory().getBindingHelper().convert(getPropertyNoDefault(key), Short.class);
   }

   @Override
   public char getCharProperty(Character defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return getFactory().getBindingHelper().convert(getPropertyNoDefault(key), Character.class);
   }

   @Override
   public byte getByteProperty(Byte defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return getFactory().getBindingHelper().convert(getPropertyNoDefault(key), Byte.class);
   }

   @Override
   public long getLongProperty(Long defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return getFactory().getBindingHelper().convert(getPropertyNoDefault(key), Long.class);
   }

   @Override
   public Color getColorProperty(Color defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return getFactory().getBindingHelper().convert(getPropertyNoDefault(key), Color.class);
   }

   @Override
   public String[] getStringProperties(String[] defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return get(key);
   }

   @Override
   public URL[] getURLProperties(URL[] defaultValue, String... keys) throws MalformedURLException {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseURLValues(getStringProperties(null, key));
   }

   @Override
   public File[] getFileProperties(File[] defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseFileValues(getStringProperties(null, key));
   }

   @Override
   public float[] getFloatProperties(float[] defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseFloatValues(getStringProperties(null, key));
   }

   @Override
   public char[] getCharProperties(char[] defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseCharValues(getStringProperties(keys, null));

   }

   @Override
   public short[] getShortProperties(short[] defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseShortValues(getStringProperties(keys, null));
   }

   @Override
   public byte[] getByteProperties(byte[] defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseByteValues(getStringProperties(keys, null));
   }

   @Override
   public double[] getDoubleProperties(double[] defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseDoubleValues(getStringProperties(null, key));
   }

   @Override
   public int[] getIntegerProperties(int[] defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseIntValues(getStringProperties(null, key));
   }

   @Override
   public boolean[] getBooleanProperties(boolean[] defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseBooleanValues(getStringProperties(null, key));
   }

   @Override
   public Color[] getColorProperties(Color[] defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseColorValues(getStringProperties(null, key));
   }

   /**
    * This implementation only includes key and value state, not the rest of the state (i.e. {@link #getUnusedKeys() }, {@link #getHelp()
    * } and {@link #getKeysNotPresent() }). The {@link #getId() id's} of the objects must be both null or the same,
    * otherwise false is returned.
    *
    * @param obj
    * @return
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (obj == this) {
         return true;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final Settings other = (Settings) obj;
      if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
         return false;
      }
      return Objects.equals(backingMap, other.backingMap);
   }

   @Override
   public PropertyHelp getHelp(String key) {
      if (help.containsKey(key)) {
         return help.get(key);
      } else {
         return new PropertyHelpImpl("no help configured for " + key);
      }
   }

   /**
    * initializes help for properties
    *
    * @param help
    * @see #getHelp(java.lang.String)
    * @see #getHelp()
    */
   @Override
   public void setHelp(Map<String, PropertyHelp> help) {
      this.help.clear();
      this.help.putAll(help);
   }

   @Override
   public Map<String, PropertyHelp> getHelp() {
      return help;
   }

   @Override
   public String printHelp() {
      StringBuilder sb = new StringBuilder(1024);
      for (Map.Entry<String, PropertyHelp> h : help.entrySet()) {
         sb.append(h.getKey()).append(": ").append(h.getValue().getType())
             .append("; ")
             .append(h.getValue().getExplanation())
             .append(System.getProperty("line.separator"));
      }
      return sb.toString();
   }

   @Override
   public long[] getLongProperties(long[] defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseLongValues(getStringProperties(null, key));
   }

   @Override
   public final String[] put(String key, String[] value) {
      unused.add(key);
      return backingMap.put(key, value);
   }

   @Override
   public final void clear() {
      unused.clear();
      notPresent.clear();
      help.clear();
      decorators.clear();
      backingMap.clear();
   }

   @Override
   public final String[] remove(Object key) {
      unused.remove(key);
      return backingMap.remove(key);
   }

   private void init(Settings vp) {
      vp.help.putAll(help);
      vp.unused.addAll(unused);
      vp.id = id;
      vp.notPresent.addAll(notPresent);
      vp.decorators.addAll(decorators);
   }

   /**
    * Creates a new identical Settings object. The backing Map will be cloned by calling clone when the Map is
    * Cloneable, otherwise putAll is used to copy the backing Map;
    *
    * @return an identical copy of these Settings.
    */
   @Override
   public Settings clone() {
      Settings vp;
      if (backingMap instanceof Cloneable) {
         try {
            Method m = backingMap.getClass().getMethod("clone");
            vp = new Settings((Map<String, String[]>) m.invoke(backingMap, null));
         } catch (InvocationTargetException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
      } else {
         vp = new Settings();
         vp.backingMap.putAll(backingMap);
      }
      init(vp);
      return vp;
   }

   /**
    *
    * @return the id of the properties or null
    */
   @Override
   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   /**
    * this implementation supports all primitives and their wrappers, Color, Date, URL, Class and arrays of those types.
    * Calls {@link #getGenericProperty(java.lang.String, java.lang.Object, java.lang.Class) } if one of the keys is
    * present in the settings.
    *
    * @param <T>
    * @param keys
    * @param defaultValue
    * @param clazz
    * @return value of the setting or the default value
    * @throws VectorPrintRuntimeException when no value is found and defaultValue is null
    */
   public <T> T getGenericProperty(T defaultValue, Class<T> clazz, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      return getGenericProperty(key, defaultValue, clazz, keys);
   }

   /**
    * this implementation supports all primitives and their wrappers, Color, Date, URL, Class and arrays of those types.
    *
    * @param <T>
    * @param key
    * @param defaultValue
    * @param clazz
    * @return value of the setting or the default value
    * @throws VectorPrintRuntimeException when no value is found and defaultValue is null
    */
   private <T> T getGenericProperty(String key, T defaultValue, Class<T> clazz, String... keys) {
      if (key == null) {
         return defaultValue;
      } else if (clazz.isArray()) {
         if (String[].class.equals(clazz)) {
            return (T) get(key);
         }
         return getFactory().getBindingHelper().convert(get(key), clazz);
      } else {
         if (String.class.equals(clazz)) {
            return (T) getPropertyNoDefault(key);
         }
         return getFactory().getBindingHelper().convert(getPropertyNoDefault(key), clazz);
      }
   }

   @Override
   public Date getDateProperty(Date defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return getFactory().getBindingHelper().convert(getPropertyNoDefault(key), Date.class);
   }

   @Override
   public Date[] getDateProperties(Date[] defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseDateValues(getStringProperties(null, key));
   }

   private final Collection<String> unused = new HashSet<>(25);

   @Override
   public Collection<String> getUnusedKeys() {
      for (Iterator<String> it = unused.iterator(); it.hasNext();) {
         if (!containsKey(it.next())) {
            it.remove();
         }
      }
      return Collections.unmodifiableCollection(unused);
   }

   private final Collection<String> notPresent = new HashSet<>(25);

   @Override
   public Collection<String> getKeysNotPresent() {
      return Collections.unmodifiableCollection(notPresent);
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
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return getFactory().getBindingHelper().convert(getPropertyNoDefault(key), Class.class);
   }

   @Override
   public Pattern getRegexProperty(Pattern defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return getFactory().getBindingHelper().convert(getPropertyNoDefault(key), Pattern.class);
   }

   @Override
   public Pattern[] getRegexProperties(Pattern[] defaultValue, String... keys) {
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseRegexValues(getStringProperties(keys, null));
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
      String key = shouldUseDefault(defaultValue, keys);
      if (key == null) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseClassValues(getStringProperties(keys, null));
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
      log.warning(String.format("NB! Settings wrapped by %s, you should use this instead of %s", outermostDecorator.getClass().getName(),
          getClass().getName()));
   }

   @Override
   public String[] put(String key, String value) {
      return put(key, new String[]{value});
   }

   @Override
   public boolean containsValue(Object value) {
      if (value == null) {
         return backingMap.containsValue(null);
      }
      if (String[].class.equals(value.getClass())) {

         for (Entry<String, String[]> e : entrySet()) {
            if (Arrays.equals(e.getValue(), (String[]) value)) {
               return true;
            }
         }
      }
      return false;
   }

   private EnhancedMapBindingFactory getFactory() {
      return SettingsBindingService.getInstance().getFactory();
   }

   @Override
   public int size() {
      return backingMap.size();
   }

   @Override
   public boolean isEmpty() {
      return backingMap.isEmpty();
   }

   @Override
   public boolean containsKey(Object key) {
      return backingMap.containsKey(key);
   }

   @Override
   public void putAll(Map<? extends String, ? extends String[]> m) {
      backingMap.putAll(m);
   }

   @Override
   public Set<String> keySet() {
      return backingMap.keySet();
   }

   @Override
   public Collection<String[]> values() {
      return backingMap.values();
   }

   @Override
   public Set<Entry<String, String[]>> entrySet() {
      return backingMap.entrySet();
   }

}
