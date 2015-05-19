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
package com.vectorprint.configuration.binding;

import com.vectorprint.VectorPrintRuntimeException;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class BindingHelperFactoryImpl implements BindingHelperFactory {
   
   public static final BindingHelperFactory BINDING_HELPER_FACTORY = new BindingHelperFactoryImpl();

   public static final Class<? extends BindingHelper> DEFAULTCLASS = BindingHelperImpl.class;

   private Class<? extends BindingHelper> bindingHelperClass = DEFAULTCLASS;

   public void setBindeingHelperClass(Class<? extends BindingHelper> bindingHelperClass) {
      this.bindingHelperClass = bindingHelperClass;
      bindingHelper = null;
   }

   private volatile BindingHelper bindingHelper = new BindingHelperImpl();

   @Override
   public BindingHelper getBindingHelper() {
      if (bindingHelper == null) {
         try {
            bindingHelper = bindingHelperClass.newInstance();
         } catch (InstantiationException ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (IllegalAccessException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
      }
      return bindingHelper;
   }

}
