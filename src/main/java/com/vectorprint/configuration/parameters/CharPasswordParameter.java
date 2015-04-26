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
public class CharPasswordParameter extends ParameterImpl<char[]> {

   private boolean clearAfterGet = true;

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
      super(key, help);
      this.clearAfterGet = clearAfterGet;
   }

   @Override
   public char[] unMarshall(String value) throws VectorPrintRuntimeException {
      return value.toCharArray();
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
         copy = Arrays.copyOf(copy, copy.length);
         ArrayHelper.clear(super.getValue());
         setValue(null);
      }
      return copy;
   }

   @Override
   public Parameter<char[]> clone() {
      CharPasswordParameter cp = (CharPasswordParameter) super.clone();
      cp.clearAfterGet=clearAfterGet;
      return cp;
   }

   @Override
   public boolean equals(Object obj) {
      return super.equals(obj) && ((CharPasswordParameter)obj).clearAfterGet==clearAfterGet;
   }
   

}
