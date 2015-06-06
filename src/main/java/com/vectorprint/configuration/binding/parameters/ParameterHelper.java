/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.configuration.binding.parameters;

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
import com.vectorprint.configuration.parameters.Parameterizable;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ParameterHelper {

   private static final Logger log = Logger.getLogger(ParameterHelper.class.getName());


   public enum SUFFIX {set_default, set_value}
   
   /**
    * looks for a default value for a key based on the simpleName of a class suffixed by .key and .suffix. This method
    * will traverse all Parameterizable superclasses in search of a default.
    * @param key the key to find
    * @param clazz
    * @param settings
    * @param suffix 
    * @return the key pointing to default setting or null
    */
   public static String findKey(String key, Class<? extends Parameterizable> clazz, EnhancedMap settings, SUFFIX suffix) {
      String simpleName = clazz.getSimpleName() + "." + key + "." + suffix;
      while (!settings.containsKey(simpleName)) {
         Class c = clazz.getSuperclass();
         if (!Parameterizable.class.isAssignableFrom(c)) {
            return null;
         }
         clazz=c;
         if (clazz == null) {
            return null;
         }
         simpleName = clazz.getSimpleName() + "." + key + "." + suffix;
      }
      if (log.isLoggable(Level.FINE)) {
         log.fine("found default " + simpleName + ": " + settings.get(simpleName));
      }
      return simpleName;
   }

   /**
    * compares two arrays
    * @param o
    * @param p
    * @return the boolean 
    */

   /**
    * compares two arrays
    *
    * @param valueClass the class of the two objects to compare
    * @param o
    * @param p
    * @return
    */
   public static boolean isArrayEqual(Object o, Object p) {
      if (o != null) {
         if (p==null) {
            return false;
         }
         Class valueClass = o.getClass();
         if (!valueClass.equals(p.getClass())) {
            return false;
         }
         if (valueClass.isArray()) {
            if (valueClass.getComponentType().isPrimitive()) {
               if (short[].class.isAssignableFrom(valueClass)) {
                  return Arrays.equals((short[]) o, (short[]) p);
               } else if (int[].class.isAssignableFrom(valueClass)) {
                  return Arrays.equals((int[]) o, (int[]) p);
               } else if (long[].class.isAssignableFrom(valueClass)) {
                  return Arrays.equals((long[]) o, (long[]) p);
               } else if (float[].class.isAssignableFrom(valueClass)) {
                  return Arrays.equals((float[]) o, (float[]) p);
               } else if (double[].class.isAssignableFrom(valueClass)) {
                  return Arrays.equals((double[]) o, (double[]) p);
               } else if (byte[].class.isAssignableFrom(valueClass)) {
                  return Arrays.equals((byte[]) o, (byte[]) p);
               } else if (boolean[].class.isAssignableFrom(valueClass)) {
                  return Arrays.equals((boolean[]) o, (boolean[]) p);
               } else if (char[].class.isAssignableFrom(valueClass)) {
                  return Arrays.equals((char[]) o, (char[]) p);
               }
            } else {
               return Arrays.equals((Object[]) o, (Object[]) p);
            }
         }
         throw new VectorPrintRuntimeException(String.format("not an array: %s", valueClass));
      } else {
         return p == null;
      }
   }
}
