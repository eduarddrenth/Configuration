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
import com.vectorprint.ArrayHelper;
import com.vectorprint.configuration.annotation.Setting;
import com.vectorprint.configuration.annotation.Settings;
import com.vectorprint.configuration.parameters.MultipleValueParser;
import com.vectorprint.configuration.parameters.ParameterHelper;
import com.vectorprint.configuration.parser.ParseException;
import java.awt.Color;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PreDestroy;

/**
 * Enhances Java Map with typing, debugging info, overriding properties from the command line
 * arguments, working with default values in code.
 * @see com.vectorprint.configuration.decoration
 * @see Settings
 * @see Setting
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class VectorPrintProperties extends HashMap<String, String>
    implements EnhancedMap {

   private static final long serialVersionUID = 1;
   /**
    * default name of the file containing help for settings
    */
   public static final String HELPFILE = "help.properties";
   public static final String MISSINGHELP = "no help configured, provide help file " + HELPFILE + " using format <property>=<type>;<description>";
   private static final Logger log = Logger.getLogger(VectorPrintProperties.class.getName());
   public static final String EOL = System.getProperty("line.separator", "\n");

   @Override
   public void listProperties(PrintStream ps) {
      ps.println("settings from: " + getId()+ ":" + EOL);
      for (Map.Entry<String, String> entry : super.entrySet()) {
         ps.println(entry.getKey() + "=" + entry.getValue());
      }
      ps.println("");
   }
   private String id;
   private final Map<String, PropertyHelp> help = new HashMap<String, PropertyHelp>(50);
   private final Set<String> propsFromArgs = new HashSet<String>(10);

   /**
    * Calls {@link HashMap#HashMap() }
    *
    */
   public VectorPrintProperties() {
      super();
   }

   /**
    * Calls {@link HashMap#HashMap(int, float) }.
    *
    */
   public VectorPrintProperties(int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
   }

   /**
    * Calls {@link HashMap#HashMap(int)  }.
    *
    */
   public VectorPrintProperties(int initialCapacity) {
      super(initialCapacity);
   }
   
   /**
    * Calls {@link HashMap#HashMap(java.util.Map) ) }.
    *
    */
   public VectorPrintProperties(Map<String, String> map) {
      super(map);
   }

   /**
    * Calls {@link HashMap#HashMap() } and calls {@link #addFromArguments(java.lang.String[]) }.
    *
    * @see ArgumentParser
    */
   public VectorPrintProperties(String[] args) {
      super();
      addFromArguments(args);
   }

   /**
    * {@link ArgumentParser#parseArgs(java.lang.String[]) parses the arguments} and calls {@link #putAll(java.util.Map)
    * }
    *
    * @param args
    */
   @Override
   public void addFromArguments(String[] args) {
      Map<String, String> props = ArgumentParser.parseArgs(args);
      if (props != null) {
         propsFromArgs.addAll(props.keySet());
         putAll(props);
      }
   }


   protected void debug(String key, Object val) {
      debug(key, val, true);
   }

   protected void debug(String key, Object val, boolean defaultVal) {
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
   public final String get(Object key) {
      unused.remove(key);
      return super.get(key);
   }

   @Override
   public String getProperty(String key) {
      if (log.isLoggable(Level.FINE)) {
         debug(key, (containsKey(key)) ? super.get(key) : null, false);
      }
      if (containsKey(key)) {
         return get(key);
      } else {
         return null;
      }
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
      return new URL(getProperty(key));
   }

   @Override
   public float getFloatProperty(String key, Float defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return Float.parseFloat(getProperty(key));
   }

   @Override
   public boolean getBooleanProperty(String key, Boolean defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return Boolean.parseBoolean(getProperty(key));
   }

   /**
    * determine if the default value should be used, check if it is set.
    *
    * @param key the value of key
    * @param defaultVal the value of defaultVal
    * @return true when the default value should be used
    * @throws VectorPrintRuntimeException when defaultVal should be used and is null
    */
   protected boolean shouldUseDefault(String key, Object defaultVal) throws VectorPrintRuntimeException {
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
      return Double.parseDouble(getProperty(key));
   }

   @Override
   public int getIntegerProperty(String key, Integer defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return Integer.parseInt(getProperty(key));
   }

   @Override
   public short getShortProperty(String key, Short defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return Short.parseShort(getProperty(key));
   }

   @Override
   public char getCharProperty(String key, Character defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return getProperty(key).charAt(0);
   }

   @Override
   public byte getByteProperty(String key, Byte defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return Byte.decode(getProperty(key));
   }

   @Override
   public long getLongProperty(String key, Long defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return Long.parseLong(getProperty(key));
   }

   @Override
   public Color getColorProperty(String key, Color defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      return ParameterHelper.getColorFromString(getProperty(key));
   }

   @Override
   public String[] getStringProperties(String key, String[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      try {
         return ArrayHelper.toArray(mvp.parseStringValues(getProperty(key)));
      } catch (ParseException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }
   private transient MultipleValueParser mvp = MultipleValueParser.getInstance();

   @Override
   public URL[] getURLProperties(String key, URL[] defaultValue) throws MalformedURLException {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      try {
         return ArrayHelper.toArray(mvp.parseURLValues(getProperty(key)));
      } catch (ParseException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   @Override
   public float[] getFloatProperties(String key, float[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      try {
         return ArrayHelper.unWrap(ArrayHelper.toArray(mvp.parseFloatValues(getProperty(key))));
      } catch (ParseException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   @Override
   public char[] getCharProperties(String key, char[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      try {
         return ArrayHelper.unWrap(ArrayHelper.toArray(mvp.parseCharValues(getProperty(key))));
      } catch (ParseException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   @Override
   public short[] getShortProperties(String key, short[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      try {
         return ArrayHelper.unWrap(ArrayHelper.toArray(mvp.parseShortValues(getProperty(key))));
      } catch (ParseException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   @Override
   public byte[] getByteProperties(String key, byte[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      try {
         return ArrayHelper.unWrap(ArrayHelper.toArray(mvp.parseByteValues(getProperty(key))));
      } catch (ParseException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   @Override
   public double[] getDoubleProperties(String key, double[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      try {
         return ArrayHelper.unWrap(ArrayHelper.toArray(mvp.parseDoubleValues(getProperty(key))));
      } catch (ParseException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   @Override
   public int[] getIntegerProperties(String key, int[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      try {
         return ArrayHelper.unWrap(ArrayHelper.toArray(mvp.parseIntValues(getProperty(key))));
      } catch (ParseException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   @Override
   public boolean[] getBooleanProperties(String key, boolean[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      try {
         return ArrayHelper.unWrap(ArrayHelper.toArray(mvp.parseBooleanValues(getProperty(key))));
      } catch (ParseException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   @Override
   public Color[] getColorProperties(String key, Color[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      try {
         return ArrayHelper.toArray(mvp.parseColorValues(getProperty(key)));
      } catch (ParseException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
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
      final VectorPrintProperties other = (VectorPrintProperties) obj;
      if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
         return false;
      }
      if (this.help != other.help && (this.help == null || !this.help.equals(other.help))) {
         return false;
      }
      if (this.propsFromArgs != other.propsFromArgs && (this.propsFromArgs == null || !this.propsFromArgs.equals(other.propsFromArgs))) {
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

   /**
    * When true this property was set from {@link #addFromArguments(String[])} .
    *
    * @param key
    * @return
    */
   @Override
   public boolean isFromArguments(String key) {
      return containsKey(key) && propsFromArgs.contains(key);
   }

   @Override
   public long[] getLongProperties(String key, long[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      try {
         return ArrayHelper.unWrap(ArrayHelper.toArray(mvp.parseLongValues(getProperty(key))));
      } catch (ParseException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   @Override
   public final String put(String key, String value) {
      unused.add(key);
      return super.put(key, value);
   }

   @Override
   public final void clear() {
      unused.clear();
      notPresent.clear();
      propsFromArgs.clear();
      help.clear();
      super.clear();
   }

   @Override
   public final String remove(Object key) {
      unused.remove(key);
      propsFromArgs.remove(key);
      return super.remove(key);
   }

   private void init(VectorPrintProperties vp) {
      vp.help.putAll(help);
      vp.propsFromArgs.addAll(propsFromArgs);
      vp.unused.addAll(unused);
      vp.id=id;
      vp.notPresent.addAll(notPresent);
   }

   @Override
   public VectorPrintProperties clone() {
      VectorPrintProperties vp = (VectorPrintProperties) super.clone();
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
    * @return
    */
   @Override
   public <T> T getGenericProperty(String key, T defaultValue, Class<T> clazz) {
      Object o = null;
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      try {
         if (Boolean.class.equals(clazz) || boolean.class.equals(clazz)) {
            o = Boolean.valueOf(getProperty(key));
         } else if (Color.class.equals(clazz)) {
            o = Color.decode(getProperty(key));
         } else if (Byte.class.equals(clazz) || byte.class.equals(clazz)) {
            o = Byte.decode(getProperty(key));
         } else if (Character.class.equals(clazz) || char.class.equals(clazz)) {
            o = getProperty(key).charAt(0);
         } else if (Short.class.equals(clazz) || short.class.equals(clazz)) {
            o = Short.valueOf(getProperty(key));
         } else if (Double.class.equals(clazz) || double.class.equals(clazz)) {
            o = Double.valueOf(getProperty(key));
         } else if (Float.class.equals(clazz) || float.class.equals(clazz)) {
            o = Float.valueOf(getProperty(key));
         } else if (Integer.class.equals(clazz) || int.class.equals(clazz)) {
            o = Integer.valueOf(getProperty(key));
         } else if (Long.class.equals(clazz) || long.class.equals(clazz)) {
            o = Long.valueOf(getProperty(key));
         } else if (String.class.equals(clazz)) {
            o = getProperty(key);
         } else if (URL.class.equals(clazz)) {
            try {
               o = new URL(getProperty(key));
            } catch (MalformedURLException ex) {
               throw new VectorPrintRuntimeException(ex);
            }
         } else if (Date.class.equals(clazz)) {
            try {
               o = new SimpleDateFormat().parse(getProperty(key));
            } catch (java.text.ParseException ex) {
               throw new VectorPrintRuntimeException(ex);
            }
         } else if (Class.class.equals(clazz)) {
            try {
               o = MultipleValueParser.classFromKey(getProperty(key));
            } catch (ClassNotFoundException ex) {
               throw new VectorPrintRuntimeException(ex);
            }
         } else if (String[].class.equals(clazz)) {
            o = ArrayHelper.toArray(mvp.parseStringValues(getProperty(key)));
         } else if (URL[].class.equals(clazz)) {
            o = ArrayHelper.toArray(mvp.parseURLValues(getProperty(key)));
         } else if (Float[].class.equals(clazz)) {
            o = ArrayHelper.toArray(mvp.parseFloatValues(getProperty(key)));
         } else if (float[].class.equals(clazz)) {
            o = ArrayHelper.unWrap(ArrayHelper.toArray(mvp.parseFloatValues(getProperty(key))));
         } else if (Double[].class.equals(clazz)) {
            o = ArrayHelper.toArray(mvp.parseDoubleValues(getProperty(key)));
         } else if (double[].class.equals(clazz)) {
            o = ArrayHelper.unWrap(ArrayHelper.toArray(mvp.parseDoubleValues(getProperty(key))));
         } else if (Short[].class.equals(clazz)) {
            o = ArrayHelper.toArray(mvp.parseShortValues(getProperty(key)));
         } else if (short[].class.equals(clazz)) {
            o = ArrayHelper.unWrap(ArrayHelper.toArray(mvp.parseShortValues(getProperty(key))));
         } else if (Character[].class.equals(clazz)) {
            o = ArrayHelper.toArray(mvp.parseCharValues(getProperty(key)));
         } else if (char[].class.equals(clazz)) {
            o = ArrayHelper.unWrap(ArrayHelper.toArray(mvp.parseCharValues(getProperty(key))));
         } else if (Byte[].class.equals(clazz)) {
            o = ArrayHelper.toArray(mvp.parseByteValues(getProperty(key)));
         } else if (byte[].class.equals(clazz)) {
            o = ArrayHelper.unWrap(ArrayHelper.toArray(mvp.parseByteValues(getProperty(key))));
         } else if (Integer[].class.equals(clazz)) {
            o = ArrayHelper.toArray(mvp.parseFloatValues(getProperty(key)));
         } else if (int[].class.equals(clazz)) {
            o = ArrayHelper.unWrap(ArrayHelper.toArray(mvp.parseFloatValues(getProperty(key))));
         } else if (Long[].class.equals(clazz)) {
            o = ArrayHelper.toArray(mvp.parseLongValues(getProperty(key)));
         } else if (long[].class.equals(clazz)) {
            o = ArrayHelper.unWrap(ArrayHelper.toArray(mvp.parseLongValues(getProperty(key))));
         } else if (Boolean[].class.equals(clazz)) {
            o = ArrayHelper.toArray(mvp.parseBooleanValues(getProperty(key)));
         } else if (boolean[].class.equals(clazz)) {
            o = ArrayHelper.unWrap(ArrayHelper.toArray(mvp.parseBooleanValues(getProperty(key))));
         } else if (Color[].class.equals(clazz)) {
            o = ArrayHelper.toArray(mvp.parseColorValues(getProperty(key)));
         } else if (Date[].class.equals(clazz)) {
            o = ArrayHelper.toArray(mvp.parseDateValues(getProperty(key)));
         } else if (Class[].class.equals(clazz)) {
            o = ArrayHelper.toArray(mvp.parseClassValues(getProperty(key)));
         } else {
            throw new VectorPrintRuntimeException(clazz.getName() + " not supported");
         }
      } catch (ParseException parseException) {
         throw new VectorPrintRuntimeException(parseException);
      }
      return (T) o;
   }

   private void readObject(java.io.ObjectInputStream s)
       throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      mvp = MultipleValueParser.getInstance();
   }

   @Override
   public Date getDateProperty(String key, Date defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      try {
         return new SimpleDateFormat().parse(getProperty(key));
      } catch (java.text.ParseException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   @Override
   public Date[] getDateProperties(String key, Date[] defaultValue) {
      if (shouldUseDefault(key, defaultValue)) {
         return defaultValue;
      }
      try {
         return ArrayHelper.toArray(mvp.parseDateValues(getProperty(key)));
      } catch (ParseException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
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
    * uses {@link MultipleValueParser#classFromKey(java.lang.String) }
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
      return MultipleValueParser.classFromKey(getProperty(key));
   }

   /**
    * uses {@link MultipleValueParser#parseClassValues(java.lang.String, boolean) }
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
      try {
         return ArrayHelper.toArray(mvp.parseClassValues(getProperty(key)));
      } catch (ParseException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

}
