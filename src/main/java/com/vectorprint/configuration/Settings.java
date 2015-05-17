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
import com.vectorprint.configuration.decoration.AbstractPropertiesDecorator;
import com.vectorprint.configuration.binding.StringConversion;
import java.awt.Color;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.annotation.PreDestroy;
import static com.vectorprint.configuration.binding.StringConversion.getStringConversion;

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
   public static final String EOL = System.getProperty("line.separator", "\n");

   @Override
   public void listProperties(PrintStream ps) {
      ps.println("settings with id " + getId() + ":" + EOL);
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
    */
   public Settings(int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
   }

   /**
    * Calls {@link HashMap#HashMap(int) }.
    *
    */
   public Settings(int initialCapacity) {
      super(initialCapacity);
   }

   /**
    * Calls {@link HashMap#HashMap(java.util.Map) ) }.
    *
    */
   public Settings(Map<String, String[]> map) {
      super(map);
   }

   private void debug(String key, Object val) {
      debug(key, val, true);
   }

   private void debug(String key, Object val, boolean defaultVal) {
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
         log.fine(String.format("looking for property %s in %s, using value %s", key, getId(), s.append((defaultVal) ? " (default)" : "").toString()));
      }
   }

   @Override
   public final String[] get(Object key) {
      unused.remove(key);
      return super.get(key);
   }

   @Override
   public String getProperty(String key) {
      if (log.isLoggable(Level.FINE)) {
         debug(key, (containsKey(key)) ? super.get(key) : null, false);
      }
      return getFirst(key);
   }

   private String getFirst(String key) {
      if (!containsKey(key)) {
         return null;
      }
      String[] l = get(key);
      return (l == null || l.length == 0) ? null : l[0];
   }

   @Override
   public String getProperty(String key, String defaultValue) {
      if (!containsKey(key)) {
         shouldUseDefault(key, defaultValue);
         return defaultValue;
      }
      return getProperty(key);
   }

   @Override
   public URL getURLProperty(String key, URL defaultValue) throws MalformedURLException {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parse(getProperty(key), URL.class);
   }

   @Override
   public float getFloatProperty(String key, Float defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parse(getProperty(key), Float.class);
   }

   @Override
   public boolean getBooleanProperty(String key, Boolean defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parse(getProperty(key), Boolean.class);
   }

   /**
    * determine if the default value should be used, check if it is set.
    *
    * @param key the value of key
    * @param defaultVal the value of defaultVal
    * @return true when the default value should be used
    * @throws VectorPrintRuntimeException when defaultVal should be used and is null
    */
   private boolean shouldUseDefault(String key, Object defaultVal) throws VectorPrintRuntimeException {
      if (!containsKey(key)) {
         if (defaultVal == null) {
            throw new VectorPrintRuntimeException(key + " not found and default is null");
         } else {
            debug(key, defaultVal);
            notPresent.add(key);
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public double getDoubleProperty(String key, Double defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parse(getProperty(key), Double.class);
   }

   @Override
   public int getIntegerProperty(String key, Integer defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parse(getProperty(key), Integer.class);
   }

   @Override
   public short getShortProperty(String key, Short defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parse(getProperty(key), Short.class);
   }

   @Override
   public char getCharProperty(String key, Character defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parse(getProperty(key), Character.class);
   }

   @Override
   public byte getByteProperty(String key, Byte defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parse(getProperty(key), Byte.class);
   }

   @Override
   public long getLongProperty(String key, Long defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parse(getProperty(key), Long.class);
   }

   @Override
   public Color getColorProperty(String key, Color defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parse(getProperty(key), Color.class);
   }

   @Override
   public String[] getStringProperties(String key, String[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return get(key);
   }

   @Override
   public URL[] getURLProperties(String key, URL[] defaultValue) throws MalformedURLException {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parseURLValues(getStringProperties(key, null));
   }

   @Override
   public float[] getFloatProperties(String key, float[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parseFloatValues(getStringProperties(key, null));
   }

   @Override
   public char[] getCharProperties(String key, char[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parseCharValues(getStringProperties(key, null));

   }

   @Override
   public short[] getShortProperties(String key, short[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parseShortValues(getStringProperties(key, null));
   }

   @Override
   public byte[] getByteProperties(String key, byte[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parseByteValues(getStringProperties(key, null));
   }

   @Override
   public double[] getDoubleProperties(String key, double[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parseDoubleValues(getStringProperties(key, null));
   }

   @Override
   public int[] getIntegerProperties(String key, int[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parseIntValues(getStringProperties(key, null));
   }

   @Override
   public boolean[] getBooleanProperties(String key, boolean[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parseBooleanValues(getStringProperties(key, null));
   }

   @Override
   public Color[] getColorProperties(String key, Color[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parseColorValues(getStringProperties(key, null));
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
   public long[] getLongProperties(String key, long[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parseLongValues(getStringProperties(key, null));
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
      for (String key : keys) {
         if (containsKey(key)) {
            return getGenericProperty(key, defaultValue, clazz);
         }
      }
      if (defaultValue == null) {
         throw new VectorPrintRuntimeException(Arrays.asList(keys).toString() + " not found and default is null");
      }
      return defaultValue;
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
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      } else {
         if (clazz.isArray()) {
            if (String[].class.equals(clazz)) {
               return (T) get(key);
            }
            return getStringConversion().parse(get(key), clazz);
         } else {
            if (String.class.equals(clazz)) {
               return (T) getProperty(key);
            }
            return getStringConversion().parse(getProperty(key), clazz);
         }
      }
   }

   @Override
   public Date getDateProperty(String key, Date defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parse(getProperty(key), Date.class);
   }

   @Override
   public Date[] getDateProperties(String key, Date[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parseDateValues(getStringProperties(key, null));
   }

   private final Collection<String> unused = new HashSet<String>(25);

   @Override
   public Collection<String> getUnusedKeys() {
      for (Iterator<String> it = unused.iterator(); it.hasNext();) {
         String string = it.next();
         if (!containsKey(string)) {
            it.remove();
         }
      }
      // cleanup here
      return Collections.unmodifiableCollection(unused);
   }

   private final Collection<String> notPresent = new HashSet<String>(25);

   @Override
   public Collection<String> getKeysNotPresent() {
      return Collections.unmodifiableCollection(notPresent);
   }

   @PreDestroy
   @Override
   protected void finalize() throws Throwable {
      log.info(String.format("Settings (%s) not used sofar: %s", getId(), getUnusedKeys()));
      log.info(String.format("Settings (%s) not present, default used: %s", getId(), getKeysNotPresent()));
   }

   /**
    * uses {@link StringConversion#classFromKey(java.lang.String)  }
    *
    * @param key
    * @param defaultValue
    * @return
    * @throws ClassNotFoundException
    */
   @Override
   public Class getClassProperty(String key, Class defaultValue) throws ClassNotFoundException {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parse(getProperty(key), Class.class);
   }

   @Override
   public Pattern getRegexProperty(String key, Pattern defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parse(getProperty(key), Pattern.class);
   }

   @Override
   public Pattern[] getRegexProperties(String key, Pattern[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parseRegexValues(getStringProperties(key, null));
   }

   /**
    * uses {@link StringConversion#parseClassValues(java.lang.String[])  }
    *
    * @param key
    * @param defaultValue
    * @return
    * @throws ClassNotFoundException
    */
   @Override
   public Class[] getClassProperties(String key, Class[] defaultValue) throws ClassNotFoundException {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getStringConversion().parseClassValues(getStringProperties(key, null));
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
    * use the {@link SettingsField} annotation in conjunction with a call to {@link SettingsAnnotationProcessor#initSettings(java.lang.Object)
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
