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
package com.vectorprint.configuration.binding.parameters;

import com.vectorprint.configuration.binding.AbstractBindingHelperDecorator;
import com.vectorprint.configuration.binding.BindingHelper;
import com.vectorprint.configuration.binding.BindingHelperImpl;
import java.awt.Color;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class JSONBindingHelper extends AbstractBindingHelperDecorator {

   public JSONBindingHelper(BindingHelper bindingHelper) {
      super(bindingHelper);
   }

   public JSONBindingHelper() {
      super(new BindingHelperImpl());
   }
   
   /**
    * quote (single) all but (B)boolean and (N)numeric, use "null" for null values
    *
    * @param value
    * @param sb
    * @param arrayValueSeparator
    */
   @Override
   public void serializeValue(Object value, StringBuilder sb, String arrayValueSeparator) {
      if (value == null) {
         sb.append("null");
         return;
      }
      Class clazz = value.getClass();
      if (!clazz.isArray()) {
         if (value instanceof Color) {
            sb.append('\'').append(AbstractBindingHelperDecorator.colorToHex((Color) value)).append('\'');
         } else {
            // parameter clazz is never primitive
            if (Number.class.isAssignableFrom(clazz) || Boolean.class.equals(clazz)) {
               sb.append(String.valueOf(value));
            } else {
               sb.append('\'').append(String.valueOf(value)).append('\'');
            }
         }
         return;
      }
      if (!clazz.getComponentType().isPrimitive()) {
         if (Number.class.isAssignableFrom(clazz.getComponentType()) || Boolean.class.equals(clazz.getComponentType())) {
            super.serializeValue(value, sb, arrayValueSeparator);
            return;
         }
         Object[] O = (Object[]) value;
         if (O.length == 0) {
            sb.append("null");
            return;
         }
         int l = O.length;
         for (int i = 0;; i++) {
            String v = "null";
            if (O[i]!=null) {
               if (O[i] instanceof Color) {
                  v = AbstractBindingHelperDecorator.colorToHex((Color) O[i]);
               } else {
                  v = String.valueOf(O[i]);
               }
            }
            if (i == l - 1) {
               sb.append('\'').append(v).append('\'');
               break;
            }
            sb.append('\'').append(v).append('\'').append(arrayValueSeparator);
         }
      } else {
         if (char[].class.isAssignableFrom(clazz)) {
            char[] s = (char[]) value;
            if (s.length == 0) {
               return;
            }
            int l = s.length;
            for (int i = 0;; i++) {
               if (i == l - 1) {
                  sb.append('\'').append(String.valueOf(s[i])).append('\'');
                  break;
               }
               sb.append('\'').append(String.valueOf(s[i])).append('\'').append(arrayValueSeparator);
            }
         } else {
            super.serializeValue(value, sb, arrayValueSeparator);
         }
      }
   }

}
