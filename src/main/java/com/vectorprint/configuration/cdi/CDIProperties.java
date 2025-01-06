/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.configuration.cdi;

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
import com.vectorprint.StringConverter;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.binding.BindingHelper;
import com.vectorprint.configuration.binding.settings.SettingsBindingService;
import com.vectorprint.configuration.decoration.AbstractPropertiesDecorator;
import com.vectorprint.configuration.decoration.visiting.CacheClearingVisitor;
import com.vectorprint.configuration.decoration.visiting.ObservableVisitor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.AnnotatedCallable;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * A CDI Producer of properties allowing you to use @Inject in combination with
 * {@link Property} on fields or methods with one parameter. Injected
 * {@link Property properties} will be updated when property file changes,
 * {@link AutoReload} is true, {@link Property#updatable() } is true and the
 * property is injected in a managed bean.
 *
 * @author Eduard Drenth at VectorPrint.nl
 * @see PropertyResolver
 */
@ApplicationScoped
public class CDIProperties extends AbstractPropertiesDecorator implements PropertyChangeListener {

    private static final BindingHelper bindingHelper = SettingsBindingService.getInstance().getFactory().getBindingHelper();

    @Inject
    public CDIProperties(@PropertyProducer EnhancedMap settings) {
        super(settings);
        ((AbstractPropertiesDecorator) settings).accept(new ObservableVisitor(this));
    }

    private final Map<String, List<InjectionPoint>> injectionPoints
            = new HashMap<String, List<InjectionPoint>>(100) {
        @Override
        public List<InjectionPoint> get(Object key) {
            if (!containsKey(key)) {
                put((String) key, new ArrayList<>(3));
            }
            return super.get(key);
        }
    };

    private boolean isUpdatable(InjectionPoint ip) {
        Bean<?> bean = ip.getBean();
        boolean rv = bean != null;
        if (rv) {
            Class<? extends Annotation> scope = bean.getScope();
            rv = scope.equals(Singleton.class)
                    || (scope.equals(ApplicationScoped.class) && ip.getMember() instanceof Method);
            if (!rv) {
                log.warn("reloading not supported for %s in %s %s".formatted(List.of(names(ip)), scope.getSimpleName(), ip.getBean().getBeanClass()));
            }

        } else {
                log.warn("reloading not supported for %s".formatted(ip.getMember().getDeclaringClass().getName()+"#"+names(ip)));
        }
        return rv;
    }

    private String[] getKeys(final InjectionPoint ip) {
        final Property property = fromIp(ip);
        String[] rv = names(ip, property);
        if (property.updatable() && isUpdatable(ip)) {
            Arrays.stream(rv).forEach(a -> injectionPoints.get(a).add(ip));
        }
        // TODO here we could log/register where in the application properties are injected
        return rv;
    }

    public static String[] names(InjectionPoint ip) {
        return names(ip, ip.getAnnotated().getAnnotation(Property.class));
    }

    public static String[] names(InjectionPoint ip, Property property) {
        return property.keys().length > 0 ? property.keys() : new String[]{ip.getMember().getName()};
    }

    public static Property fromIp(InjectionPoint ip) {
        final Member member = ip.getMember();
        return member instanceof Method m
                ? m.getAnnotation(Property.class)
                : ip.getAnnotated().getAnnotation(Property.class);
    }

    private Object getDefault(final InjectionPoint ip) {
        Class clazz = (Class) ip.getAnnotated().getBaseType();
        String[] defaultValue = fromIp(ip).defaultValue();
        if (defaultValue.length > 0) {
            return clazz.isArray()
                    ? bindingHelper.convert(defaultValue, clazz)
                    : bindingHelper.convert(defaultValue[0], clazz);
        } else if (boolean.class.equals(clazz) || Boolean.class.equals(clazz)) {
            return false;
        } else if (char.class.equals(clazz) || Character.class.equals(clazz)) {
            return '\u0000';
        } else if (double.class.equals(clazz) || Double.class.equals(clazz)) {
            return 0.0d;
        } else if (float.class.equals(clazz) || Float.class.equals(clazz)) {
            return 0.0f;
        } else if (int.class.equals(clazz) || Integer.class.equals(clazz)) {
            return 0;
        } else if (short.class.equals(clazz) || Short.class.equals(clazz)) {
            return 0;
        } else if (byte.class.equals(clazz) || Byte.class.equals(clazz)) {
            return 0;
        } else if (long.class.equals(clazz) || Long.class.equals(clazz)) {
            return 0;
        } else {
            return null;
        }
    }

    @Produces
    @Properties
    public EnhancedMap getEnhancedMap(InjectionPoint ip) {
        return this;
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public File[] getFileProperties(InjectionPoint ip) {
        return getFileProperties((File[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public File getFileProperty(InjectionPoint ip) {
        return getFileProperty((File) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public Pattern[] getRegexProperties(InjectionPoint ip) {
        return getRegexProperties((Pattern[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public Pattern getRegexProperty(InjectionPoint ip) {
        return getRegexProperty((Pattern) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public Class[] getClassProperties(InjectionPoint ip) throws ClassNotFoundException {
        return getClassProperties((Class[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public Class getClassProperty(InjectionPoint ip) throws ClassNotFoundException {
        return getClassProperty((Class) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public LocalDateTime[] getDateProperties(InjectionPoint ip) {
        return getLocalDateTimeProperties((LocalDateTime[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public LocalDateTime getDateProperty(InjectionPoint ip) {
        return getLocalDateTimeProperty((LocalDateTime) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public byte[] getByteProperties(InjectionPoint ip) {
        return getByteProperties((byte[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public char[] getCharProperties(InjectionPoint ip) {
        return getCharProperties((char[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public short[] getShortProperties(InjectionPoint ip) {
        return getShortProperties((short[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public byte getByteProperty(InjectionPoint ip) {
        return getByteProperty((Byte) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public char getCharProperty(InjectionPoint ip) {
        return getCharProperty((Character) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public short getShortProperty(InjectionPoint ip) {
        return getShortProperty((Short) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public URL[] getURLProperties(InjectionPoint ip) throws MalformedURLException {
        return getURLProperties((URL[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public URL getURLProperty(InjectionPoint ip) throws MalformedURLException {
        return getURLProperty((URL) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public Color[] getColorProperties(InjectionPoint ip) {
        return getColorProperties((Color[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public boolean[] getBooleanProperties(InjectionPoint ip) {
        return getBooleanProperties((boolean[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public long[] getLongProperties(InjectionPoint ip) {
        return getLongProperties((long[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public int[] getIntegerProperties(InjectionPoint ip) {
        return getIntegerProperties((int[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public double[] getDoubleProperties(InjectionPoint ip) {
        return getDoubleProperties((double[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public float[] getFloatProperties(InjectionPoint ip) {
        return getFloatProperties((float[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public String[] getStringProperties(InjectionPoint ip) {
        return getStringProperties((String[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public String getProperty(InjectionPoint ip) {
        return getProperty((String) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public long getLongProperty(InjectionPoint ip) {
        return getLongProperty((Long) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public int getIntegerProperty(InjectionPoint ip) {
        return getIntegerProperty((Integer) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public float getFloatProperty(InjectionPoint ip) {
        return getFloatProperty((Float) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public double getDoubleProperty(InjectionPoint ip) {
        return getDoubleProperty((Double) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public Color getColorProperty(InjectionPoint ip) {
        return getColorProperty((Color) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @Default
    @CheckInjection
    public boolean getBooleanProperty(InjectionPoint ip) {
        return getBooleanProperty((Boolean) getDefault(ip), getKeys(ip));
    }

    @Override
    public EnhancedMap clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private static final CacheClearingVisitor CACHE_CLEARING_VISITOR
            = new CacheClearingVisitor();

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        /*
        via the changes and injection points we should be able to set
        new values
         */

        String c = propertyChangeEvent.getPropertyName();
        injectionPoints.get(c).forEach(ip -> {
            // if bean is null issue a warning, injectionpoint is not in a bean (i.e. webservlet)
            Class bc = ip.getMember().getDeclaringClass();
            Object reference = CDI.current().select(bc).get();
            if (reference == null) {
                log.warn(String.format("Bean for %s not present, BeanManager cannot resolve Object holding %s", bc.getName(), List.of(names(ip))));
            } else {
                update(ip, reference, (String[]) propertyChangeEvent.getNewValue());
            }
        });
        accept(CACHE_CLEARING_VISITOR);
    }

    private void update(InjectionPoint ip, Object reference, String... strValue) {
        Annotated annotated = ip.getAnnotated();
        Class clazz = (Class) annotated.getBaseType();
        Object value = clazz.isAssignableFrom(String[].class) ? strValue
                : clazz.isAssignableFrom(String.class) && strValue != null && strValue.length == 1 ? strValue[0] : null;
        if (value == null && strValue != null) {
            StringConverter stringConverter = StringConverter.forClass(clazz);
            if (clazz.isArray() && strValue.length > 0) {
                Object ar = Array.newInstance(clazz, strValue.length);
                IntStream.range(0, strValue.length).forEach(i -> Array.set(ar, i, stringConverter.convert(strValue[i])));
            } else {
                value = stringConverter.convert(strValue[0]);
            }
        }
        if (annotated instanceof AnnotatedField af) {
            Field f = af.getJavaMember();
            try {
                boolean ac = f.canAccess(reference);
                f.setAccessible(true);
                f.set(reference, value);
                f.setAccessible(ac);
            } catch (IllegalAccessException e) {
                log.error(String.format("error updating %s with %s", f, value));
            }
        } else {
            AnnotatedParameter ap = (AnnotatedParameter) annotated;
            AnnotatedCallable declaringCallable = ap.getDeclaringCallable();
            if (declaringCallable instanceof AnnotatedMethod am) {
                Method method = am.getJavaMember();
                if (method.getParameterCount() == 1) {
                    try {
                        boolean ac = method.canAccess(reference);
                        method.setAccessible(true);
                        method.invoke(reference, value);
                        method.setAccessible(ac);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        log.error(String.format("error calling %s with %s", method, value), e);
                    }
                } else {
                    log.warn(String.format("%s has more than one argument, not supported yet", method));
                }
            } else {
                log.warn(String.format("calling constructor %s not supported", declaringCallable));
            }
        }
    }

}
