
package com.vectorprint.configuration.parameters;

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

import java.util.Arrays;

public class CharPasswordParameter extends ParameterImpl<char[]> {

   private boolean clearAfterGet;

   /**
    * Calls {@link #CharPasswordParameter(java.lang.String, java.lang.String, boolean) } with true;
    *
    * @param key
    * @param help
    */
   public CharPasswordParameter(String key, String help) {
      this(key, help, true);
   }

   public CharPasswordParameter(String key, String help, boolean clearAfterGet) {
      super(key, help, char[].class);
      this.clearAfterGet = clearAfterGet;
   }

   @Override
   public final Parameter<char[]> setDefault(char[] value) {
      return this;
   }

   @Override
   public final char[] getDefault() {
      return null;
   }

   public boolean isClearAfterGet() {
      return clearAfterGet;
   }

   /**
    * when {@link #clearAfterGet} is true, will clear the password.
    *
    * @return
    */
   @Override
   public final char[] getValue() {
      char[] copy = super.getValue();
      if (copy == null) {
         return null;
      }
      if (clearAfterGet) {
          log.warn("clearing password after first retrieval");
          copy = Arrays.copyOf(copy, copy.length);
         ArrayHelper.clear(super.getValue());
         setValue(null);
      }
      return copy;
   }

   @Override
   public Parameter<char[]> clone() throws CloneNotSupportedException {
      CharPasswordParameter cp = (CharPasswordParameter) super.clone();
      cp.clearAfterGet = clearAfterGet;
      return cp;
   }

   @Override
   public final boolean equals(Object obj) {
      return super.equals(obj) && ((CharPasswordParameter) obj).clearAfterGet == clearAfterGet;
   }

   @Override
   protected final String valueToString(char[] value) {
      return "";
   }
}
