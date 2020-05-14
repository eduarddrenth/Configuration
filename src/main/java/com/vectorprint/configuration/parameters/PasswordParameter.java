
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

public class PasswordParameter extends ParameterImpl<byte[]> {

   private boolean clearAfterGet;

   /**
    * Calls {@link #PasswordParameter(java.lang.String, java.lang.String, boolean) } with true
    *
    * @param key
    * @param help
    */
   public PasswordParameter(String key, String help) {
      this(key, help, true);
   }

   public PasswordParameter(String key, String help, boolean clearAfterGet) {
      super(key, help, byte[].class);
      this.clearAfterGet = clearAfterGet;
   }

   public boolean isClearAfterGet() {
      return clearAfterGet;
   }

   /**
    * when {@link #isClearAfterGet() } is true clear the password array
    *
    * @return
    */
   @Override
   public final byte[] getValue() {
      byte[] copy = super.getValue();
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
   public final Parameter<byte[]> setDefault(byte[] value) {
      return this;
   }

   @Override
   public final byte[] getDefault() {
      return null;
   }

   @Override
   public Parameter<byte[]> clone() throws CloneNotSupportedException {
      PasswordParameter cp = (PasswordParameter) super.clone();
      cp.clearAfterGet = clearAfterGet;
      return cp;
   }

   @Override
   public final boolean equals(Object obj) {
      return super.equals(obj) && ((PasswordParameter) obj).clearAfterGet == clearAfterGet;
   }
   
   @Override
   protected final String valueToString(byte[] value) {
      return "";
   }
}
