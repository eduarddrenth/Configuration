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

package com.vectorprint.configuration.decoration;

import com.vectorprint.configuration.EnhancedMap;

/**
 * By implementing this interface you can indicate if functionality in your settings is hidden when wrapping
 * it in some other settings implementation.
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface HiddenBy extends EnhancedMap {
   
   /**
    * return true if the functionality of this object is hidden by the argument decorator.
    * @param settings
    * @return the boolean 
    */
   boolean hiddenBy(Class<? extends AbstractPropertiesDecorator> settings);

}
