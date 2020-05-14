
package com.vectorprint.configuration.decoration;

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

import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.NoValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.regex.Pattern;

public class AllowNoValue extends AbstractPropertiesDecorator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPropertiesDecorator.class.getName());

    public AllowNoValue(EnhancedMap settings) {
        super(settings);
    }

    @Override
    public Date[] getDateProperties(Date[] defaultValue, String... keys) {
        return allowNoValue(defaultValue, Date[].class, keys);
    }

    private <T> T allowNoValue(T defaultValue, Class<T> clazz, String... keys) {
        try {
            return super.getGenericProperty(defaultValue, clazz, keys);
        } catch (NoValueException ex) {
            return null;
        }
    }

    @Override
    public String getProperty(String defaultValue, String... keys) {
        return allowNoValue(defaultValue, String.class, keys);
    }

    @Override
    public Date getDateProperty(Date defaultValue, String... keys) {
        return allowNoValue(defaultValue, Date.class, keys);
    }

    @Override
    public byte[] getByteProperties(byte[] defaultValue, String... keys) {
        return allowNoValue(defaultValue, byte[].class, keys);
    }

    @Override
    public char[] getCharProperties(char[] defaultValue, String... keys) {
        return allowNoValue(defaultValue, char[].class, keys);
    }

    @Override
    public byte getByteProperty(Byte defaultValue, String... keys) {
        return allowNoValue(defaultValue, byte.class, keys);
    }

    @Override
    public char getCharProperty(Character defaultValue, String... keys) {
        return allowNoValue(defaultValue, char.class, keys);
    }

    @Override
    public Color[] getColorProperties(Color[] defaultValue, String... keys) {
        return allowNoValue(defaultValue, Color[].class, keys);
    }

    @Override
    public boolean[] getBooleanProperties(boolean[] defaultValue, String... keys) {
        return allowNoValue(defaultValue, boolean[].class, keys);
    }

    @Override
    public double[] getDoubleProperties(double[] defaultValue, String... keys) {
        return allowNoValue(defaultValue, double[].class, keys);
    }

    @Override
    public float[] getFloatProperties(float[] defaultValue, String... keys) {
        return allowNoValue(defaultValue, float[].class, keys);
    }

    @Override
    public float getFloatProperty(Float defaultValue, String... keys) {
        return allowNoValue(defaultValue, float.class, keys);
    }

    @Override
    public double getDoubleProperty(Double defaultValue, String... keys) {
        return allowNoValue(defaultValue, double.class, keys);
    }

    @Override
    public Color getColorProperty(Color defaultValue, String... keys) {
        return allowNoValue(defaultValue, Color.class, keys);
    }

    /**
     *
     * @param defaultValue the value of defaultValue
     * @param keys
     * @return the boolean
     */
    @Override
    public boolean getBooleanProperty(Boolean defaultValue, String... keys) {
        return allowNoValue(defaultValue, boolean.class, keys);
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
        return allowNoValue(defaultValue, Class.class, keys);
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
        return allowNoValue(defaultValue, Class[].class, keys);
    }

    @Override
    public short[] getShortProperties(short[] defaultValue, String... keys) {
        return allowNoValue(defaultValue, short[].class, keys);
    }

    @Override
    public short getShortProperty(Short defaultValue, String... keys) {
        return allowNoValue(defaultValue, short.class, keys);
    }

    @Override
    public URL[] getURLProperties(URL[] defaultValue, String... keys) throws MalformedURLException {
        return allowNoValue(defaultValue, URL[].class, keys);
    }

    @Override
    public URL getURLProperty(URL defaultValue, String... keys) throws MalformedURLException {
        return allowNoValue(defaultValue, URL.class, keys);
    }

    @Override
    public File[] getFileProperties(File[] defaultValue, String... keys) {
        return allowNoValue(defaultValue, File[].class, keys);
    }

    @Override
    public File getFileProperty(File defaultValue, String... keys) {
        return allowNoValue(defaultValue, File.class, keys);
    }

    @Override
    public long[] getLongProperties(long[] defaultValue, String... keys) {
        return allowNoValue(defaultValue, long[].class, keys);
    }

    @Override
    public int[] getIntegerProperties(int[] defaultValue, String... keys) {
        return allowNoValue(defaultValue, int[].class, keys);
    }

    @Override
    public String[] getStringProperties(String[] defaultValue, String... keys) {
        return allowNoValue(defaultValue, String[].class, keys);
    }

    @Override
    public long getLongProperty(Long defaultValue, String... keys) {
        return allowNoValue(defaultValue, long.class, keys);
    }

    @Override
    public int getIntegerProperty(Integer defaultValue, String... keys) {
        return allowNoValue(defaultValue, int.class, keys);
    }

    @Override
    public Pattern getRegexProperty(Pattern defaultValue, String... keys) {
        return allowNoValue(defaultValue, Pattern.class, keys);
    }

    @Override
    public Pattern[] getRegexProperties(Pattern[] defaultValue, String... keys) {
        return allowNoValue(defaultValue, Pattern[].class, keys);
    }

    @Override
    public <T> T getGenericProperty(T defaultValue, Class<T> clazz, String... keys) {
        return allowNoValue(defaultValue, clazz, keys);
    }

    @Override
    public EnhancedMap clone() throws CloneNotSupportedException {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }

}
