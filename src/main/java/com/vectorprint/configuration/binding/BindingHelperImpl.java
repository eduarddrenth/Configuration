
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
import static com.vectorprint.configuration.binding.AbstractBindingHelperDecorator.*;
import com.vectorprint.configuration.binding.parameters.ParameterizableBindingFactory;
import com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactory;
import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.regex.Pattern;

//~--- JDK imports ------------------------------------------------------------
/**
 * Responsible for converting Strings into (atomic) values and vise versa and for escaping meaningful characters for a certain syntax.
 * Use this default implementation in {@link AbstractBindingHelperDecorator#AbstractBindingHelperDecorator(com.vectorprint.configuration.binding.BindingHelper) } when extending it.
 * 
 * Threadsafe: it is safe to call the available methods from different threads at the same time on one instance of this
 * class.
 *
 * @see StringConverter
 * @author Eduard Drenth at VectorPrint.nl
 */
public class BindingHelperImpl implements BindingHelper {

   /**
    * preferably use {@link ParameterizableBindingFactory#getBindingHelper() } or {@link EnhancedMapBindingFactory#getBindingHelper() }
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
      if (values==null|values.length==0) {
         return null;
      }
      if (!clazz.isArray()) {
         throw new VectorPrintRuntimeException(clazz.getName() + " not supported");
      }
      Object o = null;
      if (URL[].class.equals(clazz)) {
         o = parseURLValues(values);
      } else if (float[].class.equals(clazz)) {
         o = parseFloatValues(values);
      } else if (File[].class.equals(clazz)) {
         o = parseFileValues(values);
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
      } else if (clazz.isArray() && clazz.getComponentType().isEnum()) {
         o = parseEnumValues(values, (Class<? extends Enum>) clazz.getComponentType());
      } else {
         throw new VectorPrintRuntimeException(clazz.getName() + " not supported");
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
      if (value==null||value.isEmpty()) {
         return null;
      }
      Object o = null;
      if (Boolean.class.equals(clazz)) {
         o = BOOLEAN_PARSER.convert(value);
      } else if (Byte.class.equals(clazz)) {
         o = BYTE_PARSER.convert(value);
      } else if (Character.class.equals(clazz)) {
         o = CHAR_PARSER.convert(value);
      } else if (Short.class.equals(clazz)) {
         o = SHORT_PARSER.convert(value);
      } else if (Double.class.equals(clazz)) {
         o = DOUBLE_PARSER.convert(value);
      } else if (Float.class.equals(clazz)) {
         o = FLOAT_PARSER.convert(value);
      } else if (Integer.class.equals(clazz)) {
         o = INT_PARSER.convert(value);
      } else if (Long.class.equals(clazz)) {
         o = LONG_PARSER.convert(value);
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
         o = COLOR_PARSER.convert(value);
      } else if (String.class.equals(clazz)) {
         o = value;
      } else if (URL.class.equals(clazz)) {
         o = URL_PARSER.convert(value);
      } else if (File.class.equals(clazz)) {
         o = FILE_PARSER.convert(value);
      } else if (Date.class.equals(clazz)) {
         o = DATE_PARSER.convert(value);
      } else if (Class.class.equals(clazz)) {
         o = CLASS_PARSER.convert(value);
      } else if (Pattern.class.equals(clazz)) {
         o = REGEX_PARSER.convert(value);
      } else if (clazz.isEnum()) {
         try {
            o = Enum.valueOf((Class<? extends Enum>) clazz, value);
         } catch (IllegalArgumentException e) {
            o = Enum.valueOf((Class<? extends Enum>) clazz, value.toUpperCase());
         }
      } else {
         throw new VectorPrintRuntimeException(clazz.getName() + " not supported");
      }
      return (T) o;
   }

   /**
    * Escapes {@link #setEscapeChars(char[]) characters} to be escaped.
    *
    * @param value
    * @return
    */
   public final String escape(String value) {
      if (chars==null||chars.length==0||value==null||value.isEmpty()) {
         return value;
      }
      String s = value;
      for (char c : chars) {
         s = value.replace(String.valueOf(c), "\\"+c);
      }
      return s;
   }

   private char[] chars = null;

   @Override
   public void setEscapeChars(char[] chars) {
      this.chars = chars;
   }
   
   private char separator = ',';
   
   /**
    * default is ","
    * @param separator 
    */
   @Override
   public void setArrayValueSeparator(char separator) {
      this.separator = separator;
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
      } else {
         if (short[].class.isAssignableFrom(clazz)) {
            short[] s = (short[]) value;
            if (s.length == 0) {
               return null;
            }
            int l = s.length;
            for (int i = 0;; i++) {
               if (i == l - 1) {
                  sb.append(String.valueOf(s[i]));
                  break;
               }
               sb.append(String.valueOf(s[i])).append(separator);
            }
         } else if (int[].class.isAssignableFrom(clazz)) {
            int[] s = (int[]) value;
            if (s.length == 0) {
               return null;
            }
            int l = s.length;
            for (int i = 0;; i++) {
               if (i == l - 1) {
                  sb.append(String.valueOf(s[i]));
                  break;
               }
               sb.append(String.valueOf(s[i])).append(separator);
            }
         } else if (long[].class.isAssignableFrom(clazz)) {
            long[] s = (long[]) value;
            if (s.length == 0) {
               return null;
            }
            int l = s.length;
            for (int i = 0;; i++) {
               if (i == l - 1) {
                  sb.append(String.valueOf(s[i]));
                  break;
               }
               sb.append(String.valueOf(s[i])).append(separator);
            }
         } else if (float[].class.isAssignableFrom(clazz)) {
            float[] s = (float[]) value;
            if (s.length == 0) {
               return null;
            }
            int l = s.length;
            for (int i = 0;; i++) {
               if (i == l - 1) {
                  sb.append(String.valueOf(s[i]));
                  break;
               }
               sb.append(String.valueOf(s[i])).append(separator);
            }
         } else if (double[].class.isAssignableFrom(clazz)) {
            double[] s = (double[]) value;
            if (s.length == 0) {
               return null;
            }
            int l = s.length;
            for (int i = 0;; i++) {
               if (i == l - 1) {
                  sb.append(String.valueOf(s[i]));
                  break;
               }
               sb.append(String.valueOf(s[i])).append(separator);
            }
         } else if (byte[].class.isAssignableFrom(clazz)) {
            byte[] s = (byte[]) value;
            if (s.length == 0) {
               return null;
            }
            int l = s.length;
            for (int i = 0;; i++) {
               if (i == l - 1) {
                  sb.append(String.valueOf(s[i]));
                  break;
               }
               sb.append(String.valueOf(s[i])).append(separator);
            }
         } else if (boolean[].class.isAssignableFrom(clazz)) {
            boolean[] s = (boolean[]) value;
            if (s.length == 0) {
               return null;
            }
            int l = s.length;
            for (int i = 0;; i++) {
               if (i == l - 1) {
                  sb.append(String.valueOf(s[i]));
                  break;
               }
               sb.append(String.valueOf(s[i])).append(separator);
            }
         } else if (char[].class.isAssignableFrom(clazz)) {
            char[] s = (char[]) value;
            if (s.length == 0) {
               return null;
            }
            int l = s.length;
            for (int i = 0;; i++) {
               if (i == l - 1) {
                  sb.append(String.valueOf(s[i]));
                  break;
               }
               sb.append(String.valueOf(s[i])).append(separator);
            }
         }
      }
      return sb.toString();
   }

}
