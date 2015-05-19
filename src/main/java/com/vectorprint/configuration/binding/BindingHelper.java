/*
 * Copyright 2015 VectorPrint.
 *
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
 */

package com.vectorprint.configuration.binding;

import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.binding.parameters.ParameterizableParser;
import com.vectorprint.configuration.binding.parameters.ParameterizableSerializer;
import com.vectorprint.configuration.parameters.Parameter;
import java.awt.Color;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Responsible for converting Strings into (atomic) values and vise versa, for manipulating values and defaults of
 * parameters during or after parsing and serialization and for escaping meaningful characters for a certain syntax.
 * 
 * BindingHelpers should be implemented Threadsafe
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface BindingHelper {

   /**
    * use this from {@link #serializeValue(java.lang.Object, java.lang.StringBuilder, java.lang.String) } if you need
    * to escape syntax specific characters.
    * @param value
    * @return 
    */
   public String escape(String value);

   /**
    * supports arrays of primitives and their wrappers, enums, URL, Color, Date, Pattern and String
    *
    * @param <T>
    * @param values
    * @param clazz
    * @return
    */
   <T> T convert(String[] values, Class<T> clazz);

   /**
    * supports primitives and their wrappers, enums, URL, Color, Date, Pattern and String
    *
    * @param <T>
    * @param value
    * @param clazz
    * @return
    */
   <T> T convert(String value, Class<T> clazz);

   /**
    * call this from {@link ParameterizableSerializer} to give applications a chance to manipulate values before serialization
    * @param p
    * @return
    */
   <TYPE extends Serializable> TYPE getValueToSerialize(Parameter<TYPE> p, boolean useDefault);

   /**
    * supports Objects and arrays of Objects and primitives, uses String.valueOf. Array values will be separated by the
    * argument separator, they will NOT be enclosed in '[]'.
    *
    * @param value
    * @param sb
    * @param arrayValueSeparator
    */
   void serializeValue(Object value, StringBuilder sb, String arrayValueSeparator);

   /**
    *
    * Call this from {@link ParameterizableParser#initParameter(com.vectorprint.configuration.parameters.Parameter, java.lang.Object)
    * } and when a default is found to give applications a chance to manipulate values before setting it in a Parameter.
    *
    * @param parameter
    * @param value
    * @param setDefault
    */
   <TYPE extends Serializable> void setValueOrDefault(Parameter<TYPE> parameter, TYPE value, boolean setDefault);

   public static class FloatParser implements StringConverter<Float> {

      @Override
      public Float convert(String val) {
         return Float.valueOf(val);
      }
   }

   public static class LongParser implements StringConverter<Long> {

      @Override
      public Long convert(String val) {
         return Long.valueOf(val);
      }
   }

   public static class DoubleParser implements StringConverter<Double> {

      @Override
      public Double convert(String val) {
         return Double.valueOf(val);
      }
   }

   public static class BooleanParser implements StringConverter<Boolean> {

      @Override
      public Boolean convert(String val) {
         return Boolean.valueOf(val);
      }
   }

   public static class ColorParser implements StringConverter<Color> {

      @Override
      public Color convert(String value) {
         if (value.indexOf('#') == 0) {
            return Color.decode(value);
         } else {
            Field f = null;
            try {
               // assume name
               f = Color.class.getField(value);
            } catch (NoSuchFieldException ex) {
               throw new VectorPrintRuntimeException(ex);
            } catch (SecurityException ex) {
               throw new VectorPrintRuntimeException(ex);
            }
            try {
               return (Color) f.get(null);
            } catch (IllegalArgumentException ex) {
               throw new VectorPrintRuntimeException(ex);
            } catch (IllegalAccessException ex) {
               throw new VectorPrintRuntimeException(ex);
            }
         }
      }
   }

   /**
    * tries to construct a URL from a String. When a MalformedURLException is thrown and a File exists the URL is
    * created via new File.
    */
   public static class URLParser implements StringConverter<URL> {

      @Override
      public URL convert(String val) {
         try {
            return new URL(val);
         } catch (MalformedURLException ex) {
            File file = new File(val);
            if (file.exists()) {
               try {
                  return file.toURI().toURL();
               } catch (MalformedURLException ex1) {
                  throw new VectorPrintRuntimeException(ex);
               }
            }
            throw new VectorPrintRuntimeException(ex);
         }
      }
   }

   /**
    * calls {@link #classFromKey(java.lang.String) }
    */
   public static class ClassParser implements StringConverter<Class> {

      @Override
      public Class convert(String val) {
         try {
            return Class.forName(val);
         } catch (ClassNotFoundException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
      }

   }

   public static class IntParser implements StringConverter<Integer> {

      @Override
      public Integer convert(String val) {
         return Integer.valueOf(val);
      }
   }

   public static class CharParser implements StringConverter<Character> {

      @Override
      public Character convert(String val) {
         return val.charAt(0);
      }
   }

   public static class ShortParser implements StringConverter<Short> {

      @Override
      public Short convert(String val) {
         return Short.valueOf(val);
      }
   }

   public static class ByteParser implements StringConverter<Byte> {

      @Override
      public Byte convert(String val) {
         return Byte.decode(val);
      }
   }

   public static class DateParser implements StringConverter<Date> {

      @Override
      public Date convert(String val) {
         try {
            return new SimpleDateFormat().parse(val);
         } catch (java.text.ParseException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
      }
   }

   public static class RegexParser implements StringConverter<Pattern> {

      @Override
      public Pattern convert(String val) {
         return Pattern.compile(val);
      }
   }
   public static final IntParser INT_PARSER = new IntParser();
   public static final CharParser CHAR_PARSER = new CharParser();
   public static final ShortParser SHORT_PARSER = new ShortParser();
   public static final ByteParser BYTE_PARSER = new ByteParser();
   public static final LongParser LONG_PARSER = new LongParser();
   public static final FloatParser FLOAT_PARSER = new FloatParser();
   public static final DoubleParser DOUBLE_PARSER = new DoubleParser();
   public static final URLParser URL_PARSER = new URLParser();
   public static final ClassParser CLASS_PARSER = new ClassParser();
   public static final BooleanParser BOOLEAN_PARSER = new BooleanParser();
   public static final ColorParser COLOR_PARSER = new ColorParser();
   public static final DateParser DATE_PARSER = new DateParser();
   public static final RegexParser REGEX_PARSER = new RegexParser();
}
