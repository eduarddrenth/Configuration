
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

import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.binding.BindingHelper;
import com.vectorprint.configuration.binding.parameters.ParamBindingService;
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public abstract class ParameterImpl<TYPE extends Serializable> implements Parameter<TYPE> {

   private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

   @Serial
   private static final long serialVersionUID = 1;
   private String key, help;
   private TYPE value;
   private TYPE def;
   private Class<? extends Parameterizable> declaringClass;
   private Class<TYPE> valueClass;
   protected static final Logger log = LoggerFactory.getLogger(ParameterImpl.class.getName());

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

   /**
    *
    * @return help text appended with default value if it is set
    */
   @Override
   public String getHelp() {
      return def == null ? help : help + " (default: " + ParamBindingService.getInstance().getFactory().getBindingHelper().serializeValue(def) + ")";
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
      return value != null ? value : def;
   }

   @Override
   public TYPE getDefault() {
      return def;
   }

   /**
    * Sets the value and notifies Observers
    *
    * @param value the value
    * @return
    */
   @Override
   public Parameter<TYPE> setValue(TYPE value) {
      TYPE old = this.value;
      this.value = value;
      if (!Objects.deepEquals(old,value)) {
         propertyChangeSupport.firePropertyChange(key,old,value);
      }
      return this;
   }

   /**
    * Sets the value and notifies Observers
    *
    * @param value the default value
    * @return
    */
   @Override
   public Parameter<TYPE> setDefault(TYPE value) {
      TYPE old = this.def;
      this.def = value;
      if (!Objects.deepEquals(old,value)) {
         propertyChangeSupport.firePropertyChange(key,old,value);
      }
      return this;
   }

   @Override
   public void addObserver(PropertyChangeListener o) {
      propertyChangeSupport.addPropertyChangeListener(o);
   }

   @Override
   public void removeObserver(PropertyChangeListener o) {
      propertyChangeSupport.removePropertyChangeListener(o);
   }

   @Override
   public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

   }

   /**
    * uses {@link #valueToString(java.io.Serializable) }
    *
    * @return
    */
   @Override
   public final String toString() {
      return getClass().getSimpleName() + "{" + "key=" + key + ", value=" + valueToString(value) + ", def=" + valueToString(def) + ", help=" + help + ", declaringClass=" + declaringClass + '}';
   }

   /**
    * Uses {@link BindingHelper#serializeValue(java.lang.Object) }.
    *
    * @param value
    * @return
    */
   protected String valueToString(TYPE value) {
      StringBuilder sb = new StringBuilder(15);
      if (valueClass.isArray()) {
         sb.append('[');
      }
      sb.append(ParamBindingService.getInstance().getFactory().getBindingHelper().serializeValue(value));
      if (valueClass.isArray()) {
         sb.append(']');
      }
      return sb.toString();
   }

   @Override
   public Parameter<TYPE> clone() throws CloneNotSupportedException {
      try {
         ParameterImpl<TYPE> o = (ParameterImpl<TYPE>) super.clone();
         o.help = help;
         o.key = key;
         o.valueClass = valueClass;
         o.declaringClass = declaringClass;
         return o.setDefault(def).setValue(value);
      } catch (SecurityException | IllegalArgumentException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   @Override
   public Class<? extends Parameterizable> getDeclaringClass() {
      return declaringClass;
   }

   /**
    * Called from
    * {@link ParamAnnotationProcessorImpl#initParameters(com.vectorprint.configuration.parameters.Parameterizable)}.
    * <p>
    * Call this if you don't use annotations and want to know the declaring class
    *
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
       return 5;
   }

   /**
    * NB! calls {@link #getValue() },
    *
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
      if (!Objects.equals(this.key, other.key)) {
         return false;
      }
      if (this.valueClass != other.valueClass) {
         return false;
      }
      Object v = (this instanceof PasswordParameter || this instanceof CharPasswordParameter) ? value : getValue();
      Object o = (this instanceof PasswordParameter || this instanceof CharPasswordParameter) ? other.value : other.getValue();
      // compare getValue, not value, if it equals its ok, wether it originates from default or not
      if (!valueClass.isArray()) {
         if (!Objects.equals(v, o)) {
            return false;
         }
         if (!Objects.equals(this.def, other.def)) {
            return false;
         }
      } else {
         if (v != o && (v == null || !Objects.deepEquals(v, o))) {
            return false;
         }
         if (this.def != other.def && (this.def == null || !Objects.deepEquals(this.def, other.def))) {
            return false;
         }
      }
      if (this.declaringClass != other.declaringClass) {
         return false;
      }
      return Objects.equals(this.help, other.help);
   }

}
