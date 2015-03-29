/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vectorprint.configuration.parameters;

/*
 * #%L
 * VectorPrintReport
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

import com.vectorprint.ArrayHelper;
import com.vectorprint.VectorPrintRuntimeException;
import java.util.Arrays;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class PasswordParameter extends ParameterImpl<byte[]>{
   private boolean clearAfterGet = true;

   /**
    * Calls {@link #PasswordParameter(java.lang.String, java.lang.String, boolean) } with true
    * @param key
    * @param help 
    */
   public PasswordParameter(String key, String help) {
      this(key, help, true);
   }

   public PasswordParameter(String key, String help, boolean clearAfterGet) {
      super(key, help);
      this.clearAfterGet=clearAfterGet;
   }

   @Override
   public byte[] convert(String value) throws VectorPrintRuntimeException {
      return value.getBytes();
   }

   public boolean isClearAfterGet() {
      return clearAfterGet;
   }

   /**
    * when {@link #isClearAfterGet() } is true clear the password array
    * @return 
    */
   @Override
   public byte[] getValue() {
      byte[] copy = super.getValue();
      if (copy==null) {
         return null;
      }
      if (clearAfterGet) {
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
   
   
   
}
