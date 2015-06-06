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
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.binding.BindingHelper;
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessorImpl;
import com.vectorprint.configuration.binding.parameters.ParameterHelper;
import com.vectorprint.configuration.binding.parameters.ParameterizableBindingFactoryImpl;
import java.io.Serializable;
import java.util.Observable;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 * @param <TYPE>
 */
public abstract class ParameterImpl<TYPE extends Serializable> extends Observable implements Parameter<TYPE> {

   private static final long serialVersionUID = 1;
   private String key, help;
   private TYPE value;
   private TYPE def;
   private Class<? extends Parameterizable> declaringClass;
   private Class<TYPE> valueClass;

   /**
    * @param key the value of key
    * @param help the value of help
    */
   public ParameterImpl(String key, String help, Class<TYPE> clazz) {
      this.key = key;
      this.help = help;
      this.valueClass = clazz;
   }

   @Override
   public String getKey() {
      return key;
   }

   public void setKey(String key) {
      this.key = key;
   }

   @Override
   public String getHelp() {
      return help;
   }

   public void setHelp(String help) {
      this.help = help;
   }

   /**
    * NB! used in {@link #equals(java.lang.Object) }
    *
    * @return the TYPE
    */
   @Override
   public TYPE getValue() {
      return value!=null?value:def;
   }

   @Override
   public boolean valueIsDefault() {
      return value == null ? def ==null : !valueClass.isArray() ? value.equals(def) : ParameterHelper.isArrayEqual(value, def);
   }

   @Override
   public TYPE getDefault() {
      return def;
   }

   /**
    * Sets the value and notifies Observers
    * @param value the value
    * @return 
    */
   @Override
   public Parameter<TYPE> setValue(TYPE value) {
      this.value = value;
      setChanged();
      notifyObservers();
      return this;
   }

   /**
    * Sets the value and notifies Observers
    * @param value the default value
    * @return 
    */
   @Override
   public Parameter<TYPE> setDefault(TYPE value) {
      this.def = value;
      setChanged();
      notifyObservers();
      return this;
   }

   @Override
   public String toString() {
      return getClass().getSimpleName()+"{" + "key=" + key + ", value=" + valueToString(value) + ", def=" + valueToString(def) + ", help=" + help + ", declaringClass=" + declaringClass + '}';
   }


   /**
    * Uses {@link BindingHelper#serializeValue(java.lang.Object) }.
    * @param value
    * @return 
    */
   protected String valueToString(TYPE value) {
      StringBuilder sb = new StringBuilder(15);
      if (valueClass.isArray()) {
         sb.append('[');
      }
      sb.append(ParameterizableBindingFactoryImpl.getDefaultFactory().getBindingHelper().serializeValue(value));
      if (valueClass.isArray()) {
         sb.append(']');
      }
      return sb.toString();
   }

   @Override
   public Parameter<TYPE> clone() {
      try {
         ParameterImpl<TYPE> o = (ParameterImpl<TYPE>) super.clone();
         o.help=help;
         o.key=key;
         o.valueClass = valueClass;
         o.declaringClass=declaringClass;
         return o.setDefault(def).setValue(value);
      } catch (SecurityException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (IllegalArgumentException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (CloneNotSupportedException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   @Override
   public void addObserver(Parameterizable o) {
      super.addObserver(o);
   }

   @Override
   public Class<? extends Parameterizable> getDeclaringClass() {
      return declaringClass;
   }

   /**
    * Called from {@link ParamAnnotationProcessorImpl#initParameters(com.vectorprint.configuration.parameters.Parameterizable)}.
    * 
    * Call this if you don't use annotations and want to know the declaring class
    * @param declaringClass 
    */
   public void setDeclaringClass(Class<? extends Parameterizable> declaringClass) {
      this.declaringClass = declaringClass;
   }

   @Override
   public Class<TYPE> getValueClass() {
      return valueClass;
   }

   @Override
   public int hashCode() {
      int hash = 5;
      return hash;
   }

   /**
    * NB! calls {@link #getValue() }, 
    * @see ParameterHelper#isArrayEqual(java.lang.Class, java.lang.Object, java.lang.Object) 
    * @param obj
    * @return 
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (obj == this) {
         return true;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final ParameterImpl<?> other = (ParameterImpl<?>) obj;
      if ((this.key == null) ? (other.key != null) : !this.key.equals(other.key)) {
         return false;
      }
      if (this.valueClass != other.valueClass && (this.valueClass == null || !this.valueClass.equals(other.valueClass))) {
         return false;
      }
      Object v = (this instanceof PasswordParameter || this instanceof CharPasswordParameter) ? value : getValue();
      Object o = (this instanceof PasswordParameter || this instanceof CharPasswordParameter) ? other.value : other.getValue();
      // compare getValue, not value, if it equals its ok, wether it originates from default or not
      if (!valueClass.isArray()) {
         if (v != o && (v == null || !v.equals(o))) {
            return false;
         }
         if (this.def != other.def && (this.def == null || !this.def.equals(other.def))) {
            return false;
         }
      } else {
         if (v != o && (v == null || !ParameterHelper.isArrayEqual(v, o))) {
            return false;
         }
         if (this.def != other.def && (this.def == null || !ParameterHelper.isArrayEqual(this.def, other.def))) {
            return false;
         }
      }
      if (this.declaringClass != other.declaringClass && (this.declaringClass == null || !this.declaringClass.equals(other.declaringClass))) {
         return false;
      }
      if ((this.help == null) ? (other.help != null) : !this.help.equals(other.help)) {
         return false;
      }
      return true;
   }
   
}
