/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.configuration.decoration;

/*
 * #%L
 * VectorPrintConfig3.0
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
import java.io.IOException;

/**
 * A Threadsafe {@link EnhancedMap} restricting access to properties to a thread or child threads. When threads are
 * reused (pooled) unwanted access to thread variables may occur. Child threads will receive a {@link EnhancedMap#clone()
 * } of the embedded properties.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ThreadSafeProperties extends AbstractPropertiesDecorator {

   private static final long serialVersionUID = 1;
   private transient ThreadLocal< EnhancedMap> propsFromThread;

   public ThreadSafeProperties(EnhancedMap properties) {
      if (properties == null) {
         throw new IllegalArgumentException("properties may not be null");
      }
      propsFromThread = new InheritableThreadLocal<EnhancedMap>() {
         @Override
         protected EnhancedMap childValue(EnhancedMap parentValue) {
            if (parentValue != null) {
               return parentValue.clone();
            }
            return parentValue;
         }
      };
      propsFromThread.set(properties);
   }

   @Override
   protected EnhancedMap getEmbeddedProperties() {
      EnhancedMap e = propsFromThread.get();
      if (e == null) {
         throw new VectorPrintRuntimeException("No access to properties in this Thread: " + Thread.currentThread().getName());
      }
      return e;
   }

   @Override
   public EnhancedMap clone() {
      EnhancedMap em = propsFromThread.get();
      if (em == null) {
         throw new VectorPrintRuntimeException("clone not possible from this thread: " + Thread.currentThread().getName());
      }
      return new ThreadSafeProperties(em.clone());
   }

   private void writeObject(java.io.ObjectOutputStream s)
       throws IOException {
      s.defaultWriteObject();
      s.writeObject(getEmbeddedProperties());
   }

   private void readObject(java.io.ObjectInputStream s)
       throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      propsFromThread = new InheritableThreadLocal<EnhancedMap>() {
         @Override
         protected EnhancedMap childValue(EnhancedMap parentValue) {
            if (parentValue != null) {
               return parentValue.clone();
            }
            return parentValue;
         }
      };
      propsFromThread.set((EnhancedMap) s.readObject());
   }
}
