

package com.vectorprint.configuration.binding;

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


import com.vectorprint.ArrayHelper;
import com.vectorprint.StringConverter;
import com.vectorprint.VectorPrintRuntimeException;

import java.awt.*;
import java.lang.reflect.Array;

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
   
   public static <O> O[] parse(String[] values, Class<O> clazz) {
      if (values == null || values.length == 0) {
         return null;
      }
      O[] rv = (O[])Array.newInstance(clazz, values.length);
      StringConverter<O> conv = StringConverter.forClass(clazz);
      int i = 0;
      for (String s : values) {
         rv[i++] = conv.convert(s);
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

}
