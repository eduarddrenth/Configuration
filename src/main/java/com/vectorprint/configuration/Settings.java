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
import com.vectorprint.configuration.annotation.SettingsField;
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessor;
import com.vectorprint.configuration.binding.AbstractBindingHelperDecorator;
import com.vectorprint.configuration.decoration.AbstractPropertiesDecorator;
import java.awt.Color;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.annotation.PreDestroy;
import static com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactoryImpl.getDefaultFactory;

/**
 * Enhances Java Map with support for data types, debugging info, overriding properties from command line arguments,
 * working with default values in code. You cannot subclass this class, instead subclass
 * {@link AbstractPropertiesDecorator} and wrap an instance of this class.
 *
 * @see com.vectorprint.configuration.decoration
 * @see SettingsAnnotationProcessor
 * @see SettingsField
 * @see Setting
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public final class Settings extends HashMap<String, String[]>
    implements EnhancedMap {

   private static final long serialVersionUID = 1;
   private static final Logger log = Logger.getLogger(Settings.class.getName());

   @Override
   public void listProperties(PrintStream ps) {
      ps.println("settings with id " + getId() + ":");
      ps.println();
      for (Map.Entry<String, String[]> entry : super.entrySet()) {
         ps.println(entry.getKey() + "=" + Arrays.asList(entry.getValue()));
      }
      ps.println("");
      ps.println("settings wrapped by " + decorators.toString());
   }
   private String id;
   private final Map<String, PropertyHelp> help = new HashMap<String, PropertyHelp>(50);
   private final List<Class<? extends AbstractPropertiesDecorator>> decorators
       = new ArrayList<Class<? extends AbstractPropertiesDecorator>>(3);
   private AbstractPropertiesDecorator outermostWrapper;

   /**
    * Calls {@link HashMap#HashMap() }
    *
    */
   public Settings() {
      super();
   }

   /**
    * Calls {@link HashMap#HashMap(int, float) }.
    *
    * @param initialCapacity
    * @param loadFactor
    */
   public Settings(int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
   }

   /**
    * Calls {@link HashMap#HashMap(int) }.
    *
    * @param initialCapacity
    */
   public Settings(int initialCapacity) {
      super(initialCapacity);
   }

   /**
    * Calls {@link HashMap#HashMap(java.util.Map) ) }.
    *
    * @param map
    */
   public Settings(Map<String, String[]> map) {
      super(map);
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
            } else {
               if (compType.equals(boolean.class)) {
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
         }
         log.fine(String.format("looking for property %s in %s, using value %s", Arrays.asList(keys), getId(), s.append((defaultVal) ? " (default)" : "").toString()));
      }
   }

   @Override
   public final String[] get(Object key) {
      unused.remove(key);
      return super.get(key);
   }

   @Override
   public String getPropertyNoDefault(String... keys) {
      String key = getFirstKeyPresent(keys);
      if (log.isLoggable(Level.FINE)) {
         debug((key!=null) ? super.get(key) : null, false, key);
      }
      return getFirst(key);
   }

   private String getFirstKeyPresent(String... keys) {
      if (keys==null||keys.length==0||keys[0]==null) {
         throw new VectorPrintRuntimeException("You should provide at least one key");
      }
      if (keys.length==1) {
         if (containsKey(keys[0])) {
            return keys[0];
         } else {
            notPresent.add(keys[0]);
            return null;
         }
      }
      for (String k : keys) {
         if (containsKey(k)) {
            return k;
         } else {
            notPresent.add(k);
         }
         
      }
      return null;
   }
   
   private String getFirst(String key) {
      if (key==null) {
         return null;
      }
      String[] l = get(key);
      if (l!=null) {
         if (l.length > 1) {
            throw new VectorPrintRuntimeException(String.format("more then one value (%s) for %s, expected one",
                getDefaultFactory().getBindingHelper().serializeValue(l),key));
         }
         return l.length ==0 ? null : l[0];
      } else {
         return null;
      }
      
   }

   @Override
   public String getProperty(String defaultValue, String... keys) {
      if (defaultValue!=null&&keys==null||keys.length==0) {
         // assume defaultValue is key
         return getPropertyNoDefault(defaultValue);
      }
      String key = getFirstKeyPresent(keys);
      if (key==null) {
         shouldUseDefault(key, defaultValue);
         return defaultValue;
      }
      return getPropertyNoDefault(key);
   }

   @Override
   public URL getURLProperty(URL defaultValue, String... keys) throws MalformedURLException {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault( key,defaultValue)) {
         return defaultValue;
      }
      return getDefaultFactory().getBindingHelper().convert(getPropertyNoDefault(key), URL.class);
   }

   @Override
   public float getFloatProperty(Float defaultValue, String... keys) {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getDefaultFactory().getBindingHelper().convert(getPropertyNoDefault(keys), Float.class);
   }

   @Override
   public boolean getBooleanProperty(Boolean defaultValue, String... keys) {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getDefaultFactory().getBindingHelper().convert(getPropertyNoDefault(key), Boolean.class);
   }

   /**
    * determine if the default value should be used, check if it is set.
    *
    * @param defaultVal the value of defaultVal
    * @param keys the value of key
    * @throws VectorPrintRuntimeException when defaultVal should be used and is null
    * @return the boolean
    */
   private boolean shouldUseDefault(String key, Object defaultVal) throws VectorPrintRuntimeException {
      if (key==null||!containsKey(key)) {
         if (defaultVal == null) {
            throw new VectorPrintRuntimeException(key + " not found and default is null");
         } else {
            debug( defaultVal, key);
            if (!notPresent.contains(key)) {
               notPresent.add(key);
            }
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public double getDoubleProperty(Double defaultValue, String... keys) {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getDefaultFactory().getBindingHelper().convert(getPropertyNoDefault(key), Double.class);
   }

   @Override
   public int getIntegerProperty(Integer defaultValue, String... keys) {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getDefaultFactory().getBindingHelper().convert(getPropertyNoDefault(key), Integer.class);
   }

   @Override
   public short getShortProperty(Short defaultValue, String... keys) {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getDefaultFactory().getBindingHelper().convert(getPropertyNoDefault(key), Short.class);
   }

   @Override
   public char getCharProperty(Character defaultValue, String... keys) {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getDefaultFactory().getBindingHelper().convert(getPropertyNoDefault(key), Character.class);
   }

   @Override
   public byte getByteProperty(Byte defaultValue, String... keys) {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getDefaultFactory().getBindingHelper().convert(getPropertyNoDefault(key), Byte.class);
   }

   @Override
   public long getLongProperty(Long defaultValue, String... keys) {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getDefaultFactory().getBindingHelper().convert(getPropertyNoDefault(key), Long.class);
   }

   @Override
   public Color getColorProperty(Color defaultValue, String... keys) {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getDefaultFactory().getBindingHelper().convert(getPropertyNoDefault(key), Color.class);
   }

   @Override
   public String[] getStringProperties(String[] defaultValue, String... keys) {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return get(key);
   }

   @Override
   public URL[] getURLProperties(URL[] defaultValue, String... keys) throws MalformedURLException {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseURLValues(getStringProperties(null, key));
   }

   @Override
   public float[] getFloatProperties(float[] defaultValue, String... keys) {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseFloatValues(getStringProperties(null, key));
   }

   @Override
   public char[] getCharProperties(char[] defaultValue, String... keys) {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseCharValues(getStringProperties(keys, null));

   }

   @Override
   public short[] getShortProperties(short[] defaultValue, String... keys) {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseShortValues(getStringProperties(keys, null));
   }

   @Override
   public byte[] getByteProperties(byte[] defaultValue, String... keys) {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseByteValues(getStringProperties(keys, null));
   }

   @Override
   public double[] getDoubleProperties(double[] defaultValue, String... keys) {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseDoubleValues(getStringProperties(null, key));
   }

   @Override
   public int[] getIntegerProperties(int[] defaultValue, String... keys) {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseIntValues(getStringProperties(null, key));
   }

   @Override
   public boolean[] getBooleanProperties(boolean[] defaultValue, String... keys) {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseBooleanValues(getStringProperties(null, key));
   }

   @Override
   public Color[] getColorProperties(Color[] defaultValue, String... keys) {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseColorValues(getStringProperties(null, key));
   }

   @Override
   public int hashCode() {
      int hash = 7;
      return hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      if (!super.equals(obj)) {
         return false;
      }
      final Settings other = (Settings) obj;
      if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
         return false;
      }
      if (this.help != other.help && (this.help == null || !this.help.equals(other.help))) {
         return false;
      }
      if (this.unused != other.unused && (this.unused == null || !this.unused.equals(other.unused))) {
         return false;
      }
      if (this.notPresent != other.notPresent && (this.notPresent == null || !this.notPresent.equals(other.notPresent))) {
         return false;
      }
      return true;
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
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseLongValues(getStringProperties(null, key));
   }

   @Override
   public final String[] put(String key, String[] value) {
      unused.add(key);
      return super.put(key, value);
   }

   @Override
   public final void clear() {
      unused.clear();
      notPresent.clear();
      help.clear();
      decorators.clear();
      super.clear();
   }

   @Override
   public final String[] remove(Object key) {
      unused.remove(key);
      return super.remove(key);
   }

   private void init(Settings vp) {
      vp.help.putAll(help);
      vp.unused.addAll(unused);
      vp.id = id;
      vp.notPresent.addAll(notPresent);
      vp.decorators.addAll(decorators);
   }

   @Override
   public Settings clone() {
      Settings vp = (Settings) super.clone();
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
      String key = getFirstKeyPresent(keys);
      return getGenericProperty(key, defaultValue, clazz);
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
   private <T> T getGenericProperty(String key, T defaultValue, Class<T> clazz) {
      if (shouldUseDefault(key,defaultValue)) {
         return defaultValue;
      } else {
         if (clazz.isArray()) {
            if (String[].class.equals(clazz)) {
               return (T) get(key);
            }
            return getDefaultFactory().getBindingHelper().convert(get(key), clazz);
         } else {
            if (String.class.equals(clazz)) {
               return (T) getPropertyNoDefault(key);
            }
            return getDefaultFactory().getBindingHelper().convert(getPropertyNoDefault(key), clazz);
         }
      }
   }

   @Override
   public Date getDateProperty(Date defaultValue, String... keys) {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getDefaultFactory().getBindingHelper().convert(getPropertyNoDefault(key), Date.class);
   }

   @Override
   public Date[] getDateProperties(Date[] defaultValue, String... keys) {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseDateValues(getStringProperties(null, key));
   }

   private final Collection<String> unused = new HashSet<String>(25);

   @Override
   public Collection<String> getUnusedKeys() {
      for (Iterator<String> it = unused.iterator(); it.hasNext();) {
         if (!containsKey(it.next())) {
            it.remove();
         }
      }
      return Collections.unmodifiableCollection(unused);
   }

   private final Collection<String> notPresent = new HashSet<String>(25);

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
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getDefaultFactory().getBindingHelper().convert(getPropertyNoDefault(key), Class.class);
   }

   @Override
   public Pattern getRegexProperty(Pattern defaultValue, String... keys) {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getDefaultFactory().getBindingHelper().convert(getPropertyNoDefault(key), Pattern.class);
   }

   @Override
   public Pattern[] getRegexProperties(Pattern[] defaultValue, String... keys) {
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
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
      String key = getFirstKeyPresent(keys);
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return AbstractBindingHelperDecorator.parseClassValues(getStringProperties(keys, null));
   }

   /**
    * @see AbstractPropertiesDecorator#AbstractPropertiesDecorator(com.vectorprint.configuration.EnhancedMap)
    * @return a list of decorators that wrap these settings
    */
   public List<Class<? extends AbstractPropertiesDecorator>> getDecorators() {
      return decorators;
   }

   /**
    * Called by {@link AbstractPropertiesDecorator#AbstractPropertiesDecorator(com.vectorprint.configuration.EnhancedMap)
    * }
    * to build a list of wrappers for these settings. When wrapped your code should call methods on the outermost
    * wrapper only, if you don't, functionality of wrappers will not be called. The preferred way to achieve this is to
    * use the {@link SettingsField} annotation in conjunction with a call to {@link SettingsAnnotationProcessor#initSettings(java.lang.Object, com.vectorprint.configuration.EnhancedMap) }
    * }.
    *
    * @param clazz
    */
   public void addDecorator(Class<? extends AbstractPropertiesDecorator> clazz) {
      decorators.add(clazz);
   }

   /**
    * Set by {@link AbstractPropertiesDecorator#AbstractPropertiesDecorator(com.vectorprint.configuration.EnhancedMap)
    * }, if it is not null use it instead of these settings.
    *
    * @see SettingsAnnotationProcessor
    * @return
    */
   public AbstractPropertiesDecorator getOutermostWrapper() {
      return outermostWrapper;
   }

   /**
    * Called by {@link AbstractPropertiesDecorator#AbstractPropertiesDecorator(com.vectorprint.configuration.EnhancedMap)
    * }.
    *
    * @see SettingsAnnotationProcessor
    * @param outermostWrapper
    */
   public void setOutermostWrapper(AbstractPropertiesDecorator outermostWrapper) {
      this.outermostWrapper = outermostWrapper;
      log.warning(String.format("NB! Settings wrapped by %s, you should use this instead of %s", outermostWrapper.getClass().getName(),
          getClass().getName()));
   }

   @Override
   public String[] put(String key, String value) {
      return put(key, new String[]{value});
   }

   @Override
   public boolean containsValue(Object value) {
      if (value == null) {
         return super.containsValue(null);
      }
      if (String[].class.equals(value.getClass())) {

         for (Entry<String, String[]> e : entrySet()) {
            if (Arrays.equals(e.getValue(), (String[])value)) {
               return true;
            }
         }
      }
      return false;
   }

}
