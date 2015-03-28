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
import com.vectorprint.ClassHelper;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessorImpl;
import com.vectorprint.configuration.parser.MultiValueParamParserConstants;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Observable;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public abstract class ParameterImpl<TYPE extends Serializable> extends Observable implements Parameter<TYPE> {

   private static final long serialVersionUID = 1;
   private String key, help;
   private TYPE value;
   private TYPE def;
   private Class<? extends Parameterizable> declaringClass;

   /**
    *
    * @param key the value of key
    * @param help the value of help
    */
   public ParameterImpl(String key, String help) {
      this.key = key;
      this.help = help;
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
    *
    *
    * @param value the value or the default
    * @return the TYPE
    */
   @Override
   public TYPE getValue() {
      return value!=null?value:def;
   }

   @Override
   public TYPE getDefault() {
      return def;
   }

   /**
    * Sets the value and notifies Observers
    * @param value the value
    */
   @Override
   public Parameter<TYPE> setValue(TYPE value) {
      this.value = value;
      setChanged();
      notifyObservers();
      return this;
   }

   /**
    *
    * @param value the default value
    */
   @Override
   public Parameter<TYPE> setDefault(TYPE value) {
      this.def = value;
      return this;
   }

   @Override
   public String toString() {
      return "ParameterImpl{" + "key=" + key + ", value=" + value + ", def=" + def + ", help=" + help + ", declaringClass=" + declaringClass + '}';
   }


   /**
    * used by {@link #serializeValue() }, calls String.valueOf, give subclasses a chance to do something other than
    * String.valueOf if needed.
    *
    * @param value
    * @return
    */
   protected String valueToString(Object value) {
      return String.valueOf(value);
   }

   /**
    * uses {@link #valueToString(java.lang.Object) } and {@link MultiValueParamParserConstants#PIPE} to append values in arrays
    * @param o
    * @param sb 
    */
   protected final void append(Object o, StringBuilder sb) {
      sb.append(valueToString(o)).append(MultiValueParamParserConstants.tokenImage[MultiValueParamParserConstants.PIPE].substring(1, 2));
   }

   /**
    * supports arrays of Float, Double, Integer, Boolean, URL and String, calls {@link #append(java.lang.Object, java.lang.StringBuilder) }
    *
    * @param array
    * @param clazz
    * @return
    */
   protected String serializeArray(Object array, Class clazz) {
      StringBuilder sb = new StringBuilder(20);
      if (Float[].class.isAssignableFrom(clazz)) {
         for (Object o : (Float[]) value) {
            append(o, sb);
         }
      } else if (Integer[].class.isAssignableFrom(clazz)) {
         for (Object o : (Integer[]) value) {
            append(o, sb);
         }
      } else if (Double[].class.isAssignableFrom(clazz)) {
         for (Object o : (Double[]) value) {
            append(o, sb);
         }
      } else if (Boolean[].class.isAssignableFrom(clazz)) {
         for (Object o : (Boolean[]) value) {
            append(o, sb);
         }
      } else if (URL[].class.isAssignableFrom(clazz)) {
         for (Object o : (URL[]) value) {
            append(o, sb);
         }
      } else if (String[].class.isAssignableFrom(clazz)) {
         for (Object o : (String[]) value) {
            append(o, sb);
         }
      }
      return sb.toString();
   }

   /**
    * Calls {@link #valueToString(java.lang.Object) } or {@link #serializeArray(java.lang.Object, java.lang.Class) }.
    * Supports arrays of Float, Integer and String in {@link #serializeArray(java.lang.Object, java.lang.Class) }
    * 
    */
   @Override
   public final String serializeValue(TYPE value) {
      if (value != null) {
         Class clazz = ClassHelper.findParameterClass(0, this.getClass(), Parameter.class);
         if (clazz.isArray()) {
            String s = serializeArray(value, clazz);
            return (s.length()>0) ? s.substring(0, s.length() -1) : s;
         } else {
            return valueToString(value);
         }
      } else {
         return null;
      }
   }

   @Override
   public Parameter<TYPE> clone() {
      try {
         Constructor con = getClass().getConstructor(String.class, String.class);
         ParameterImpl o = (ParameterImpl) con.newInstance(getKey(), getHelp());
         return o.setDefault(getDefault()).setValue(setDefault(null).getValue());
      } catch (NoSuchMethodException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (SecurityException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (InstantiationException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (IllegalAccessException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (IllegalArgumentException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (InvocationTargetException ex) {
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
    * Called from {@link ParamAnnotationProcessorImpl#initParameters(com.vectorprint.configuration.parameters.Parameterizable) 
    * Call this if you don't use annotations and want to know the declaring class
    * @param declaringClass 
    */
   public void setDeclaringClass(Class<? extends Parameterizable> declaringClass) {
      this.declaringClass = declaringClass;
   }

}
