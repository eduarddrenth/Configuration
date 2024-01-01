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

import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.binding.BindingHelper;
import com.vectorprint.configuration.binding.settings.SettingsBindingService;
import com.vectorprint.configuration.decoration.AbstractPropertiesDecorator;
import com.vectorprint.configuration.decoration.Changes;
import com.vectorprint.configuration.decoration.Observable;
import com.vectorprint.configuration.decoration.Observer;
import com.vectorprint.configuration.decoration.visiting.CacheClearingVisitor;
import com.vectorprint.configuration.decoration.visiting.ObservableVisitor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.AnnotatedCallable;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A CDI Producer of properties allowing you to use @Inject in combination with
 * {@link Property}.
 * NOTE parameters option for javac (java 8+) must be used when {@link Property} is used on
 * method parameters without {@link Property#keys()}. Injected {@link Property properties} will be updated
 * when property file changes, {@link AutoReload} is true, {@link Property#updatable() } is true and the property is injected in a managed bean.
 * @author Eduard Drenth at VectorPrint.nl
 * @see PropertyResolver
 */
@ApplicationScoped
public class CDIProperties extends AbstractPropertiesDecorator implements Observer {

    private static final BindingHelper bindingHelper = SettingsBindingService.getInstance().getFactory().getBindingHelper();

    @Inject
    public CDIProperties(@PropertyProducer EnhancedMap settings) {
        super(settings);
        ((AbstractPropertiesDecorator) settings).accept(new ObservableVisitor(this));
    }

    private final Map<String, List<InjectionPoint>> injectionPoints =
            new HashMap<String, List<InjectionPoint>>(100) {
                @Override
                public List<InjectionPoint> get(Object key) {
                    if (!containsKey(key)) {
                        put((String) key, new ArrayList<>(3));
                    }
                    return super.get(key);
                }
            };

    private String[] getKeys(final InjectionPoint ip) {
        String[] rv = (ip.getAnnotated().getAnnotation(Property.class).keys().length > 0)
                ? ip.getAnnotated().getAnnotation(Property.class).keys()
                : new String[]{name(ip.getAnnotated())};
        Arrays.stream(rv).forEach(a -> injectionPoints.get(a).add(ip));
        return rv;
    }

    private static final Pattern ARGNAME = Pattern.compile("^arg[0-9]+");

    private String name(Annotated annotated) {
        String name = annotated instanceof AnnotatedField ?
                ((AnnotatedField)annotated).getJavaMember().getName() :
                ((AnnotatedParameter)annotated).getJavaParameter().getName();
        if (annotated instanceof AnnotatedParameter && ARGNAME.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    String.format("compile using -parameters (java 8+) or use keys() argument to Property, to get" +
                            " real name for %s", ((AnnotatedParameter)annotated).getDeclaringCallable())
            );
        }
        return name;
    }

    private Object getDefault(final InjectionPoint ip) {
        Class clazz = (Class) ip.getAnnotated().getBaseType();
        if (ip.getAnnotated().getAnnotation(Property.class).defaultValue().length > 0) {
            return clazz.isArray()
                    ? bindingHelper.convert(ip.getAnnotated().getAnnotation(Property.class).defaultValue(), clazz)
                    : bindingHelper.convert(ip.getAnnotated().getAnnotation(Property.class).defaultValue()[0], clazz);
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
    @CheckRequired
    public File[] getFileProperties(InjectionPoint ip) {
        return getFileProperties((File[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public File getFileProperty(InjectionPoint ip) {
        return getFileProperty((File) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public Pattern[] getRegexProperties(InjectionPoint ip) {
        return getRegexProperties((Pattern[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public Pattern getRegexProperty(InjectionPoint ip) {
        return getRegexProperty((Pattern) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public Class[] getClassProperties(InjectionPoint ip) throws ClassNotFoundException {
        return getClassProperties((Class[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public Class getClassProperty(InjectionPoint ip) throws ClassNotFoundException {
        return getClassProperty((Class) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public Date[] getDateProperties(InjectionPoint ip) {
        return getDateProperties((Date[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public Date getDateProperty(InjectionPoint ip) {
        return getDateProperty((Date) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public byte[] getByteProperties(InjectionPoint ip) {
        return getByteProperties((byte[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public char[] getCharProperties(InjectionPoint ip) {
        return getCharProperties((char[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public short[] getShortProperties(InjectionPoint ip) {
        return getShortProperties((short[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public byte getByteProperty(InjectionPoint ip) {
        return getByteProperty((Byte) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public char getCharProperty(InjectionPoint ip) {
        return getCharProperty((Character) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public short getShortProperty(InjectionPoint ip) {
        return getShortProperty((Short) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public URL[] getURLProperties(InjectionPoint ip) throws MalformedURLException {
        return getURLProperties((URL[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public URL getURLProperty(InjectionPoint ip) throws MalformedURLException {
        return getURLProperty((URL) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public Color[] getColorProperties(InjectionPoint ip) {
        return getColorProperties((Color[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public boolean[] getBooleanProperties(InjectionPoint ip) {
        return getBooleanProperties((boolean[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public long[] getLongProperties(InjectionPoint ip) {
        return getLongProperties((long[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public int[] getIntegerProperties(InjectionPoint ip) {
        return getIntegerProperties((int[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public double[] getDoubleProperties(InjectionPoint ip) {
        return getDoubleProperties((double[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public float[] getFloatProperties(InjectionPoint ip) {
        return getFloatProperties((float[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public String[] getStringProperties(InjectionPoint ip) {
        return getStringProperties((String[]) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public String getProperty(InjectionPoint ip) {
        return getProperty((String) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public long getLongProperty(InjectionPoint ip) {
        return getLongProperty((Long) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public int getIntegerProperty(InjectionPoint ip) {
        return getIntegerProperty((Integer) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public float getFloatProperty(InjectionPoint ip) {
        return getFloatProperty((Float) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public double getDoubleProperty(InjectionPoint ip) {
        return getDoubleProperty((Double) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public Color getColorProperty(InjectionPoint ip) {
        return getColorProperty((Color) getDefault(ip), getKeys(ip));
    }

    @Produces
    @Property
    @CheckRequired
    public boolean getBooleanProperty(InjectionPoint ip) {
        return getBooleanProperty((Boolean) getDefault(ip), getKeys(ip));
    }

    @Override
    public EnhancedMap clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public void update(Observable object, Changes changes) {
        /*
        via the changes and injectionpoints we should be able to set
        new values
         */
        BeanManager beanManager = CDI.current().getBeanManager();

        List<ToUpdate> updates = new ArrayList<>();
        Stream.concat(
                changes.getChanged().stream(),
                changes.getAdded().stream()
        )
        .forEach(c ->
            injectionPoints.get(c).forEach(ip -> {
                Annotated a = ip.getAnnotated();
                if (a.getAnnotation(Property.class).updatable()) {
                    Bean<?> bean = ip.getBean();
                    // if bean is null issue a warning, injectionpoint is not in a bean (i.e. webservlet)
                    Class bc = ip.getMember().getDeclaringClass();
                    if (bean==null) {
                        String name = a instanceof AnnotatedField ?
                                ((AnnotatedField)a).getJavaMember().getName() :
                                    ((AnnotatedParameter)a).getDeclaringCallable() instanceof AnnotatedMethod ?
                                    ((AnnotatedParameter)a).getDeclaringCallable().getJavaMember().getName() : "unknown field or method";
                        log.warn(String.format("Bean for %s not present, BeanManager cannot resolve Object holding %s", bc.getName(),name));
                    } else {
                        CreationalContext<?> creationalContext =
                                beanManager.createCreationalContext(bean);
                        Object reference = beanManager.getReference(bean, bc, creationalContext);
                        updates.add(new ToUpdate(a, reference, c));
                    }
                }
            })
        );
        accept(CACHE_CLEARING_VISITOR);
        updates.forEach(u -> u.update(getGenericProperty(null, (Class) u.annotated.getBaseType(), u.key)));
    }

    private static final CacheClearingVisitor CACHE_CLEARING_VISITOR =
            new CacheClearingVisitor();

    private static class ToUpdate {
        private final Annotated annotated;
        private final Object reference;
        private final String key;

        public ToUpdate(Annotated annotated, Object reference, String key) {
            this.annotated = annotated;
            this.reference = reference;
            this.key = key;
        }

        void update(Object value) {
            if (annotated instanceof AnnotatedField) {
                Field f = ((AnnotatedField) annotated).getJavaMember();
                try {
                    boolean ac = f.canAccess(reference);
                    f.setAccessible(true);
                    f.set(reference, value);
                    f.setAccessible(ac);
                } catch (IllegalAccessException e) {
                    log.error(String.format("error updating %s with %s",
                            f, value));
                }
            } else {
                AnnotatedParameter ap = (AnnotatedParameter) annotated;
                AnnotatedCallable declaringCallable = ap.getDeclaringCallable();
                if (declaringCallable instanceof AnnotatedMethod) {
                    Method method = ((AnnotatedMethod) declaringCallable).getJavaMember();
                    if (method.getParameterCount()==1) {
                        try {
                            method.invoke(reference,value);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            log.error(String.format("error calling %s with %s",
                                    method, value));
                        }
                    } else {
                        log.warn(String.format("%s has more than one argument, not supported yet",
                                method));
                    }
                } else {
                    log.warn(String.format("calling constructor %s not supported",declaringCallable));
                }
            }
        }
    }

}
