/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.configuration.parameters;

/*
 * #%L
 * VectorPrintConfig
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
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.parser.ObjectParserConstants;
import java.awt.Color;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ParameterHelper {

   private static final Logger log = Logger.getLogger(ParameterHelper.class.getName());

   /**
    * looks for a default value for a key based on the simpleName of a class suffixed by .key. This method
    * will traverse all Parameterizable superclasses in search of a default.
    *
    * @param key
    * @return the key to the default for a Parameterizable class
    */
   public static String findDefaultKey(String key, Class clazz, EnhancedMap settings) {
      String simpleName = clazz.getSimpleName() + "." + key;
      while (!settings.containsKey(simpleName)) {
         clazz = clazz.getSuperclass();
         if (!Parameterizable.class.isAssignableFrom(clazz)) {
            return null;
         }
         if (clazz == null) {
            return null;
         }
         simpleName = clazz.getSimpleName() + "." + key;
      }
      if (log.isLoggable(Level.FINE)) {
         log.fine("found default " + simpleName + ": " + settings.get(simpleName));
      }
      return simpleName;
   }

   /**
    * returns a String in the form simpleClassName(key=value,key2=v1|v2|v3,key3=value)
    *
    * @param parameterizable
    * @param printOnlyNonDefault when true only print non default values
    * @return
    */
   public static String toConfig(Parameterizable parameterizable, boolean printOnlyNonDefault) {
      StringBuilder sb = new StringBuilder(10 + parameterizable.getParameters().size() * 15);
      sb.append(parameterizable.getClass().getSimpleName());
      if (!parameterizable.getParameters().isEmpty()) {
         int offset = sb.length();
         sb.append(toConfig(parameterizable.getParameters().values(),printOnlyNonDefault));
         if (sb.length() > offset) {
            sb.insert(offset, ObjectParserConstants.tokenImage[ObjectParserConstants.LEFTPAREN].substring(1, 2))
                .append(ObjectParserConstants.tokenImage[ObjectParserConstants.RIGHTPAREN].substring(1, 2));
         }
      }
      return sb.toString();
   }

   /**
    * returns a String in the form key=value,key2=v1|v2|v3,key3=value. Only includes parameters with non default values,
    * see {@link
    * Parameter#getDefault() }.
    *
    * @param parameters
    * @param printOnlyNonDefault when true only print non default values
    * @return
    */
   public static StringBuilder toConfig(Collection<Parameter> parameters, boolean printOnlyNonDefault) {
      StringBuilder sb = new StringBuilder(parameters.size() * 15);
      if (!parameters.isEmpty()) {
         boolean del = false;
         for (Parameter p : parameters) {
            if (include(p, printOnlyNonDefault)) {
               sb.append(toConfig(p, printOnlyNonDefault));
               sb.append(ObjectParserConstants.tokenImage[ObjectParserConstants.KOMMA].substring(1, 2));
               del = true;
            }
         }
         if (del) {
            sb.deleteCharAt(sb.length() - 1);
         }
      }
      return sb;
   }
   
   private static boolean include(Parameter p, boolean printOnlyNonDefault) {
      if (null == p.getValue()) {
         return false;
      }
      if (printOnlyNonDefault) {
         return !p.getValue().equals(p.getDefault());
      } else {
         return true;
      }
   }

   /**
    * returns a String in the form key=value or key2=v1|v2|v3, but only when the value is non default, see {@link
    * Parameter#getDefault() }.
    *
    * @param p
    * @param printOnlyNonDefault when true only print non default values
    * 
    * @return
    */
   public static StringBuilder toConfig(Parameter p, boolean printOnlyNonDefault) {
      StringBuilder sb = new StringBuilder(15);
      if (include(p, printOnlyNonDefault)) {
         sb.append(p.getKey()).append(ObjectParserConstants.tokenImage[ObjectParserConstants.EQ].substring(1, 2))
             .append(p.marshall(p.getValue()));
      }
      return sb;
   }

   public static Color getColorFromString(String value) {
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
