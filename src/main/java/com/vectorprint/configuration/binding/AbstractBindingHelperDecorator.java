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

/*
 * #%L
 * Config
 * %%
 * Copyright (C) 2015 VectorPrint
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

import com.vectorprint.ArrayHelper;
import com.vectorprint.VectorPrintRuntimeException;
import static com.vectorprint.configuration.binding.BindingHelper.BOOLEAN_PARSER;
import static com.vectorprint.configuration.binding.BindingHelper.BYTE_PARSER;
import static com.vectorprint.configuration.binding.BindingHelper.CHAR_PARSER;
import static com.vectorprint.configuration.binding.BindingHelper.CLASS_PARSER;
import static com.vectorprint.configuration.binding.BindingHelper.COLOR_PARSER;
import static com.vectorprint.configuration.binding.BindingHelper.DATE_PARSER;
import static com.vectorprint.configuration.binding.BindingHelper.DOUBLE_PARSER;
import static com.vectorprint.configuration.binding.BindingHelper.FLOAT_PARSER;
import static com.vectorprint.configuration.binding.BindingHelper.INT_PARSER;
import static com.vectorprint.configuration.binding.BindingHelper.LONG_PARSER;
import static com.vectorprint.configuration.binding.BindingHelper.REGEX_PARSER;
import static com.vectorprint.configuration.binding.BindingHelper.SHORT_PARSER;
import static com.vectorprint.configuration.binding.BindingHelper.URL_PARSER;
import com.vectorprint.configuration.binding.parameters.ParameterizableBindingFactory;
import com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactory;
import java.awt.Color;
import java.io.File;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * The decoration pattern is advised if you need different behaviour on top of the two implementaions
 * of the BindingHelper interface offered by this library.
 * @param <T>
 * @see ParameterizableBindingFactory
 * @see EnhancedMapBindingFactory
 * @author Eduard Drenth at VectorPrint.nl
 */
public abstract class AbstractBindingHelperDecorator<T extends BindingHelper> implements BindingHelper {
   
   protected final T bindingHelper;

   public AbstractBindingHelperDecorator(T bindingHelper) {
      this.bindingHelper = bindingHelper;
   }

   public static String colorToHex(Color c) {
      return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
   }

   @Override
   public String escape(String value) {
      return bindingHelper.escape(value);
   }

   @Override
   public void setEscapeChars(char[] chars) {
      bindingHelper.setEscapeChars(chars);
   }

   @Override
   public <T> T convert(String[] values, Class<T> clazz) {
      return bindingHelper.convert(values, clazz);
   }

   @Override
   public <T> T convert(String value, Class<T> clazz) {
      return bindingHelper.convert(value, clazz);
   }

   @Override
   public void setArrayValueSeparator(char separator) {
      bindingHelper.setArrayValueSeparator(separator);
   }

   @Override
   public char getArrayValueSeparator() {
      return bindingHelper.getArrayValueSeparator();
   }

   @Override
   public String serializeValue(Object value) {
      return bindingHelper.serializeValue(value);
   }

   
   public static float[] parseFloatValues(String[] values) {
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

   public static Float[] parseFloatObjects(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      Float[] rv = new Float[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = FLOAT_PARSER.convert(s);
      }
      return rv;
   }

   public static Date[] parseDateValues(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      Date[] rv = new Date[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = DATE_PARSER.convert(s);
      }
      return rv;
   }

   public static long[] parseLongValues(String[] values) {
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

   public static Long[] parseLongObjects(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      Long[] rv = new Long[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = LONG_PARSER.convert(s);
      }
      return rv;
   }

   public static char[] parseCharValues(String[] values) {
      if (values == null || values.length == 0 || values[0] == null) {
         return null;
      }
      if (values.length > 1) {
         throw new VectorPrintRuntimeException(String.format("cannot turn mutliple strings (%s) into a char[]",values.length));
      }
      char[] rv = new char[values[0].length()];
      for (int i = 0; i < values[0].length(); i++) {
         rv[i] = values[0].charAt(i);
      }
      return rv;
   }

   public static Character[] parseCharObjects(String[] values) {
      if (values == null || values.length == 0 || values[0] == null) {
         return null;
      }
      if (values.length > 1) {
         throw new VectorPrintRuntimeException(String.format("cannot turn mutliple strings (%s) into a Character[]",values.length));
      }
      Character[] rv = new Character[values[0].length()];
      for (int i = 0; i < values[0].length(); i++) {
         rv[i] = values[0].charAt(i);
      }
      return rv;
   }

   public static short[] parseShortValues(String[] values) {
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

   public static Short[] parseShortObjects(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      Short[] rv = new Short[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = SHORT_PARSER.convert(s);
      }
      return rv;
   }

   public static byte[] parseByteValues(String[] values) {
      if (values == null || values.length == 0 || values[0] == null) {
         return null;
      }
      if (values.length > 1) {
         throw new VectorPrintRuntimeException(String.format("cannot turn mutliple strings (%s) into a byte[]",values.length));
      }
      return values[0].getBytes();
   }

   public static Byte[] parseByteObjects(String[] values) {
      if (values == null || values.length == 0 || values[0] == null) {
         return null;
      }
      if (values.length > 1) {
         throw new VectorPrintRuntimeException(String.format("cannot turn mutliple strings (%s) into a Byte[]",values.length));
      }
      return ArrayHelper.wrap(values[0].getBytes());
   }

   public static double[] parseDoubleValues(String[] values) {
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

   public static Double[] parseDoubleObjects(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      Double[] rv = new Double[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = DOUBLE_PARSER.convert(s);
      }
      return rv;
   }

   public static Integer[] parseIntObjects(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      Integer[] rv = new Integer[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = INT_PARSER.convert(s);
      }
      return rv;
   }

   public static int[] parseIntValues(String[] values) {
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

   public static Pattern[] parseRegexValues(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      Pattern[] rv = new Pattern[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = REGEX_PARSER.convert(s);
      }
      return rv;
   }

   public static URL[] parseURLValues(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      URL[] rv = new URL[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = URL_PARSER.convert(s);
      }
      return rv;
   }

   public static File[] parseFileValues(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      File[] rv = new File[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = FILE_PARSER.convert(s);
      }
      return rv;
   }

   public static Class[] parseClassValues(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      Class[] rv = new Class[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = CLASS_PARSER.convert(s);
      }
      return rv;
   }

   public static <T extends Enum> T[] parseEnumValues(String[] values, Class<T> clazz) {
      if (values == null || values.length == 0) {
         return null;
      }
      T[] t = (T[]) Array.newInstance(clazz, values.length);
      int i = 0;
      for (String s : values) {
         try {
            t[i++] = (T) Enum.valueOf(clazz, s);
         } catch (IllegalArgumentException e) {
            t[i++] = (T) Enum.valueOf(clazz, s.toUpperCase());
         }
      }
      return t;
   }

   public static Color[] parseColorValues(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      Color[] rv = new Color[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = COLOR_PARSER.convert(s);
      }
      return rv;
   }

   public static boolean[] parseBooleanValues(String[] values) {
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

   public static Boolean[] parseBooleanObjects(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }
      Boolean[] rv = new Boolean[values.length];
      int i = 0;
      for (String s : values) {
         rv[i++] = BOOLEAN_PARSER.convert(s);
      }
      return rv;
   }

}
