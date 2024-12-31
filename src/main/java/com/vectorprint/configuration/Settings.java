package com.vectorprint.configuration;

/*-
 * #%L
 * Config
 * %%
 * Copyright (C) 2015 - 2018 VectorPrint
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
import com.vectorprint.configuration.binding.AbstractBindingHelperDecorator;
import com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactory;
import com.vectorprint.configuration.binding.settings.SettingsBindingService;
import com.vectorprint.configuration.decoration.AbstractPropertiesDecorator;
import com.vectorprint.configuration.decoration.DecorationAware;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.PrintStream;
import java.io.Serial;
import java.lang.ref.Cleaner;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public final class Settings implements EnhancedMap, DecorationAware {

    private static final Cleaner CLEANER = Cleaner.create();
    @Serial
    private static final long serialVersionUID = 1;
    private static final Logger log = LoggerFactory.getLogger(Settings.class.getName());
    private final Map<String, String[]> backingMap;

    private transient final Runnable finalizer = new Runnable() {

        @Override
        public void run() {
            log.warn("not used, possibly obsolete settings in: %s: %s".formatted(this, getUnusedKeys()));
        }
    };

    private final transient Cleaner.Cleanable cleanable;

    @Override
    public void listProperties(PrintStream ps) {
        ps.printf("settings with id %s:%n", getId());
        ps.println();
        backingMap.forEach((key, value) -> ps.printf("%s=%s%n", key, value != null ? Arrays.asList(value) : ""));
        ps.println();
        ps.printf("settings wrapped by %s%n", decorators);
    }
    private String id;
    private final Map<String, PropertyHelp> help = new HashMap<>(50);
    private final List<Class<? extends AbstractPropertiesDecorator>> decorators
            = new ArrayList<>(3);
    private AbstractPropertiesDecorator outermostDecorator;

    public Settings() {
        backingMap = new HashMap<>();
        cleanable = CLEANER.register(this, finalizer);
    }

    /**
     * Creates a backing map {@link HashMap#HashMap(int, float) }.
     *
     * @param initialCapacity
     * @param loadFactor
     */
    public Settings(int initialCapacity, float loadFactor) {
        backingMap = new HashMap<>(initialCapacity, loadFactor);
        cleanable = CLEANER.register(this, finalizer);
    }

    /**
     * Creates a backing map {@link HashMap#HashMap(int) }.
     *
     * @param initialCapacity
     */
    public Settings(int initialCapacity) {
        backingMap = new HashMap<>(initialCapacity);
        cleanable = CLEANER.register(this, finalizer);
    }

    /**
     * Uses the provided Map as backing map, throws an IllegalArgumentException
     * if the map is an instance of EnhancedMap.
     *
     * @param map
     */
    public Settings(Map<String, String[]> map) {
        Objects.requireNonNull(map);
        if (map instanceof EnhancedMap) {
            throw new IllegalArgumentException("instance of " + EnhancedMap.class.getName() + " not allowed");
        }
        backingMap = new HashMap<>(map);
        cleanable = CLEANER.register(this, finalizer);
    }

    private void debug(Object val, String... keys) {
        debug(val, true, keys);
    }

    private void debug(Object val, boolean defaultVal, String... keys) {
        if (log.isDebugEnabled()) {
            StringBuilder s = new StringBuilder(String.valueOf(val));
            if (val != null && val.getClass().isArray()) {
                s = new StringBuilder();
                if (val instanceof boolean[] b) {
                    for (boolean o : b) s.append(o).append("; ");
                } else if (val instanceof char[] c) {
                    for (char o : c) s.append(o).append("; ");
                } else if (val instanceof byte[] b) {
                    for (byte o : b) s.append(String.valueOf(o)).append("; ");
                } else if (val instanceof short[] sh) {
                    for (short o : sh) s.append(String.valueOf(o)).append("; ");
                } else if (val instanceof int[] i) {
                    for (int o : i) s.append(o).append("; ");
                } else if (val instanceof long[] l) {
                    for (long o : l) s.append(o).append("; ");
                } else if (val instanceof float[] f) {
                    for (float o : f) s.append(o).append("; ");
                } else if (val instanceof double[] d) {
                    for (double o : d) s.append(o).append("; ");
                } else {
                    for (Object o : (Object[])val) s.append(o).append("; ");
                }
            }
            log.debug(String.format("looking for property %s in %s, using value %s", keys!=null?Arrays.asList(keys):null, getId(), s.append((defaultVal) ? " (default)" : "")));
        }
    }

    @Override
    public String[] get(Object key) {
        unused.remove(key);
        return backingMap.get(key);
    }

    private String getFirst(String key) {
        if (log.isDebugEnabled()) {
            debug((key != null) ? backingMap.get(key) : null, false, key);
        }
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

    /**
     * When only one argument is given it is assumed to be a key and not a
     * default value.
     *
     * @param defaultValue
     * @param keys
     * @return
     */
    @Override
    public String getProperty(String defaultValue, String... keys) {
        boolean defaultIsKey = defaultValue != null && (keys == null || keys.length == 0);
        String key = defaultIsKey
                ? getKey(null, defaultValue)
                : getKey(defaultValue, keys);
        return key == null ? defaultValue : getFirst(key);
    }

    @Override
    public URL getURLProperty(URL defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : getFactory().getBindingHelper().convert(getFirst(key), URL.class);
    }

    @Override
    public File getFileProperty(File defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : getFactory().getBindingHelper().convert(getFirst(key), File.class);
    }

    @Override
    public float getFloatProperty(Float defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : getFactory().getBindingHelper().convert(getFirst(key), Float.class);
    }

    @Override
    public boolean getBooleanProperty(Boolean defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : getFactory().getBindingHelper().convert(getFirst(key), Boolean.class);
    }

    /**
     * determine the {@link #determineKey(java.lang.String...) key to be used}, call {@link #handleNoValue(java.lang.String...) } when no key is found
     * and no default is given.
     *
     * @param defaultVal a default value
     * @param keys the keys to look for
     * @return the key to be used or null to use a default value
     */
    private String getKey(Object defaultVal, String... keys) {
        String key = determineKey(keys);
        if (key == null) {
            if (defaultVal == null) {
                handleNoValue(keys);
            } else {
                debug(defaultVal);
            }
        } else {
            return key;
        }
        return null;
    }

    /**
     * throws a {@link NoValueException}
     * @param keys
     */
    private void handleNoValue(String... keys) {
        throw new NoValueException(Arrays.asList(keys) + " not found and default is null");
    }

    /**
     * Return the first key from the list of arguments present in the settings.
     * Logs the key that is returned, maintains
     * {@link #getKeysNotPresent() set of keys not present}.
     *
     * @param keys
     * @return the first key found in settings or null
     */
    private String determineKey(String... keys) {
        if (keys == null || keys.length == 0 || keys[0] == null) {
            throw new VectorPrintRuntimeException("You should provide at least one key");
        }
        if (keys.length == 1) {
            if (containsKey(keys[0])) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Returning key \"%s\" from %s, it was first found in settings", keys[0], Arrays.asList(keys)));
                }
                notPresent.remove(keys[0]);
                return keys[0];
            } else {
                notPresent.add(keys[0]);
                return null;
            }
        }
        for (String k : keys) {
            if (containsKey(k)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Returning key \"%s\" from %s, it was first found in settings", k, Arrays.asList(keys)));
                }
                notPresent.remove(keys[0]);
                return k;
            } else notPresent.add(k);

        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("None of %s found in settings", Arrays.asList(keys)));
        }
        return null;
    }

    @Override
    public double getDoubleProperty(Double defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : getFactory().getBindingHelper().convert(getFirst(key), Double.class);
    }

    @Override
    public int getIntegerProperty(Integer defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : getFactory().getBindingHelper().convert(getFirst(key), Integer.class);
    }

    @Override
    public short getShortProperty(Short defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : getFactory().getBindingHelper().convert(getFirst(key), Short.class);
    }

    @Override
    public char getCharProperty(Character defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : getFactory().getBindingHelper().convert(getFirst(key), Character.class);
    }

    @Override
    public byte getByteProperty(Byte defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : getFactory().getBindingHelper().convert(getFirst(key), Byte.class);
    }

    @Override
    public long getLongProperty(Long defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : getFactory().getBindingHelper().convert(getFirst(key), Long.class);
    }

    @Override
    public Color getColorProperty(Color defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : getFactory().getBindingHelper().convert(getFirst(key), Color.class);
    }

    @Override
    public String[] getStringProperties(String[] defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        if (key == null) {
            return defaultValue;
        }
        return get(key);
    }

    @Override
    public URL[] getURLProperties(URL[] defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : AbstractBindingHelperDecorator.parse(get(key),URL.class);
    }

    @Override
    public File[] getFileProperties(File[] defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : AbstractBindingHelperDecorator.parse(get(key),File.class);
    }

    @Override
    public float[] getFloatProperties(float[] defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : AbstractBindingHelperDecorator.parseFloatValues(get(key));
    }

    @Override
    public char[] getCharProperties(char[] defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : AbstractBindingHelperDecorator.parseCharValues(get(key));

    }

    @Override
    public short[] getShortProperties(short[] defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : AbstractBindingHelperDecorator.parseShortValues(get(key));
    }

    @Override
    public byte[] getByteProperties(byte[] defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : AbstractBindingHelperDecorator.parseByteValues(get(key));
    }

    @Override
    public double[] getDoubleProperties(double[] defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : AbstractBindingHelperDecorator.parseDoubleValues(get(key));
    }

    @Override
    public int[] getIntegerProperties(int[] defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : AbstractBindingHelperDecorator.parseIntValues(get(key));
    }

    @Override
    public boolean[] getBooleanProperties(boolean[] defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : AbstractBindingHelperDecorator.parseBooleanValues(get(key));
    }

    @Override
    public Color[] getColorProperties(Color[] defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : AbstractBindingHelperDecorator.parse(get(key),Color.class);
    }

    /**
     * This implementation only includes key and value state, not the rest of
     * the state (i.e. {@link #getUnusedKeys() }, {@link #getHelp()
     * } and {@link #getKeysNotPresent() }). The {@link #getId() id's} of the
     * objects must be both null or the same, otherwise false is returned.
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
        if (!Objects.equals(this.id, other.id)) {
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
        help.forEach((key, value) -> sb.append(key).append(": ").append(value.getType())
                .append("; ")
                .append(value.getExplanation())
                .append(System.lineSeparator()));
        return sb.toString();
    }

    @Override
    public long[] getLongProperties(long[] defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : AbstractBindingHelperDecorator.parseLongValues(get(key));
    }

    @Override
    public String[] put(String key, String[] value) {
        if (!backingMap.containsKey(key)) unused.add(key);
        return backingMap.put(key, value);
    }

    @Override
    public void clear() {
        unused.clear();
        notPresent.clear();
        help.clear();
        decorators.clear();
        backingMap.clear();
    }

    @Override
    public String[] remove(Object key) {
        unused.remove(key);
        return backingMap.remove(key);
    }

    private void init(Settings vp) {
        vp.help.putAll(help);
        vp.unused.addAll(keySet());
        vp.id = id;
        vp.notPresent.addAll(notPresent);
        vp.decorators.addAll(decorators);
        vp.outermostDecorator = outermostDecorator;
    }

    /**
     * Creates a new identical Settings object. Note that the id of the clone
     * will equal that of the original. The backing Map will be cloned by
     * calling clone when it is Cloneable, otherwise newInstance and putAll are
     * used to clone the backing Map. The
     * {@link #getOutermostDecorator() outermostDecorator} will be copied, not
     * cloned.
     *
     * @return an identical copy of these Settings.
     */
    @Override
    public Settings clone() {
        Settings vp;
        if (backingMap instanceof Cloneable) {
            try {
                Method m = backingMap.getClass().getMethod("clone");
                vp = new Settings((Map<String, String[]>) m.invoke(backingMap));
            } catch (InvocationTargetException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException ex) {
                throw new VectorPrintRuntimeException(ex);
            }
        } else {
            try {
                vp = new Settings(backingMap.getClass().getConstructor().newInstance());
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
                throw new VectorPrintRuntimeException(ex);
            }
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
     * this implementation supports all primitives and their wrappers, Color,
     * Date, URL, Class and arrays of those types. Calls {@link #getGenericProperty(java.lang.String, java.lang.Object, java.lang.Class)
     * } if one of the keys is present in the settings.
     *
     * @param <T>
     * @param keys
     * @param defaultValue
     * @param clazz
     * @return value of the setting or the default value
     * @throws VectorPrintRuntimeException when no value is found and
     * defaultValue is null
     */
    public <T> T getGenericProperty(T defaultValue, Class<T> clazz, String... keys) {
        String key = getKey(defaultValue, keys);
        return getGenericProperty(key, defaultValue, clazz);
    }

    /**
     * this implementation supports all primitives and their wrappers, Color,
     * Date, URL, Class and arrays of those types.
     *
     * @param <T>
     * @param key
     * @param defaultValue
     * @param clazz
     * @return value of the setting or the default value
     * defaultValue is null
     */
    private <T> T getGenericProperty(String key, T defaultValue, Class<T> clazz) {
        if (key == null) {
            return defaultValue;
        } else if (clazz.isArray()) {
            if (String[].class.equals(clazz)) {
                return (T) get(key);
            }
            return getFactory().getBindingHelper().convert(get(key), clazz);
        } else {
            if (String.class.equals(clazz)) {
                return (T) getFirst(key);
            }
            return getFactory().getBindingHelper().convert(getFirst(key), clazz);
        }
    }

    @Override
    public LocalDateTime getLocalDateTimeProperty(LocalDateTime defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : getFactory().getBindingHelper().convert(getFirst(key), LocalDateTime.class);
    }

    @Override
    public LocalDateTime[] getLocalDateTimeProperties(LocalDateTime[] defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : AbstractBindingHelperDecorator.parse(get(key),LocalDateTime.class);
    }

    private final Collection<String> unused = new HashSet<>(25);

    @Override
    public Collection<String> getUnusedKeys() {
        unused.removeIf(s -> !containsKey(s));
        return Collections.unmodifiableCollection(unused);
    }

    private final Set<String> notPresent = new HashSet<>(25);

    @Override
    public Collection<String> getKeysNotPresent() {
        return Collections.unmodifiableCollection(notPresent);
    }


    @Override
    public String toString() {
        return "Settings{" + "id=" + id + ", decorators=" + decorators + '}';
    }
    
    

    /**
     *
     * @param defaultValue the value of defaultValue
     * @param keys the value of keys
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    public Class getClassProperty(Class defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : getFactory().getBindingHelper().convert(getFirst(key), Class.class);
    }

    @Override
    public Pattern getRegexProperty(Pattern defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : getFactory().getBindingHelper().convert(getFirst(key), Pattern.class);
    }

    @Override
    public Pattern[] getRegexProperties(Pattern[] defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : AbstractBindingHelperDecorator.parse(get(key),Pattern.class);
    }

    /**
     *
     * @param defaultValue the value of defaultValue
     * @param keys the value of keys
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    public Class[] getClassProperties(Class[] defaultValue, String... keys) {
        String key = getKey(defaultValue, keys);
        return key == null ? defaultValue : AbstractBindingHelperDecorator.parse(get(key),Class.class);
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
        log.warn(String.format("NB! Settings wrapped by %s, you should use this instead of %s", outermostDecorator.getClass().getName(),
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

            return entrySet().stream().anyMatch((e) -> (Arrays.equals(e.getValue(), (String[]) value)));
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
        m.forEach((k, v) -> {
            if (!backingMap.containsKey(k)) unused.add(k);
        });
        backingMap.putAll(m);
    }

    @Override
    public void put(Map<String, String> m) {
        m.forEach(this::put);
    }

    @Override
    public @NotNull Set<String> keySet() {
        return backingMap.keySet();
    }

    @Override
    public @NotNull Collection<String[]> values() {
        return backingMap.values();
    }

    @Override
    public @NotNull Set<Entry<String, String[]>> entrySet() {
        return backingMap.entrySet();
    }

}
