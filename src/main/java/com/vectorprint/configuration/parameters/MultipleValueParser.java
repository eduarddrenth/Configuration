
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.configuration.parameters;

/*
 * #%L
 * VectorPrintReport4.0
 * %%
 * Copyright (C) 2012 - 2013 VectorPrint
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
//~--- non-JDK imports --------------------------------------------------------
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.parser.MultiValueParamParser;
import com.vectorprint.configuration.parser.MultiValueParser;
import com.vectorprint.configuration.parser.ParseException;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

//~--- JDK imports ------------------------------------------------------------
/**
 * responsible for turning a String into an array of values of a certain type. It uses a {@link ValueParser} to yield
 * values for the array. Used by the {@link EnhancedMap} methods that return an array of values and by {@link Parameter#convert(java.lang.String)
 * } when the parameter type is an array. Threadsafe: it is safe to call the available parse methods from different
 * threads at the same time on one instance of this class.
 *
 * @see MultiValueParser
 * @see MultiValueParamParser
 * @author Eduard Drenth at VectorPrint.nl
 */
public class MultipleValueParser {

   public static final StringParser STRING_PARSER = new StringParser();
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

   private MultipleValueParser() {
   }
   private MultiValueParser parser;
   private MultiValueParamParser paramParser;
   private static MultipleValueParser paramInstance;
   private static MultipleValueParser instance;

   /**
    *
    * @return the instance that uses {@link MultiValueParamParser} for parsing multiple values
    */
   public static MultipleValueParser getParamInstance() {
      if (paramInstance == null) {
         paramInstance = new MultipleValueParser();
         paramInstance.paramParser = new MultiValueParamParser(new ByteArrayInputStream(new byte[0]));
      }
      return paramInstance;
   }

   /**
    *
    * @return the instance that uses {@link MultiValueParser} for parsing multiple values
    */
   public static MultipleValueParser getInstance() {
      if (instance == null) {
         instance = new MultipleValueParser();
         instance.parser = new MultiValueParser(new ByteArrayInputStream(new byte[0]));
      }
      return instance;
   }

   private List<String> parse(String s) throws ParseException {
      if (paramParser == null) {
         synchronized (parser) {
            parser.ReInit(new StringReader(s));
            return parser.parse();
         }
      } else {
         synchronized (paramParser) {
            paramParser.ReInit(new StringReader(s));
            return paramParser.parse();
         }
      }
   }

   /**
    * bottleneck method
    *
    * @param <T>
    * @param values
    * @param clazz
    * @param parser
    * @return
    * @throws ParseException
    */
   public final <T> List<T> parseValues(String values, ValueParser<T> parser, boolean trim) throws ParseException {
      List<String> ll = parse(values);
      List<T> l = new ArrayList<T>(ll.size());
      for (String s : ll) {
         l.add(parser.parseString((trim)?s.trim():s));
      }

      return l;
   }

   public List<Float> parseFloatValues(String values, boolean trim) throws ParseException {
      return parseValues(values, FLOAT_PARSER, trim);
   }

   public List<Date> parseDateValues(String values, boolean trim) throws ParseException {
      return parseValues(values, DATE_PARSER,trim);
   }

   public List<Long> parseLongValues(String values, boolean trim) throws ParseException {
      return parseValues(values, LONG_PARSER, trim);
   }

   public List<Character> parseCharValues(String values, boolean trim) throws ParseException {
      return parseValues(values, CHAR_PARSER, trim);
   }

   public List<Short> parseShortValues(String values, boolean trim) throws ParseException {
      return parseValues(values, SHORT_PARSER, trim);
   }

   public List<Byte> parseByteValues(String values, boolean trim) throws ParseException {
      return parseValues(values, BYTE_PARSER, trim);
   }

   public List<Double> parseDoubleValues(String values, boolean trim) throws ParseException {
      return parseValues(values, DOUBLE_PARSER, trim);
   }

   public List<Integer> parseIntValues(String values, boolean trim) throws ParseException {
      return parseValues(values, INT_PARSER, trim);
   }

   public List<String> parseStringValues(String values, boolean trim) throws ParseException {
      return parseValues(values, STRING_PARSER, trim);
   }

   public List<URL> parseURLValues(String values, boolean trim) throws ParseException {
      return parseValues(values, URL_PARSER, trim);
   }

   public List<Class> parseClassValues(String values, boolean trim) throws ParseException {
      return parseValues(values, CLASS_PARSER, trim);
   }

   public List<Color> parseColorValues(String values, boolean trim) throws ParseException {
      return parseValues(values, COLOR_PARSER, trim);
   }

   public List<Boolean> parseBooleanValues(String values, boolean trim) throws ParseException {
      return parseValues(values, BOOLEAN_PARSER, trim);
   }

   public static class FloatParser implements ValueParser<Float> {

      @Override
      public Float parseString(String val) {
         return Float.parseFloat(val);
      }
   }

   public static class LongParser implements ValueParser<Long> {

      @Override
      public Long parseString(String val) {
         return Long.parseLong(val);
      }
   }

   public static class DoubleParser implements ValueParser<Double> {

      @Override
      public Double parseString(String val) {
         return Double.parseDouble(val);
      }
   }

   public static class BooleanParser implements ValueParser<Boolean> {

      @Override
      public Boolean parseString(String val) {
         return Boolean.parseBoolean(val);
      }
   }

   public static class ColorParser implements ValueParser<Color> {

      @Override
      public Color parseString(String value) {
         return ParameterHelper.getColorFromString(value);
      }
   }

   public static class URLParser implements ValueParser<URL> {

      @Override
      public URL parseString(String val) {
         try {
            return new URL(val);
         } catch (MalformedURLException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
      }
   }
   private static final Map<String, Class> cache = new HashMap<String, Class>(3);

   /**
    * uses static cache
    * @param key
    * @return
    * @throws ClassNotFoundException 
    */
   public static Class classFromKey(String key) throws ClassNotFoundException {
      if (cache.containsKey(key)) {
         return cache.get(key);
      } else {
         synchronized (cache) {
            cache.put(key, Class.forName(key));
         }
         return cache.get(key);
      }
   }

   /**
    * calls {@link #classFromKey(java.lang.String) }
    */
   public static class ClassParser implements ValueParser<Class> {

      @Override
      public Class parseString(String val) {
         try {
            return classFromKey(val);
         } catch (ClassNotFoundException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
      }
      
   }

   public static class IntParser implements ValueParser<Integer> {

      @Override
      public Integer parseString(String val) {
         return Integer.parseInt(val);
      }
   }

   public static class StringParser implements ValueParser<String> {

      @Override
      public String parseString(String val) {
         return val;
      }
   }

   public static class CharParser implements ValueParser<Character> {

      @Override
      public Character parseString(String val) {
         return val.charAt(0);
      }
   }

   public static class ShortParser implements ValueParser<Short> {

      @Override
      public Short parseString(String val) {
         return Short.valueOf(val);
      }
   }

   public static class ByteParser implements ValueParser<Byte> {

      @Override
      public Byte parseString(String val) {
         return Byte.decode(val);
      }
   }

   public static class DateParser implements ValueParser<Date> {

      @Override
      public Date parseString(String val) {
         try {
            return new SimpleDateFormat().parse(val);
         } catch (java.text.ParseException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
      }
   }
}
