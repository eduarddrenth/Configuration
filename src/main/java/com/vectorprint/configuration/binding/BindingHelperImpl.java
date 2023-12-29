
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

//~--- non-JDK imports --------------------------------------------------------

import com.vectorprint.StringConverter;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.binding.parameters.ParameterizableBindingFactory;
import com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactory;

import java.awt.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.vectorprint.configuration.binding.AbstractBindingHelperDecorator.*;

//~--- JDK imports ------------------------------------------------------------
public class BindingHelperImpl implements BindingHelper {

   /**
    * preferably use {@link ParameterizableBindingFactory#getBindingHelper() } or {@link EnhancedMapBindingFactory#getBindingHelper()
    * }
    */
   public BindingHelperImpl() {
   }

   /**
    * supports arrays of primitives and their wrappers, enums, URL, Color, Date, Pattern and String
    *
    * @param <T>
    * @param values
    * @param clazz
    * @return
    */
   @Override
   public <T> T convert(String[] values, Class<T> clazz) {
      if (values == null || values.length == 0) {
         return null;
      }
      if (!clazz.isArray()) {
         throw new VectorPrintRuntimeException(clazz.getName() + " not supported");
      }
      Object o;
      if (String[].class.equals(clazz)) {
         return (T) values;
      }
      if (float[].class.equals(clazz)) {
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
      } else if (Character[].class.equals(clazz)) {
         o = parseCharObjects(values);
      } else if (Byte[].class.equals(clazz)) {
         o = parseByteObjects(values);
      } else if (clazz.getComponentType().isEnum()) {
         o = parseEnumValues(values, (Class<? extends Enum>) clazz.getComponentType());
      } else {
         o = parse(values,clazz.getComponentType());
      }
      return (T) o;
   }

   /**
    * supports primitives and their wrappers, enums, URL, Color, Date, Pattern and String
    *
    * @param <T>
    * @param value
    * @param clazz
    * @return
    */
   @Override
   public <T> T convert(String value, Class<T> clazz) {
      if (value == null || value.isEmpty()) {
         return null;
      }
      Object o;
      if (String.class.equals(clazz)) {
         o = value;
      } else if (boolean.class.equals(clazz)) {
         o = Boolean.valueOf(value);
      } else if (byte.class.equals(clazz)) {
         o = Byte.valueOf(value);
      } else if (char.class.equals(clazz)) {
         o = value.charAt(0);
      } else if (short.class.equals(clazz)) {
         o = Short.valueOf(value);
      } else if (double.class.equals(clazz)) {
         o = Double.valueOf(value);
      } else if (float.class.equals(clazz)) {
         o = Float.valueOf(value);
      } else if (int.class.equals(clazz)) {
         o = Integer.valueOf(value);
      } else if (long.class.equals(clazz)) {
         o = Long.valueOf(value);
      } else if (clazz.isEnum()) {
         try {
            o = Enum.valueOf((Class<? extends Enum>) clazz, value);
         } catch (IllegalArgumentException e) {
            o = Enum.valueOf((Class<? extends Enum>) clazz, value.toUpperCase());
         }
      } else {
         o = StringConverter.forClass(clazz).convert(value);
      }
      return (T) o;
   }

   /**
    * Escapes {@link #setEscapeChars(char[]) characters} to be escaped. There is no magic, this method simply puts a \
    * before characters to be escaped. This works fine when serializing values that are the result of parsing a String
    * with special characters.
    *
    * @param value
    * @return
    */
   public final String escape(String value) {
      if (chars == null || chars.length == 0 || value == null || value.isEmpty()) {
         return value;
      }
      String s = value;
      for (char c : chars) {
         s = s.replace(String.valueOf(c), "\\" + c);
      }
      return s;
   }

   private char[] chars = null;

   @Override
   public void setEscapeChars(char[] chars) {
      this.chars = chars;
   }

   private char separator = ',';

   private String SEP = String.valueOf(separator);

   /**
    * default is ","
    *
    * @param separator
    */
   @Override
   public void setArrayValueSeparator(char separator) {
      this.separator = separator;
      this.SEP=String.valueOf(separator);
   }

   @Override
   public char getArrayValueSeparator() {
      return separator;
   }

   /**
    *
    * @param value
    * @return the String
    */
   @Override
   public String serializeValue(Object value) {
      StringBuilder sb = new StringBuilder();
      if (value == null) {
         return null;
      }
      Class clazz = value.getClass();
      if (!clazz.isArray()) {
         if (value instanceof Color) {
            sb.append(colorToHex((Color) value));
         } else {
            sb.append(escape(String.valueOf(value)));
         }
         return sb.toString();
      }
      if (!clazz.getComponentType().isPrimitive()) {
         Object[] O = (Object[]) value;
         if (O.length == 0) {
            return null;
         }
         int l = O.length;
         for (int i = 0;; i++) {
            String v = "null";
            if (O[i] != null) {
               if (O[i] instanceof Color) {
                  v = colorToHex((Color) O[i]);
               } else {
                  v = String.valueOf(O[i]);
               }
            }
            v = escape(v);
            if (i == l - 1) {
               sb.append(v);
               break;
            }
            sb.append(v).append(separator);
         }
      } else if (value instanceof short[]) {
         short[] s = (short[]) value;
         if (s.length>0) {
            sb.append(IntStream.range(0,s.length).mapToObj(i -> String.valueOf(s[i]))
                    .collect(Collectors.joining(SEP)));
         } else return null;
      } else if (value instanceof int[]) {
         int[] s = (int[]) value;
         if (s.length>0) {
            sb.append(IntStream.range(0,s.length).mapToObj(i -> String.valueOf(s[i]))
                    .collect(Collectors.joining(SEP)));
         } else return null;
      } else if (value instanceof long[]) {
         long[] s = (long[]) value;
         if (s.length>0) {
            sb.append(IntStream.range(0,s.length).mapToObj(i -> String.valueOf(s[i]))
                    .collect(Collectors.joining(SEP)));
         } else return null;
      } else if (value instanceof float[]) {
         float[] s = (float[]) value;
         if (s.length>0) {
            sb.append(IntStream.range(0,s.length).mapToObj(i -> String.valueOf(s[i]))
                    .collect(Collectors.joining(SEP)));
         } else return null;
      } else if (value instanceof double[]) {
         double[] s = (double[]) value;
         if (s.length == 0) {
            return null;
         }
         if (s.length>0) {
            sb.append(IntStream.range(0,s.length).mapToObj(i -> String.valueOf(s[i]))
                    .collect(Collectors.joining(SEP)));
         } else return null;
      } else if (value instanceof byte[]) {
         byte[] s = (byte[]) value;
         if (s.length>0) {
            sb.append(IntStream.range(0,s.length).mapToObj(i -> String.valueOf(s[i]))
                    .collect(Collectors.joining(SEP)));
         } else return null;
      } else if (value instanceof boolean[]) {
         boolean[] s = (boolean[]) value;
         if (s.length>0) {
            sb.append(IntStream.range(0,s.length).mapToObj(i -> String.valueOf(s[i]))
                    .collect(Collectors.joining(SEP)));
         } else return null;
      } else if (value instanceof char[]) {
         char[] s = (char[]) value;
         if (s.length>0) {
            sb.append(IntStream.range(0,s.length).mapToObj(i -> String.valueOf(s[i]))
                    .collect(Collectors.joining(SEP)));
         } else return null;
      }
      return sb.toString();
   }

}
