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
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.annotation.Property;
import java.awt.Color;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.regex.Pattern;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * A EE Producer of properties allowing you to use @Inject in combination with {@link Property}.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class EEProperties extends AbstractPropertiesDecorator {

    public EEProperties(EnhancedMap properties) {
        super(properties);
    }
    
    private String[] getKeys(final InjectionPoint ip) {
        return (ip.getAnnotated().isAnnotationPresent(Property.class) && 
                ip.getAnnotated().getAnnotation(Property.class).keys().length>0) ? 
                ip.getAnnotated().getAnnotation(Property.class).keys() :
                new String[] {ip.getMember().getName()};
    }    

    @Produces
    @Property
    public File[] getFileProperties(InjectionPoint ip) {
        return getFileProperties(null, getKeys(ip));
    }

    @Produces
    @Property
    public File getFileProperty(InjectionPoint ip) {
        return getFileProperty(null, getKeys(ip));
    }

    @Produces
    @Property
    public Pattern[] getRegexProperties(InjectionPoint ip) {
        return getRegexProperties(null,getKeys(ip));
    }

    @Produces
    @Property
    public Pattern getRegexProperty(InjectionPoint ip) {
        return getRegexProperty(null, getKeys(ip));
    }

    @Produces
    @Property
    public Class[] getClassProperties(InjectionPoint ip) throws ClassNotFoundException {
        return getClassProperties(null, getKeys(ip));
    }

    @Produces
    @Property
    public Class getClassProperty(InjectionPoint ip) throws ClassNotFoundException {
        return getClassProperty(null, getKeys(ip));
    }

    @Produces
    @Property
    public Date[] getDateProperties(InjectionPoint ip) {
        return getDateProperties(null, getKeys(ip));
    }

    @Produces
    @Property
    public Date getDateProperty(InjectionPoint ip) {
        return getDateProperty(null, getKeys(ip));
    }

    @Produces
    @Property
    public byte[] getByteProperties(InjectionPoint ip) {
        return getByteProperties(null, getKeys(ip));
    }

    @Produces
    @Property
    public char[] getCharProperties(InjectionPoint ip) {
        return getCharProperties(null, getKeys(ip));
   }

    @Produces
    @Property
    public short[] getShortProperties(InjectionPoint ip) {
        return getShortProperties(null, getKeys(ip));
    }

    @Produces
    @Property
    public byte getByteProperty(InjectionPoint ip) {
        return getByteProperty(null, getKeys(ip));
    }

    @Produces
    @Property
    public char getCharProperty(InjectionPoint ip) {
        return getCharProperty(null, getKeys(ip));
    }

    @Produces
    @Property
    public short getShortProperty(InjectionPoint ip) {
        return getShortProperty(null, getKeys(ip));
    }

    @Produces
    @Property
    public URL[] getURLProperties(InjectionPoint ip) throws MalformedURLException {
        return getURLProperties(null, getKeys(ip));
    }

    @Produces
    @Property
    public URL getURLProperty(InjectionPoint ip) throws MalformedURLException {
        return getURLProperty(null, getKeys(ip));
    }

    @Produces
    @Property
    public Color[] getColorProperties(InjectionPoint ip) {
        return getColorProperties(null, getKeys(ip));
    }

    @Produces
    @Property
    public boolean[] getBooleanProperties(InjectionPoint ip) {
        return getBooleanProperties(null, getKeys(ip));
    }

    @Produces
    @Property
    public long[] getLongProperties(InjectionPoint ip) {
        return getLongProperties(null, getKeys(ip));
    }

    @Produces
    @Property
    public int[] getIntegerProperties(InjectionPoint ip) {
        return getIntegerProperties(null, getKeys(ip));
    }

    @Produces
    @Property
    public double[] getDoubleProperties(InjectionPoint ip) {
        return getDoubleProperties(null, getKeys(ip));
    }

    @Produces
    @Property
    public float[] getFloatProperties(InjectionPoint ip) {
        return getFloatProperties(null, getKeys(ip));
    }

    @Produces
    @Property
    public String[] getStringProperties(InjectionPoint ip) {
        return getStringProperties(null, getKeys(ip));
    }

    @Produces
    @Property
    public String getProperty(InjectionPoint ip) {
        return getProperty(null, getKeys(ip));
    }

    @Produces
    @Property
    public long getLongProperty(InjectionPoint ip) {
        return getLongProperty(null, getKeys(ip));
    }

    @Produces
    @Property
    public int getIntegerProperty(InjectionPoint ip) {
        return getIntegerProperty(null, getKeys(ip));
    }

    @Produces
    @Property
    public float getFloatProperty(InjectionPoint ip) {
        return getFloatProperty(null, getKeys(ip));
    }

    @Produces
    @Property
    public double getDoubleProperty(InjectionPoint ip) {
        return getDoubleProperty(null, getKeys(ip));
    }

    @Produces
    @Property
    public Color getColorProperty(InjectionPoint ip) {
        return getColorProperty(null, getKeys(ip));
    }

    @Produces
    @Property
    public boolean getBooleanProperty(InjectionPoint ip) {
        return getBooleanProperty(null, getKeys(ip));
    }

}
