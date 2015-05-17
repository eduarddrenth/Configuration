
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.configuration.binding;

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
import java.awt.Color;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

//~--- JDK imports ------------------------------------------------------------
/**
 * Responsible for turning a String into a value or a array of values and for serializing values and arrays of values.
 * Threadsafe: it is safe to call the available methods from different
 * threads at the same time on one instance of this class.
 * 
 * @see ValueParser
 * @author Eduard Drenth at VectorPrint.nl
 */
public class StringConversion {
   private static StringConversion stringConversion = new StringConversion();

   public static StringConversion getStringConversion() {
      return stringConversion;
   }

   public static void setStringConversion(StringConversion stringConversion) {
      StringConversion.stringConversion = stringConversion;
   }

   /**
    * preferably use {@link #getStringConversion() }
    */
   public StringConversion() {
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

   /**
    * supports arrays of primitives and their wrappers, enums, URL, Color, Date, Pattern and String
    * @param <T>
    * @param values
    * @param clazz
    * @return 
    */
   public <T> T parse(String[] values, Class<T> clazz) {
      if (!clazz.isArray()) {
         throw new VectorPrintRuntimeException(clazz.getName() + " not supported");
      }
      Object o = null;
      if (URL[].class.equals(clazz)) {
         o = parseURLValues(values);
      } else if (float[].class.equals(clazz)) {
         o = parseFloatValues(values);
      } else if (double[].class.equals(clazz)) {
         o = parseDoubleValues(values);
      } else if (short[].class.equals(clazz)) {
         o = parseShortValues(values);
      } else if (char[].class.equals(clazz)) {
         o = parseCharValues(values);
      } else if (byte[].class.equals(clazz)) {
         o = parseByteValues(values);
      } else if (int[].class.equals(clazz)) {
         o = parseIntValues(values);
      } else if (long[].class.equals(clazz)) {
         o = parseLongValues(values);
      } else if (boolean[].class.equals(clazz)) {
         o = parseBooleanValues(values);
      } else if (Float[].class.equals(clazz)) {
         o = parseFloatObjects(values);
      } else if (Double[].class.equals(clazz)) {
         o = parseDoubleObjects(values);
      } else if (Short[].class.equals(clazz)) {
         o = parseShortObjects(values);
      } else if (Character[].class.equals(clazz)) {
         o = parseCharObjects(values);
      } else if (Byte[].class.equals(clazz)) {
         o = parseByteObjects(values);
      } else if (Integer[].class.equals(clazz)) {
         o = parseIntObjects(values);
      } else if (Long[].class.equals(clazz)) {
         o = parseLongObjects(values);
      } else if (Boolean[].class.equals(clazz)) {
         o = parseBooleanObjects(values);
      } else if (Color[].class.equals(clazz)) {
         o = parseColorValues(values);
      } else if (Date[].class.equals(clazz)) {
         o = parseDateValues(values);
      } else if (Class[].class.equals(clazz)) {
         o = parseClassValues(values);
      } else if (clazz.isArray()&&clazz.getComponentType().isEnum()) {
         o = parseEnumValues(values, (Class<? extends Enum>) clazz.getComponentType());
      } else {
         throw new VectorPrintRuntimeException(clazz.getName() + " not supported");
      }
      return (T) o;
   }

   /**
    * supports primitives and their wrappers, enums, URL, Color, Date, Pattern and String
    * @param <T>
    * @param value
    * @param clazz
    * @return 
    */
   public <T> T parse(String value, Class<T> clazz) {
      Object o = null;
      if (Boolean.class.equals(clazz)) {
         o = BOOLEAN_PARSER.parseString(value);
      } else if (Byte.class.equals(clazz)) {
         o = BYTE_PARSER.parseString(value);
      } else if (Character.class.equals(clazz)) {
         o = CHAR_PARSER.parseString(value);
      } else if (Short.class.equals(clazz)) {
         o = SHORT_PARSER.parseString(value);
      } else if (Double.class.equals(clazz)) {
         o = DOUBLE_PARSER.parseString(value);
      } else if (Float.class.equals(clazz)) {
         o = FLOAT_PARSER.parseString(value);
      } else if (Integer.class.equals(clazz)) {
         o = INT_PARSER.parseString(value);
      } else if (Long.class.equals(clazz)) {
         o = LONG_PARSER.parseString(value);
      } else if (boolean.class.equals(clazz)) {
         o = Boolean.parseBoolean(value);
      } else if (byte.class.equals(clazz)) {
         o = Byte.parseByte(value);
      } else if (char.class.equals(clazz)) {
         o = value.charAt(0);
      } else if (short.class.equals(clazz)) {
         o = Short.parseShort(value);
      } else if (double.class.equals(clazz)) {
         o = Double.parseDouble(value);
      } else if (float.class.equals(clazz)) {
         o = Float.parseFloat(value);
      } else if (int.class.equals(clazz)) {
         o = Integer.parseInt(value);
      } else if (long.class.equals(clazz)) {
         o = Long.parseLong(value);
      } else if (Color.class.equals(clazz)) {
         o = COLOR_PARSER.parseString(value);
      } else if (String.class.equals(clazz)) {
         o = value;
      } else if (URL.class.equals(clazz)) {
         o = URL_PARSER.parseString(value);
      } else if (Date.class.equals(clazz)) {
         o = DATE_PARSER.parseString(value);
      } else if (Class.class.equals(clazz)) {
         o = CLASS_PARSER.parseString(value);
      } else if (Pattern.class.equals(clazz)) {
         o = REGEX_PARSER.parseString(value);
      } else if (clazz.isEnum()) {
         o = Enum.valueOf( (Class<? extends Enum>)clazz, value);
      } else {
         throw new VectorPrintRuntimeException(clazz.getName() + " not supported");
      }
      return (T) o;
   }

   public float[] parseFloatValues(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      float[] rv = new float[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = Float.parseFloat(s);
      }
      return rv;
   }
   public Float[] parseFloatObjects(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      Float[] rv = new Float[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = FLOAT_PARSER.parseString(s);
      }
      return rv;
   }

   public Date[] parseDateValues(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      Date[] rv = new Date[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = DATE_PARSER.parseString(s);
      }
      return rv;
   }

   public long[] parseLongValues(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      long[] rv = new long[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = Long.parseLong(s);
      }
      return rv;
   }
   public Long[] parseLongObjects(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      Long[] rv = new Long[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = LONG_PARSER.parseString(s);
      }
      return rv;
   }

   public char[] parseCharValues(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      char[] rv = new char[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = s.charAt(0);
      }
      return rv;
   }
   public Character[] parseCharObjects(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      Character[] rv = new Character[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = CHAR_PARSER.parseString(s);
      }
      return rv;
   }

   public short[] parseShortValues(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      short[] rv = new short[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = Short.parseShort(s);
      }
      return rv;
   }
   public Short[] parseShortObjects(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      Short[] rv = new Short[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = SHORT_PARSER.parseString(s);
      }
      return rv;
   }

   public byte[] parseByteValues(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      byte[] rv = new byte[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = Byte.parseByte(s);
      }
      return rv;
   }
   public Byte[] parseByteObjects(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      Byte[] rv = new Byte[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = BYTE_PARSER.parseString(s);
      }
      return rv;
   }

   public double[] parseDoubleValues(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      double[] rv = new double[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = Double.parseDouble(s);
      }
      return rv;
   }

   public Double[] parseDoubleObjects(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      Double[] rv = new Double[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = DOUBLE_PARSER.parseString(s);
      }
      return rv;
   }

   public Integer[] parseIntObjects(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      Integer[] rv = new Integer[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = INT_PARSER.parseString(s);
      }
      return rv;
   }
   public int[] parseIntValues(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      int[] rv = new int[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = Integer.parseInt(s);
      }
      return rv;
   }

   public Pattern[] parseRegexValues(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      Pattern[] rv = new Pattern[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = REGEX_PARSER.parseString(s);
      }
      return rv;
   }

   public URL[] parseURLValues(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      URL[] rv = new URL[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = URL_PARSER.parseString(s);
      }
      return rv;
   }

   public Class[] parseClassValues(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      Class[] rv = new Class[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = CLASS_PARSER.parseString(s);
      }
      return rv;
   }
   
   public <T extends Enum> T[] parseEnumValues(String[] values, Class<T> clazz) {
      if (values == null || values.length == 0) {
         return null;
      }
      T[] t = (T[]) Array.newInstance(clazz, values.length);
      int i = 0;
      for (String s : values) {
         t[i++] = (T) Enum.valueOf( clazz, s);
      }
      return t;
   }

   public Color[] parseColorValues(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      Color[] rv = new Color[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = COLOR_PARSER.parseString(s);
      }
      return rv;
   }

   public boolean[] parseBooleanValues(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      boolean[] rv = new boolean[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = Boolean.parseBoolean(s);
      }
      return rv;
   }
   public Boolean[] parseBooleanObjects(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      Boolean[] rv = new Boolean[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = BOOLEAN_PARSER.parseString(s);
      }
      return rv;
   }
   
   protected String colorToHex(Color c) {
      return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
   }
   
   /**
    * supports Objects and arrays of Objects and primitives, uses String.valueOf. Array values will be separated
    * by the argument separator, they will NOT be enclosed in '[]'.
    * @param value
    * @param sb
    * @param arrayValueSeparator 
    */
   public void serializeValue(Object value, StringBuilder sb, String arrayValueSeparator) {
      if (value == null) {
         return;
      }
      Class clazz = value.getClass();
      if (!clazz.isArray()) {
         if (value instanceof Color) {
            sb.append(colorToHex((Color) value));
         } else {
            sb.append(String.valueOf(value));
         }
         return;
      }
      if (!clazz.getComponentType().isPrimitive()) {
         Object[] O = (Object[]) value;
         if (O.length == 0) {
            return;
         }
         int l = O.length;
         for (int i = 0;; i++) {
            String v = "null";
            if (O[i]!=null) {
               if (O[i] instanceof Color) {
                  v = colorToHex((Color) O[i]);
               } else {
                  v = String.valueOf(O[i]);
               }
            }
            if (i == l - 1) {
               sb.append(v);
               break;
            }
            sb.append(v).append(arrayValueSeparator);
         }
      } else {
         if (short[].class.isAssignableFrom(clazz)) {
            short[] s = (short[]) value;
            if (s.length == 0) {
               return;
            }
            int l = s.length;
            for (int i = 0;; i++) {
               if (i == l - 1) {
                  sb.append(String.valueOf(s[i]));
                  break;
               }
               sb.append(String.valueOf(s[i])).append(arrayValueSeparator);
            }
         } else if (int[].class.isAssignableFrom(clazz)) {
            int[] s = (int[]) value;
            if (s.length == 0) {
               return;
            }
            int l = s.length;
            for (int i = 0;; i++) {
               if (i == l - 1) {
                  sb.append(String.valueOf(s[i]));
                  break;
               }
               sb.append(String.valueOf(s[i])).append(arrayValueSeparator);
            }
         } else if (long[].class.isAssignableFrom(clazz)) {
            long[] s = (long[]) value;
            if (s.length == 0) {
               return;
            }
            int l = s.length;
            for (int i = 0;; i++) {
               if (i == l - 1) {
                  sb.append(String.valueOf(s[i]));
                  break;
               }
               sb.append(String.valueOf(s[i])).append(arrayValueSeparator);
            }
         } else if (float[].class.isAssignableFrom(clazz)) {
            float[] s = (float[]) value;
            if (s.length == 0) {
               return;
            }
            int l = s.length;
            for (int i = 0;; i++) {
               if (i == l - 1) {
                  sb.append(String.valueOf(s[i]));
                  break;
               }
               sb.append(String.valueOf(s[i])).append(arrayValueSeparator);
            }
         } else if (double[].class.isAssignableFrom(clazz)) {
            double[] s = (double[]) value;
            if (s.length == 0) {
               return;
            }
            int l = s.length;
            for (int i = 0;; i++) {
               if (i == l - 1) {
                  sb.append(String.valueOf(s[i]));
                  break;
               }
               sb.append(String.valueOf(s[i])).append(arrayValueSeparator);
            }
         } else if (byte[].class.isAssignableFrom(clazz)) {
            byte[] s = (byte[]) value;
            if (s.length == 0) {
               return;
            }
            int l = s.length;
            for (int i = 0;; i++) {
               if (i == l - 1) {
                  sb.append(String.valueOf(s[i]));
                  break;
               }
               sb.append(String.valueOf(s[i])).append(arrayValueSeparator);
            }
         } else if (boolean[].class.isAssignableFrom(clazz)) {
            boolean[] s = (boolean[]) value;
            if (s.length == 0) {
               return;
            }
            int l = s.length;
            for (int i = 0;; i++) {
               if (i == l - 1) {
                  sb.append(String.valueOf(s[i]));
                  break;
               }
               sb.append(String.valueOf(s[i])).append(arrayValueSeparator);
            }
         } else if (char[].class.isAssignableFrom(clazz)) {
            char[] s = (char[]) value;
            if (s.length == 0) {
               return;
            }
            int l = s.length;
            for (int i = 0;; i++) {
               if (i == l - 1) {
                  sb.append(String.valueOf(s[i]));
                  break;
               }
               sb.append(String.valueOf(s[i])).append(arrayValueSeparator);
            }
         }
      }
   }

   public static class FloatParser implements ValueParser<Float> {

      @Override
      public Float parseString(String val) {
         return Float.valueOf(val);
      }
   }

   public static class LongParser implements ValueParser<Long> {

      @Override
      public Long parseString(String val) {
         return Long.valueOf(val);
      }
   }

   public static class DoubleParser implements ValueParser<Double> {

      @Override
      public Double parseString(String val) {
         return Double.valueOf(val);
      }
   }

   public static class BooleanParser implements ValueParser<Boolean> {

      @Override
      public Boolean parseString(String val) {
         return Boolean.valueOf(val);
      }
   }

   public static class ColorParser implements ValueParser<Color> {

      @Override
      public Color parseString(String value) {
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
   public static class URLParser implements ValueParser<URL> {

      @Override
      public URL parseString(String val) {
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
   public static class ClassParser implements ValueParser<Class> {

      @Override
      public Class parseString(String val) {
         try {
            return Class.forName(val);
         } catch (ClassNotFoundException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
      }

   }

   public static class IntParser implements ValueParser<Integer> {

      @Override
      public Integer parseString(String val) {
         return Integer.valueOf(val);
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

   public static class RegexParser implements ValueParser<Pattern> {

      @Override
      public Pattern parseString(String val) {
         return Pattern.compile(val);
      }
   }
}
