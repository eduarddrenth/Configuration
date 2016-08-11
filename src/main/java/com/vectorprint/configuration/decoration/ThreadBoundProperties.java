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
import java.io.PrintStream;
import java.util.Collection;
import java.util.Set;

/**
 * A {@link EnhancedMap} restricting access to properties to the instantiating thread or child threads. NOTE that when
 * threads are reused (pooled) unwanted access to thread variables may occur.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ThreadBoundProperties extends AbstractPropertiesDecorator implements HiddenBy {

   private static final long serialVersionUID = 1;
   private transient ThreadLocal< EnhancedMap> propsFromThread;

   public ThreadBoundProperties(EnhancedMap properties) {
      super(properties);
      if (properties == null) {
         throw new IllegalArgumentException("properties may not be null");
      }
      propsFromThread = new InheritableThreadLocal<>();
      propsFromThread.set(properties);
   }

   @Override
   public String[] put(String key, String value) {
      checkThread("put");
      return super.put(key, value);
   }

   @Override
   public String[] put(String key, String[] value) {
      checkThread("put");
      return super.put(key, value);
   }

   @Override
   public String[] get(Object key) {
      checkThread("get");
      return super.get(key);
   }

   @Override
   public void clear() {
      checkThread("clear");
      super.clear();
   }

   private void checkThread(String method) {
      if (propsFromThread.get() == null) {
         throw new VectorPrintRuntimeException(method + " not possible from this thread: " + Thread.currentThread().getName());
      }
   }

   @Override
   public String[] remove(Object key) {
      checkThread("remove");
      return super.remove(key);
   }

   @Override
   public EnhancedMap clone() {
      checkThread("clone");
      return new ThreadBoundProperties(propsFromThread.get().clone());
   }

   @Override
   public Set<Entry<String, String[]>> entrySet() {
      checkThread("entrySet");
      return super.entrySet();
   }

   @Override
   public Collection values() {
      checkThread("values");
      return super.values();
   }

   @Override
   public Set<String> keySet() {
      checkThread("keySet");
      return super.keySet();
   }

   @Override
   public boolean containsValue(Object value) {
      checkThread("containsValue");
      return super.containsValue(value);
   }

   @Override
   public boolean containsKey(Object key) {
      checkThread("containsKey");
      return super.containsKey(key);
   }

   @Override
   public void listProperties(PrintStream ps) {
      checkThread("listProperties");
      super.listProperties(ps);
   }

   @Override
   public String getPropertyNoDefault(String... keys) {
      checkThread("getProperty");
      return super.getPropertyNoDefault(keys);
   }

   private void writeObject(java.io.ObjectOutputStream s)
       throws IOException {
      s.defaultWriteObject();
      super.writeEmbeddedSettings(s);
   }

   private void readObject(java.io.ObjectInputStream s)
       throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      propsFromThread = new InheritableThreadLocal<>();
      propsFromThread.set((EnhancedMap) s.readObject());
   }

   @Override
   public boolean hiddenBy(Class<? extends AbstractPropertiesDecorator> settings) {
      return CachingProperties.class.isAssignableFrom(settings);
   }
}