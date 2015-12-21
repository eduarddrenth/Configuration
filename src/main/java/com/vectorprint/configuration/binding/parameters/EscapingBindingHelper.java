
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.configuration.binding.parameters;

/*
 * #%L
 * VectorPrintReport4.0
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
//~--- non-JDK imports --------------------------------------------------------
import com.vectorprint.configuration.binding.*;
import com.vectorprint.configuration.generated.parser.PropertiesParser;

//~--- JDK imports ------------------------------------------------------------
/**
 * implementation for the syntax supported by {@link PropertiesParser}, escapes ',', '|' and ')', , uses '|' as separator for array values
 *
 * Threadsafe: it is safe to call the available methods from different threads at the same time on one instance of this
 * class.
 *
 * @see StringConverter
 * @author Eduard Drenth at VectorPrint.nl
 */
public final class EscapingBindingHelper extends AbstractParamBindingHelperDecorator {

   public EscapingBindingHelper() {
      super(new ParamBindingHelperImpl());
      setEscapeChars(new char[]{',','|',')'});
      setArrayValueSeparator('|');
   }

}
