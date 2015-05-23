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

package com.vectorprint.configuration.binding.parameters;

import com.vectorprint.configuration.binding.BindingHelper;
import java.io.Reader;

/**
 *
 * @see ParameterizableBindingFactoryImpl#getFactory(java.lang.Class, java.lang.Class) 
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface ParameterizableBindingFactory {
         
   ParameterizableParser getParser(Reader input);
   
   ParameterizableSerializer getSerializer();
   
   BindingHelper getBindingHelper();
   
}
